INSERT INTO Time (time_slot) VALUES
('07:00:00'),
('07:30:00'),
('08:00:00'),
('08:30:00'),
('09:00:00'),
('09:30:00'),
('10:00:00'),
('10:30:00'),
('11:00:00'),
('11:30:00'),
('12:00:00'),
('12:30:00'),
('13:00:00'),
('13:30:00'),
('14:00:00'),
('14:30:00'),
('15:00:00'),
('15:30:00'),
('16:00:00'),
('16:30:00'),
('17:00:00'),
('17:30:00'),
('18:00:00'),
('18:30:00');

-- Insert days
INSERT INTO `day` (`day_ID`, `day_name`) VALUES
(1, 'Monday'),
(2, 'Tuesday'),
(3, 'Wednesday'),
(4, 'Thursday'),
(5, 'Friday'),
(6, 'Saturday'),
(7, 'Sunday');


INSERT INTO `account_type` (`account_ID`, `account_name`) VALUES
(1, 'Admin'),
(2, 'Teacher');

-- Insert rooms
INSERT INTO `room` ( `room_name`, `room_capacity`) VALUES
('Lab 1', 30),
('Lab 2', 30),
('LR 1', 40),
('LR 2', 40),
('LR 3', 25),
('LR 4', 40),
('LR 5', 25);

INSERT INTO Subject (subject_code, subject_name) VALUES
('ACT 115', 'ACT 115'),
('ACT 116', 'ACT 116'),
('ACT 213', 'ACT 213'),
('ACT 214', 'ACT 214'),
('ACT 215', 'ACT 215'),
('ADS 133', 'ADS 133'),
('ATFL 135', 'ATFL 135'),
('CALC 139', 'CALC 139'),
('CC 100', 'CC 100'),
('CC 101', 'CC 101'),
('CC 103', 'CC 103'),
('CC 104', 'CC 104'),
('CC 105', 'CC 105'),
('CDI 108', 'CDI 108'),
('CSE 131', 'CSE 131'),
('CSE 133', 'CSE 133'),
('DS 111', 'DS 111'),
('FIL 101', 'FIL 101'),
('IT 143', 'IT 143'),
('IT 145', 'IT 145'),
('IT 211', 'IT 211'),
('IT 212', 'IT 212'),
('IT 213', 'IT 213'),
('IT 311', 'IT 311'),
('IT 312', 'IT 312'),
('IT 313', 'IT 313'),
('IT 314', 'IT 314'),
('MAD 121', 'MAD 121'),
('NC 127', 'NC 127'),
('OS 137', 'OS 137'),
('SE 131', 'SE 131'),
('SIPP 125', 'SIPP 125'),
('WD 123', 'WD 123');

INSERT INTO Section (section_name, section_year) VALUES
('AD 1A', 1),
('AD 1B', 1),
('AD 2', 2),

('CRIM 4A', 4),
('CRIM 4B', 4),
('CRIM 4C', 4),
('CRIM 4D', 4),

('CS 1A', 1),
('CS 1B', 1),
('CS 2', 2),
('CS 3A', 3),
('CS 3B', 3),
('CS 3C', 3),

('IT 1A', 1),
('IT 1B', 1),
('IT 2', 2),
('IT 3A', 3),
('IT 3B', 3),
('IT 3C', 3),
('IT 4A', 4),
('IT 4B', 4),
('IT 4C', 4),

('NW 1A', 1),
('NW 1B', 1),
('NW 2', 2);

-- Insert names
INSERT INTO `name` (`name_ID`, `name_first`, `name_second`, `name_middle`, `name_last`, `name_suffix`) VALUES
(1, 'John', NULL, 'Michael', 'Doe', NULL),
(2, 'Jane', NULL, 'Marie', 'Smith', NULL),
(3, 'Robert', NULL, 'James', 'Johnson', NULL),
(4, NULL, NULL, NULL, 'ABIRIN', NULL),
(5, 'A', NULL, NULL, 'ARIP', NULL),
(6, 'JA', NULL, NULL, 'ARIP', NULL),
(7, 'JP', NULL, NULL, 'ARIP', NULL),
(8, NULL, NULL, NULL, 'BALAN', NULL),
(9, NULL, NULL, NULL, 'BALLAHO', NULL),
(10, NULL, NULL, NULL, 'BANQUERIGO', NULL),
(11, NULL, NULL, NULL, 'BELAMIDE', NULL),
(12, NULL, NULL, NULL, 'CATADMAN', NULL),
(13, 'A', NULL, NULL, 'ESCORIAL', NULL),
(14, 'J', NULL, NULL, 'ESCORIAL', NULL),
(15, NULL, NULL, NULL, 'FELIX', NULL),
(16, NULL, NULL, NULL, 'FLORES', NULL),
(17, NULL, NULL, NULL, 'JAAFAR', NULL),
(18, NULL, NULL, NULL, 'JAILANI', NULL),
(19, NULL, NULL, NULL, 'LINES', NULL),
(20, 'JEZ', NULL, NULL, 'LORENZO', NULL),
(21, 'JEN', NULL, NULL, 'LORENZO', NULL),
(22, NULL, NULL, NULL, 'MARAVILLAS', NULL),
(23, NULL, NULL, NULL, 'ROJAS', NULL),
(24, NULL, NULL, NULL, 'SAAVEDRA', NULL),
(25, NULL, NULL, NULL, 'TAHIL', NULL),
(26, NULL, NULL, NULL, 'TIMPANGCO', NULL),
(27, NULL, NULL, NULL, 'ZABALA', NULL);


