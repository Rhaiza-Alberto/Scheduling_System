<?php
// get_teachers.php

require_once 'config.php';

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit();
}

$sql = "SELECT person_ID AS id, CONCAT(
            COALESCE(n.name_first, ''),
            IF(n.name_middle IS NOT NULL AND n.name_middle != '', CONCAT(' ', n.name_middle), ''),
            ' ', COALESCE(n.name_last, '')
        ) AS teacher_name, person_username AS email FROM person JOIN Name AS n ON person.name_ID = n.name_ID WHERE account_ID = 2 AND person_isDeleted = 0";

$result = $conn->query($sql);
if (!$result) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $conn->error]);
    exit();
}

$teachers = [];
while ($row = $result->fetch_assoc()) {
    $teachers[] = $row;
}

echo json_encode([
    'success' => true,
    'teachers' => $teachers
]);

$conn->close();
?>