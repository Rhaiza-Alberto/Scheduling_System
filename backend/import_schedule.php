<?php
require 'assets/vendor/autoload.php'; 
use PhpOffice\PhpSpreadsheet\IOFactory;

if ($_FILES['excel']['name']) {
    $file = $_FILES['excel']['tmp_name'];
    $spreadsheet = IOFactory::load($file);
    $sheet = $spreadsheet->getActiveSheet();
    $rows = $sheet->toArray();

    // Skip header row
    array_shift($rows);

    $pdo = new PDO("mysql:host=localhost;dbname=scheduling-system", "root", "");
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    $pdo->beginTransaction();

    foreach ($rows as $row) {
        list($dayName, $subjectCode, $sectionName, $teacherEmail, $startTime, $endTime, $roomName, $status) = $row;

        // Clean data
        $dayName     = trim($dayName);
        $subjectCode = trim($subjectCode);
        $sectionName = trim($sectionName);
        $teacherEmail= trim(strtolower($teacherEmail));
        $roomName    = trim($roomName);
        $status      = empty($status) ? 'Pending' : trim($status);

        // Convert times to time_ID
        $startID = $pdo->query("SELECT time_ID FROM Time WHERE TIME_FORMAT(time_slot, '%H:%i') = '$startTime'")->fetchColumn();
        $endID   = $pdo->query("SELECT time_ID FROM Time WHERE TIME_FORMAT(time_slot, '%H:%i') = '$endTime'")->fetchColumn();

        if (!$startID || !$endID) {
            echo "Time not found: $startTime - $endTime<br>";
            continue;
        }

        // Get IDs
        $dayID      = $pdo->query("SELECT day_ID FROM Day WHERE day_name = '$dayName'")->fetchColumn();
        $subjectID  = $pdo->query("SELECT subject_ID FROM Subject WHERE subject_code = '$subjectCode'")->fetchColumn();
        $sectionID  = $pdo->query("SELECT section_ID FROM Section WHERE section_name = '$sectionName'")->fetchColumn();
        $teacherID  = $pdo->query("SELECT person_ID FROM Person WHERE person_username = '$teacherEmail'")->fetchColumn();
        $roomID     = $pdo->query("SELECT room_ID FROM Room WHERE room_name = '$roomName'")->fetchColumn();

        if ($dayID && $subjectID && $sectionID && $teacherID && $roomID) {
            $sql = "INSERT INTO Schedule 
                (day_ID, subject_ID, section_ID, teacher_ID, time_start_ID, time_end_ID, room_ID, schedule_status) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            $stmt = $pdo->prepare($sql);
            $stmt->execute([$dayID, $subjectID, $sectionID, $teacherID, $startID, $endID, $roomID, $status]);
            echo "Added: $subjectCode - $sectionName - $roomName<br>";
        } else {
            echo "SKIP (not found): $subjectCode | $sectionName | $teacherEmail | $roomName<br>";
        }
    }

    $pdo->commit();
    echo "<h3>Import completed!</h3>";
}
?>

<!-- Simple Upload Form -->
<form method="post" enctype="multipart/form-data">
    <h2>Upload Schedule Excel/CSV</h2>
    <input type="file" name="excel" accept=".xlsx,.csv" required>
    <button type="submit">Import Schedule</button>
</form>