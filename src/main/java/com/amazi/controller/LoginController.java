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
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController {
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());
    private static final String ERROR_COLOR = "#fb7185";

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleSelector;
    @FXML private Label messageLabel;

    @FXML private Button adminTab, staffTab, studentTab;

    @FXML
    public void initialize() {
        // Default to Student role on startup
        roleSelector.setValue("Student");
    }

    @FXML
    private void handleTabSwitch(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        messageLabel.setText("");

        // Reset all tab styles
        adminTab.getStyleClass().setAll("tab-button", "button");
        staffTab.getStyleClass().setAll("tab-button", "button");
        studentTab.getStyleClass().setAll("tab-button", "button");

        // Set active style to clicked tab
        clicked.getStyleClass().setAll("tab-button-active", "button");

        // Update internal role logic
        if (clicked == adminTab) {
            roleSelector.setValue("Admin");
        } else if (clicked == staffTab) {
            roleSelector.setValue("Faculty");
        } else {
            roleSelector.setValue("Student");
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String selectedRole = roleSelector.getValue();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both user name and password.");
            return;
        }

        User user = DataManager.validateUser(email, password);

        if (user != null) {
            validateRoleAndNavigate(user, selectedRole, event);
        } else {
            showError("Invalid user name or password.");
        }
    }

    private void validateRoleAndNavigate(User user, String selectedRole, ActionEvent event) {
        String actualRole = user.getRole();

        if (actualRole != null && actualRole.equalsIgnoreCase(selectedRole)) {
            String fxmlPath = switch (selectedRole.toLowerCase()) {
                case "admin" -> "/com/amazi/view/AdminDashboard.fxml";
                case "faculty" -> "/com/amazi/view/FacultyReview.fxml";
                case "student" -> "/com/amazi/view/DashBoard.fxml";
                default -> null;
            };

            if (fxmlPath != null) {
                changeScene(fxmlPath, selectedRole + " Portal", event);
            }
        } else {
            showError("Access Denied: You are not authorized as " + selectedRole);
        }
    }

    @FXML
    private void handleCreateAccount(ActionEvent event) {
        changeScene("/com/amazi/view/Register.fxml", "Create Account", event);
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        messageLabel.setText("Reset link sent to your email!");
        messageLabel.setStyle("-fx-text-fill: #4ade80;");
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + ";");
    }

    private void changeScene(String fxml, String title, ActionEvent event) {
        try {
            URL loc = getClass().getResource(fxml);
            if (loc == null) {
                LOGGER.log(Level.SEVERE, "Resource not found: " + fxml);
                return;
            }

            FXMLLoader loader = new FXMLLoader(loc);
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            URL css = getClass().getResource("/com/amazi/view/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            stage.setTitle("AMAZI | " + title);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Navigation failed", e);
            showError("Error loading page.");
        }
    }
}