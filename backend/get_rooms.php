<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

$conn = new mysqli("localhost", "root", "", "scheduling-system");

if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "DB connection failed"]);
    exit();
}

// Optional: Later you can filter by teacher, schedule, etc.
// For now, return ALL rooms with current status
$sql = "SELECT
            r.room_ID,
            r.room_name,
            r.room_capacity,
            COALESCE(b.status, 'Available') AS status
        FROM Room r
        LEFT JOIN (
            SELECT room_ID, 'Occupied' AS status
            FROM Booking
            WHERE NOW() BETWEEN booking_start AND booking_end
              AND booking_status = 'Approved'
        ) b ON r.room_ID = b.room_ID
        ORDER BY r.room_name";

$result = $conn->query($sql);

$rooms = [];
while ($row = $result->fetch_assoc()) {
    $isAvailable = ($row['status'] !== 'Occupied');
    $statusText = $isAvailable ? "Available" : "Occupied";

    $rooms[] = [
        "id" => (int)$row['room_ID'],
        "name" => $row['room_name'],
        "capacity" => (int)$row['room_capacity'],
        "status" => $statusText,
        "isAvailable" => $isAvailable
    ];
}

echo json_encode([
    "success" => true,
    "rooms" => $rooms
]);

$conn->close();
?>