-- Вариант 9, MySQL 8.0.16+. Запустить один раз на пустой базе.
CREATE DATABASE IF NOT EXISTS facultatives CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE facultatives;

CREATE TABLE departments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  CONSTRAINT uq_departments_name UNIQUE (name)
) ENGINE=InnoDB;

CREATE TABLE subjects (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  CONSTRAINT uq_subjects_name UNIQUE (name)
) ENGINE=InnoDB;

CREATE TABLE students (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  surname VARCHAR(80) NOT NULL,
  first_name VARCHAR(80) NOT NULL,
  patronymic VARCHAR(80) NOT NULL,
  address VARCHAR(255) NOT NULL,
  phone VARCHAR(30) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE electives (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  department_id BIGINT NOT NULL,
  subject_id BIGINT NOT NULL,
  CONSTRAINT uq_electives_department_subject UNIQUE (department_id, subject_id),
  CONSTRAINT fk_electives_department FOREIGN KEY (department_id) REFERENCES departments (id)
      ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_electives_subject FOREIGN KEY (subject_id) REFERENCES subjects (id)
      ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE semesters (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  start_year INT NOT NULL,
  term_number INT NOT NULL,
  CONSTRAINT uq_semesters_year_term UNIQUE (start_year, term_number),
  CONSTRAINT chk_semester_term CHECK (term_number IN (1,2)),
  CONSTRAINT chk_semester_year CHECK (start_year BETWEEN 2000 AND 2100)
) ENGINE=InnoDB;

CREATE TABLE offerings (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  elective_id BIGINT NOT NULL,
  semester_id BIGINT NOT NULL,
  lecture_hours INT NOT NULL,
  practice_hours INT NOT NULL,
  lab_hours INT NOT NULL,
  CONSTRAINT uq_offerings_elective_semester UNIQUE (elective_id, semester_id),
  CONSTRAINT chk_offering_positive CHECK (lecture_hours >= 0 AND practice_hours >= 0 AND lab_hours >= 0
      AND lecture_hours <= 65535 AND practice_hours <= 65535 AND lab_hours <= 65535
      AND lecture_hours + practice_hours + lab_hours > 0),
  CONSTRAINT fk_offerings_elective FOREIGN KEY (elective_id) REFERENCES electives(id)
      ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_offerings_semester FOREIGN KEY (semester_id) REFERENCES semesters(id)
      ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE enrollments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  student_id BIGINT NOT NULL,
  offering_id BIGINT NOT NULL,
  grade INT NULL,
  CONSTRAINT uq_enrollments_student_offering UNIQUE (student_id, offering_id),
  CONSTRAINT chk_enrollment_grade CHECK (grade IS NULL OR grade BETWEEN 2 AND 5),
  CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id) REFERENCES students(id)
      ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT fk_enrollments_offering FOREIGN KEY (offering_id) REFERENCES offerings(id)
      ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  login VARCHAR(80) NOT NULL,
  password_hash VARCHAR(60) NOT NULL,
  role ENUM('ADMIN','STUDENT') NOT NULL,
  student_id BIGINT NULL,
  CONSTRAINT uq_users_login UNIQUE (login),
  CONSTRAINT uq_users_student UNIQUE (student_id),
  CONSTRAINT chk_users_role_student CHECK (
     (role='ADMIN' AND student_id IS NULL) OR
     (role='STUDENT' AND student_id IS NOT NULL)),
  CONSTRAINT fk_users_student FOREIGN KEY (student_id) REFERENCES students(id)
      ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

-- Примеры данных. Учётная запись администратора создаётся приложением из
-- переменных ADMIN_LOGIN / ADMIN_PASSWORD с BCrypt, НЕ простым SQL-паролем.
INSERT INTO departments (id,name) VALUES
 (1,'Кафедра информатики'), (2,'Кафедра математики');
INSERT INTO subjects (id,name) VALUES
 (1,'Основы баз данных'), (2,'Введение в Python'), (3,'Дискретная математика');
INSERT INTO students (id,surname,first_name,patronymic,address,phone) VALUES
 (1,'Иванов','Павел','Сергеевич','г. Казань, ул. Лесная, 5','+7 900 100-00-01'),
 (2,'Петрова','Анна','Олеговна','г. Казань, ул. Мира, 8','+7 900 100-00-02'),
 (3,'Сидоров','Илья','Андреевич','г. Казань, ул. Садовая, 11','+7 900 100-00-03');
INSERT INTO electives (id,department_id,subject_id) VALUES
 (1,1,1),(2,1,2),(3,2,3);
INSERT INTO semesters (id,start_year,term_number) VALUES
 (1,2024,2),(2,2025,1),(3,2025,2);
INSERT INTO offerings (id,elective_id,semester_id,lecture_hours,practice_hours,lab_hours) VALUES
 (1,1,1,18,18,0), (2,1,2,12,12,12),
 (3,2,2,10,18,8), (4,2,3,12,12,12), (5,3,3,12,24,0);
INSERT INTO enrollments (id,student_id,offering_id,grade) VALUES
 (1,1,1,4), (2,1,2,5), (3,1,3,4),
 (4,2,1,5), (5,2,4,NULL), (6,3,5,NULL);
