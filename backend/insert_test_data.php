<?php
require_once 'config.php';

echo "=== Inserting Test Data ===\n\n";

// 1. Insert Account Types
echo "[1/5] Inserting Account Types...\n";
$accountTypes = [
    ['Admin', 'Administrator'],
    ['Teacher', 'Teacher'],
    ['Student', 'Student']
];

foreach ($accountTypes as $type) {
    $stmt = $conn->prepare("INSERT INTO Account_Type (account_name) VALUES (?)");
    $stmt->bind_param("s", $type[0]);
    if ($stmt->execute()) {
        echo "  ✓ Inserted: {$type[0]}\n";
    } else {
        echo "  ✗ Failed to insert {$type[0]}: " . $stmt->error . "\n";
    }
    $stmt->close();
}

// 2. Insert Names
echo "\n[2/5] Inserting Names...\n";
$names = [
    ['John', null, null, 'Doe', null],
    ['Jane', null, null, 'Smith', null],
    ['Michael', null, null, 'Johnson', null]
];

$nameIds = [];
foreach ($names as $name) {
    $stmt = $conn->prepare("INSERT INTO Name (name_first, name_second, name_middle, name_last, name_suffix) VALUES (?, ?, ?, ?, ?)");
    $stmt->bind_param("sssss", $name[0], $name[1], $name[2], $name[3], $name[4]);
    if ($stmt->execute()) {
        $nameIds[] = $conn->insert_id;
        echo "  ✓ Inserted: {$name[0]} {$name[3]} (ID: {$conn->insert_id})\n";
    } else {
        echo "  ✗ Failed to insert name: " . $stmt->error . "\n";
    }
    $stmt->close();
}

// 3. Insert Persons (Users)
echo "\n[3/5] Inserting Persons (Users)...\n";
$persons = [
    ['admin@example.com', 'password123', 1, $nameIds[0] ?? 1],
    ['teacher@example.com', 'password123', 2, $nameIds[1] ?? 2],
    ['student@example.com', 'password123', 3, $nameIds[2] ?? 3]
];

foreach ($persons as $person) {
    $stmt = $conn->prepare("INSERT INTO Person (person_username, person_password, account_ID, name_ID) VALUES (?, ?, ?, ?)");
    $stmt->bind_param("ssii", $person[0], $person[1], $person[2], $person[3]);
    if ($stmt->execute()) {
        echo "  ✓ Inserted: {$person[0]} (Account ID: {$person[2]})\n";
    } else {
        echo "  ✗ Failed to insert person: " . $stmt->error . "\n";
    }
    $stmt->close();
}

// 4. Insert Days
echo "\n[4/5] Inserting Days...\n";
$days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
foreach ($days as $day) {
    $stmt = $conn->prepare("INSERT INTO Day (day_name) VALUES (?)");
    $stmt->bind_param("s", $day);
    if ($stmt->execute()) {
        echo "  ✓ Inserted: $day\n";
    } else {
        echo "  ✗ Failed to insert day: " . $stmt->error . "\n";
    }
    $stmt->close();
}

// 5. Insert Time Slots
echo "\n[5/5] Inserting Time Slots...\n";
$times = [
    ['08:00:00', '09:00:00'],
    ['09:00:00', '10:00:00'],
    ['10:00:00', '11:00:00'],
    ['11:00:00', '12:00:00'],
    ['13:00:00', '14:00:00'],
    ['14:00:00', '15:00:00'],
    ['15:00:00', '16:00:00'],
    ['16:00:00', '17:00:00']
];

foreach ($times as $time) {
    $stmt = $conn->prepare("INSERT INTO Time (time_start, time_end) VALUES (?, ?)");
    $stmt->bind_param("ss", $time[0], $time[1]);
    if ($stmt->execute()) {
        echo "  ✓ Inserted: {$time[0]} - {$time[1]}\n";
    } else {
        echo "  ✗ Failed to insert time: " . $stmt->error . "\n";
    }
    $stmt->close();
}

echo "\n=== Test Data Insertion Complete ===\n";
echo "\nYou can now login with:\n";
echo "  Email: admin@example.com\n";
echo "  Password: password123\n";

$conn->close();
?>
