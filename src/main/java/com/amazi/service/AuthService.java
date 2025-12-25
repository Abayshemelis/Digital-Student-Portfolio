package com.amazi.service;

import com.amazi.model.User;

public class AuthService {

    /**
     * UPDATED: Now provides all 5 parameters required by the User constructor
     * (userID, name, email, password, role)
     */
    public User authenticate(String email, String password) {

        if (email.equals("std") && password.equals("1234")) {
            // Added "1234" as the 4th parameter (password)
            return new User(1, "Student User", email, "1234", "STUDENT");
        }

        if (email.equals("admin") && password.equals("admin")) {
            // Added "admin" as the 4th parameter (password)
            return new User(2, "Admin User", email, "admin", "ADMIN");
        }

        return null; // invalid credentials
    }
}