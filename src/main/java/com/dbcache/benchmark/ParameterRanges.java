package com.dbcache.benchmark;

public class ParameterRanges {
    
    // Student parameters
    public static final int STUDENT_ID_MIN = 1;
    public static final int STUDENT_ID_MAX = 1000;
    public static final int DEPARTMENT_ID_MIN = 1;
    public static final int DEPARTMENT_ID_MAX = 5;
    public static final int YEAR_MIN = 1;
    public static final int YEAR_MAX = 4;
    public static final double GPA_MIN = 0.0;
    public static final double GPA_MAX = 4.0;
    
    // Course parameters
    public static final int COURSE_ID_MIN = 1;
    public static final int COURSE_ID_MAX = 100;
    public static final int CREDITS_MIN = 1;
    public static final int CREDITS_MAX = 6;
    
    // Semester values
    public static final String[] SEMESTERS = {
        "Fall 2023", "Spring 2024", "Fall 2024", "Spring 2025"
    };
    
    // Grade values
    public static final String[] GRADES = {
        "A", "B", "C", "D", "F"
    };
    
    // Department names
    public static final String[] DEPARTMENT_NAMES = {
        "Computer Science", "Mathematics", "Physics", "Chemistry", "Biology"
    };

    // Department heads
    public static final String[] DEPARTMENT_HEADS = {
        "Dr. Smith", "Dr. Johnson", "Dr. Williams", "Dr. Brown", "Dr. Jones"
    };
}
