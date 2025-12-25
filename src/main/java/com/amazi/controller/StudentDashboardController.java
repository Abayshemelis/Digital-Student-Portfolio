package com.amazi.controller;

import com.amazi.model.Submission;
import com.amazi.service.DataManager;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("all") // Clears spellcheck (Shimelis) and FXML warnings
public class StudentDashboardController {

    private static final Logger LOGGER = Logger.getLogger(StudentDashboardController.class.getName());
    private static final String STUDENT_NAME = "Abay Shimelis";

    @FXML private VBox mainContentArea;
    @FXML private VBox activityListContainer;
    @FXML private ImageView userProfileImage;
    @FXML private Label notificationBadge;
    @FXML private Label portfolioScoreLabel;
    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        refreshDashboardData();
    }

    @FXML
    public void handleRefresh() {
        refreshDashboardData();
        if (activityListContainer != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(500), activityListContainer);
            ft.setFromValue(0.4);
            ft.setToValue(1.0);
            ft.play();
        }
    }

    // FIX: Resolves "Cannot resolve symbol 'handleSettings'"
    @FXML
    private void handleSettings() {
        try {
            URL res = getClass().getResource("/com/amazi/view/SettingsView.fxml");
            if (res != null && mainContentArea != null) {
                Node node = FXMLLoader.load(res);
                mainContentArea.getChildren().setAll(node);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load settings", e);
        }
    }

    // FIX: Resolves "Incompatible event handler" (Changed ActionEvent to MouseEvent)
    @FXML
    public void handleImageUpload(MouseEvent event) {
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (file != null && userProfileImage != null) {
            userProfileImage.setImage(new Image(file.toURI().toString()));
        }
    }

    private void refreshDashboardData() {
        if (activityListContainer == null) return;
        activityListContainer.getChildren().clear();

        List<Submission> all = DataManager.getAllSubmissions();
        List<Submission> approved = all.stream()
                .filter(s -> s.getStudentName().equalsIgnoreCase(STUDENT_NAME))
                .filter(s -> "APPROVED".equalsIgnoreCase(s.getStatus()))
                .toList();

        if (notificationBadge != null) {
            long unread = approved.stream().filter(s -> !s.isViewedByStudent()).count();
            notificationBadge.setText(String.valueOf(unread));
            notificationBadge.setVisible(unread > 0);
        }

        if (approved.isEmpty()) {
            activityListContainer.getChildren().add(new Label("No approved results yet."));
            return;
        }

        if (portfolioScoreLabel != null) {
            portfolioScoreLabel.setText(approved.getLast().getGrade());
        }

        approved.stream().limit(3).forEach(s -> {
            VBox card = new VBox(5);
            card.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-border-radius: 5;");
            card.getChildren().add(new Label(s.getTitle() + " | Grade: " + s.getGrade()));
            activityListContainer.getChildren().add(card);
        });
    }

    private void navigateTo(String path, String title, ActionEvent event) {
        try {
            URL resource = getClass().getResource(path);
            if (resource == null) return;
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AMAZI | " + title);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Navigation failed", e);
        }
    }

    @FXML private void handleDashboardHome(ActionEvent e) { navigateTo("/com/amazi/view/DashBoard.fxml", "Dashboard", e); }
    @FXML private void handleCreatePortfolio(ActionEvent e) { navigateTo("/com/amazi/view/Portfolio.fxml", "Create", e); }
    @FXML private void handleSubmitPortfolio(ActionEvent e) { navigateTo("/com/amazi/view/MyPortfolio.fxml", "Portfolios", e); }
    @FXML private void handleLogout(ActionEvent e) { navigateTo("/com/amazi/view/Login.fxml", "Login", e); }
}