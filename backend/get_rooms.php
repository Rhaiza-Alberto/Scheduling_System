<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

$conn = new mysqli("localhost", "root", "", "scheduling-system");

if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "DB connection failed"]);
    exit();
}

$sql = "SELECT 
            r.room_ID,
            r.room_name,
            r.room_capacity
        FROM Room r
        ORDER BY r.room_name";

$result = $conn->query($sql);

$rooms = [];
while ($row = $result->fetch_assoc()) {
    $room_name_with_linebreak = "\n";
    $rooms[] = [
        "id" => (int)$row['room_ID'],
        "name" => $row['room_name'],
        "capacity" => (int)$row['room_capacity']
    ];
}

echo json_encode([
    "success" => true,
    "rooms" => $rooms
]);

$conn->close();
?>