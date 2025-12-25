package com.amazi.service;

import com.amazi.model.User;

public class AuthService {
    public User authenticate(String identifier, String password) {
        // Matches the 6-argument constructor
        if (identifier.equals("std") && password.equals("1234")) {
            return new User(1, "Student User", "std", "student@amazi.com", "1234", "Student");
        }
        if (identifier.equals("admin") && password.equals("admin")) {
            return new User(2, "Admin User", "admin", "admin@amazi.com", "admin", "Admin");
        }
        return null;
    }
}