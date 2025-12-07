-- ===============================
-- ACCOUNT TYPE
-- ===============================
CREATE TABLE Account_Type (
    account_ID INT(11) AUTO_INCREMENT PRIMARY KEY,
    account_name VARCHAR(50) NOT NULL
) ENGINE=InnoDB;

-- ===============================
-- DAY
-- ===============================
CREATE TABLE Day (
    day_ID INT(11) AUTO_INCREMENT PRIMARY KEY,
    day_name VARCHAR(15) NOT NULL
) ENGINE=InnoDB;

-- ===============================
-- NAME
-- ===============================
CREATE TABLE Name (
    name_ID INT(11) AUTO_INCREMENT PRIMARY KEY,
    name_first VARCHAR(50) NOT NULL,
    name_second VARCHAR(50) DEFAULT NULL,
    name_middle VARCHAR(50) DEFAULT NULL,
    name_last VARCHAR(50) NOT NULL,
    name_suffix VARCHAR(10) DEFAULT NULL
) ENGINE=InnoDB;

-- ===============================
-- PERSON
-- ===============================
CREATE TABLE Person (
    person_ID INT(11) AUTO_INCREMENT PRIMARY KEY,
    person_username VARCHAR(50) NOT NULL,
    person_password VARCHAR(255) NOT NULL,
    account_ID INT(11) NOT NULL,
    name_ID INT(11) NOT NULL,

    FOREIGN KEY (account_ID) REFERENCES Account_Type(account_ID),
    FOREIGN KEY (name_ID) REFERENCES Name(name_ID)
) ENGINE=InnoDB;


-- ===============================
-- ROOM
-- ===============================
CREATE TABLE Room (
    room_ID INT(11) AUTO_INCREMENT PRIMARY KEY,
    room_name VARCHAR(50) NOT NULL,
    room_capacity INT(11) DEFAULT NULL
) ENGINE=InnoDB;

-- ===============================
-- SECTION
-- ===============================
CREATE TABLE Section (
    section_ID INT(11) AUTO_INCREMENT PRIMARY KEY,
    section_name VARCHAR(50) NOT NULL,
    section_year INT(11) NOT NULL
) ENGINE=InnoDB;

-- ===============================
-- SUBJECT
-- ===============================
CREATE TABLE Subject (
    subject_ID INT(11) AUTO_INCREMENT PRIMARY KEY,
    subject_code VARCHAR(20) NOT NULL,
    subject_name VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

-- ===============================
-- TIME
-- ===============================
CREATE TABLE Time (
    time_ID      INT AUTO_INCREMENT PRIMARY KEY,
    time_slot    TIME NOT NULL UNIQUE,                    -- stores 07:00:00, 07:30:00, etc.
    display_name VARCHAR(12) AS (TIME_FORMAT(time_slot, '%l:%i %p')) STORED
        -- Example: 07:00:00 → "7:00 AM", 13:30:00 → "1:30 PM"
) ENGINE=InnoDB;

-- ===============================
-- SCHEDULE
-- ===============================
CREATE TABLE Schedule (
    schedule_ID     INT(11) AUTO_INCREMENT PRIMARY KEY,
    day_ID          INT(11) NOT NULL,
    subject_ID      INT(11) NOT NULL,
    section_ID      INT(11) NOT NULL,
    teacher_ID      INT(11) NOT NULL,
    
    time_start_ID   INT(11) NOT NULL,   -- e.g. 07:00-07:30 → time_ID = 1
    time_end_ID     INT(11) NOT NULL,   -- e.g.08:30-09:00 → time_ID = 6 (class ends at 08:30)

    room_ID         INT(11) NOT NULL,
    schedule_status ENUM('Completed','Suspended','Cancelled','Pending','On-Going','isDeleted','Occupied') 
                    NOT NULL DEFAULT 'Pending',

    FOREIGN KEY (day_ID)     REFERENCES Day(day_ID),
    FOREIGN KEY (subject_ID) REFERENCES Subject(subject_ID),
    FOREIGN KEY (section_ID) REFERENCES Section(section_ID),
    FOREIGN KEY (teacher_ID) REFERENCES Person(person_ID),
    FOREIGN KEY (time_start_ID) REFERENCES Time(time_ID),
    FOREIGN KEY (time_end_ID)   REFERENCES Time(time_ID),
    FOREIGN KEY (room_ID)    REFERENCES Room(room_ID),
    
    CHECK (time_start_ID <= time_end_ID)
) ENGINE=InnoDB;

CREATE TABLE Notification (
    notif_ID INT AUTO_INCREMENT PRIMARY KEY,
    person_ID INT NOT NULL,
    schedule_ID INT NOT NULL,
    notif_description ENUM('Schedule Created', 'Schedule Updated', 'Schedule Cancelled', 'Schedule Suspended', 'Schedule Completed') NOT NULL,
    notif_message VARCHAR(255) NOT NULL,
    notif_status ENUM('Pending', 'Sent', 'Accepted', 'Rejected') NOT NULL DEFAULT 'Pending',
    is_read TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (person_ID) REFERENCES Person(person_ID),
    FOREIGN KEY (schedule_ID) REFERENCES Schedule(schedule_ID)
) ENGINE=InnoDB;

