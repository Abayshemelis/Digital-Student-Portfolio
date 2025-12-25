package com.amazi.model;

public class User {
    private final int userID; // ID remains final as it shouldn't change
    private String name;      // Removed final to allow profile updates
    private String email;     // Removed final to allow email changes
    private String password;  // Removed final for "Forgot Password" logic
    private String role;      // Removed final for Admin to promote/demote users

    public User(int userID, String name, String email, String password, String role) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.password = password;
        // Safeguard: If no role is provided, default to Student
        this.role = (role == null || role.isEmpty()) ? "Student" : role;
    }

    // --- GETTERS (Used by TableView PropertyValueFactory) ---
    public int getUserID() { return userID; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    // --- SETTERS (Necessary for Settings and Admin Dashboard) ---
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }

    // --- HELPER METHOD ---
    @Override
    public String toString() {
        return "User{" +
                "id=" + userID +
                ", name='" + name + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}