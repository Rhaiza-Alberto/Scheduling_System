<?php
header("Content-Type: application/json");

if (!extension_loaded('mongodb')) {
    echo json_encode([
        "success" => false,
        "message" => "MongoDB PHP extension is not installed"
    ]);
    exit();
}

require_once __DIR__ . '/vendor/autoload.php';

try {
    $client = new MongoDB\Client("mongodb://localhost:27017");
    $database = $client->scheduling_db;
    
    $usersCount = $database->users->countDocuments();
    $schedulesCount = $database->schedules->countDocuments();
    
    echo json_encode([
        "success" => true,
        "message" => "MongoDB connection successful!",
        "users_count" => $usersCount,
        "schedules_count" => $schedulesCount
    ], JSON_PRETTY_PRINT);
} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "message" => "Connection failed: " . $e->getMessage()
    ], JSON_PRETTY_PRINT);
}
?>

