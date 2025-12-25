package com.amazi.service;

import com.amazi.model.User;
import com.amazi.model.Submission;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataManager {
    private static final Logger LOGGER = Logger.getLogger(DataManager.class.getName());

    private static final List<User> users = new ArrayList<>();
    private static final ObservableList<Submission> allSubmissions = FXCollections.observableArrayList();
    private static final String SUBMISSION_FILE = "submissions.txt";
    private static final String USERS_FILE = "users.txt";
    private static int nextId = 1;

    static {
        loadUsersFromFile();

        // Ensure default users exist with 6 arguments (ID, Name, Username, Email, Password, Role)
        if (users.isEmpty()) {
            users.add(new User(nextId++, "Admin", "admin", "admin@amazi.com", "admin123", "Admin"));
            users.add(new User(nextId++, "Faculty User", "faculty", "faculty@amazi.com", "faculty123", "Faculty"));
            users.add(new User(nextId++, "Student User", "student", "student@amazi.com", "student123", "Student"));
            saveUsersToFile();
        }
        loadSubmissionsFromFile();
    }

    // --- USER MANAGEMENT ---

    /**
     * UPDATED: Accepts 5 parameters from RegisterController
     * and passes 6 parameters to the User constructor.
     */
    public static void addUser(String name, String username, String email, String password, String role) {
        String standardizedRole = role.equalsIgnoreCase("Instructor") ? "Faculty" : role;

        users.add(new User(nextId++, name.trim(), username.trim(), email.trim(), password, standardizedRole));
        saveUsersToFile();
    }

    /**
     * UPDATED: Validates against either Username OR Email.
     */
    public static User validateUser(String identifier, String password) {
        if (identifier == null || password == null) return null;

        String cleanId = identifier.trim();
        return users.stream()
                .filter(u -> (u.getUsername().equalsIgnoreCase(cleanId) || u.getEmail().equalsIgnoreCase(cleanId))
                        && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    /**
     * FIXED: This method was missing, causing the "symbol not found" error.
     */
    public static List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    private static void saveUsersToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User u : users) {
                // FIXED: Now saves 6 fields including Username
                writer.println(u.getUserID() + "|" + u.getName() + "|" + u.getUsername() + "|" +
                        u.getEmail() + "|" + u.getPassword() + "|" + u.getRole());
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not save users to file", e);
        }
    }

    private static void loadUsersFromFile() {
        File file = new File(USERS_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|");
                // FIXED: Now expects 6 fields
                if (p.length >= 6) {
                    int id = Integer.parseInt(p[0]);
                    users.add(new User(id, p[1], p[2], p[3], p[4], p[5]));
                    nextId = Math.max(nextId, id + 1);
                }
            }
        } catch (IOException | NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Error loading users from file", e);
        }
    }

    // --- SUBMISSION MANAGEMENT (Keep existing logic) ---

    public static ObservableList<Submission> getAllSubmissions() {
        return allSubmissions;
    }

    public static void addSubmission(Submission submission) {
        allSubmissions.add(submission);
        saveSubmissionsToFile();
    }

    public static void updateSubmissionInFile(Submission submission) {
        for (int i = 0; i < allSubmissions.size(); i++) {
            Submission current = allSubmissions.get(i);
            if (current.getTitle().equals(submission.getTitle()) &&
                    current.getStudentName().equals(submission.getStudentName())) {
                allSubmissions.set(i, submission);
                break;
            }
        }
        saveSubmissionsToFile();
    }

    private static void loadSubmissionsFromFile() {
        File file = new File(SUBMISSION_FILE);
        if (!file.exists()) return;

        allSubmissions.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 6) {
                    Submission s = new Submission(parts[0], parts[1], "Project", "General",
                            LocalDate.now(), "No description", "None",
                            parts[3], parts[2]);
                    s.setFeedback(parts[4]);
                    s.setGrade(parts[5]);
                    allSubmissions.add(s);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error loading submissions", e);
        }
    }

    private static void saveSubmissionsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SUBMISSION_FILE))) {
            for (Submission s : allSubmissions) {
                writer.println(s.getTitle() + "|" + s.getCourse() + "|" + s.getStudentName() + "|" +
                        s.getStatus() + "|" + (s.getFeedback() == null ? "No feedback" : s.getFeedback()) + "|" +
                        (s.getGrade() == null ? "N/A" : s.getGrade()));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Critical error saving submissions", e);
        }
    }
}