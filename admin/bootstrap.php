<?php

declare(strict_types=1);

require_once __DIR__ . '/db.php';
require_once __DIR__ . '/config_store.php';

date_default_timezone_set('Asia/Kolkata');

header('X-Frame-Options: DENY');
header('X-Content-Type-Options: nosniff');
header('Referrer-Policy: no-referrer');
header("Content-Security-Policy: default-src 'self'; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline'; img-src 'self' data: blob:; form-action 'self'; frame-ancestors 'none'; base-uri 'self'");
header('Cache-Control: no-store, private');

session_name('emi_admin');
$sessionPath = rtrim(str_replace('\\', '/', dirname((string)($_SERVER['SCRIPT_NAME'] ?? '/admin/index.php'))), '/');
$sessionPath = $sessionPath === '' ? '/' : $sessionPath;
session_set_cookie_params([
    'lifetime' => 0,
    'path' => $sessionPath,
    'secure' => (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off'),
    'httponly' => true,
    'samesite' => 'Strict',
]);
ini_set('session.use_strict_mode', '1');
ini_set('session.use_only_cookies', '1');
session_start();

const ADMIN_EMAIL = 'admin@gmail.com';
const ADMIN_NAME = 'EMI Admin';
const ADMIN_ROLE = 'owner';
const ADMIN_PASSWORD_HASH = '$2y$12$t4AFOliJ6YRMaOFElgvByeZALdmNw/rQX/nDNVaMRXGOyfLDTCiCS';

function e(mixed $value): string
{
    return htmlspecialchars((string)$value, ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}

function csrf(): string
{
    if (!isset($_SESSION['csrf']) || !is_string($_SESSION['csrf'])) {
        $_SESSION['csrf'] = bin2hex(random_bytes(32));
    }
    return $_SESSION['csrf'];
}

function requireCsrf(): void
{
    $token = (string)($_POST['csrf'] ?? '');
    if (!hash_equals(csrf(), $token)) {
        throw new RuntimeException('Your session form expired. Please try again.');
    }
}

function post(string $key, int $max = 2000): string
{
    $value = trim((string)($_POST[$key] ?? ''));
    if (mb_strlen($value) > $max) {
        throw new RuntimeException('A submitted value is too long.');
    }
    return $value;
}

function flash(string $type, string $message): void
{
    $_SESSION['flash'] = [$type, $message];
}

function redirect(string $page, array $query = []): never
{
    $query = array_merge(['page' => $page], $query);
    header('Location: index.php?' . http_build_query($query), true, 303);
    exit;
}

function admin(): array
{
    return $_SESSION['admin'] ?? [];
}

function requireAdmin(): void
{
    if (admin() === []) {
        redirect('login');
    }
    $last = (int)($_SESSION['last_activity'] ?? 0);
    if ($last > 0 && time() - $last > 28800) {
        $_SESSION = [];
        session_destroy();
        redirect('login');
    }
    $_SESSION['last_activity'] = time();
}

function renderHeader(string $title, string $page = ''): void
{
    $current = admin();
    $links = [
        'dashboard' => 'Dashboard',
        'settings' => 'App settings',
        'ads_config' => 'Ads configuration',
    ];
    ?><!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title><?= e($title) ?> · EMI Admin</title>
<link rel="stylesheet" href="style.css?v=<?= e((string)@filemtime(__DIR__ . '/style.css')) ?>">
</head>
<body>
<?php if ($current !== []): ?>
<header class="topbar">
<a class="brand" href="index.php">EMI Loan <span>Admin</span></a>
<div class="account"><?= e($current['name']) ?> · <?= e(ucfirst((string)$current['role'])) ?>
<form method="post" class="inline" action="index.php"><input type="hidden" name="csrf" value="<?= e(csrf()) ?>"><input type="hidden" name="action" value="logout"><button class="link-button">Log out</button></form>
</div>
</header>
<div class="shell">
<nav class="nav">
<div class="nav-section">Main</div>
<?php foreach ($links as $key => $label): ?><a class="<?= $page === $key ? 'active' : '' ?>" href="index.php?page=<?= e($key) ?>"><?= e($label) ?></a><?php endforeach; ?>
</nav>
<main>
<?php else: ?><main class="auth-main"><?php endif; ?>
<?php $notice = $_SESSION['flash'] ?? null; unset($_SESSION['flash']); if (is_array($notice)): ?><div class="flash <?= e($notice[0]) ?>"><?= e($notice[1]) ?></div><?php endif; ?>
<div class="page-head"><div><p class="eyebrow">EMI Loan operations</p><h1><?= e($title) ?></h1></div></div>
<?php
}

function renderFooter(): void
{
    ?></main><?php if (admin() !== []): ?></div><?php endif; ?></body></html><?php
}
