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
    private static final String HISTORY_CONFIG = "history_config.txt";

    private static int nextId = 1;

    static {
        loadUsersFromFile();

        if (users.isEmpty()) {
            users.add(new User(nextId++, "Admin", "admin", "admin@amazi.com", "admin123", "Admin"));
            users.add(new User(nextId++, "Faculty User", "faculty", "faculty@amazi.com", "faculty123", "Faculty"));
            users.add(new User(nextId++, "Student User", "student", "student@amazi.com", "student123", "Student"));
            saveUsersToFile();
        }
        loadSubmissionsFromFile();
    }

    // --- FIXED: USER MANAGEMENT METHODS FOR ADMIN ---
    public static List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public static void deleteUser(User user) {
        if (user == null) return;
        users.removeIf(u -> u.getUserID() == user.getUserID());
        saveUsersToFile();
    }

    public static void addUser(String name, String username, String email, String password, String role) {
        String standardizedRole = role.equalsIgnoreCase("Instructor") ? "Faculty" : role;
        users.add(new User(nextId++, name.trim(), username.trim(), email.trim(), password, standardizedRole));
        saveUsersToFile();
    }

    public static User validateUser(String identifier, String password) {
        if (identifier == null || password == null) return null;
        String cleanId = identifier.trim();
        return users.stream()
                .filter(u -> (u.getUsername().equalsIgnoreCase(cleanId) || u.getEmail().equalsIgnoreCase(cleanId))
                        && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    private static void saveUsersToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User u : users) {
                writer.println(u.getUserID() + "|" + u.getName() + "|" + u.getUsername() + "|" +
                        u.getEmail() + "|" + u.getPassword() + "|" + u.getRole());
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not save users", e);
        }
    }

    private static void loadUsersFromFile() {
        File file = new File(USERS_FILE);
        if (!file.exists()) return;
        users.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 6) {
                    int id = Integer.parseInt(p[0]);
                    users.add(new User(id, p[1], p[2], p[3], p[4], p[5]));
                    nextId = Math.max(nextId, id + 1);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error loading users", e);
        }
    }

    public static ObservableList<Submission> getAllSubmissions() {
        return allSubmissions;
    }

    public static void addSubmission(Submission submission) {
        submission.setLastUpdated(System.currentTimeMillis());
        allSubmissions.add(submission);
        saveSubmissionsToFile();
    }

    public static void updateSubmissionInFile(Submission submission) {
        for (int i = 0; i < allSubmissions.size(); i++) {
            Submission current = allSubmissions.get(i);
            if (current.getTitle().trim().equalsIgnoreCase(submission.getTitle().trim()) &&
                    current.getStudentName().trim().equalsIgnoreCase(submission.getStudentName().trim())) {

                allSubmissions.set(i, submission);
                break;
            }
        }
        saveSubmissionsToFile();
    }

    // --- HISTORY CLEARANCE LOGIC ---

    public static void saveLastClearTime(String studentName) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(HISTORY_CONFIG, true))) {
            writer.println(studentName.trim() + "|" + System.currentTimeMillis());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not save history config", e);
        }
    }

    public static long getLastClearTime(String studentName) {
        File file = new File(HISTORY_CONFIG);
        if (!file.exists()) return 0;
        long lastClear = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 2 && p[0].equalsIgnoreCase(studentName.trim())) {
                    lastClear = Long.parseLong(p[1]);
                }
            }
        } catch (Exception e) { return 0; }
        return lastClear;
    }


    // --- FILE I/O ---
    private static void loadSubmissionsFromFile() {
        File file = new File(SUBMISSION_FILE);
        if (!file.exists()) return;

        allSubmissions.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length >= 6) {
                    Submission s = new Submission(
                            p[0], p[1], "Project", "General",
                            LocalDate.now(),
                            p.length > 6 ? p[6] : "No description",
                            "None", p[3], p[2]
                    );
                    s.setFeedback(p[4]);
                    s.setGrade(p[5]);
                    if (p.length > 7) s.setOrganizationName(p[7]);
                    if (p.length > 8) s.setEmail(p[8]);
                    if (p.length > 9) s.setCreditHours(p[9]);
                    if (p.length > 10) s.setLastUpdated(Long.parseLong(p[10]));

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
                writer.println(
                        s.getTitle() + "|" +
                                s.getCourse() + "|" +
                                s.getStudentName() + "|" +
                                s.getStatus() + "|" +
                                (s.getFeedback() == null || s.getFeedback().isEmpty() ? "No feedback" : s.getFeedback()) + "|" +
                                (s.getGrade() == null || s.getGrade().isEmpty() ? "N/A" : s.getGrade()) + "|" +
                                (s.getDescription() == null ? "" : s.getDescription()) + "|" +
                                (s.getOrganizationName() == null ? "" : s.getOrganizationName()) + "|" +
                                (s.getEmail() == null ? "" : s.getEmail()) + "|" +
                                (s.getCreditHours() == null ? "0" : s.getCreditHours()) + "|" +
                                s.getLastUpdated()
                );
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Critical error saving submissions", e);
        }
    }
}