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
$required_fields = ['username', 'password', 'first_name', 'last_name', 'account_type'];
foreach ($required_fields as $field) {
    if (!isset($data[$field]) || empty(trim($data[$field]))) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => ucfirst(str_replace('_', ' ', $field)) . ' is required']);
        exit();
    }
}

$username = trim($data['username']);
$password = trim($data['password']);
$first_name = trim($data['first_name']);
$middle_name = isset($data['middle_name']) ? trim($data['middle_name']) : null;
$last_name = trim($data['last_name']);
$suffix = isset($data['suffix']) ? trim($data['suffix']) : null;
$account_type = trim($data['account_type']);

// Validate password strength
if (strlen($password) < 6) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Password must be at least 6 characters long']);
    exit();
}

// Check if username already exists
$check_query = "SELECT person_ID FROM person WHERE person_username = ?";
$check_stmt = $conn->prepare($check_query);
$check_stmt->bind_param("s", $username);
$check_stmt->execute();
$check_result = $check_stmt->get_result();

if ($check_result->num_rows > 0) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Username already exists']);
    exit();
}

// Get account_ID from account_name
$account_query = "SELECT account_ID FROM account_type WHERE account_name = ?";
$account_stmt = $conn->prepare($account_query);
$account_stmt->bind_param("s", $account_type);
$account_stmt->execute();
$account_result = $account_stmt->get_result();

if ($account_result->num_rows === 0) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Invalid account type']);
    exit();
}

$account_row = $account_result->fetch_assoc();
$account_ID = $account_row['account_ID'];

// Start transaction
$conn->begin_transaction();

try {
    // Insert into name table
    $name_query = "INSERT INTO name (name_first, name_middle, name_last, name_suffix) VALUES (?, ?, ?, ?)";
    $name_stmt = $conn->prepare($name_query);
    $name_stmt->bind_param("ssss", $first_name, $middle_name, $last_name, $suffix);
    $name_stmt->execute();
    $name_ID = $conn->insert_id;

    // Insert into person table (plain text password)
    $person_query = "INSERT INTO person (person_username, person_password, account_ID, name_ID) VALUES (?, ?, ?, ?)";
    $person_stmt = $conn->prepare($person_query);
    $person_stmt->bind_param("ssii", $username, $password, $account_ID, $name_ID);
    $person_stmt->execute();
    $person_ID = $conn->insert_id;

    // Commit transaction
    $conn->commit();

    http_response_code(201);
    echo json_encode([
        'success' => true,
        'message' => 'Registration successful',
        'user' => [
            'person_ID' => $person_ID,
            'username' => $username,
            'account_type' => $account_type,
            'name' => trim($first_name . ' ' . ($middle_name ? $middle_name . ' ' : '') . $last_name . ($suffix ? ' ' . $suffix : ''))
        ]
    ]);

} catch (Exception $e) {
    // Rollback transaction
    $conn->rollback();
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Registration failed: ' . $e->getMessage()]);
}

$check_stmt->close();
$account_stmt->close();
$conn->close();
?>
