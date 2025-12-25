package com.amazi.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

 public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // 1. FIXED PATH: Match your actual filename DashBoard.fxml (Capital B)
        URL fxmlLocation = getClass().getResource("/com/amazi/view/Login.fxml");

        if (fxmlLocation == null) {
            System.err.println("FATAL ERROR: Java cannot find Login.fxml at /com/amazi/view/Login.fxml");
            return;
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();
        Scene scene = new Scene(root );

        // 2. FIXED CSS PATH: Match your /css/style.css folder in the screenshot
        URL cssLocation = getClass().getResource("/css/style.css");
        if (cssLocation != null) {
            scene.getStylesheets().add(cssLocation.toExternalForm());
            System.out.println("CSS Loaded Successfully!");
        } else {
            System.err.println("ERROR: CSS file not found at /css/style.css");
        }

        stage.setTitle("Digital Student Portfolio");

        stage.setScene(scene);

        stage.show();
    }

     public static void main(String[] args) {
        launch(args);
    }
}