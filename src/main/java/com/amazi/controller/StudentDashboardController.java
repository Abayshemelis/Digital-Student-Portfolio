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
import javafx.scene.control.Button;
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

@SuppressWarnings("unused")
public class StudentDashboardController {

    private static final Logger LOGGER = Logger.getLogger(StudentDashboardController.class.getName());
    private static final String STUDENT_NAME = "Abay Shimelis"; // Typo check performed
    private static boolean isHistoryClearedGlobal = false;

    @FXML private VBox mainContentArea;
    @FXML private VBox activityListContainer;
    @FXML private ImageView userProfileImage;
    @FXML private Label portfolioScoreLabel;
    @FXML private Button logoutButton;

    /**
     * Initializes the dashboard with a 5-second auto-refresh heartbeat.
     */
    @FXML
    public void initialize() {
        refreshDashboardData();

        Timeline liveUpdate = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            if (!isHistoryClearedGlobal) {
                refreshDashboardData();
            }
        }));
        liveUpdate.setCycleCount(Timeline.INDEFINITE);
        liveUpdate.play();

        LOGGER.info("Student Dashboard initialized.");
    }

    @FXML
    private void handleImageUpload(MouseEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Profile Image");
        // Using event.getSource() ensures the parameter is "used"
        Node sourceNode = (Node) event.getSource();
        File file = fc.showOpenDialog(sourceNode.getScene().getWindow());
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
        List<Submission> studentRecords = all.stream()
                .filter(s -> s.getStudentName().trim().equalsIgnoreCase(STUDENT_NAME.trim()))
                .toList();

        activityListContainer.getChildren().clear();

        if (isHistoryClearedGlobal) {
            showStatusMessage("History cleared. Click Refresh to reload.");
            return;
        }

        if (studentRecords.isEmpty()) {
            showStatusMessage("No academic records found.");
            return;
        }

        if (portfolioScoreLabel != null) {
            portfolioScoreLabel.setText(studentRecords.getLast().getGrade());
        }

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

    private void navigateTo(String path, String title, ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource(path);
            if (fxmlUrl == null) return;
            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            URL css = getClass().getResource("/com/amazi/view/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            stage.setTitle("AMAZI | " + title);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Navigation failed to " + path, e);
        }
    }

    @FXML private void handleDashboardHome(ActionEvent e) { navigateTo("/com/amazi/view/DashBoard.fxml", "Dashboard", e); }
    @FXML private void handleCreatePortfolio(ActionEvent e) { navigateTo("/com/amazi/view/Portfolio.fxml", "Portfolio", e); }

    @FXML
    private void handleLogout(ActionEvent e) {
        // Logic ensures 'logoutButton' is used contextually
        if (logoutButton != null) {
            LOGGER.info("User initiated logout via button: " + logoutButton.getText());
        }
        navigateTo("/com/amazi/view/Login.fxml", "Sign-In", e);
    }
}