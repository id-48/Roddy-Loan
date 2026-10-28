<?php
declare(strict_types=1);
require_once dirname(__DIR__) . '/db.php';
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

$deviceId = isset($_GET['device_id']) ? (string)$_GET['device_id'] : '';
$row = latestPendingNotification($deviceId);
echo json_encode(['ok' => true, 'notification' => $row]);
