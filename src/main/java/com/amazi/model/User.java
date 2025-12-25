package com.amazi.model;

public class User {
    private final int userID;
    private String name;
    private String username;
    private String email;
    private String password;
    private String role;

    public User(int userID, String name, String username, String email, String password, String role) {
        this.userID = userID;
        this.name = name;
        this.username = (username == null || username.isEmpty()) ? "user" + userID : username;
        this.email = email;
        this.password = password;
        this.role = (role == null || role.isEmpty()) ? "Student" : role;
    }

    // GETTERS
    public int getUserID() { return userID; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    // SETTERS
    public void setName(String name) { this.name = name; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "User{id=" + userID + ", username='" + username + "', role='" + role + "'}";
    }
}