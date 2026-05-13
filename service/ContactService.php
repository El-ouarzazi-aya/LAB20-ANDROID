<?php
require_once __DIR__ . '/../config/Database.php';

class ContactService {
    private $conn;
    private $table = "contact";

    public function __construct() {
        $db = new Database();
        $this->conn = $db->getConnection();
    }

    public function existsByPhone($phone) {
        $sql = "SELECT COUNT(*) FROM " . $this->table . " WHERE phone = :phone";
        $stmt = $this->conn->prepare($sql);
        $stmt->execute([':phone' => $phone]);
        return (int)$stmt->fetchColumn() > 0;
    }

    public function insert($name, $phone, $source = "mobile") {
        if ($this->existsByPhone($phone)) {
            return "duplicate";
        }
        $sql = "INSERT INTO " . $this->table . " (name, phone, source)
                VALUES (:name, :phone, :source)";
        $stmt = $this->conn->prepare($sql);
        $ok = $stmt->execute([
            ':name'   => trim($name),
            ':phone'  => trim($phone),
            ':source' => $source
        ]);
        return $ok ? "inserted" : "error";
    }

    public function getAll() {
        $sql = "SELECT * FROM " . $this->table . " ORDER BY name ASC";
        $stmt = $this->conn->prepare($sql);
        $stmt->execute();
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    public function search($keyword) {
        $sql = "SELECT * FROM " . $this->table . "
                WHERE name LIKE :kw OR phone LIKE :kw
                ORDER BY name ASC";
        $stmt = $this->conn->prepare($sql);
        $stmt->execute([':kw' => '%' . trim($keyword) . '%']);
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    public function delete($id) {
        $sql = "DELETE FROM " . $this->table . " WHERE id = :id";
        $stmt = $this->conn->prepare($sql);
        return $stmt->execute([':id' => (int)$id]);
    }

    public function getAllPhones() {
        $sql = "SELECT phone FROM " . $this->table;
        $stmt = $this->conn->prepare($sql);
        $stmt->execute();
        return array_column($stmt->fetchAll(PDO::FETCH_ASSOC), 'phone');
    }
}
?>