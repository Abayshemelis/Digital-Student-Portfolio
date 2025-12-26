package com.amazi.controller;

import com.amazi.model.Submission;
import com.amazi.service.DataManager;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

@SuppressWarnings("all")
public class FacultyReviewController {

    @FXML private TableView<Submission> submissionTable;
    @FXML private TableColumn<Submission, String> colStudent, colTitle, colStatus;
    @FXML private TextField searchField, gradeField;
    @FXML private TextArea feedbackArea;
    @FXML private Label studentLabel, titleLabel;
    @FXML private VBox detailsPane;
    @FXML private Circle statusCircle;

    private Submission selectedSubmission;

    @FXML
    public void initialize() {
        // 1. Column Mapping
        colStudent.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 2. Search Logic (Filtering based on DataManager list)
        FilteredList<Submission> filteredData = new FilteredList<>(DataManager.getAllSubmissions(), p -> true);

        searchField.textProperty().addListener((obs, old, newVal) -> {
            filteredData.setPredicate(s -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return s.getStudentName().toLowerCase().contains(lower) ||
                        s.getTitle().toLowerCase().contains(lower);
            });
        });

        submissionTable.setItems(filteredData);

        // 3. Selection Logic: Populate the Evaluation Toolkit
        submissionTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                showDetails(newVal);
            } else {
                hideDetails();
            }
        });

        // Online Status Indicator
        if (statusCircle != null) {
            statusCircle.setFill(javafx.scene.paint.Color.web("#10b981")); // Success Green
        }
    }

    private void showDetails(Submission s) {
        selectedSubmission = s;
        if (detailsPane != null) {
            detailsPane.setVisible(true);
            detailsPane.setManaged(true);
        }

        studentLabel.setText(s.getStudentName());
        titleLabel.setText(s.getTitle());

        feedbackArea.setText(s.getFeedback() != null ? s.getFeedback() : "");
        gradeField.setText(s.getGrade() != null ? s.getGrade() : "");
    }

    private void hideDetails() {
        if (detailsPane != null) {
            detailsPane.setVisible(false);
            detailsPane.setManaged(false);
        }
    }

    // Toolkit Button Actions
    @FXML private void handleApprove() { updateSubmission("APPROVED"); }
    @FXML private void handleReject() { updateSubmission("REJECTED"); }
    @FXML private void handleRevision() { updateSubmission("REVISION"); }

    private void updateSubmission(String newStatus) {
        if (selectedSubmission == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a submission to evaluate.");
            return;
        }

        // 1. Update the Model with Toolkit data
        selectedSubmission.setStatus(newStatus);
        selectedSubmission.setFeedback(feedbackArea.getText());
        selectedSubmission.setGrade(gradeField.getText().toUpperCase());

        // 2. Persist to DataManager (Writes to submissions.txt)
        DataManager.updateSubmissionInFile(selectedSubmission);

        // 3. UI Refresh
        submissionTable.refresh();
        clearToolkit();

        showAlert(Alert.AlertType.INFORMATION, "Success", "Evaluation for " + selectedSubmission.getStudentName() + " has been published!");
    }

    private void clearToolkit() {
        hideDetails();
        submissionTable.getSelectionModel().clearSelection();
        selectedSubmission = null;
        feedbackArea.clear();
        gradeField.clear();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        navigateTo("/com/amazi/view/Login.fxml", "Login", event);
    }

    private void navigateTo(String fxml, String title, ActionEvent event) {
        try {
            URL loc = getClass().getResource(fxml);
            Parent root = FXMLLoader.load(loc);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AMAZI | " + title);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}