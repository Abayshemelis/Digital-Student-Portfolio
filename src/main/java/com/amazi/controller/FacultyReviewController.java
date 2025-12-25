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

@SuppressWarnings("all")
public class FacultyReviewController {

    @FXML private TableView<Submission> submissionTable;
    @FXML private TableColumn<Submission, String> colStudent, colTitle, colStatus;
    @FXML private TextField searchField, gradeField;
    @FXML private TextArea feedbackArea;
    @FXML private Label studentLabel, titleLabel; // Matches Abay Shimelis / plan designing labels
    @FXML private VBox detailsPane; // This is your "Evaluation Toolkit" container
    @FXML private Circle statusCircle;

    private Submission selectedSubmission;

    @FXML
    public void initialize() {
        // 1. Column Mapping - Matches the STUDENT, PROJECT, STATUS headers in your image
        colStudent.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 2. Search Logic (Matching the "Search student..." field in top right)
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
            }
        });

        // Set active indicator color (the green dot in your top right)
        if (statusCircle != null) statusCircle.setFill(javafx.scene.paint.Color.web("#10b981"));
    }

    private void showDetails(Submission s) {
        selectedSubmission = s;
        detailsPane.setVisible(true);
        detailsPane.setManaged(true);

        // Setting text to match the image hierarchy
        studentLabel.setText(s.getStudentName()); // e.g., "Ben Carter"
        titleLabel.setText(s.getTitle());         // e.g., "Intro to AI"

        feedbackArea.setText(s.getFeedback() != null ? s.getFeedback() : "");
        gradeField.setText(s.getGrade() != null ? s.getGrade() : "");
    }

    // Button Actions from your Toolkit
    @FXML private void handleApprove() { updateSubmission("APPROVED"); }
    @FXML private void handleReject() { updateSubmission("REJECTED"); }
    @FXML private void handleRevision() { updateSubmission("NEEDS REVISION"); }

    private void updateSubmission(String newStatus) {
        if (selectedSubmission == null) return;

        // 1. Update the Model
        selectedSubmission.setStatus(newStatus);
        selectedSubmission.setFeedback(feedbackArea.getText());
        selectedSubmission.setGrade(gradeField.getText().toUpperCase());
        selectedSubmission.setViewedByStudent(false);

        // 2. Save to DataManager (This makes it appear on the Student side)
        DataManager.updateSubmissionInFile(selectedSubmission);

        // 3. UI Refresh
        submissionTable.refresh();
        clearToolkit();

        // Optional: Confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Evaluation Published!");
        alert.setHeaderText(null);
        alert.show();
    }

    private void clearToolkit() {
        detailsPane.setVisible(false);
        detailsPane.setManaged(false);
        submissionTable.getSelectionModel().clearSelection();
        selectedSubmission = null;
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/amazi/view/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("AMAZI | Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}