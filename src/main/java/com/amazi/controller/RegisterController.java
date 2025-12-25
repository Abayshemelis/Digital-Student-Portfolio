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
    // FIXED: Using robust logging instead of printStackTrace
    private static final Logger LOGGER = Logger.getLogger(RegisterController.class.getName());

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label messageLabel;

    @FXML private Circle bubble1;
    @FXML private Circle bubble2;

    @FXML
    public void initialize() {
        if (roleComboBox != null) {
            roleComboBox.getItems().addAll("Student", "Faculty", "Admin");
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
        String name = (nameField.getText() != null) ? nameField.getText().trim() : "";
        String email = (emailField.getText() != null) ? emailField.getText().trim() : "";
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if (name.isEmpty() || email.isEmpty() || password == null || password.isEmpty()) {
            messageLabel.setText("Please fill in all fields!");
            messageLabel.setStyle("-fx-text-fill: #fb7185;");
            return;
        }

        DataManager.addUser(name, email, password, role);

        // FIXED: Renamed method to use the verb "logIn" (Noun: Login, Verb: Log In)
        navigateToLogIn(event);
    }

    @FXML
    private void backToLogin(ActionEvent event) {
        navigateToLogIn(event);
    }

    /**
     * FIXED: This method addresses the "Value of parameter is always..." warning.
     * Since we currently only navigate to the Login screen from here,
     * we've created a specific method for it.
     */
    private void navigateToLogIn(ActionEvent event) {
        String fxml = "/com/amazi/view/Login.fxml";
        String title = "Log In - AMAZI";

        try {
            URL loc = getClass().getResource(fxml);
            if (loc == null) {
                LOGGER.log(Level.SEVERE, "FXML resource not found: {0}", fxml);
                return;
            }

            FXMLLoader loader = new FXMLLoader(loc);
            Parent root = loader.load();
            Scene scene = new Scene(root);

            URL css = getClass().getResource("/com/amazi/view/style.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            // FIXED: Replaced printStackTrace with robust logging
            LOGGER.log(Level.SEVERE, "Failed to navigate to Log In screen", e);
        }
    }
}