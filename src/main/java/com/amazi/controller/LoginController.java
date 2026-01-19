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

@SuppressWarnings("unused") // Suppresses "field never assigned" warnings from IDE
public class LoginController {
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());
    private static final String ERROR_COLOR = "#fb7185";
    private static final String SUCCESS_COLOR = "#4ade80";

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleSelector;
    @FXML private Label messageLabel;

    @FXML private Button adminTab, staffTab, studentTab;
    @FXML private Button signInButton;

    @FXML
    public void initialize() {
        // Default UI state
        if (roleSelector != null) {
            roleSelector.setValue("Student");
        }

        if (studentTab != null) {
            studentTab.getStyleClass().add("pill-student-active");
        }
    }

    @FXML
    private void handleTabSwitch(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        messageLabel.setText("");

        adminTab.getStyleClass().removeAll("pill-admin-active", "pill-button-active");
        staffTab.getStyleClass().removeAll("pill-staff-active", "pill-button-active");
        studentTab.getStyleClass().removeAll("pill-student-active", "pill-button-active");

        adminTab.getStyleClass().add("pill-button");
        staffTab.getStyleClass().add("pill-button");
        studentTab.getStyleClass().add("pill-button");

        if (clicked == adminTab) {
            clicked.getStyleClass().add("pill-admin-active");
            roleSelector.setValue("Admin");
            updateSignInTheme("btn-admin");
        } else if (clicked == staffTab) {
            clicked.getStyleClass().add("pill-staff-active");
            roleSelector.setValue("Faculty");
            updateSignInTheme("btn-staff");
        } else {
            clicked.getStyleClass().add("pill-student-active");
            roleSelector.setValue("Student");
            updateSignInTheme("btn-student");
        }
    }

    private void updateSignInTheme(String themeClass) {
        if (signInButton != null) {
            signInButton.getStyleClass().removeAll("btn-admin", "btn-staff", "btn-student");
            signInButton.getStyleClass().add(themeClass);
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String identifier = emailField.getText().trim();
        String password = passwordField.getText();
        String selectedRole = roleSelector.getValue();

        if (identifier.isEmpty() || password.isEmpty()) {
            showStatus("Username/Email and Password are required.", ERROR_COLOR);
            return;
        }

        User user = DataManager.validateUser(identifier, password);

        if (user != null) {
            validateRoleAndNavigate(user, selectedRole, event);
        } else {
            showStatus("Invalid credentials. Please try again.", ERROR_COLOR);
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
            showStatus("Access Denied: Account is not registered as " + selectedRole, ERROR_COLOR);
        }
    }

    @FXML
    private void handleCreateAccount(ActionEvent event) {
        changeScene("/com/amazi/view/Register.fxml", "Create Account", event);
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        // Use event to find the stage for potential dialogs
        String userEmail = emailField.getText();
        if (userEmail.isEmpty()) {
            showStatus("Please enter your email first.", ERROR_COLOR);
        } else {
            showStatus("Reset link sent to: " + userEmail, SUCCESS_COLOR);
        }
    }

    private void showStatus(String message, String color) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    private void changeScene(String fxml, String title, ActionEvent event) {
        try {
            URL loc = getClass().getResource(fxml);
            if (loc == null) {
                LOGGER.log(Level.SEVERE, "FXML Path not found: {0}", fxml);
                showStatus("UI Error: Missing view file.", ERROR_COLOR);
                return;
            }

            FXMLLoader loader = new FXMLLoader(loc);
            Parent root = loader.load();


            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(root);
            URL css = getClass().getResource("/com/amazi/view/style.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("AMAZI | " + title);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Scene change failed", e);
            showStatus("Critical Error: Navigation failed.", ERROR_COLOR);
        }
    }
}