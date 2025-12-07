<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');
header('Access-Control-Allow-Headers: Content-Type');

// Database configuration
$host = 'localhost';
$dbname = 'scheduling_system';
$username = 'root';
$password = '';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    // Get parameters
    $view = isset($_GET['view']) ? $_GET['view'] : 'day';
    $date = isset($_GET['date']) ? $_GET['date'] : date('Y-m-d');
    
    // Validate view parameter
    if (!in_array($view, ['day', 'week'])) {
        $view = 'day';
    }
    
    // Determine date range based on view
    if ($view == 'day') {
        $startDate = $date;
        $endDate = $date;
    } else {
        // Week view - get current week (Monday to Sunday)
        $timestamp = strtotime($date);
        $dayOfWeek = date('N', $timestamp); // 1 (Monday) to 7 (Sunday)
        
        // Calculate Monday of the week
        $mondayTimestamp = strtotime("-" . ($dayOfWeek - 1) . " days", $timestamp);
        $startDate = date('Y-m-d', $mondayTimestamp);
        
        // Calculate Sunday of the week
        $sundayTimestamp = strtotime("+6 days", $mondayTimestamp);
        $endDate = date('Y-m-d', $sundayTimestamp);
    }
    
    // Get current time
    $currentTime = date('H:i:s');
    $currentDate = date('Y-m-d');
    
    // Query to get all rooms with their schedule status
    $query = "
        SELECT 
            r.room_ID,
            r.room_name,
            r.room_capacity,
            CASE 
                WHEN EXISTS (
                    SELECT 1 
                    FROM schedules s
                    WHERE s.room_ID = r.room_ID
                    AND s.schedule_date BETWEEN :startDate AND :endDate
                    AND (
                        (s.schedule_date = :currentDate AND s.end_time > :currentTime)
                        OR (s.schedule_date > :currentDate)
                    )
                    AND s.status = 'approved'
                ) THEN 0
                ELSE 1
            END as isAvailable,
            CASE 
                WHEN EXISTS (
                    SELECT 1 
                    FROM schedules s
                    WHERE s.room_ID = r.room_ID
                    AND s.schedule_date = :currentDate
                    AND s.start_time <= :currentTime
                    AND s.end_time > :currentTime
                    AND s.status = 'approved'
                ) THEN CONCAT('Occupied until ', 
                    (SELECT TIME_FORMAT(s.end_time, '%h:%i %p') 
                     FROM schedules s 
                     WHERE s.room_ID = r.room_ID
                     AND s.schedule_date = :currentDate
                     AND s.start_time <= :currentTime
                     AND s.end_time > :currentTime
                     AND s.status = 'approved'
                     LIMIT 1))
                WHEN EXISTS (
                    SELECT 1 
                    FROM schedules s
                    WHERE s.room_ID = r.room_ID
                    AND s.schedule_date = :currentDate
                    AND s.start_time > :currentTime
                    AND s.status = 'approved'
                ) THEN CONCAT('Reserved at ', 
                    (SELECT TIME_FORMAT(s.start_time, '%h:%i %p') 
                     FROM schedules s 
                     WHERE s.room_ID = r.room_ID
                     AND s.schedule_date = :currentDate
                     AND s.start_time > :currentTime
                     AND s.status = 'approved'
                     ORDER BY s.start_time ASC
                     LIMIT 1))
                WHEN EXISTS (
                    SELECT 1 
                    FROM schedules s
                    WHERE s.room_ID = r.room_ID
                    AND s.schedule_date BETWEEN :startDate AND :endDate
                    AND s.schedule_date > :currentDate
                    AND s.status = 'approved'
                ) THEN 'Reserved this week'
                ELSE CONCAT('Available (Capacity: ', r.room_capacity, ')')
            END as status
        FROM rooms r
        ORDER BY r.room_name ASC
    ";
    
    $stmt = $pdo->prepare($query);
    $stmt->bindParam(':startDate', $startDate);
    $stmt->bindParam(':endDate', $endDate);
    $stmt->bindParam(':currentDate', $currentDate);
    $stmt->bindParam(':currentTime', $currentTime);
    $stmt->execute();
    
    $rooms = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    // Convert isAvailable to boolean
    foreach ($rooms as &$room) {
        $room['isAvailable'] = (bool)$room['isAvailable'];
    }
    
    echo json_encode([
        'success' => true,
        'view' => $view,
        'startDate' => $startDate,
        'endDate' => $endDate,
        'rooms' => $rooms,
        'message' => 'Rooms retrieved successfully'
    ]);
    
} catch (PDOException $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Database error: ' . $e->getMessage()
    ]);
} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage()
    ]);
}
?>