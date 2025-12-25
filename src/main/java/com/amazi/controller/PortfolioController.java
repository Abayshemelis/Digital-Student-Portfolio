package com.amazi.controller;

import com.amazi.model.Submission;
import com.amazi.service.DataManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PortfolioController {

    private static final Logger LOGGER = Logger.getLogger(PortfolioController.class.getName());
    private static final String STUDENT_NAME_KEY = "Abay Shimelis";

    // --- FXML UI Components (Resolves "Unresolved fx:id" errors) ---
    @FXML private ListView<Submission> portfolioListView; // Used in MyPortfolio view
    @FXML private TextField titleField;                   // Used in CreatePortfolio view
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<String> courseComboBox;
    @FXML private ComboBox<String> assignmentComboBox;
    @FXML private DatePicker submissionDatePicker;
    @FXML private Label fileNameLabel;

    @FXML
    public void initialize() {
        // Initialize List if present
        if (portfolioListView != null) {
            loadStudentSubmissions();
        }

        // Initialize Form if present
        if (courseComboBox != null) {
            courseComboBox.getItems().setAll("Computer Science", "Engineering", "Art", "Business");
        }
        if (assignmentComboBox != null) {
            assignmentComboBox.getItems().setAll("Project", "Assignment", "Thesis", "Lab Report");
        }
        if (submissionDatePicker != null) {
            submissionDatePicker.setValue(LocalDate.now());
        }
    }

    // --- Event Handlers (Resolves "Cannot resolve symbol" errors) ---

    @FXML
    private void handleFileUpload(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Project File");
        File file = fc.showOpenDialog(((Node) event.getSource()).getScene().getWindow());
        if (file != null && fileNameLabel != null) {
            fileNameLabel.setText(file.getName());
        }
    }

    @FXML
    private void handlePublish(ActionEvent event) {
        if (titleField == null || titleField.getText().isEmpty()) {
            showSimpleAlert("Validation Error", "Please enter a project title.");
            return;
        }

        Submission newSubmission = new Submission(
                titleField.getText(),
                courseComboBox.getValue() != null ? courseComboBox.getValue() : "General",
                assignmentComboBox.getValue() != null ? assignmentComboBox.getValue() : "Project",
                "Academic",
                submissionDatePicker.getValue() != null ? submissionDatePicker.getValue() : LocalDate.now(),
                descriptionArea.getText(),
                fileNameLabel != null ? fileNameLabel.getText() : "No file",
                "SUBMITTED",
                STUDENT_NAME_KEY
        );

        DataManager.addSubmission(newSubmission);
        handleBack(event); // Return to dashboard after saving
    }

    @FXML
    private void handleDiscard(ActionEvent event) {
        handleBack(event);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        switchScene("/com/amazi/view/DashBoard.fxml", "Dashboard", event);
    }

    // --- Navigation Logic ---

    private void switchScene(String fxmlPath, String windowTitle, ActionEvent event) {
        try {
            URL res = getClass().getResource(fxmlPath);
            if (res == null) {
                LOGGER.severe("Path missing: " + fxmlPath);
                return;
            }

            Parent root = FXMLLoader.load(res);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            // Apply CSS if exists
            URL css = getClass().getResource("/com/amazi/view/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());

            stage.setScene(scene);
            stage.setTitle("AMAZI | " + windowTitle);
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Navigation Error", e);
        }
    }

    private void loadStudentSubmissions() {
        List<Submission> allData = DataManager.getAllSubmissions();
        ObservableList<Submission> studentData = FXCollections.observableArrayList(
                allData.stream()
                        .filter(s -> s.getStudentName().equalsIgnoreCase(STUDENT_NAME_KEY))
                        .toList()
        );
        portfolioListView.setItems(studentData);
        setupCellFactory();
    }

    private void setupCellFactory() {
        portfolioListView.setCellFactory(lv -> new ListCell<>() {
            private final HBox root = new HBox(15);
            private final VBox textGroup = new VBox(5);
            private final Label title = new Label();
            private final Label status = new Label();
            private final Label info = new Label();

            {
                title.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
                status.setStyle("-fx-font-size: 11px; -fx-padding: 2 8; -fx-background-radius: 10;");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                textGroup.getChildren().addAll(title, info);
                root.getChildren().addAll(textGroup, spacer, status);
                root.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(Submission item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    title.setText(item.getTitle());
                    info.setText(item.getCourse() + " | " + item.getCompletionDate());
                    status.setText(item.getStatus());

                    if ("APPROVED".equals(item.getStatus())) {
                        status.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 2 8; -fx-background-radius: 10;");
                    } else {
                        status.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-padding: 2 8; -fx-background-radius: 10;");
                    }
                    setGraphic(root);
                }
            }
        });
    }

    private void showSimpleAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}