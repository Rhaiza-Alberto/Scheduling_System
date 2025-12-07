<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

$conn = new mysqli("localhost", "root", "", "scheduling-system");

if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "DB error"]);
    exit();
}

$sql = "SELECT p.person_ID, p.first_name, p.last_name, a.account_name AS role
        FROM person p
        JOIN account_type a ON p.account_ID = a.account_ID
        ORDER BY p.last_name";

$result = $conn->query($sql);
$users = [];

while ($row = $result->fetch_assoc()) {
    $users[] = [
        "id" => (int)$row['person_ID'],
        "firstName" => $row['first_name'],
        "lastName" => $row['last_name'],
        "role" => $row['role']
    ];
}

echo json_encode(["success" => true, "users" => $users]);
$conn->close();
?>