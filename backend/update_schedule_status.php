<?php
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST, GET, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");
header("Content-Type: application/json");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once 'config.php';

try {
    $pdo = new PDO("mysql:host=" . DB_HOST . ";dbname=" . DB_NAME, DB_USER, DB_PASS);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    if ($_SERVER['REQUEST_METHOD'] === 'POST') {
        $data = json_decode(file_get_contents('php://input'), true);
        
        $scheduleId = $data['schedule_id'] ?? null;
        $newStatus = $data['status'] ?? null;
        
        if (!$scheduleId || !$newStatus) {
            echo json_encode(['success' => false, 'message' => 'Missing required fields']);
            exit;
        }

        // Validate status
        $validStatuses = ['Completed', 'Suspended', 'Cancelled', 'Pending', 'On-Going', 'Occupied'];
        if (!in_array($newStatus, $validStatuses)) {
            echo json_encode(['success' => false, 'message' => 'Invalid status']);
            exit;
        }

        $stmt = $pdo->prepare("UPDATE Schedule SET schedule_status = ? WHERE schedule_ID = ?");
        $stmt->execute([$newStatus, $scheduleId]);

        echo json_encode([
            'success' => true,
            'message' => 'Status updated successfully',
            'schedule_id' => $scheduleId,
            'new_status' => $newStatus
        ]);
    }
} catch (Exception $e) {
    echo json_encode(['success' => false, 'message' => $e->getMessage()]);
}
?>