-- Выполнить один раз в phpMyAdmin на УЖЕ СУЩЕСТВУЮЩЕЙ БД.
-- Таблицы студентов, пользователей, записей и оценки не удаляются.
USE facultatives;

ALTER TABLE electives
    ADD COLUMN name VARCHAR(160) NULL AFTER id;

-- Заполняем названия всех существующих факультативов,
-- включая факультативы, созданные позднее тестовых записей.
UPDATE electives e
JOIN subjects s ON s.id = e.subject_id
SET e.name = CASE
    WHEN s.name = 'Основы баз данных' THEN 'Практикум по базам данных'
    WHEN s.name = 'Введение в Python' THEN 'Программирование на Python'
    WHEN s.name = 'Дискретная математика' THEN 'Задачи по дискретной математике'
    ELSE CONCAT('Факультатив по предмету ', s.name)
END;

ALTER TABLE electives
    MODIFY COLUMN name VARCHAR(160) NOT NULL;

SELECT e.id, e.name AS 'Факультатив',
       s.name AS 'Предмет', d.name AS 'Кафедра'
FROM electives e
JOIN subjects s ON s.id=e.subject_id
JOIN departments d ON d.id=e.department_id
ORDER BY e.id;
