<?php
// add_schedule.php

require_once 'config.php';

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit();
}

// Read JSON input
$input = json_decode(file_get_contents('php://input'), true);

if (!$input) {
    echo json_encode(['success' => false, 'message' => 'Invalid JSON input']);
    exit;
}

// Extract fields
$day_name = $input['day_name'] ?? null;
$subject_id = $input['subject_id'] ?? null;
$section_id = $input['section_id'] ?? null;
$teacher_id = $input['teacher_id'] ?? null;
$start_display = $input['start_display'] ?? null;
$end_display = $input['end_display'] ?? null;
$room_id = $input['room_id'] ?? null;
$schedule_status = $input['schedule_status'] ?? 'Pending';

// Validate required fields
if (empty($day_name) || empty($subject_id) || empty($section_id) || empty($teacher_id) ||
    empty($start_display) || empty($end_display) || empty($room_id)) {
    echo json_encode(['success' => false, 'message' => 'Missing required fields']);
    exit;
}

// Prepare and bind
$stmt = $conn->prepare("INSERT INTO schedules (day_name, subject_id, section_id, teacher_id, start_display, end_display, room_id, schedule_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
if (!$stmt) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $conn->error]);
    exit();
}
$stmt->bind_param("siiissis", $day_name, $subject_id, $section_id, $teacher_id, $start_display, $end_display, $room_id, $schedule_status);

if ($stmt->execute()) {
    echo json_encode(['success' => true, 'message' => 'Schedule added successfully', 'schedule_id' => $conn->insert_id]);
} else {
    echo json_encode(['success' => false, 'message' => 'Error: ' . $stmt->error]);
}

$stmt->close();
$conn->close();
?>