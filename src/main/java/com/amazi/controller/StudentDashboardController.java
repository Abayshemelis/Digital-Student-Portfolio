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
import javafx.scene.control.Label;
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

public class StudentDashboardController {

    private static final Logger LOGGER = Logger.getLogger(StudentDashboardController.class.getName());
    private static final String STUDENT_NAME = "Abay Shimelis";
    private static boolean isHistoryClearedGlobal = false;

    @FXML private VBox mainContentArea;
    @FXML private VBox activityListContainer;
    @FXML private ImageView userProfileImage;
    @FXML private Label portfolioScoreLabel;

    /**
     * UPDATED: Initialize now sets up an Auto-Refresh Timeline.
     * This ensures grades and comments appear "immediately" (every 5 seconds).
     */
    @FXML
    public void initialize() {
        // Initial data load
        refreshDashboardData();

        // Setup Auto-Refresh (Live Updates)
        Timeline liveUpdate = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            // Only auto-refresh if the user hasn't manually cleared the history
            if (!isHistoryClearedGlobal) {
                refreshDashboardData();
            }
        }));
        liveUpdate.setCycleCount(Timeline.INDEFINITE);
        liveUpdate.play();

        LOGGER.info("Dashboard initialized with 5-second live update heartbeat.");
    }

    @FXML
    private void handleImageUpload(MouseEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Profile Image");
        File file = fc.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (file != null && userProfileImage != null) {
            userProfileImage.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void handleSettings() {
        try {
            URL res = getClass().getResource("/com/amazi/view/SettingsView.fxml");
            if (res != null && mainContentArea != null) {
                Node node = FXMLLoader.load(res);
                mainContentArea.getChildren().setAll(node);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load settings view", e);
        }
    }

    @FXML
    private void handleClearHistory() {
        isHistoryClearedGlobal = true;
        refreshDashboardData();
    }

    @FXML
    public void handleRefresh() {
        isHistoryClearedGlobal = false;
        refreshDashboardData();
    }

    private void refreshDashboardData() {
        if (activityListContainer == null) return;

        List<Submission> all = DataManager.getAllSubmissions();

        // Filter student records with .trim() to ensure no name mismatch
        List<Submission> studentRecords = all.stream()
                .filter(s -> s.getStudentName().trim().equalsIgnoreCase(STUDENT_NAME.trim()))
                .toList();

        // UI Update: Clear and rebuild
        activityListContainer.getChildren().clear();

        if (isHistoryClearedGlobal) {
            showStatusMessage("History cleared. Click Refresh to reload.");
            return;
        }

        if (studentRecords.isEmpty()) {
            showStatusMessage("No academic records found.");
            return;
        }

        // Update CGPA Card with the latest grade
        if (portfolioScoreLabel != null) {
            portfolioScoreLabel.setText(studentRecords.getLast().getGrade());
        }

        // Generate Rows for Recent Activity
        for (Submission s : studentRecords) {
            activityListContainer.getChildren().add(createActivityRow(s));
        }
    }

    private HBox createActivityRow(Submission s) {
        HBox row = new HBox(20);
        row.getStyleClass().add("activity-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new javafx.geometry.Insets(12, 20, 12, 20));

        VBox details = new VBox(5);
        Label title = new Label(s.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 14px;");

        // Use getFeedback() from your model
        String feedbackText = (s.getFeedback() != null && !s.getFeedback().isEmpty())
                ? "Faculty: " + s.getFeedback()
                : "Status: " + s.getStatus();

        Label commentLabel = new Label(feedbackText);
        commentLabel.getStyleClass().add("faculty-comment-text");
        commentLabel.setWrapText(true);
        commentLabel.setMaxWidth(380);

        details.getChildren().addAll(title, commentLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox resBox = new VBox(2);
        resBox.setAlignment(Pos.CENTER);
        Label head = new Label("RESULT");
        head.getStyleClass().add("result-header-text");
        Label gradeBadge = new Label(s.getGrade());
        gradeBadge.getStyleClass().add("badge-passed");

        resBox.getChildren().addAll(head, gradeBadge);
        row.getChildren().addAll(details, spacer, resBox);
        return row;
    }

    private void showStatusMessage(String message) {
        Label msg = new Label(message);
        msg.getStyleClass().add("loading-text");
        activityListContainer.getChildren().add(msg);
    }

    private void navigateTo(String path, ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource(path);
            if (fxmlUrl == null) return;
            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Navigation failed", e);
        }
    }

    @FXML private void handleDashboardHome(ActionEvent e) { navigateTo("/com/amazi/view/DashBoard.fxml", e); }
    @FXML private void handleCreatePortfolio(ActionEvent e) { navigateTo("/com/amazi/view/Portfolio.fxml", e); }
    @FXML private void handleLogout(ActionEvent e) { navigateTo("/com/amazi/view/Login.fxml", e); }
}