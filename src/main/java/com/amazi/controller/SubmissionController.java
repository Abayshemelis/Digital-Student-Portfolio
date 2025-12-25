package com.amazi.controller;

import com.amazi.model.Submission;
import com.amazi.service.DataManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class SubmissionController {

    @FXML private ListView<Submission> portfolioListView;

    @FXML
    public void initialize() {
        // Load data from DataManager
        if (DataManager.getAllSubmissions() != null) {
            portfolioListView.setItems(DataManager.getAllSubmissions());
        }

        // FIX: Changed '_' to 'lv' to support Java 21
        portfolioListView.setCellFactory(lv -> new ListCell<>() {
            private final VBox container = new VBox(5);
            private final Label lblTitle = new Label();
            private final Label lblDesc = new Label();

            {
                lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
                lblDesc.setWrapText(true);
                lblDesc.setStyle("-fx-text-fill: #7f8c8d;");
                container.getChildren().addAll(lblTitle, lblDesc);
                container.setPadding(new javafx.geometry.Insets(10));
            }

            @Override
            protected void updateItem(Submission item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    lblTitle.setText(item.getTitle());
                    // Ensure your Submission model has getDescription() or update this line
                    lblDesc.setText(item.getCategory());
                    setGraphic(container);
                }
            }
        });
    }

    @FXML
    private void handleBack(ActionEvent event) {
        navigateTo("/com/amazi/view/DashBoard.fxml", "Dashboard", event);
    }

    @FXML
    private void handleFinalSubmit(ActionEvent event) {
        Submission selected = portfolioListView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showNotify("Warning", "Please select an item from the list first!", Alert.AlertType.WARNING);
            return;
        }

        showNotify("Success", "Submission '" + selected.getTitle() + "' has been sent!", Alert.AlertType.INFORMATION);
    }

    private void showNotify(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void navigateTo(String fxmlPath, String title, ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("FXML file not found: " + fxmlPath);
                return;
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 850, 700));
            stage.setTitle(title);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}