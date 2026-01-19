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

@SuppressWarnings("all")
public class FacultyReviewController {

    private static final Logger LOGGER = Logger.getLogger(FacultyReviewController.class.getName());

    @FXML private TableView<Submission> submissionTable;
    @FXML private TableColumn<Submission, String> colStudent, colTitle, colStatus;
    @FXML private TextField searchField, gradeField;
    @FXML private TextField creditField;
    @FXML private TextArea feedbackArea;
    @FXML private TextArea submissionDescriptionArea;
    @FXML private ScrollPane scrollWrapper;

    @FXML private Label studentLabel, titleLabel;
    @FXML private VBox detailsPane;
    @FXML private Circle statusCircle;
    @FXML private Label orgLabel;
    @FXML private Label emailLabel;
    @FXML private Button logoutButton;

    private Submission selectedSubmission;

    @FXML
    public void initialize() {
        colStudent.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        ObservableList<Submission> masterData = DataManager.getAllSubmissions();
        FilteredList<Submission> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((obs, old, newVal) -> filteredData.setPredicate(s -> {
            if (newVal == null || newVal.isEmpty()) return true;
            String lower = newVal.toLowerCase();
            return s.getStudentName().toLowerCase().contains(lower) ||
                    s.getTitle().toLowerCase().contains(lower);
        }));

        submissionTable.setItems(filteredData);

        submissionTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) showDetails(newVal);
            else hideDetails();
        });

        if (submissionDescriptionArea != null) submissionDescriptionArea.setWrapText(true);
        if (feedbackArea != null) feedbackArea.setWrapText(true);

        if (statusCircle != null) {
            statusCircle.setFill(javafx.scene.paint.Color.web("#10b981"));
        }
    }

    private void showDetails(Submission s) {
        selectedSubmission = s;
        if (scrollWrapper != null) {
            scrollWrapper.setVisible(true);
            scrollWrapper.setManaged(true);
        }

        studentLabel.setText(s.getStudentName());
        titleLabel.setText(s.getTitle());

        if (submissionDescriptionArea != null) {
            submissionDescriptionArea.setText(s.getDescription() != null ? s.getDescription() : "No description provided.");
            submissionDescriptionArea.setEditable(false);
        }

        orgLabel.setText(s.getOrganizationName() != null && !s.getOrganizationName().isEmpty() ? s.getOrganizationName() : "N/A");
        emailLabel.setText(s.getEmail() != null && !s.getEmail().isEmpty() ? s.getEmail() : "N/A");
        feedbackArea.setText(s.getFeedback() != null ? s.getFeedback() : "");
        gradeField.setText(s.getGrade() != null ? s.getGrade() : "");

        // Load existing credit hours if already assigned
        if (creditField != null) {
            creditField.setText(s.getCreditHours() != null ? s.getCreditHours() : "");
        }
    }

    private void hideDetails() {
        if (scrollWrapper != null) {
            scrollWrapper.setVisible(false);
            scrollWrapper.setManaged(false);
        }
    }

    @FXML private void handleApprove() { updateSubmission("APPROVED"); }
    @FXML private void handleReject() { updateSubmission("REJECTED"); }
    @FXML private void handleRevision() { updateSubmission("REVISION"); }

    private void updateSubmission(String newStatus) {
        if (selectedSubmission == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a record.");
            return;
        }

        // 1. Capture data from UI to the object
        selectedSubmission.setStatus(newStatus);
        selectedSubmission.setFeedback(feedbackArea.getText());
        selectedSubmission.setGrade(gradeField.getText().toUpperCase());

        // Save Credit Hours to the submission object
        if (creditField != null) {
            selectedSubmission.setCreditHours(creditField.getText());
        }

        // 2. Push update to DataManager (which writes to file)
        DataManager.updateSubmissionInFile(selectedSubmission);

        // 3. Force the table to visually update the status column
        submissionTable.refresh();

        // 4. Alert success and reset
        showAlert(Alert.AlertType.INFORMATION, "Update Successful",
                "Record for " + selectedSubmission.getStudentName() + " has been updated.");

        clearToolkit();
    }

    private void clearToolkit() {
        hideDetails();
        submissionTable.getSelectionModel().clearSelection();
        selectedSubmission = null;
        feedbackArea.clear();
        gradeField.clear();
        if(creditField != null) creditField.clear();
        if(submissionDescriptionArea != null) submissionDescriptionArea.clear();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        navigateTo("/com/amazi/view/Login.fxml", "Sign-In", event);
    }

    private void navigateTo(String fxml, String title, ActionEvent event) {
        try {
            URL loc = getClass().getResource(fxml);
            if (loc == null) return;
            Parent root = FXMLLoader.load(loc);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            URL css = getClass().getResource("/com/amazi/view/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene);
            stage.setTitle("FACULTY | " + title);
            if (fxml.contains("Login.fxml")) {
                stage.setFullScreen(false);
            } else {
                stage.setMaximized(true);
                stage.setFullScreen(true);
                stage.setFullScreenExitHint("");
            }
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Navigation failed", e);
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