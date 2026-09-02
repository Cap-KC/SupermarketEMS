package com.example.supermarketems;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // 1. Load login-view.fxml instead of hello-view.fxml
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));

        // 2. Adjust window dimensions to fit the login layout nicely (380x450)
        Scene scene = new Scene(fxmlLoader.load(), 380, 450);

        // 3. Update title
        stage.setTitle("Supermarket EMS - Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
