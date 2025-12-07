<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");

// Connect to database
$conn = new mysqli("localhost", "root", "", "scheduling-system");

if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "DB connection failed: " . $conn->connect_error]);
    exit();
}

// 1. Count Teachers
$sql_teachers = "SELECT COUNT(*) as total_teachers 
                 FROM person p 
                 JOIN account_type a ON a.account_ID = p.account_ID 
                 WHERE a.account_name = 'Teacher'";
$result_teachers = $conn->query($sql_teachers);
$row_teachers = $result_teachers->fetch_assoc();
$total_teachers = (int)$row_teachers['total_teachers'];

// 2. Count Rooms
$sql_rooms = "SELECT COUNT(*) as total_rooms FROM room";
$result_rooms = $conn->query($sql_rooms);
$row_rooms = $result_rooms->fetch_assoc();
$total_rooms = (int)$row_rooms['total_rooms'];

// 3. Count Schedules (adjust table name as needed)
$sql_schedules = "SELECT COUNT(*) as total_schedules FROM schedule";  // or booking_requests?
$result_schedules = $conn->query($sql_schedules);
$row_schedules = $result_schedules->fetch_assoc();
$total_schedules = (int)$row_schedules['total_schedules'];

echo json_encode([
    "success" => true,
    "stats" => [
        "total_teachers" => $total_teachers,
        "total_rooms" => $total_rooms,
        "total_schedules" => $total_schedules
    ]
]);

$conn->close();
?>