USE facultatives;
-- Проверять ПО ОДНОМУ тесту: ожидаемое сообщение об ошибке означает успех контроля.
-- 1 PK: ошибка Duplicate entry
-- INSERT INTO departments (id,name) VALUES (1,'Другая кафедра');
-- 2 UNIQUE: повтор кафедры
-- INSERT INTO departments (name) VALUES ('Кафедра информатики');
-- 3 NOT NULL
-- INSERT INTO subjects(name) VALUES (NULL);
-- 4 FK: несуществующая кафедра
-- INSERT INTO electives(department_id,subject_id) VALUES (99999,1);
-- 5 FK: несуществующий студент
-- INSERT INTO enrollments(student_id,offering_id,grade) VALUES (99999,1,4);
-- 6 UNIQUE: студент уже записан на это проведение
-- INSERT INTO enrollments(student_id,offering_id,grade) VALUES (1,1,4);
-- 7 CHECK: оценка вне диапазона
-- INSERT INTO enrollments(student_id,offering_id,grade) VALUES (3,3,7);
-- 8 CHECK: сумма часов равна нулю
-- INSERT INTO offerings(elective_id,semester_id,lecture_hours,practice_hours,lab_hours)
-- VALUES (3,2,0,0,0);
-- 9 RESTRICT: нельзя удалить кафедру, если у неё есть факультатив
-- DELETE FROM departments WHERE id=1;
-- 10 Можно изменить название кафедры, связи сохраняются:
START TRANSACTION;
UPDATE departments SET name='Тестовое название' WHERE id=1;
SELECT e.id,d.name FROM electives e JOIN departments d ON d.id=e.department_id WHERE d.id=1;
ROLLBACK;
-- 11 FK не позволит перевести существующий факультатив на несуществующую кафедру:
-- UPDATE electives SET department_id=99999 WHERE id=1;
-- 12 Можно перенести факультатив на существующую кафедру:
START TRANSACTION;
UPDATE electives SET department_id=2 WHERE id=1;
SELECT e.id, d.name FROM electives e JOIN departments d ON d.id=e.department_id WHERE e.id=1;
ROLLBACK;
-- 13 Удаление зависимостей в правильном порядке (проверка с откатом):
START TRANSACTION;
DELETE FROM enrollments WHERE offering_id=1;
DELETE FROM offerings WHERE id=1;
ROLLBACK;
