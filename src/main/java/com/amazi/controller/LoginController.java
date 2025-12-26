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
    private static final String SUCCESS_COLOR = "#4ade80";

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleSelector;
    @FXML private Label messageLabel;

    @FXML private Button adminTab, staffTab, studentTab;

    @FXML
    public void initialize() {
        // Default UI state
        if (roleSelector != null) {
            roleSelector.setValue("Student");
        }
        // Initialize the first tab as active visually
        if (studentTab != null) {
            studentTab.getStyleClass().add("tab-button-active");
        }
    }

    /**
     * Updates the UI tabs and the internal role selection.
     */
    @FXML
    private void handleTabSwitch(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        messageLabel.setText("");

        // Reset all buttons to standard tab-button style
        adminTab.getStyleClass().removeAll("tab-button-active");
        staffTab.getStyleClass().removeAll("tab-button-active");
        studentTab.getStyleClass().removeAll("tab-button-active");

        adminTab.getStyleClass().add("tab-button");
        staffTab.getStyleClass().add("tab-button");
        studentTab.getStyleClass().add("tab-button");

        // Apply active style to the selected tab
        clicked.getStyleClass().remove("tab-button");
        clicked.getStyleClass().add("tab-button-active");

        // Update the hidden or visible role selector
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
        String identifier = emailField.getText().trim(); // Can be username or email
        String password = passwordField.getText();
        String selectedRole = roleSelector.getValue();

        if (identifier.isEmpty() || password.isEmpty()) {
            showStatus("Username/Email and Password are required.", ERROR_COLOR);
            return;
        }

        // Logic check against DataManager
        User user = DataManager.validateUser(identifier, password);

        if (user != null) {
            validateRoleAndNavigate(user, selectedRole, event);
        } else {
            showStatus("Invalid credentials. Please try again.", ERROR_COLOR);
        }
    }

    /**
     * Ensures the user's account role matches the tab they are trying to log in from.
     */
    private void validateRoleAndNavigate(User user, String selectedRole, ActionEvent event) {
        String actualRole = user.getRole();

        // Security check: Match database role with UI selection
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
        showStatus("Reset link sent to: " + emailField.getText(), SUCCESS_COLOR);
    }

    private void showStatus(String message, String color) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    /**
     * Standardized scene switcher with CSS injection.
     */
    private void changeScene(String fxml, String title, ActionEvent event) {
        try {
            URL loc = getClass().getResource(fxml);
            if (loc == null) {
                LOGGER.log(Level.SEVERE, "FXML Path not found: " + fxml);
                showStatus("UI Error: Missing view file.", ERROR_COLOR);
                return;
            }

            FXMLLoader loader = new FXMLLoader(loc);
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(root);

            // Re-apply global CSS
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