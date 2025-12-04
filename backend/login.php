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
if (!isset($data['username']) || !isset($data['password'])) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Username and password are required']);
    exit();
}

$username = trim($data['username']);
$password = trim($data['password']);

// Validate input is not empty
if (empty($username) || empty($password)) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Username and password cannot be empty']);
    exit();
}

// Query to get user with account type
$query = "SELECT p.person_ID, p.person_username, p.person_password, 
                 p.account_ID, a.account_name, 
                 n.name_first, n.name_middle, n.name_last, n.name_suffix
          FROM person p
          JOIN account_type a ON p.account_ID = a.account_ID
          LEFT JOIN name n ON p.name_ID = n.name_ID
          WHERE p.person_username = ?";

$stmt = $conn->prepare($query);

if (!$stmt) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $conn->error]);
    exit();
}

$stmt->bind_param("s", $username);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    http_response_code(401);
    echo json_encode(['success' => false, 'message' => 'Invalid username or password']);
    exit();
}

$user = $result->fetch_assoc();

// Verify password (plain text comparison)
if ($password !== $user['person_password']) {
    http_response_code(401);
    echo json_encode(['success' => false, 'message' => 'Invalid username or password']);
    exit();
}

// Build full name from name parts
$nameParts = [];
if (!empty($user['name_first'])) $nameParts[] = $user['name_first'];
if (!empty($user['name_middle'])) $nameParts[] = $user['name_middle'];
if (!empty($user['name_last'])) $nameParts[] = $user['name_last'];
if (!empty($user['name_suffix'])) $nameParts[] = $user['name_suffix'];

$fullName = !empty($nameParts) ? implode(' ', $nameParts) : $user['person_username'];

// Successful login
$response = [
    'success' => true,
    'message' => 'Login successful',
    'user' => [
        'person_ID' => $user['person_ID'],
        'username' => $user['person_username'],
        'account_type' => $user['account_name'],
        'account_ID' => $user['account_ID'],
        'name' => $fullName
    ]
];

http_response_code(200);
echo json_encode($response);

$stmt->close();
$conn->close();
?>