INSERT INTO `person` (`person_username`, `person_password`, `account_ID`, `name_ID`) VALUES
('admin@example.com',       'password123', 1,  1),
('teacher@example.com',     'password123', 2,  2),
('abirin@example.com',      'password123', 2,  4),
('arip.a@example.com',      'password123', 2,  5),
('arip.ja@example.com',     'password123', 2,  6),
('arip.jp@example.com',     'password123', 2,  7),
('balan@example.com',       'password123', 2,  8),
('ballaho@example.com',     'password123', 2,  9),
('banquerigo@example.com',  'password123', 2, 10),
('belamide@example.com',    'password123', 2, 11),
('catadman@example.com',    'password123', 2, 12),
('escorial.a@example.com',  'password123', 2, 13),
('escorial.j@example.com',  'password123', 2, 14),
('felix@example.com',       'password123', 2, 15),
('flores@example.com',      'password123', 2, 16),
('jaafar@example.com',      'password123', 2, 17),
('jailani@example.com',     'password123', 2, 18),
('lines@example.com',       'password123', 2, 19),
('lorenzo.jez@example.com', 'password123', 2, 20),
('lorenzo.jen@example.com', 'password123', 2, 21),
('maravillas@example.com',  'password123', 2, 22),
('rojas@example.com',       'password123', 2, 23),
('saavedra@example.com',    'password123', 2, 24),
('tahil@example.com',       'password123', 2, 25),
('timpangco@example.com',   'password123', 2, 26),
('zabala@example.com',      'password123', 2, 27);

-- FULL SCHEDULE INSERT (ALL ROOMS) - READY TO RUN
INSERT INTO Schedule (
    day_ID,
    subject_ID,
    section_ID,
    teacher_ID,
    time_start_ID,   -- 07:00 = 1, 07:30 = 2, 08:00 = 3, 08:30 = 4, etc.
    time_end_ID,     -- class ends at this slot (exclusive)
    room_ID,
    schedule_status
) VALUES

-- MONDAY (day_ID = 1)
(1, (SELECT subject_ID FROM Subject WHERE subject_code='CC 104'), (SELECT section_ID FROM Section WHERE section_name='CS 2'), (SELECT person_ID FROM Person WHERE person_username='felix@example.com'),        1,  4, 1, 'Occupied'),  -- 7:00–8:30  → Lab 1
(1, (SELECT subject_ID FROM Subject WHERE subject_code='CC 103'), (SELECT section_ID FROM Section WHERE section_name='AD 2'), (SELECT person_ID FROM Person WHERE person_username='jaafar@example.com'),      4,  7, 1, 'Occupied'),  -- 8:30–10:00 → Lab 1
(1, (SELECT subject_ID FROM Subject WHERE subject_code='CC 104'), (SELECT section_ID FROM Section WHERE section_name='IT 2'), (SELECT person_ID FROM Person WHERE person_username='felix@example.com'),       10, 13, 1, 'Occupied'), -- 11:30–1:00 → Lab 1

-- TUESDAY (day_ID = 2)
(2, (SELECT subject_ID FROM Subject WHERE subject_code='CC 104'), (SELECT section_ID FROM Section WHERE section_name='AD 2'), (SELECT person_ID FROM Person WHERE person_username='felix@example.com'),        1,  4, 1, 'Occupied'),  -- 7:00–8:30  → Lab 1
(2, (SELECT subject_ID FROM Subject WHERE subject_code='NC 127'), (SELECT section_ID FROM Section WHERE section_name='AD 2'), (SELECT person_ID FROM Person WHERE person_username='flores@example.com'),       4,  7, 1, 'Occupied'),  -- 8:30–10:00 → Lab 1
(2, (SELECT subject_ID FROM Subject WHERE subject_code='CC 104'), (SELECT section_ID FROM Section WHERE section_name='NW 2'), (SELECT person_ID FROM Person WHERE person_username='felix@example.com'),       10, 13, 1, 'Occupied'), -- 11:30–1:00 → Lab 1

-- WEDNESDAY (day_ID = 3)
(3, (SELECT subject_ID FROM Subject WHERE subject_code='CC 100'), (SELECT section_ID FROM Section WHERE section_name='AD 1A'), (SELECT person_ID FROM Person WHERE person_username='balan@example.com'),     1,  4, 2, 'Occupied'),  -- 7:00–8:30  → Lab 2 (or change room_ID)
(3, (SELECT subject_ID FROM Subject WHERE subject_code='IT 311'), (SELECT section_ID FROM Section WHERE section_name='IT 3B'), (SELECT person_ID FROM Person WHERE person_username='arip.jp@example.com'), 4,  7, 1, 'Occupied'),  -- 8:30–10:00 → Lab 1
(3, (SELECT subject_ID FROM Subject WHERE subject_code='CSE 131'), (SELECT section_ID FROM Section WHERE section_name='CS 3C'), (SELECT person_ID FROM Person WHERE person_username='lorenzo.jez@example.com'),10, 13, 2, 'Occupied'), -- 11:30–1:00 → Lab 2

