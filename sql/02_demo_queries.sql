USE facultatives;
-- 1 Все студенты
SELECT id, surname, first_name, patronymic, address, phone FROM students ORDER BY surname;
-- 2 Все факультативы
SELECT e.id, d.name AS department, s.name AS subject
FROM electives e JOIN departments d ON d.id=e.department_id
JOIN subjects s ON s.id=e.subject_id ORDER BY e.id;
-- 3 Факультативы кафедры id=1
SELECT e.id, s.name AS subject FROM electives e
JOIN subjects s ON s.id=e.subject_id WHERE e.department_id=1;
-- 4 Студенты факультатива id=1 (за все семестры, без повторов)
SELECT DISTINCT st.id, st.surname, st.first_name FROM students st
JOIN enrollments en ON en.student_id=st.id
JOIN offerings o ON o.id=en.offering_id
WHERE o.elective_id=1 ORDER BY st.surname;
-- 5 Часы факультатива id=1 в семестре id=2
SELECT o.lecture_hours, o.practice_hours, o.lab_hours,
 (o.lecture_hours+o.practice_hours+o.lab_hours) AS total_hours
FROM offerings o WHERE o.elective_id=1 AND o.semester_id=2;
-- 6 Все выставленные оценки
SELECT st.surname, st.first_name, sb.name AS subject,
 sm.start_year, sm.term_number, en.grade
FROM enrollments en JOIN students st ON st.id=en.student_id
JOIN offerings o ON o.id=en.offering_id
JOIN semesters sm ON sm.id=o.semester_id
JOIN electives e ON e.id=o.elective_id
JOIN subjects sb ON sb.id=e.subject_id
WHERE en.grade IS NOT NULL
ORDER BY st.surname, sb.name, sm.start_year, sm.term_number;
-- 7 Последняя оценка студента id=1 по предмету id=1
SELECT en.grade, sm.start_year, sm.term_number
FROM enrollments en JOIN offerings o ON o.id=en.offering_id
JOIN semesters sm ON sm.id=o.semester_id
JOIN electives e ON e.id=o.elective_id
WHERE en.student_id=1 AND e.subject_id=1 AND en.grade IS NOT NULL
ORDER BY sm.start_year DESC, sm.term_number DESC LIMIT 1;
-- 8 Посещённые (завершённые) студентом id=1 факультативы
SELECT DISTINCT sb.name FROM subjects sb
JOIN electives e ON e.subject_id=sb.id
JOIN offerings o ON o.elective_id=e.id
JOIN enrollments en ON en.offering_id=o.id
WHERE en.student_id=1 AND en.grade IS NOT NULL;
-- 9 JOIN: какая кафедра ведёт какой факультатив и когда
SELECT d.name AS department, sb.name AS subject, sm.start_year,
 sm.term_number FROM offerings o
JOIN electives e ON e.id=o.elective_id
JOIN departments d ON d.id=e.department_id
JOIN subjects sb ON sb.id=e.subject_id
JOIN semesters sm ON sm.id=o.semester_id;
-- 10 GROUP BY: сумма часов по каждому факультативу
SELECT e.id, sb.name,
 SUM(o.lecture_hours+o.practice_hours+o.lab_hours) AS total_hours
FROM electives e JOIN subjects sb ON sb.id=e.subject_id
JOIN offerings o ON o.elective_id=e.id
GROUP BY e.id, sb.name ORDER BY e.id;
-- 11 COUNT: число студентов на каждое проведение (включая нулевые)
SELECT o.id, sb.name, COUNT(en.id) AS student_count
FROM offerings o JOIN electives e ON e.id=o.elective_id
JOIN subjects sb ON sb.id=e.subject_id
LEFT JOIN enrollments en ON en.offering_id=o.id
GROUP BY o.id, sb.name ORDER BY o.id;
-- 12 WHERE: оценки 5
SELECT id, student_id, offering_id FROM enrollments WHERE grade=5;
-- 13 INSERT: временная запись для демонстрации
INSERT INTO enrollments (student_id,offering_id,grade) VALUES (3,3,NULL);
SET @demo_enrollment_id = LAST_INSERT_ID();
-- 14 UPDATE: выставить оценку временной записи
UPDATE enrollments SET grade=5 WHERE id=@demo_enrollment_id;
-- 15 DELETE: удалить временную запись
DELETE FROM enrollments WHERE id=@demo_enrollment_id;
