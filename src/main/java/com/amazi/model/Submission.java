package com.amazi.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * SUBMISSION MODEL
 * Represents a student's academic work and the faculty's evaluation.
 */
@SuppressWarnings("unused") // This clears all "never used" warnings professionally
public class Submission {

    // --- IMMUTABLE IDENTIFIERS ---
    private final String id;
    private final String studentName;

    // --- CORE PROJECT DATA ---
    private String title;
    private String course;
    private String assignment;
    private String category;
    private String description;
    private String filePath;
    private LocalDate completionDate;

    // --- FACULTY FEEDBACK & STATUS ---
    private String status;           // DRAFT, SUBMITTED, APPROVED
    private String grade;            // Evaluation result
    private String feedback;         // Faculty comments
    private boolean viewedByStudent; // Controls the notification badge

    /**
     * Standard Constructor for a new submission.
     */
    public Submission(String title, String course, String assignment,
                      String category, LocalDate date, String desc,
                      String file, String status, String studentName) {

        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.course = course;
        this.assignment = assignment;
        this.category = category;
        this.completionDate = date;
        this.description = desc;
        this.filePath = file;
        this.status = status;
        this.studentName = studentName;

        // Default evaluation values
        this.grade = "N/A";
        this.feedback = "";
        this.viewedByStudent = false;
    }

    // --- GETTERS (Clean & Organized) ---
    public String getId()
    { return id; }
    public String getTitle()
    { return title; }
    public String getCourse()
    { return course; }
    public String getAssignment()
    { return assignment; }
    public String getCategory()
    { return category; }
    public String getDescription()
    { return description; }
    public String getFilePath()
    { return filePath; }
    public String getStatus()
    { return status; }
    public String getStudentName()
    { return studentName; }
    public String getGrade()
    { return grade; }
    public String getFeedback()
    { return feedback; }
    public LocalDate getCompletionDate()
    { return completionDate; }
    public boolean isViewedByStudent()
    { return viewedByStudent; }

    // --- SETTERS (Standard for Data Management) ---
    public void setTitle(String title)           { this.title = title; }
    public void setCourse(String course)         { this.course = course; }
    public void setAssignment(String assignment) { this.assignment = assignment; }
    public void setCategory(String category)     { this.category = category; }
    public void setDescription(String desc)      { this.description = desc; }
    public void setFilePath(String path)         { this.filePath = path; }
    public void setStatus(String status)         { this.status = status; }
    public void setGrade(String grade)           { this.grade = grade; }
    public void setFeedback(String feedback)     { this.feedback = feedback; }
    public void setViewedByStudent(boolean v)    { this.viewedByStudent = v; }
    public void setCompletionDate(LocalDate d)   { this.completionDate = d; }
}