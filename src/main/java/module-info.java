module com.example.supermarketems {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // Required for SwingFXUtils and BufferedImage support
    requires java.desktop;
    requires javafx.swing;

    // Webcam capture hardware support
    requires webcam.capture;

    // Google ZXing QR reading engine
    requires com.google.zxing;
    requires com.google.zxing.javase;

    // Allow JavaFX FXML to reflectively access your controllers
    opens com.example.supermarketems.controller to javafx.fxml;
    exports com.example.supermarketems.controller;

    // Allow JavaFX FXML access to your main package
    opens com.example.supermarketems to javafx.fxml;
    exports com.example.supermarketems;

    // CRITICAL FIX: Open model package to javafx.base so TableView can access getters via reflection
    opens com.example.supermarketems.model to javafx.base, javafx.fxml;
    exports com.example.supermarketems.model;

    exports com.example.supermarketems.dao;
}