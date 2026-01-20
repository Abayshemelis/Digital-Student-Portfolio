package com.amazi.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Student {

    private final String id;          // Unique ID for each student
    private String fullName;          
    private String email;             
    private String department;       
    private String password;          // Password for login (hashed in real app)
    private List<Submission> submissions; // List of portfolio/assignment submissions
    private boolean active;           // Whether the student account is active

    // Constructor
    public Student(String fullName, String email, String department, String password) {
        this.id = UUID.randomUUID().toString(); // Auto-generate unique ID
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.password = password;
        this.submissions = new ArrayList<>();  // Initialize empty submission list
        this.active = true;                     // Default account active
    }

    // --- GETTERS ---
    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getDepartment() { return department;
    public String getPassword() { return password; }
    public List<Submission> getSubmissions() { return submissions; }
    public boolean isActive() { return active; }

    // --- SETTERS ---
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setDepartment(String department) { this.department = department; }
    public void setPassword(String password) { this.password = password; }
    public void setActive(boolean active) { this.active = active; }

    // --- METHODS TO MANAGE SUBMISSIONS ---
    public void addSubmission(Submission submission) {
        this.submissions.add(submission);
    }

    public void removeSubmission(Submission submission) {
        this.submissions.remove(submission);
    }

    public Submission getSubmissionById(String submissionId) {
        for (Submission sub : submissions) {
            if (sub.getId().equals(submissionId)) {
                return sub;
            }
        }
        return null; // Not found
    }
}
