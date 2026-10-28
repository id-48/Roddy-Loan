<?php
declare(strict_types=1);
require_once dirname(__DIR__) . '/db.php';
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Cache-Control: no-store');

$deviceId = preg_replace('/[^a-zA-Z0-9_-]/', '', (string)($_GET['device_id'] ?? $_POST['device_id'] ?? '')) ?? '';
$token = trim((string)($_GET['fcm_token'] ?? $_POST['fcm_token'] ?? ''));
$audience = strtolower(trim((string)($_GET['audience'] ?? $_POST['audience'] ?? 'organic')));
if ($audience !== 'marketing') {
    $audience = 'organic';
}

if ($deviceId === '' || strlen($deviceId) > 64 || $token === '') {
    http_response_code(400);
    echo json_encode(['ok' => false, 'message' => 'device_id and fcm_token required']);
    exit;
}

try {
    $pdo = db();
    $pdo->prepare(
        'INSERT INTO fcm_devices (device_id, fcm_token, updated_at)
         VALUES (?, ?, ?)
         ON DUPLICATE KEY UPDATE fcm_token = VALUES(fcm_token), updated_at = VALUES(updated_at)'
    )->execute([$deviceId, $token, utcNow()]);
    echo json_encode(['ok' => true, 'audience' => $audience]);
} catch (Throwable $e) {
    http_response_code(500);
    echo json_encode(['ok' => false, 'message' => $e->getMessage()]);
}
