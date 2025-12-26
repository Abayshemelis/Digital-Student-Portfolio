package com.amazi.controller;

import com.amazi.model.User;
import com.amazi.service.DataManager;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminDashBoardController {

    private static final Logger LOGGER = Logger.getLogger(AdminDashBoardController.class.getName());

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;

    @FXML private TextField searchField;
    @FXML private Label userCountLabel;

    @FXML private Label totalStudentsLabel;
    @FXML private Label totalFacultyLabel;
    @FXML private Label totalAdminLabel;
    @FXML private Label pendingSubmissionsLabel;

    private ObservableList<User> masterData = FXCollections.observableArrayList();
    private FilteredList<User> filteredData;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("userID"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Add Double-Click Listener for Rows
        userTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    handleOpenUserProfile(row.getItem());
                }
            });
            return row;
        });

        refreshTableData();
    }

    private void refreshTableData() {
        masterData = FXCollections.observableArrayList(DataManager.getAllUsers());
        filteredData = new FilteredList<>(masterData, p -> true);

        // Expression Lambda for Search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(newVal));

        SortedList<User> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(userTable.comparatorProperty());
        userTable.setItems(sortedData);

        updateUserCount(filteredData.size());
        updateStatistics();
    }

    private void applyFilter(String searchText) {
        filteredData.setPredicate(user -> {
            if (searchText == null || searchText.isEmpty()) return true;
            String lowerFilter = searchText.toLowerCase();
            return user.getName().toLowerCase().contains(lowerFilter) ||
                    user.getEmail().toLowerCase().contains(lowerFilter);
        });
        updateUserCount(filteredData.size());
    }

    @FXML
    private void handleShowPendingOnly() {
        filteredData.setPredicate(user -> "Pending".equalsIgnoreCase(user.getRole()));
        updateUserCount(filteredData.size());
        searchField.setPromptText("Viewing Pending Reviews...");
        showToast("Filtering: Pending Users");
    }

    @FXML
    private void handleResetFilter() {
        searchField.clear();
        searchField.setPromptText("Quick search...");
        filteredData.setPredicate(user -> true);
        updateUserCount(filteredData.size());
        showToast("List Reset");
    }

    @FXML
    private void handleApproveUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null && "Pending".equalsIgnoreCase(selected.getRole())) {
            selected.setRole("Student");
            // DataManager.updateUser(selected); // Logic to save to DB
            refreshTableData();
            showToast("✓ Approved: " + selected.getName());
        } else {
            showToast("Please select a Pending user");
        }
    }

    @FXML
    private void handleRejectUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Reject and delete " + selected.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                DataManager.deleteUser(selected);
                refreshTableData();
                showToast("User Rejected");
            }
        });
    }

    @FXML
    private void handleExportData() {
        ObservableList<User> items = userTable.getItems();
        if (items.isEmpty()) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export User Directory");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(userTable.getScene().getWindow());

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("ID,Name,Email,Role");
                for (User u : items) {
                    writer.printf("%d,%s,%s,%s%n", u.getUserID(), u.getName(), u.getEmail(), u.getRole());
                }
                showToast("✓ Data Exported");
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Export failed", e);
            }
        }
    }

    private void handleOpenUserProfile(User user) {
        showToast("Opening profile: " + user.getName());
        // Logic to open Profile FXML would go here
    }

    private void updateStatistics() {
        long studentCount = masterData.stream().filter(u -> "Student".equalsIgnoreCase(u.getRole())).count();
        long facultyCount = masterData.stream().filter(u -> "Faculty".equalsIgnoreCase(u.getRole())).count();
        long adminCount = masterData.stream().filter(u -> "Admin".equalsIgnoreCase(u.getRole())).count();
        long pendingCount = masterData.stream().filter(u -> "Pending".equalsIgnoreCase(u.getRole())).count();

        if (totalStudentsLabel != null) totalStudentsLabel.setText(String.valueOf(studentCount));
        if (totalFacultyLabel != null) totalFacultyLabel.setText(String.valueOf(facultyCount));
        if (totalAdminLabel != null) totalAdminLabel.setText(String.valueOf(adminCount));
        if (pendingSubmissionsLabel != null) pendingSubmissionsLabel.setText(String.valueOf(pendingCount));
    }

    @FXML
    private void handleDeleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a user.");
            return;
        }
        if ("Admin".equalsIgnoreCase(selected.getRole())) {
            showAlert(Alert.AlertType.ERROR, "Denied", "Admins cannot be deleted.");
            return;
        }
        DataManager.deleteUser(selected);
        refreshTableData();
        showToast("User Deleted");
    }

    private void showToast(String message) {
        Stage stage = (Stage) userTable.getScene().getWindow();
        Stage toastStage = new Stage();
        toastStage.initOwner(stage);
        toastStage.initStyle(StageStyle.TRANSPARENT);

        Label label = new Label(message);
        label.getStyleClass().add("toast-label");
        StackPane root = new StackPane(label);
        root.getStyleClass().add("toast-root");
        root.setOpacity(0);

        Scene scene = new Scene(root);
        scene.setFill(null);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        toastStage.setScene(scene);
        toastStage.show();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
        fadeIn.setFromValue(0); fadeIn.setToValue(0.9);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), root);
        fadeOut.setFromValue(0.9); fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(1.5));
        fadeOut.setOnFinished(e -> toastStage.close());
        fadeIn.play(); fadeOut.play();
    }

    private void updateUserCount(int count) {
        if (userCountLabel != null) userCountLabel.setText("Total Entries: " + count);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/amazi/view/Login.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Logout failed", e);
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