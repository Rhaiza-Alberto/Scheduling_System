<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

$conn = new mysqli("localhost", "root", "", "scheduling-system");

if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "DB connection failed"]);
    exit();
}

$sql = "SELECT 
            d.day_ID,
            d.day_name
        FROM Day d
        ORDER BY d.day_ID";

$result = $conn->query($sql);

$days = [];
while ($row = $result->fetch_assoc()) {
    $days[] = [
        "id" => (int)$row['day_ID'],
        "name" => $row['day_name']
    ];
}

echo json_encode([
    "success" => true,
    "days" => $days
]);

$conn->close();
?>