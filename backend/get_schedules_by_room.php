<?php
require_once 'config.php';
 
header('Content-Type: application/json');
 
if ($_SERVER['REQUEST_METHOD'] !== 'GET') {    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit();
}
 
if (!isset($_GET['room_id']) || empty($_GET['room_id'])) {
    echo json_encode(['success' => false, 'message' => 'Room ID not provided']);
    exit();
}

$roomId = (int)$_GET['room_id'];

// SQL Query remains the same
$sql = "SELECT
            s.day_name,
            s.start_display,
            s.end_display,
            sub.subject_name,
            sec.section_name,
            t.teacher_name
        FROM
            schedules AS s
        JOIN
            subjects AS sub ON s.subject_id = sub.subject_id
        JOIN
            sections AS sec ON s.section_id = sec.section_id
        JOIN
            teachers AS t ON s.teacher_id = t.teacher_id
        WHERE
            s.room_id = ?";

$stmt = $conn->prepare($sql);
if (!$stmt) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $conn->error]);
    exit();
}

$stmt->bind_param("i", $roomId);
if (!$stmt->execute()) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $stmt->error]);
    exit();
}

$result = $stmt->get_result();
if (!$result) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $stmt->error]);
    exit();
}

$schedules = [];
while ($row = $result->fetch_assoc()) {
    $schedules[] = $row;
}

if (empty($schedules)) {
    // Return 'success' => false, but still with a 200 OK status
    echo json_encode(['success' => false, 'message' => 'No schedules found for this room']);
} else {
    echo json_encode([
        'success' => true,
        'schedules' => $schedules
    ]);
}

$stmt->close();
$conn->close();
?>