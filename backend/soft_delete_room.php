<?php
require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit();
}

$input = json_decode(file_get_contents('php://input'), true);
$room_id = $input['room_id'] ?? null;

if (!$room_id || !is_numeric($room_id)) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Valid room_id required']);
    exit();
}

$room_id = (int)$room_id;

// Mark as deleted (soft delete)
$stmt = $conn->prepare("UPDATE Room SET room_isDeleted = 1 WHERE room_ID = ?");
$stmt->bind_param("i", $room_id);

if ($stmt->execute() && $stmt->affected_rows > 0) {
    echo json_encode([
        'success' => true,
        'message' => 'Room removed from list'
    ]);
} else {
    echo json_encode([
        'success' => false,
        'message' => 'Room not found or already deleted'
    ]);
}

$stmt->close();
$conn->close();
?>