-- THURSDAY (day_ID = 4)
(4, (SELECT subject_ID FROM Subject WHERE subject_code='CC 101'), (SELECT section_ID FROM Section WHERE section_name='IT 1B'), (SELECT person_ID FROM Person WHERE person_username='banquerigo@example.com'),1,  4, 2, 'Occupied'),  -- 7:00–8:30
(4, (SELECT subject_ID FROM Subject WHERE subject_code='WD 123'), (SELECT section_ID FROM Section WHERE section_name='AD 2'), (SELECT person_ID FROM Person WHERE person_username='ballaho@example.com'),     4,  7, 1, 'Occupied'),  -- 8:30–10:00 → Lab 1
(4, (SELECT subject_ID FROM Subject WHERE subject_code='IT 211'), (SELECT section_ID FROM Section WHERE section_name='IT 2'),   (SELECT person_ID FROM Person WHERE person_username='ballaho@example.com'),    10, 13, 1, 'Occupied'), -- 11:30–1:00 → Lab 1
(4, (SELECT subject_ID FROM Subject WHERE subject_code='MAD 121'), (SELECT section_ID FROM Section WHERE section_name='AD 2'), (SELECT person_ID FROM Person WHERE person_username='banquerigo@example.com'),  13, 16, 2, 'Occupied'), -- 1:00–2:30

-- FRIDAY (day_ID = 5)
(5, (SELECT subject_ID FROM Subject WHERE subject_code='NC 127'), (SELECT section_ID FROM Section WHERE section_name='CS 2'), (SELECT person_ID FROM Person WHERE person_username='flores@example.com'),       1,  4, 1, 'Occupied'),  -- 7:00–8:30  → Lab 1
(5, (SELECT subject_ID FROM Subject WHERE subject_code='WD 123'), (SELECT section_ID FROM Section WHERE section_name='CS 2'), (SELECT person_ID FROM Person WHERE person_username='ballaho@example.com'),         4,  7, 1, 'Occupied'),  -- 8:30–10:00 → Lab 1
(5, (SELECT subject_ID FROM Subject WHERE subject_code='CC 101'), (SELECT section_ID FROM Section WHERE section_name='AD 1A'), (SELECT person_ID FROM Person WHERE person_username='maravillas@example.com'),10, 13, 3, 'Occupied'), -- 11:30–1:00
(5, (SELECT subject_ID FROM Subject WHERE subject_code='CC 103'), (SELECT section_ID FROM Section WHERE section_name='NW 2'), (SELECT person_ID FROM Person WHERE person_username='jailani@example.com'),    13, 16, 2, 'Occupied'), -- 1:00–2:30
(5, (SELECT subject_ID FROM Subject WHERE subject_code='ACT 214'), (SELECT section_ID FROM Section WHERE section_name='NW 2'), (SELECT person_ID FROM Person WHERE person_username='arip.jp@example.com'),  16, 19, 1, 'Occupied'), -- 2:30–4:00 → Lab 1
(5, (SELECT subject_ID FROM Subject WHERE subject_code='CC 101'), (SELECT section_ID FROM Section WHERE section_name='IT 1A'), (SELECT person_ID FROM Person WHERE person_username='banquerigo@example.com'),16, 19, 4, 'Occupied'), -- 4:00–5:30
(5, (SELECT subject_ID FROM Subject WHERE subject_code='CC 101'), (SELECT section_ID FROM Section WHERE section_name='NW 1B'), (SELECT person_ID FROM Person WHERE person_username='banquerigo@example.com'),16, 19, 5, 'Occupied'),
(5, (SELECT subject_ID FROM Subject WHERE subject_code='CC 101'), (SELECT section_ID FROM Section WHERE section_name='NW 1A'), (SELECT person_ID FROM Person WHERE person_username='banquerigo@example.com'),16, 19, 6, 'Occupied'),
(5, (SELECT subject_ID FROM Subject WHERE subject_code='CC 101'), (SELECT section_ID FROM Section WHERE section_name='AD 1B'), (SELECT person_ID FROM Person WHERE person_username='jailani@example.com'),     16, 19, 7, 'Occupied'),
(5, (SELECT subject_ID FROM Subject WHERE subject_code='CC 103'), (SELECT section_ID FROM Section WHERE section_name='CS 2'),   (SELECT person_ID FROM Person WHERE person_username='jaafar@example.com'),      16, 19, 2, 'Occupied'),
(5, (SELECT subject_ID FROM Subject WHERE subject_code='MAD 121'), (SELECT section_ID FROM Section WHERE section_name='CS 2'),   (SELECT person_ID FROM Person WHERE person_username='banquerigo@example.com'),    19, 22, 1, 'Occupied'); -- 5:30–7:00 → Lab 1

-- Lab 2

