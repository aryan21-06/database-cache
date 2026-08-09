-- Database Query Cache System - Schema
-- Run this script to create the database schema

CREATE DATABASE IF NOT EXISTS dbcache;
USE dbcache;

-- Departments table
CREATE TABLE departments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    head VARCHAR(100)
);

-- Students table (~1000 rows)
CREATE TABLE students (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    department_id INT,
    year INT,
    gpa DECIMAL(3,2),
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- Courses table (~100 rows)
CREATE TABLE courses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    credits INT,
    department_id INT,
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- Enrollments table (~2000 rows)
CREATE TABLE enrollments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT,
    course_id INT,
    semester VARCHAR(20),
    grade VARCHAR(2),
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (course_id) REFERENCES courses(id)
);
