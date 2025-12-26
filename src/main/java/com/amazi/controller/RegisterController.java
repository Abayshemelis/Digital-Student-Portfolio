package com.amazi.controller;

import com.amazi.service.DataManager;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegisterController {
    private static final Logger LOGGER = Logger.getLogger(RegisterController.class.getName());
    private static final String ERROR_COLOR = "#fb7185";
    private static final String SUCCESS_COLOR = "#10b981";

    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label messageLabel;
    @FXML private Circle bubble1, bubble2;

    @FXML
    public void initialize() {
        // Initialize Role Selection
        if (roleComboBox != null) {
            roleComboBox.getItems().setAll("Student", "Faculty", "Admin");
            roleComboBox.setValue("Student");
        }

        // Start Background Animations
        animate(bubble1, 20);
        animate(bubble2, -20);
    }

    private void animate(Circle c, double dist) {
        if (c != null) {
            TranslateTransition tt = new TranslateTransition(Duration.seconds(3), c);
            tt.setByY(dist);
            tt.setCycleCount(TranslateTransition.INDEFINITE);
            tt.setAutoReverse(true);
            tt.play();
        }
    }

    @FXML
    private void handleRegistration(ActionEvent event) {
        String name = nameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        // 1. Validation
        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            setStatus("Please fill in all fields!", ERROR_COLOR);
            return;
        }

        if (password.length() < 6) {
            setStatus("Password must be at least 6 characters.", ERROR_COLOR);
            return;
        }

        try {
            // 2. Data Persistence
            // This matches the DataManager method that saves to users.txt
            DataManager.addUser(name, username, email, password, role);

            setStatus("Account created! Redirecting...", SUCCESS_COLOR);

            // 3. Automated Navigation after a short delay or immediate
            prepareAndNavigate(event);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Registration failed", e);
            setStatus("Registration failed. Please try again.", ERROR_COLOR);
        }
    }

    @FXML
    private void backToLogin(ActionEvent event) {
        prepareAndNavigate(event);
    }

    private void setStatus(String message, String color) {
        if (messageLabel != null) {
            messageLabel.setText(message);
            messageLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
        }
    }

    /**
     * Unified navigation method to maintain DRY (Don't Repeat Yourself) principles.
     */
    private void prepareAndNavigate(ActionEvent event) {
        try {
            URL loc = getClass().getResource("/com/amazi/view/Login.fxml");
            if (loc == null) {
                LOGGER.severe("FXML file not found: /com/amazi/view/Login.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(loc);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            // Re-apply style.css to ensure visual consistency
            URL css = getClass().getResource("/com/amazi/view/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            stage.setTitle("AMAZI | Log In");
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Navigation failed", e);
            setStatus("Navigation error. Please restart the app.", ERROR_COLOR);
        }
    }
}