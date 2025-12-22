<?php
require_once 'config.php';

// Only allow GET requests
if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit();
}

date_default_timezone_set('Asia/Manila');

// Optional filters
$account_type = $_GET['type'] ?? null;  // e.g., ?type=Teacher
$search = $_GET['search'] ?? null; 

$query = " SELECT 
        p.person_ID,
        p.person_username AS email,
        p.person_password AS password_hash, -- only for admin use, never send to app!
        a.account_name AS account_type,
        a.account_ID,
        
        n.name_first,
        n.name_second,
        n.name_middle,
        n.name_last,
        n.name_suffix,
        
        CONCAT(
            COALESCE(n.name_first, ''),
            IF(n.name_middle IS NOT NULL AND n.name_middle != '', CONCAT(' ', n.name_middle), ''),
            ' ', COALESCE(n.name_last, ''),
            IF(n.name_suffix IS NOT NULL AND n.name_suffix != '', CONCAT(' ', n.name_suffix), '')
        ) AS full_name

    FROM Person p
    JOIN Account_Type a ON p.account_ID = a.account_ID
    LEFT JOIN Name n ON p.name_ID = n.name_ID
    WHERE 1=1
";

$params = [];
$types = '';

if ($account_type) {
    $query .= " AND a.account_name = ?";
    $params[] = $account_type;
    $types .= 's';
}

if ($search) {
    $query .= " AND (
        p.person_username LIKE ? OR
        n.name_first LIKE ? OR
        n.name_last LIKE ? OR
        CONCAT(n.name_first, ' ', n.name_last) LIKE ?
    )";
    $like = "%$search%";
    $params[] = $like;
    $params[] = $like;
    $params[] = $like;
    $params[] = $like;
    $types .= 'ssss';
}

$query .= " ORDER BY a.account_name, n.name_last, n.name_first";

$stmt = $conn->prepare($query);

if (!$stmt) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'SQL Error: ' . $conn->error
    ]);
    exit();
}

if (!empty($params)) {
    $stmt->bind_param($types, ...$params);
}

$stmt->execute();
$result = $stmt->get_result();

$users = [];
while ($row = $result->fetch_assoc()) {
    $users[] = [
        'person_ID'      => (int)$row['person_ID'],
        'email'          => $row['email'],
        'account_type'   => $row['account_type'],
        'account_ID'     => (int)$row['account_ID'],
        'full_name'      => trim($row['full_name']) ?: $row['email'],
        'first_name'     => $row['name_first'] ?? '',
        'middle_name'    => $row['name_middle'] ?? '',
        'last_name'      => $row['name_last'] ?? '',
        'suffix'         => $row['name_suffix'] ?? ''
        // Optional: hide password in production!
        // 'password_hash' => $row['password_hash']
    ];
}

http_response_code(200);
echo json_encode([ 
    'success' => true,
    'count'   => count($users),
    'users'   => $users
    
]);

$stmt->close();
$conn->close();
?>