package com.amazi.controller;

import com.amazi.model.Submission;
import com.amazi.service.DataManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("all")
public class StudentDashboardController {

    private static final Logger LOGGER = Logger.getLogger(StudentDashboardController.class.getName());
    private static final String STUDENT_NAME = "Abay Shimelis";

    // --- System Settings State ---
    private static boolean showCgpa = true;
    private static boolean showCreditHours = true;
    private static boolean showHistory = true;

    @FXML private VBox mainContentArea;
    @FXML private VBox activityListContainer;
    @FXML private ImageView userProfileImage;
    @FXML private Label portfolioScoreLabel;
    @FXML private Label totalCreditsLabel;
    @FXML private ProgressBar creditProgressBar;
    @FXML private Button logoutButton;

    @FXML private VBox cgpaSection;
    @FXML private VBox creditHourSection;
    @FXML private VBox historySection;

    @FXML
    public void initialize() {
        applySystemSettings();
        refreshDashboardData();

        // Live update every 5 seconds to catch new faculty feedback
        Timeline liveUpdate = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            refreshDashboardData();
        }));
        liveUpdate.setCycleCount(Timeline.INDEFINITE);
        liveUpdate.play();
    }

    // --- SYSTEM SETTINGS CONTROL ---
    @FXML
    private void handleSystemSettings() {
        VBox settingsPanel = new VBox(20);
        settingsPanel.setStyle("-fx-padding: 40; -fx-background-color: #ffffff; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        settingsPanel.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("Dashboard Control Center");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        CheckBox cgpaToggle = new CheckBox("Show CGPA / Grade Card");
        cgpaToggle.setSelected(showCgpa);

        CheckBox creditToggle = new CheckBox("Show Completed Credits Card");
        creditToggle.setSelected(showCreditHours);

        CheckBox historyToggle = new CheckBox("Show Recent Activity History");
        historyToggle.setSelected(showHistory);

        Button saveBtn = new Button("Apply Changes");
        saveBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            showCgpa = cgpaToggle.isSelected();
            showCreditHours = creditToggle.isSelected();
            showHistory = historyToggle.isSelected();
            applySystemSettings();
            handleDashboardHome(e);
        });

        settingsPanel.getChildren().addAll(title, cgpaToggle, creditToggle, historyToggle, saveBtn);

        if (mainContentArea != null) {
            mainContentArea.getChildren().setAll(settingsPanel);
        }
    }

    private void applySystemSettings() {
        if (cgpaSection != null) {
            cgpaSection.setVisible(showCgpa);
            cgpaSection.setManaged(showCgpa);
        }
        if (creditHourSection != null) {
            creditHourSection.setVisible(showCreditHours);
            creditHourSection.setManaged(showCreditHours);
        }
        if (historySection != null) {
            historySection.setVisible(showHistory);
            historySection.setManaged(showHistory);
        }
    }

    // --- REFRESH LOGIC (UPDATED WITH TIMESTAMP FILTERING) ---
    private void refreshDashboardData() {
        if (activityListContainer == null) return;

        // 1. Get current clear timestamp (Persists after restart)
        long lastClearTime = DataManager.getLastClearTime(STUDENT_NAME);

        List<Submission> all = DataManager.getAllSubmissions();
        List<Submission> studentRecords = all.stream()
                .filter(s -> s.getStudentName().trim().equalsIgnoreCase(STUDENT_NAME.trim()))
                .toList();

        activityListContainer.getChildren().clear();

        double totalGradePoints = 0;
        int totalCredits = 0;

        for (Submission s : studentRecords) {
            // A. Academic Logic (Calculated from ALL approved records regardless of history clear)
            if ("APPROVED".equalsIgnoreCase(s.getStatus())) {
                try {
                    int credits = Integer.parseInt(s.getCreditHours() != null ? s.getCreditHours() : "0");
                    totalCredits += credits;
                    totalGradePoints += (getGradeValue(s.getGrade()) * credits);
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.WARNING, "Invalid credit format for: " + s.getTitle());
                }
            }

            // B. History Display Logic
            // Show only if:
            // 1. Status is NOT "PENDING" (Faculty has graded/feedbacked)
            // 2. The submission was updated AFTER the student last clicked "Clear History"
            if (!"PENDING".equalsIgnoreCase(s.getStatus())) {
                if (s.getLastUpdated() > lastClearTime) {
                    activityListContainer.getChildren().add(createActivityRow(s));
                }
            }
        }

        // Update Dashboard Cards
        updateDashboardCards(totalCredits, totalGradePoints);

        if (activityListContainer.getChildren().isEmpty()) {
            showStatusMessage("History cleared. New faculty responses will appear here.");
        }
    }

    private void updateDashboardCards(int totalCredits, double totalGradePoints) {
        if (totalCreditsLabel != null) {
            totalCreditsLabel.setText(String.valueOf(totalCredits));
            if (creditProgressBar != null) {
                // Graduation target assumed at 120 credits
                creditProgressBar.setProgress(totalCredits / 120.0);
            }
        }

        if (portfolioScoreLabel != null) {
            if (totalCredits > 0) {
                double cgpa = totalGradePoints / totalCredits;
                portfolioScoreLabel.setText(String.format("%.2f", cgpa));
            } else {
                portfolioScoreLabel.setText("0.00");
            }
        }
    }

    private double getGradeValue(String grade) {
        if (grade == null) return 0.0;
        return switch (grade.toUpperCase().trim()) {
            case "A+", "A" -> 4.0;
            case "B+" -> 3.5;
            case "B" -> 3.0;
            case "C+" -> 2.5;
            case "C" -> 2.0;
            case "D" -> 1.0;
            default -> 0.0;
        };
    }

    private HBox createActivityRow(Submission s) {
        HBox row = new HBox(20);
        row.getStyleClass().add("activity-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new javafx.geometry.Insets(12, 20, 12, 20));

        VBox details = new VBox(5);
        Label title = new Label(s.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 14px;");

        String feedbackText = (s.getFeedback() != null && !s.getFeedback().isEmpty())
                ? "Faculty: " + s.getFeedback() : "Status: " + s.getStatus();

        Label commentLabel = new Label(feedbackText);
        commentLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        commentLabel.setWrapText(true);
        commentLabel.setMaxWidth(380);

        details.getChildren().addAll(title, commentLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox resBox = new VBox(2);
        resBox.setAlignment(Pos.CENTER);
        Label head = new Label("RESULT");
        head.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");
        Label gradeBadge = new Label(s.getGrade() != null ? s.getGrade() : "P");

        // UI Styling for badges
        String badgeColor = "#f0fdf4"; String textColor = "#16a34a";
        if ("REJECTED".equalsIgnoreCase(s.getStatus())) { badgeColor = "#fef2f2"; textColor = "#dc2626"; }
        if ("REVISION".equalsIgnoreCase(s.getStatus())) { badgeColor = "#fffbeb"; textColor = "#d97706"; }

        gradeBadge.setStyle("-fx-background-color: "+badgeColor+"; -fx-text-fill: "+textColor+"; -fx-padding: 4 10; -fx-background-radius: 5; -fx-font-weight: bold;");

        resBox.getChildren().addAll(head, gradeBadge);
        row.getChildren().addAll(details, spacer, resBox);
        return row;
    }

    private void showStatusMessage(String message) {
        Label msg = new Label(message);
        msg.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-padding: 20;");
        activityListContainer.getChildren().add(msg);
    }

    // --- UPDATED CLEAR HISTORY ---
    @FXML
    private void handleClearHistory() {
        // Saves the current timestamp to history_config.txt
        DataManager.saveLastClearTime(STUDENT_NAME);

        // Refresh to instantly hide current records
        refreshDashboardData();
    }

    @FXML public void handleRefresh() {
        refreshDashboardData();
    }

    // --- NAVIGATION & UTILS ---
    @FXML
    private void handleImageUpload(MouseEvent event) {
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (file != null && userProfileImage != null) {
            userProfileImage.setImage(new Image(file.toURI().toString()));
        }
    }

    private void navigateTo(String path, String title, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            URL css = getClass().getResource("/com/amazi/view/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene);
            stage.setTitle("STUDENT | " + title);
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Navigation failed", e);
        }
    }

    @FXML private void handleDashboardHome(ActionEvent e) { navigateTo("/com/amazi/view/DashBoard.fxml", "Dashboard", e); }
    @FXML private void handleCreatePortfolio(ActionEvent e) { navigateTo("/com/amazi/view/Portfolio.fxml", "Portfolio", e); }
    @FXML private void handleLogout(ActionEvent e) { navigateTo("/com/amazi/view/Login.fxml", "Sign-In", e); }
}