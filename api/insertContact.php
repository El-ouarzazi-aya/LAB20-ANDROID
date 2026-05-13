<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

require_once __DIR__ . '/../service/ContactService.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(["success" => false, "message" => "Methode non autorisee"]);
    exit;
}

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['name']) || empty($data['phone'])) {
    echo json_encode(["success" => false, "message" => "Champs manquants"]);
    exit;
}

$service = new ContactService();
$result  = $service->insert($data['name'], $data['phone'], "mobile");

if ($result === "duplicate") {
    echo json_encode(["success" => false, "message" => "Doublon detecte", "duplicate" => true]);
} elseif ($result === "inserted") {
    echo json_encode(["success" => true, "message" => "Contact insere"]);
} else {
    echo json_encode(["success" => false, "message" => "Erreur insertion"]);
}
?>