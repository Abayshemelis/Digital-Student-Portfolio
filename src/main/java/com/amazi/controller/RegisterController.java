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
        if (roleComboBox != null) {
            roleComboBox.getItems().setAll("Student", "Faculty", "Admin");
            roleComboBox.setValue("Student");
        }
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

        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            setStatus("Please fill in all fields!", ERROR_COLOR);
            return;
        }

        // FIX: Ensure your DataManager.addUser method signature matches these 5 arguments
        // If you cannot change DataManager, remove 'name' and just pass 'username'
        try {
            DataManager.addUser(name, username, email, password, role);
            setStatus("Account created! Redirecting...", SUCCESS_COLOR);
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
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: " + color + ";");
    }

    /**
     * FIX: Unified navigation method to remove "Duplicated Code Fragment" warning.
     * This replaces navigateToLogIn and backToLogin duplication.
     */
    private void prepareAndNavigate(ActionEvent event) {
        try {
            URL loc = getClass().getResource("/com/amazi/view/Login.fxml");
            FXMLLoader loader = new FXMLLoader(loc);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            URL css = getClass().getResource("/com/amazi/view/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            stage.setTitle("AMAZI | Log In");
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Navigation failed", e);
        }
    }
}