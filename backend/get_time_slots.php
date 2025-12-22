<?php
// get_time_slots.php

require_once 'config.php';

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit();
}

$sql = "SELECT time_ID AS id, display_name, time_slot AS start_time, time_slot AS end_time FROM time";

$result = $conn->query($sql);
if (!$result) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $conn->error]);
    exit();
}

$timeSlots = [];
while ($row = $result->fetch_assoc()) {
    $timeSlots[] = $row;
}

echo json_encode([
    'success' => true,
    'times' => $timeSlots
]);

$conn->close();
?>