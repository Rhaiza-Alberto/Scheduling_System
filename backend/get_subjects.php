<?php
// get_subjects.php

require_once 'config.php';

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit();
}

$sql = "SELECT subject_ID AS id, subject_code AS code, subject_name AS name FROM Subject";

$result = $conn->query($sql);
if (!$result) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $conn->error]);
    exit();
}

$subjects = [];
while ($row = $result->fetch_assoc()) {
    $subjects[] = $row;
}

echo json_encode([
    'success' => true,
    'subjects' => $subjects
]);

$conn->close();
?>