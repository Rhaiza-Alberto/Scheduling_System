<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET");
header("Access-Control-Allow-Headers: Content-Type");

// Database configuration
$host = "localhost";
$username = "root";
$password = "";
$dbname = "scheduling-system";

// Create database connection
$conn = new mysqli($host, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    echo json_encode([
        "success" => false,
        "message" => "Database connection failed: " . $conn->connect_error
    ]);
    exit();
}

// Get schedule ID from query parameter
$scheduleId = isset($_GET['schedule_id']) ? (int)$_GET['schedule_id'] : 0;

if ($scheduleId <= 0) {
    echo json_encode([
        "success" => false,
        "message" => "Invalid schedule ID"
    ]);
    exit();
}

// Prepare SQL query to get schedule details with all related information
$sql = "SELECT 
    s.schedule_ID,
    s.schedule_status,
    d.day_name,
    t.display_name AS time_start,
    t2.display_name AS time_end,
    sub.subject_code,
    sub.subject_name,
    sec.section_name,
    sec.section_year,
    p.person_name AS teacher_name,
    p.person_email AS teacher_email,
    r.room_name
FROM schedule s
LEFT JOIN day d ON s.day_ID = d.day_ID
LEFT JOIN time t ON s.time_start_ID = t.time_ID
LEFT JOIN time t2 ON s.time_end_ID = t2.time_ID
LEFT JOIN subject sub ON s.subject_ID = sub.subject_ID
LEFT JOIN section sec ON s.section_ID = sec.section_ID
LEFT JOIN person p ON s.person_ID = p.person_ID
LEFT JOIN room r ON s.room_ID = r.room_ID
WHERE s.schedule_ID = ?";

$stmt = $conn->prepare($sql);
if ($stmt === false) {
    echo json_encode([
        "success" => false,
        "message" => "Query preparation failed: " . $conn->error
    ]);
    exit();
}

$stmt->bind_param("i", $scheduleId);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $schedule = $result->fetch_assoc();
    
    echo json_encode([
        "success" => true,
        "schedule" => [
            "schedule_ID" => $schedule['schedule_ID'],
            "day_name" => $schedule['day_name'],
            "time_start" => $schedule['time_start'],
            "time_end" => $schedule['time_end'],
            "subject_code" => $schedule['subject_code'],
            "subject_name" => $schedule['subject_name'],
            "section_name" => $schedule['section_name'],
            "section_year" => $schedule['section_year'],
            "teacher_name" => $schedule['teacher_name'],
            "teacher_email" => $schedule['teacher_email'],
            "room_name" => $schedule['room_name'],
            "schedule_status" => $schedule['schedule_status'] ?? 'Pending'
        ]
    ]);
} else {
    echo json_encode([
        "success" => false,
        "message" => "Schedule not found"
    ]);
}

$stmt->close();
$conn->close();
?>
