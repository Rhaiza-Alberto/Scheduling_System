<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

date_default_timezone_set('Asia/Manila');

$conn = new mysqli("localhost", "root", "", "scheduling-system");

if ($conn->connect_error) {
    die(json_encode(["error" => $conn->connect_error]));
}

/* --- Get today's day name (Monday, Tuesday, etc.) --- */

$today = date("l"); // Returns full day name

$sql = "
SELECT 
    s.schedule_ID,
    d.day_name,
    DATE_FORMAT(t.time_start, '%h:%i %p') AS time_start,
    DATE_FORMAT(t.time_end, '%h:%i %p') AS time_end,
    sub.subject_code,
    sub.subject_name,
    sec.section_name,
    sec.section_year,
    r.room_name,
    n.name_first,
    n.name_last
FROM Schedule s
JOIN Day d ON s.day_ID = d.day_ID
JOIN Time t ON s.time_ID = t.time_ID
JOIN Subject sub ON s.subject_ID = sub.subject_ID
JOIN Section sec ON s.section_ID = sec.section_ID
JOIN Room r ON s.room_ID = r.room_ID
JOIN Person p ON s.teacher_ID = p.person_ID
JOIN Name n ON p.name_ID = n.name_ID
WHERE d.day_name = '$today'
ORDER BY t.time_start
";

$result = $conn->query($sql);

$data = [];
while ($row = $result->fetch_assoc()) {
    $data[] = $row;
}

echo json_encode($data);
$conn->close();
?>
