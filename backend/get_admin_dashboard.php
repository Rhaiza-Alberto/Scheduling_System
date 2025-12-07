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
$today = date("l");

try {
    // Get total counts
    $stats = [];
    
    // Total teachers
    $result = $conn->query("SELECT COUNT(*) as count FROM Teacher");
    $stats['total_teachers'] = $result->fetch_assoc()['count'];
    
    // Total subjects
    $result = $conn->query("SELECT COUNT(*) as count FROM Subject");
    $stats['total_subjects'] = $result->fetch_assoc()['count'];
    
    // Total sections
    $result = $conn->query("SELECT COUNT(*) as count FROM Section");
    $stats['total_sections'] = $result->fetch_assoc()['count'];
    
    // Total rooms
    $result = $conn->query("SELECT COUNT(*) as count FROM Room");
    $stats['total_rooms'] = $result->fetch_assoc()['count'];
    
    // Total schedules
    $result = $conn->query("SELECT COUNT(*) as count FROM Schedule");
    $stats['total_schedules'] = $result->fetch_assoc()['count'];
    
    // Schedules by status
    $result = $conn->query("
        SELECT schedule_status, COUNT(*) as count 
        FROM Schedule 
        GROUP BY schedule_status
    ");
    $stats['schedules_by_status'] = [];
    while ($row = $result->fetch_assoc()) {
        $stats['schedules_by_status'][$row['schedule_status']] = (int)$row['count'];
    }
    
    // Today's schedule
    $query = "
    SELECT 
        s.schedule_ID,
        DATE_FORMAT(t.time_start, '%h:%i %p') AS time_start,
        DATE_FORMAT(t.time_end, '%h:%i %p') AS time_end,
        sub.subject_code,
        sub.subject_name,
        sec.section_name,
        sec.section_year,
        r.room_name,
        CONCAT(n.name_first, ' ', COALESCE(n.name_middle, ''), ' ', n.name_last) AS teacher_name,
        s.schedule_status
    FROM Schedule s
    JOIN Day d ON s.day_ID = d.day_ID
    JOIN Time t ON s.time_ID = t.time_ID
    JOIN Subject sub ON s.subject_ID = sub.subject_ID
    JOIN Section sec ON s.section_ID = sec.section_ID
    JOIN Room r ON s.room_ID = r.room_ID
    JOIN Teacher teach ON s.teacher_ID = teach.teacher_ID
    JOIN Person p ON teach.person_ID = p.person_ID
    JOIN Name n ON p.name_ID = n.name_ID
    WHERE d.day_name = '$today'
    ORDER BY t.time_start
    ";
    
    $result = $conn->query($query);
    $today_schedules = [];
    while ($row = $result->fetch_assoc()) {
        $today_schedules[] = [
            'schedule_ID' => (int)$row['schedule_ID'],
            'time_start' => $row['time_start'],
            'time_end' => $row['time_end'],
            'subject_code' => $row['subject_code'],
            'subject_name' => $row['subject_name'],
            'section_name' => $row['section_name'],
            'section_year' => (int)$row['section_year'],
            'room_name' => $row['room_name'],
            'teacher_name' => trim($row['teacher_name']),
            'schedule_status' => $row['schedule_status']
        ];
    }
    
    // Recent schedules (last 10)
    $query = "
    SELECT 
        s.schedule_ID,
        d.day_name,
        DATE_FORMAT(t.time_start, '%h:%i %p') AS time_start,
        DATE_FORMAT(t.time_end, '%h:%i %p') AS time_end,
        sub.subject_code,
        sec.section_name,
        r.room_name,
        CONCAT(n.name_first, ' ', COALESCE(n.name_middle, ''), ' ', n.name_last) AS teacher_name
    FROM Schedule s
    JOIN Day d ON s.day_ID = d.day_ID
    JOIN Time t ON s.time_ID = t.time_ID
    JOIN Subject sub ON s.subject_ID = sub.subject_ID
    JOIN Section sec ON s.section_ID = sec.section_ID
    JOIN Room r ON s.room_ID = r.room_ID
    JOIN Teacher teach ON s.teacher_ID = teach.teacher_ID
    JOIN Person p ON teach.person_ID = p.person_ID
    JOIN Name n ON p.name_ID = n.name_ID
    ORDER BY s.schedule_ID DESC
    LIMIT 10
    ";
    
    $result = $conn->query($query);
    $recent_schedules = [];
    while ($row = $result->fetch_assoc()) {
        $recent_schedules[] = [
            'schedule_ID' => (int)$row['schedule_ID'],
            'day_name' => $row['day_name'],
            'time_start' => $row['time_start'],
            'time_end' => $row['time_end'],
            'subject_code' => $row['subject_code'],
            'section_name' => $row['section_name'],
            'room_name' => $row['room_name'],
            'teacher_name' => trim($row['teacher_name'])
        ];
    }
    
    http_response_code(200);
    echo json_encode([
        'success' => true,
        'stats' => $stats,
        'today' => $today,
        'today_schedules' => $today_schedules,
        'recent_schedules' => $recent_schedules
    ]);
    
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Error fetching dashboard data: ' . $e->getMessage()
    ]);
}

$conn->close();
?>