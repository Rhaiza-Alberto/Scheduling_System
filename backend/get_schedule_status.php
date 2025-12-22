<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

require_once 'config.php';

try {
    $pdo = new PDO("mysql:host=" . DB_HOST . ";dbname=" . DB_NAME, DB_USER, DB_PASS);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // Get current day and time
    $currentDay = date('l'); // Monday, Tuesday, etc.
    $currentTime = date('H:i:s');

    $query = "
        SELECT 
            s.schedule_ID,
            s.schedule_status,
            d.day_name,
            t_start.time_slot as time_start,
            t_end.time_slot as time_end,
            r.room_ID,
            r.room_name,
            sub.subject_code,
            sub.subject_name,
            sec.section_name,
            CONCAT(n.name_first, ' ', n.name_last) as teacher_name
        FROM Schedule s
        INNER JOIN Day d ON s.day_ID = d.day_ID
        INNER JOIN Time t_start ON s.time_start_ID = t_start.time_ID
        INNER JOIN Time t_end ON s.time_end_ID = t_end.time_ID
        INNER JOIN Room r ON s.room_ID = r.room_ID
        INNER JOIN Subject sub ON s.subject_ID = sub.subject_ID
        INNER JOIN Section sec ON s.section_ID = sec.section_ID
        INNER JOIN Person p ON s.teacher_ID = p.person_ID
        INNER JOIN Name n ON p.name_ID = n.name_ID
        WHERE d.day_name = ?
        AND s.schedule_status != 'isDeleted'
        ORDER BY t_start.time_slot ASC
    ";

    $stmt = $pdo->prepare($query);
    $stmt->execute([$currentDay]);
    $schedules = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Process each schedule and determine real-time status
    $processedSchedules = [];
    foreach ($schedules as $schedule) {
        $startTime = strtotime($schedule['time_start']);
        $endTime = strtotime($schedule['time_end']);
        $now = strtotime($currentTime);

        // Auto-update status based on time
        $autoStatus = $schedule['schedule_status'];
        $timeInfo = [];

        if ($now >= $startTime && $now < $endTime) {
            // Class is currently happening
            $autoStatus = 'Occupied';
            $minutesRemaining = ceil(($endTime - $now) / 60);
            $timeInfo['status'] = 'ongoing';
            $timeInfo['message'] = "Ends in {$minutesRemaining} mins";
            $timeInfo['minutes_remaining'] = $minutesRemaining;
        } elseif ($now < $startTime) {
            // Class hasn't started yet
            $minutesUntilStart = ceil(($startTime - $now) / 60);
            
            if ($minutesUntilStart <= 60) {
                $timeInfo['status'] = 'upcoming';
                $timeInfo['message'] = "Starts in {$minutesUntilStart} mins";
                $timeInfo['minutes_until_start'] = $minutesUntilStart;
            } else {
                $timeInfo['status'] = 'scheduled';
                $timeInfo['message'] = "Starts at " . date('g:i A', $startTime);
                $timeInfo['start_time_formatted'] = date('g:i A', $startTime);
            }
        } elseif ($now >= $endTime) {
            // Class has ended
            if ($schedule['schedule_status'] !== 'Completed') {
                $autoStatus = 'Completed';
            }
            $timeInfo['status'] = 'completed';
            $timeInfo['message'] = 'Completed';
        }

        // Update database if status changed
        if ($autoStatus !== $schedule['schedule_status']) {
            $updateStmt = $pdo->prepare("UPDATE Schedule SET schedule_status = ? WHERE schedule_ID = ?");
            $updateStmt->execute([$autoStatus, $schedule['schedule_ID']]);
        }

        $processedSchedules[] = array_merge($schedule, [
            'computed_status' => $autoStatus,
            'time_info' => $timeInfo,
            'is_current' => ($autoStatus === 'Occupied'),
            'time_start_formatted' => date('g:i A', $startTime),
            'time_end_formatted' => date('g:i A', $endTime)
        ]);
    }

    echo json_encode([
        'success' => true,
        'current_day' => $currentDay,
        'current_time' => date('g:i A', strtotime($currentTime)),
        'schedules' => $processedSchedules,
        'count' => count($processedSchedules)
    ]);

} catch (Exception $e) {
    echo json_encode(['success' => false, 'message' => $e->getMessage()]);
}
?>