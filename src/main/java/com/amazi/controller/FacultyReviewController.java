package com.amazi.controller;

import com.amazi.model.Submission;
import com.amazi.service.DataManager;
import javafx.collections.ObservableList;
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
import java.util.logging.Level;
import java.util.logging.Logger;
@SuppressWarnings("unused")
public class FacultyReviewController {

    private static final Logger LOGGER = Logger.getLogger(FacultyReviewController.class.getName());

    @FXML private TableView<Submission> submissionTable;
    @FXML private TableColumn<Submission, String> colStudent, colTitle, colStatus;
    @FXML private TextField searchField, gradeField;
    @FXML private TextArea feedbackArea;
    @FXML private Label studentLabel, titleLabel;
    @FXML private VBox detailsPane;
    @FXML private Circle statusCircle;
    @FXML private Label orgLabel;
    @FXML private Label emailLabel;
    @FXML private Button logoutButton;

    private Submission selectedSubmission;

    @FXML
    public void initialize() {
        // 1. Column Mapping
        colStudent.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 2. Data Sync
        ObservableList<Submission> masterData = DataManager.getAllSubmissions();
        FilteredList<Submission> filteredData = new FilteredList<>(masterData, p -> true);

        // Expression Lambda for search filtering
        searchField.textProperty().addListener((obs, old, newVal) -> filteredData.setPredicate(s -> {
            if (newVal == null || newVal.isEmpty()) return true;
            String lower = newVal.toLowerCase();
            return s.getStudentName().toLowerCase().contains(lower) ||
                    s.getTitle().toLowerCase().contains(lower);
        }));

        submissionTable.setItems(filteredData);

        // 3. Selection Logic
        submissionTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) showDetails(newVal);
            else hideDetails();
        });

        // UI Enhancement: Status Indicator
        if (statusCircle != null) {
            statusCircle.setFill(javafx.scene.paint.Color.web("#10b981"));
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

        orgLabel.setText(s.getOrganizationName() != null && !s.getOrganizationName().isEmpty()
                ? s.getOrganizationName() : "N/A");

        emailLabel.setText(s.getEmail() != null && !s.getEmail().isEmpty()
                ? s.getEmail() : "N/A");

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
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a submission.");
            return;
        }

        selectedSubmission.setStatus(newStatus);
        selectedSubmission.setFeedback(feedbackArea.getText());
        selectedSubmission.setGrade(gradeField.getText().toUpperCase());

        DataManager.updateSubmissionInFile(selectedSubmission);
        submissionTable.refresh();
        clearToolkit();

        showAlert(Alert.AlertType.INFORMATION, "Success", "Evaluation Published!");
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
        navigateTo("/com/amazi/view/Login.fxml", "Sign-In", event);
    }

    /**
     * Standardized navigation with robust logging.
     */
    private void navigateTo(String fxml, String title, ActionEvent event) {
        try {
            URL loc = getClass().getResource(fxml);
            if (loc == null) {
                LOGGER.log(Level.SEVERE, "FXML file not found at: {0}", fxml);
                return;
            }

            Parent root = FXMLLoader.load(loc);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);


            URL css = getClass().getResource("/com/amazi/view/style.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }

            stage.setScene(scene);
            stage.setTitle("STUDENT| " + title);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Navigation failed to {0}", fxml);
            LOGGER.log(Level.FINE, "Trace: ", e);
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