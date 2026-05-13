<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

require_once __DIR__ . '/../service/ContactService.php';

$keyword = isset($_GET['keyword']) ? trim($_GET['keyword']) : '';

if ($keyword === '') {
    echo json_encode([]);
    exit;
}

$service = new ContactService();
echo json_encode($service->search($keyword));
?>