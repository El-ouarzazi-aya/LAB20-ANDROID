<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

require_once __DIR__ . '/../service/ContactService.php';

$data = json_decode(file_get_contents("php://input"), true);
$phonesFromApp = isset($data['phones']) ? $data['phones'] : [];

$service = new ContactService();
$phonesOnServer = $service->getAllPhones();

$missingOnApp = [];
foreach ($phonesOnServer as $p) {
    if (!in_array($p, $phonesFromApp)) {
        $results = $service->search($p);
        if (!empty($results)) {
            $missingOnApp[] = $results[0];
        }
    }
}

echo json_encode(["missing_on_app" => $missingOnApp]);
?>