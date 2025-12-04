<?php
require_once 'config.php';

// Only allow POST requests
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit();
}

// Get POST data
$data = json_decode(file_get_contents("php://input"), true);

// Validate input
if (!isset($data['person_ID'])) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'person_ID is required']);
    exit();
}

$person_ID = intval($data['person_ID']);

// Query to get user
$query = "SELECT p.person_ID, p.person_username, p.account_ID, a.account_name, 
                 n.name_first, n.name_middle, n.name_last, n.name_suffix
          FROM person p
          JOIN account_type a ON p.account_ID = a.account_ID
          JOIN name n ON p.name_ID = n.name_ID
          WHERE p.person_ID = ?";

$stmt = $conn->prepare($query);

if (!$stmt) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $conn->error]);
    exit();
}

$stmt->bind_param("i", $person_ID);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    http_response_code(401);
    echo json_encode(['success' => false, 'message' => 'User not found']);
    exit();
}

$user = $result->fetch_assoc();

// Successful verification
$response = [
    'success' => true,
    'message' => 'Token verified',
    'user' => [
        'person_ID' => $user['person_ID'],
        'username' => $user['person_username'],
        'account_type' => $user['account_name'],
        'account_ID' => $user['account_ID'],
        'name' => trim($user['name_first'] . ' ' . ($user['name_middle'] ? $user['name_middle'] . ' ' : '') . $user['name_last'] . ($user['name_suffix'] ? ' ' . $user['name_suffix'] : ''))
    ]
];

http_response_code(200);
echo json_encode($response);

$stmt->close();
$conn->close();
?>
