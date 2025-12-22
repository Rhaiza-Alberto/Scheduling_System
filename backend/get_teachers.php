<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

$conn = new mysqli("localhost", "root", "", "scheduling-system");

if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "DB connection failed"]);
    exit();
}

$sql = "SELECT 
            p.person_ID,
            n.name,
            p.email
        FROM Person p
        JOIN Name n ON p.name_ID = n.name_ID
        JOIN Account_type at ON p.account_type_ID = at.account_type_ID
        WHERE at.account_type_name = 'Teacher'
        ORDER BY n.name";

$result = $conn->query($sql);

$teachers = [];
while ($row = $result->fetch_assoc()) {
    $teachers[] = [
        "id" => (int)$row['person_ID'],
        "name" => $row['name'],
        "email" => $row['email']
    ];
}

echo json_encode([
    "success" => true,
    "teachers" => $teachers
]);

$conn->close();
?>