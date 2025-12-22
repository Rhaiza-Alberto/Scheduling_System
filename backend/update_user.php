<?php
require_once 'config.php';

// Only allow POST requests
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit();
}

date_default_timezone_set('Asia/Manila');

// Get JSON input
$input = json_decode(file_get_contents('php://input'), true);

if ($input === null) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Invalid JSON']);
    exit();
}

// Required fields
$room_id      = $input['room_id'] ?? null;
$room_name    = trim($input['room_name'] ?? '');
$room_capacity = $input['room_capacity'] ?? null;

// Validate
if (!$room_id || $room_name === '' || $room_capacity === null || $room_capacity < 1) {
    http_response_code(400);
    echo json_encode([
        'success' => false,
        'message' => 'All fields are required and capacity must be positive'
    ]);
    exit();
}

// Optional: Prevent duplicate room names (except for same room)
$stmt = $conn->prepare("SELECT room_ID FROM Room WHERE room_name = ? AND room_ID != ?");
$stmt->bind_param("si", $room_name, $room_id);
$stmt->execute();
if ($stmt->get_result()->num_rows > 0) {
    echo json_encode([
        'success' => false,
        'message' => 'Room name already exists'
    ]);
    $stmt->close();
    exit();
}
$stmt->close();

// Update the room
$stmt = $conn->prepare("UPDATE Room SET room_name = ?, room_capacity = ? WHERE room_ID = ?");
$stmt->bind_param("sii", $room_name, $room_capacity, $room_id);

if ($stmt->execute()) {
    if ($stmt->affected_rows > 0) {
        echo json_encode([
            'success' => true,
            'message' => 'Room updated successfully',
            'room' => [
                'room_id' => (int)$room_id,
                'room_name' => $room_name,
                'room_capacity' => (int)$room_capacity
            ]
        ]);
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'No changes made or room not found'
        ]);
    }
} else {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Database error: ' . $stmt->error
    ]);
}

$stmt->close();
$conn->close();
?>