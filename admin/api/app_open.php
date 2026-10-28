<?php
declare(strict_types=1);
require_once dirname(__DIR__) . '/db.php';
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
$deviceId = (string)($_GET['device_id'] ?? $_POST['device_id'] ?? '');
$audience = (string)($_GET['audience'] ?? $_POST['audience'] ?? 'organic');
echo json_encode(trackAppOpen($deviceId, $audience));
