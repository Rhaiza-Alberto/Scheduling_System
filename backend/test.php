<?php
// Simple test endpoint to verify connectivity
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

echo json_encode([
    'success' => true,
    'message' => 'Connection successful',
    'timestamp' => date('Y-m-d H:i:s')
]);
?>
