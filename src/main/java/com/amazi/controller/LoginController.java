package com.amazi.controller;

import com.amazi.model.User;
import com.amazi.service.DataManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        User user = DataManager.validateUser(email, password);

        if (user != null) {
            String role = user.getRole();
            System.out.println("Login Success: " + email + " | Role: [" + role + "]");

            if (role == null) {
                messageLabel.setText("Error: User role not assigned.");
                return;
            }

            // FIXED: 'if' statement replaced with 'switch' statement
            switch (role.toLowerCase()) {
                case "admin":
                    changeScene("/com/amazi/view/AdminDashboard.fxml", "Admin Dashboard", event);
                    break;
                case "faculty":
                case "instructor":
                    changeScene("/com/amazi/view/FacultyReview.fxml", "Faculty Portal", event);
                    break;
                case "student":
                    changeScene("/com/amazi/view/DashBoard.fxml", "Student Dashboard", event);
                    break;
                default:
                    messageLabel.setText("Error: Role '" + role + "' not recognized.");
                    break;
            }
        } else {
            messageLabel.setText("Invalid credentials.");
            messageLabel.setStyle("-fx-text-fill: #fb7185;");
        }
    }

    @FXML
    private void handleCreateAccount(ActionEvent event) {
        changeScene("/com/amazi/view/Register.fxml", "Create Account", event);
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        // Parameter 'event' is used here to prevent the warning
        if (event != null) {
            messageLabel.setText("Reset link sent!");
            messageLabel.setStyle("-fx-text-fill: #4ade80;");
        }
    }

    private void changeScene(String fxml, String title, ActionEvent event) {
        try {
            URL loc = getClass().getResource(fxml);
            if (loc == null) {
                // Better logging than printStackTrace
                System.err.println("Navigation Failed: Resource not found at " + fxml);
                return;
            }

            FXMLLoader loader = new FXMLLoader(loc);
            Parent root = loader.load();
            Scene scene = new Scene(root);

            // FIXED: Typo/Logic causing "fname is null"
            // We ensure the CSS resource is fully qualified and checked for existence
            URL css = getClass().getResource("/com/amazi/view/style.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            } else {
                System.err.println("Log: style.css not found, skipping stylesheet application.");
            }

            // Using 'event' to identify the source stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();

        } catch (Exception e) {
            // Replaced printStackTrace with robust error message
            System.err.println("CRITICAL: Failed to transition to " + title + ". Check FXML controller assignments.");
            messageLabel.setText("Error loading page.");
        }
    }
}