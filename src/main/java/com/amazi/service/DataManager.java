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
    // FIXED: Using a proper Logger instead of printStackTrace
    private static final Logger LOGGER = Logger.getLogger(DataManager.class.getName());

    private static final List<User> users = new ArrayList<>();
    private static final ObservableList<Submission> allSubmissions = FXCollections.observableArrayList();
    private static final String SUBMISSION_FILE = "submissions.txt";
    private static final String USERS_FILE = "users.txt";
    private static int nextId = 1;

    static {
        loadUsersFromFile();

        // Ensure default users exist if file is empty
        if (users.isEmpty()) {
            users.add(new User(nextId++, "Admin", "admin@amazi.com", "admin123", "Admin"));
            users.add(new User(nextId++, "Faculty User", "faculty@amazi.com", "faculty123", "Faculty"));
            users.add(new User(nextId++, "Student User", "student@amazi.com", "student123", "Student"));
            saveUsersToFile();
        }
        loadSubmissionsFromFile();
    }

    // --- USER MANAGEMENT ---

    public static void addUser(String name, String email, String password, String role) {
        // CLEANUP: Ensure role names are standardized to "Faculty" for consistency
        String standardizedRole = role;
        if (role.equalsIgnoreCase("Instructor")) {
            standardizedRole = "Faculty";
        }

        users.add(new User(nextId++, name.trim(), email.trim(), password, standardizedRole));
        saveUsersToFile();
    }

    public static User validateUser(String email, String password) {
        if (email == null || password == null) return null;

        return users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email.trim()) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    public static List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    private static void saveUsersToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User u : users) {
                writer.println(u.getUserID() + "|" + u.getName() + "|" + u.getEmail() + "|" + u.getPassword() + "|" + u.getRole());
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
                if (p.length >= 5) {
                    int id = Integer.parseInt(p[0]);
                    users.add(new User(id, p[1], p[2], p[3], p[4]));
                    nextId = Math.max(nextId, id + 1);
                }
            }
        } catch (IOException | NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Error loading users from file", e);
        }
    }

    // --- SUBMISSION MANAGEMENT ---

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
            // Match based on unique combination of student and project title
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
                    // Reconstructing Submission object from text file parts
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
                writer.println(s.getTitle() + "|" +
                        s.getCourse() + "|" +
                        s.getStudentName() + "|" +
                        s.getStatus() + "|" +
                        (s.getFeedback() == null ? "No feedback" : s.getFeedback()) + "|" +
                        (s.getGrade() == null ? "N/A" : s.getGrade()));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Critical error saving submissions", e);
        }
    }
}