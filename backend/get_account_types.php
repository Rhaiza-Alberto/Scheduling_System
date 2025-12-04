<?php
require_once 'config.php';

// Only allow GET requests
if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit();
}

$query = "SELECT account_ID, account_name FROM account_type ORDER BY account_name";
$result = $conn->query($query);

if (!$result) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $conn->error]);
    exit();
}

$account_types = [];
while ($row = $result->fetch_assoc()) {
    $account_types[] = $row;
}

http_response_code(200);
echo json_encode([
    'success' => true,
    'account_types' => $account_types
]);

$conn->close();
?>
