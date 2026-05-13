<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

require_once __DIR__ . '/../service/ContactService.php';

$service = new ContactService();
echo json_encode($service->getAll());
?>