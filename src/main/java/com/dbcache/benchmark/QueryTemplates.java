package com.dbcache.benchmark;

public class QueryTemplates {
    
    // Students table queries
    public static final String[] STUDENT_TEMPLATES = {
        "SELECT * FROM students WHERE id = ?",
        "SELECT * FROM students WHERE department_id = ?",
        "SELECT * FROM students WHERE year = ?",
        "SELECT * FROM students WHERE gpa > ?",
        "SELECT name, email FROM students WHERE department_id = ? AND year = ?",
        "SELECT COUNT(*) FROM students WHERE department_id = ?",
        "SELECT AVG(gpa) FROM students WHERE department_id = ?",
        "SELECT * FROM students WHERE year = ? AND gpa > ?",
        "SELECT * FROM students WHERE department_id = ? AND gpa > ?",
        "SELECT COUNT(*) FROM students WHERE year = ?",
        "SELECT id, name FROM students WHERE year = ?",
    };
    
    // Courses table queries
    public static final String[] COURSE_TEMPLATES = {
        "SELECT * FROM courses WHERE id = ?",
        "SELECT * FROM courses WHERE credits > ?",
        "SELECT * FROM courses WHERE department_id = ?",
        "SELECT title, credits FROM courses WHERE credits >= ?",
        "SELECT COUNT(*) FROM courses WHERE department_id = ?",
        "SELECT * FROM courses WHERE department_id = ? AND credits > ?",
        "SELECT AVG(credits) FROM courses WHERE department_id = ?",
        "SELECT id, title FROM courses WHERE credits = ?",
    };
    
    // Enrollments table queries
    public static final String[] ENROLLMENT_TEMPLATES = {
        "SELECT * FROM enrollments WHERE student_id = ?",
        "SELECT * FROM enrollments WHERE course_id = ?",
        "SELECT * FROM enrollments WHERE semester = ?",
        "SELECT COUNT(*) FROM enrollments WHERE student_id = ?",
        "SELECT * FROM enrollments WHERE grade = ?",
        "SELECT * FROM enrollments WHERE student_id = ? AND semester = ?",
        "SELECT * FROM enrollments WHERE semester = ? AND grade = ?",
        "SELECT COUNT(*) FROM enrollments WHERE course_id = ? AND grade = ?",
    };
    
    // Departments table queries
    public static final String[] DEPARTMENT_TEMPLATES = {
        "SELECT * FROM departments WHERE id = ?",
        "SELECT * FROM departments WHERE name = ?",
        "SELECT * FROM departments WHERE head = ?",
    };
    
    // Join queries
    public static final String[] JOIN_TEMPLATES = {
        "SELECT s.name, c.title FROM students s JOIN enrollments e ON s.id = e.student_id JOIN courses c ON e.course_id = c.id WHERE s.department_id = ?",
        "SELECT s.name, e.grade FROM students s JOIN enrollments e ON s.id = e.student_id WHERE e.course_id = ?",
        "SELECT c.title, COUNT(e.id) FROM courses c JOIN enrollments e ON c.id = e.course_id WHERE c.department_id = ? GROUP BY c.title",
        "SELECT s.name, e.grade FROM students s JOIN enrollments e ON s.id = e.student_id JOIN courses c ON e.course_id = c.id WHERE e.semester = ? AND s.year = ?",
        "SELECT s.department_id, COUNT(*) FROM students s JOIN enrollments e ON s.id = e.student_id WHERE e.semester = ? GROUP BY s.department_id",
    };
}
