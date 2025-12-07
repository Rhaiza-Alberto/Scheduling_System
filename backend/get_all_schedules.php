<?php
require_once 'config.php';

// Only allow GET requests
if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit();
}

// Set timezone
date_default_timezone_set('Asia/Manila');

// Optional filters
$day = isset($_GET['day']) ? $_GET['day'] : null;
$room_id = isset($_GET['room_id']) ? intval($_GET['room_id']) : null;
$section_id = isset($_GET['section_id']) ? intval($_GET['section_id']) : null;

// Build query
$query = "SELECT
    s.schedule_ID,
    d.day_name,
    
    -- Start time in 12-hour format (e.g., "7:00 AM", "1:30 PM")
    ts.display_name AS time_start,
    
    -- End time in 12-hour format
    te.display_name AS time_end,
    
    -- Raw time_slot values if needed
    ts.time_slot AS raw_start_time,
    te.time_slot AS raw_end_time,
    
    sub.subject_code,
    sub.subject_name,
    sec.section_name,
    sec.section_year,
    r.room_name,
    r.room_capacity,
    
    -- Teacher full name
    CONCAT(
        COALESCE(n.name_first, ''),
        IF(n.name_middle IS NOT NULL AND n.name_middle != '', CONCAT(' ', n.name_middle), ''),
        ' ', n.name_last
    ) AS teacher_name,
    
    s.schedule_status

FROM Schedule s
JOIN Day d ON s.day_ID = d.day_ID

-- Join Time table twice: once for start, once for end
JOIN Time ts ON s.time_start_ID = ts.time_ID
JOIN Time te ON s.time_end_ID = te.time_ID

JOIN Subject sub ON s.subject_ID = sub.subject_ID
JOIN Section sec ON s.section_ID = sec.section_ID
JOIN Room r ON s.room_ID = r.room_ID

-- Teacher join
JOIN Person p ON s.teacher_ID = p.person_ID
JOIN Name n ON p.name_ID = n.name_ID

-- Optional: Filter by room, day, etc.
-- WHERE r.room_name = 'Lab 1'
--   AND d.day_name = 'Monday'

ORDER BY 
    FIELD(d.day_name, 'Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday'),
    ts.time_slot;
";

// Add filters
$params = [];
$types = '';

if ($day) {
    $query .= " AND d.day_name = ?";
    $params[] = $day;
    $types .= 's';
}

if ($room_id) {
    $query .= " AND r.room_ID = ?";
    $params[] = $room_id;
    $types .= 'i';
}

if ($section_id) {
    $query .= " AND sec.section_ID = ?";
    $params[] = $section_id;
    $types .= 'i';
}

$query .= " ORDER BY 
    FIELD(d.day_name, 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'),
    t.time_start
";

$stmt = $conn->prepare($query);

if (!$stmt) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $conn->error]);
    exit();
}

// Bind parameters if any
if (!empty($params)) {
    $stmt->bind_param($types, ...$params);
}

$stmt->execute();
$result = $stmt->get_result();

$schedules = [];
while ($row = $result->fetch_assoc()) {
    $schedules[] = [
        'schedule_ID' => (int)$row['schedule_ID'],
        'day_name' => $row['day_name'],
        'time_start' => $row['time_start'],
        'time_end' => $row['time_end'],
        'raw_start' => $row['raw_start'],
        'raw_end' => $row['raw_end'],
        'subject_code' => $row['subject_code'],
        'subject_name' => $row['subject_name'],
        'section_name' => $row['section_name'],
        'section_year' => (int)$row['section_year'],
        'room_name' => $row['room_name'],
        'room_capacity' => (int)$row['room_capacity'],
        'teacher_name' => trim($row['teacher_name']),
        'schedule_status' => $row['schedule_status']
    ];
}

http_response_code(200);
echo json_encode([
    'success' => true,
    'count' => count($schedules),
    'schedules' => $schedules
]);

$stmt->close();
$conn->close();
?>