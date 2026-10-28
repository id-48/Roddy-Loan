<?php

declare(strict_types=1);

function db(): PDO
{
    static $pdo = null;
    if ($pdo instanceof PDO) {
        return $pdo;
    }

    $cfg = require __DIR__ . '/db_config.php';
    $dsn = sprintf(
        'mysql:host=%s;port=%d;dbname=%s;charset=%s',
        $cfg['host'],
        (int)($cfg['port'] ?? 3306),
        $cfg['name'],
        $cfg['charset'] ?? 'utf8mb4'
    );
    $pdo = new PDO($dsn, $cfg['user'], $cfg['pass'], [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES => false,
    ]);

    ensureMysqlSchema($pdo);
    return $pdo;
}

function ensureMysqlSchema(PDO $pdo): void
{
    $pdo->exec('CREATE TABLE IF NOT EXISTS app_open_devices (
        device_id VARCHAR(64) PRIMARY KEY,
        audience VARCHAR(32) NOT NULL DEFAULT \'organic\',
        open_count INT NOT NULL DEFAULT 0,
        first_opened_at DATETIME NOT NULL,
        last_opened_at DATETIME NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4');

    $pdo->exec('CREATE TABLE IF NOT EXISTS app_open_events (
        id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
        device_id VARCHAR(64) NOT NULL,
        audience VARCHAR(32) NOT NULL,
        opened_at DATETIME NOT NULL,
        INDEX idx_device (device_id),
        INDEX idx_opened (opened_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4');

    $pdo->exec('CREATE TABLE IF NOT EXISTS notification_logs (
        id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
        title VARCHAR(190) NOT NULL,
        body TEXT NOT NULL,
        image_url TEXT NULL,
        main_link TEXT NULL,
        devices_at_send INT NOT NULL DEFAULT 0,
        sent_at DATETIME NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4');

    $pdo->exec('CREATE TABLE IF NOT EXISTS fcm_devices (
        device_id VARCHAR(64) PRIMARY KEY,
        fcm_token TEXT NULL,
        updated_at DATETIME NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4');

    $pdo->exec('CREATE TABLE IF NOT EXISTS pending_notifications (
        id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
        title VARCHAR(190) NOT NULL,
        body TEXT NOT NULL,
        image_url TEXT NULL,
        main_link TEXT NULL,
        created_at DATETIME NOT NULL,
        active TINYINT(1) NOT NULL DEFAULT 1
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4');
}

function utcNow(): string
{
    return gmdate('Y-m-d H:i:s');
}

function formatIst(string $utcSql): string
{
    try {
        $dt = new DateTimeImmutable($utcSql, new DateTimeZone('UTC'));
        return $dt->setTimezone(new DateTimeZone('Asia/Kolkata'))->format('d M Y, h:i A');
    } catch (Throwable) {
        return $utcSql;
    }
}

function trackAppOpen(string $deviceId, string $audience): array
{
    $deviceId = preg_replace('/[^a-zA-Z0-9_-]/', '', $deviceId) ?? '';
    if ($deviceId === '' || strlen($deviceId) > 64) {
        return ['ok' => false, 'message' => 'Invalid device_id'];
    }
    $audience = $audience === 'marketing' ? 'marketing' : 'organic';
    $now = utcNow();
    $pdo = db();
    $pdo->prepare(
        'INSERT INTO app_open_devices (device_id, audience, open_count, first_opened_at, last_opened_at)
         VALUES (:d, :a, 1, :n, :n)
         ON DUPLICATE KEY UPDATE
           audience = VALUES(audience),
           open_count = open_count + 1,
           last_opened_at = VALUES(last_opened_at)'
    )->execute(['d' => $deviceId, 'a' => $audience, 'n' => $now]);
    $pdo->prepare(
        'INSERT INTO app_open_events (device_id, audience, opened_at) VALUES (:d, :a, :n)'
    )->execute(['d' => $deviceId, 'a' => $audience, 'n' => $now]);
    $stmt = $pdo->prepare('SELECT open_count FROM app_open_devices WHERE device_id=?');
    $stmt->execute([$deviceId]);
    return ['ok' => true, 'open_count' => (int)$stmt->fetchColumn(), 'audience' => $audience];
}

function deviceSummary(): array
{
    $pdo = db();
    $total = (int)$pdo->query('SELECT COUNT(*) FROM app_open_devices')->fetchColumn();
    $organic = (int)$pdo->query("SELECT COUNT(*) FROM app_open_devices WHERE audience='organic'")->fetchColumn();
    $marketing = (int)$pdo->query("SELECT COUNT(*) FROM app_open_devices WHERE audience='marketing'")->fetchColumn();
    return compact('total', 'organic', 'marketing');
}

function devicePage(int $page = 1, int $perPage = 20, string $audience = ''): array
{
    $pdo = db();
    $page = max(1, $page);
    $perPage = max(5, min(100, $perPage));
    $where = '';
    $params = [];
    if ($audience === 'organic' || $audience === 'marketing') {
        $where = ' WHERE audience = ?';
        $params[] = $audience;
    }
    $countStmt = $pdo->prepare('SELECT COUNT(*) FROM app_open_devices' . $where);
    $countStmt->execute($params);
    $total = (int)$countStmt->fetchColumn();
    $pages = max(1, (int)ceil($total / $perPage));
    if ($page > $pages) {
        $page = $pages;
    }
    $offset = ($page - 1) * $perPage;
    $sql = 'SELECT device_id, audience, open_count, last_opened_at FROM app_open_devices'
        . $where . ' ORDER BY last_opened_at DESC LIMIT ' . (int)$perPage . ' OFFSET ' . (int)$offset;
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    return [
        'items' => $stmt->fetchAll(),
        'total' => $total,
        'page' => $page,
        'pages' => $pages,
        'perPage' => $perPage,
    ];
}

function saveNotification(string $title, string $body, ?string $imageUrl, ?string $link): int
{
    $pdo = db();
    $devices = (int)$pdo->query('SELECT COUNT(*) FROM app_open_devices')->fetchColumn();
    $pdo->prepare(
        'INSERT INTO notification_logs (title, body, image_url, main_link, devices_at_send, sent_at)
         VALUES (?,?,?,?,?,?)'
    )->execute([$title, $body, $imageUrl, $link, $devices, utcNow()]);
    $pdo->prepare(
        'UPDATE pending_notifications SET active=0 WHERE active=1'
    )->execute();
    $pdo->prepare(
        'INSERT INTO pending_notifications (title, body, image_url, main_link, created_at, active)
         VALUES (?,?,?,?,?,1)'
    )->execute([$title, $body, $imageUrl, $link, utcNow()]);
    return (int)$pdo->lastInsertId();
}

function latestPendingNotification(?string $deviceId = null): ?array
{
    $pdo = db();
    // Expire old + clear leftover short test pushes (e.g. title "t" / body "g")
    $pdo->exec(
        "UPDATE pending_notifications SET active=0
         WHERE active=1 AND (
            created_at < DATE_SUB(UTC_TIMESTAMP(), INTERVAL 12 HOUR)
            OR CHAR_LENGTH(TRIM(title)) <= 2
            OR CHAR_LENGTH(TRIM(body)) <= 2
         )"
    );
    $stmt = $pdo->query('SELECT * FROM pending_notifications WHERE active=1 ORDER BY id DESC LIMIT 1');
    $row = $stmt->fetch();
    if ($row === false) {
        return null;
    }

    // New installs must NOT receive broadcasts that were sent before this device existed.
    $deviceId = preg_replace('/[^a-zA-Z0-9_-]/', '', (string)$deviceId) ?? '';
    if ($deviceId === '' || strlen($deviceId) > 64) {
        return null;
    }
    $devStmt = $pdo->prepare(
        'SELECT first_opened_at FROM app_open_devices WHERE device_id = ? LIMIT 1'
    );
    $devStmt->execute([$deviceId]);
    $dev = $devStmt->fetch();
    if ($dev === false) {
        // Device not tracked yet (brand-new install / race) → no pending push
        return null;
    }
    $firstOpen = (string)($dev['first_opened_at'] ?? '');
    $createdAt = (string)($row['created_at'] ?? '');
    if ($firstOpen === '' || $createdAt === '' || $firstOpen >= $createdAt) {
        // Opened/installed at or after this notification was created → skip
        return null;
    }
    return $row;
}

function notificationHistory(int $limit = 30): array
{
    $stmt = db()->query('SELECT * FROM notification_logs ORDER BY id DESC LIMIT ' . (int)$limit);
    return $stmt->fetchAll();
}
