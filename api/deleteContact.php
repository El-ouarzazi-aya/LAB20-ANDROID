<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: DELETE, POST");
header("Access-Control-Allow-Headers: Content-Type");

require_once __DIR__ . '/../service/ContactService.php';

$data = json_decode(file_get_contents("php://input"), true);

if (empty($data['id'])) {
    echo json_encode(["success" => false, "message" => "ID manquant"]);
    exit;
}

$service = new ContactService();
$ok = $service->delete($data['id']);

echo json_encode([
    "success" => $ok,
    "message" => $ok ? "Contact supprime" : "Erreur suppression"
]);
?>