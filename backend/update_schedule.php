<?php
// update_schedule.php

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
$schedule_id = $input['schedule_id'] ?? null;
$day_name = $input['day_name'] ?? null;
$subject_id = $input['subject_id'] ?? null;
$section_id = $input['section_id'] ?? null;
$teacher_id = $input['teacher_id'] ?? null;
$start_display = $input['start_display'] ?? null;
$end_display = $input['end_display'] ?? null;
$room_id = $input['room_id'] ?? null;
$schedule_status = $input['schedule_status'] ?? null;

// Validate required fields
if (empty($schedule_id)) {
    echo json_encode(['success' => false, 'message' => 'Missing schedule_id']);
    exit;
}

// Build the update query dynamically based on provided fields
$updates = [];
$params = [];
$types = '';

if (!empty($day_name)) {
    $updates[] = "day_name = ?";
    $params[] = $day_name;
    $types .= 's';
}
if (!empty($subject_id)) {
    $updates[] = "subject_id = ?";
    $params[] = $subject_id;
    $types .= 'i';
}
if (!empty($section_id)) {
    $updates[] = "section_id = ?";
    $params[] = $section_id;
    $types .= 'i';
}
if (!empty($teacher_id)) {
    $updates[] = "teacher_id = ?";
    $params[] = $teacher_id;
    $types .= 'i';
}
if (!empty($start_display)) {
    $updates[] = "start_display = ?";
    $params[] = $start_display;
    $types .= 's';
}
if (!empty($end_display)) {
    $updates[] = "end_display = ?";
    $params[] = $end_display;
    $types .= 's';
}
if (!empty($room_id)) {
    $updates[] = "room_id = ?";
    $params[] = $room_id;
    $types .= 'i';
}
if (!empty($schedule_status)) {
    $updates[] = "schedule_status = ?";
    $params[] = $schedule_status;
    $types .= 's';
}

if (empty($updates)) {
    echo json_encode(['success' => false, 'message' => 'No fields to update']);
    exit;
}

// Add schedule_id to params
$params[] = $schedule_id;
$types .= 'i';

$updateQuery = "UPDATE schedules SET " . implode(', ', $updates) . " WHERE schedule_id = ?";

$stmt = $conn->prepare($updateQuery);
if (!$stmt) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $conn->error]);
    exit();
}
$stmt->bind_param($types, ...$params);

if ($stmt->execute()) {
    if ($stmt->affected_rows > 0) {
        echo json_encode(['success' => true, 'message' => 'Schedule updated successfully']);
    } else {
        echo json_encode(['success' => false, 'message' => 'No changes made or schedule not found']);
    }
} else {
    echo json_encode(['success' => false, 'message' => 'Error: ' . $stmt->error]);
}

$stmt->close();
$conn->close();
?>