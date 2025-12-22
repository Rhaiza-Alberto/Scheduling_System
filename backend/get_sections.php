<?php
// get_sections.php

require_once 'config.php';

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit();
}

// Get teacher_id and subject_id from query parameters (for future use)
$teacherId = isset($_GET['teacher_id']) ? (int)$_GET['teacher_id'] : null;
$subjectId = isset($_GET['subject_id']) ? (int)$_GET['subject_id'] : null;

// Build SQL query - sections table doesn't have teacher/subject relationships
// So we return all sections for now
$sql = "SELECT section_ID AS id, section_name AS name, section_year AS year FROM section ORDER BY section_name, section_year";

$stmt = $conn->prepare($sql);
if ($stmt === false) {
    echo json_encode(['success' => false, 'message' => 'Query preparation failed: ' . $conn->error]);
    exit();
}

$stmt->execute();
$result = $stmt->get_result();

if (!$result) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $conn->error]);
    exit();
}

$sections = [];
while ($row = $result->fetch_assoc()) {
    $sections[] = [
        'id' => (int)$row['id'],
        'name' => $row['name'],
        'year' => (int)$row['year']
    ];
}

echo json_encode([
    'success' => true,
    'sections' => $sections
]);

$stmt->close();
$conn->close();
?>
