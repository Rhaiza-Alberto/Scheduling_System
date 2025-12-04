<?php
// Verification script to check if everything is set up correctly
header('Content-Type: application/json');

$checks = [
    'php_version' => phpversion(),
    'extensions' => [],
    'database' => [],
    'files' => [],
    'endpoints' => []
];

// Check required extensions
$required_extensions = ['mysqli', 'json'];
foreach ($required_extensions as $ext) {
    $checks['extensions'][$ext] = extension_loaded($ext) ? 'OK' : 'MISSING';
}

// Check database connection
try {
    $conn = new mysqli('localhost', 'root', '', 'scheduling-system');
    if ($conn->connect_error) {
        $checks['database']['connection'] = 'FAILED: ' . $conn->connect_error;
    } else {
        $checks['database']['connection'] = 'OK';
        
        // Check tables
        $tables = ['person', 'account_type', 'name', 'day', 'time', 'room', 'section', 'subject', 'schedule'];
        foreach ($tables as $table) {
            $result = $conn->query("SHOW TABLES LIKE '$table'");
            $checks['database']['table_' . $table] = ($result && $result->num_rows > 0) ? 'OK' : 'MISSING';
        }
        
        // Check test user
        $result = $conn->query("SELECT COUNT(*) as count FROM person WHERE person_username = 'admin@example.com'");
        $row = $result->fetch_assoc();
        $checks['database']['test_user_admin'] = ($row['count'] > 0) ? 'OK' : 'MISSING';
        
        $conn->close();
    }
} catch (Exception $e) {
    $checks['database']['connection'] = 'ERROR: ' . $e->getMessage();
}

// Check files
$files = ['config.php', 'login.php', 'test.php', 'register.php', 'get_account_types.php'];
foreach ($files as $file) {
    $checks['files'][$file] = file_exists($file) ? 'OK' : 'MISSING';
}

// Check endpoints
$endpoints = [
    'test.php' => 'GET',
    'login.php' => 'POST',
    'register.php' => 'POST'
];
foreach ($endpoints as $endpoint => $method) {
    $checks['endpoints'][$endpoint] = $method;
}

echo json_encode([
    'success' => true,
    'message' => 'Setup verification complete',
    'timestamp' => date('Y-m-d H:i:s'),
    'checks' => $checks
], JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
?>
