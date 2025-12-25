package com.amazi.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class SettingController {
    @FXML private TextField usernameField;

    @FXML
    private void handleSaveSettings() {
        System.out.println("Settings Saved!");
    }
}