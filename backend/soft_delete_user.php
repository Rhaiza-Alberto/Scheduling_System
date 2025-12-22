<?php
require_once 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit();
}

$input = json_decode(file_get_contents('php://input'), true);
$person_id = $input['person_id'] ?? null;

if (!$person_id || !is_numeric($person_id)) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Valid person_id required']);
    exit();
}

$person_id = (int)$person_id;

// Mark as deleted (soft delete)
$stmt = $conn->prepare("UPDATE Person SET person_isDeleted = 1 WHERE person_ID = ?");
$stmt->bind_param("i", $person_id);

if ($stmt->execute() && $stmt->affected_rows > 0) {
    echo json_encode([
        'success' => true,
        'message' => 'Person removed from list'
    ]);
} else {
    echo json_encode([
        'success' => false,
        'message' => 'Person not found or already deleted'
    ]);
}

$stmt->close();
$conn->close();
?>