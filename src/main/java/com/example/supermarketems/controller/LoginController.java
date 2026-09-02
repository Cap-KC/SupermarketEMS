package com.example.supermarketems.controller;

import com.example.supermarketems.dao.EmployeeDAO;
import com.example.supermarketems.model.Employee;
import com.example.supermarketems.util.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // 1. Basic UI Validation
        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            statusLabel.setText("Please enter both email and password.");
            return;
        }

        // 2. Authenticate against MySQL via EmployeeDAO
        Employee loggedInEmployee = employeeDAO.authenticate(email, password);

        if (loggedInEmployee != null) {
            // Save global session reference
            UserSession.setCurrentUser(loggedInEmployee);

            statusLabel.setStyle("-fx-text-fill: #27ae60;");
            statusLabel.setText("Login successful! Redirecting...");

            // 3. Navigate to Dashboard based on Role
            openDashboard(loggedInEmployee);
        } else {
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            statusLabel.setText("Invalid email or password.");
        }
    }

    private void openDashboard(Employee employee) {
        try {
            String role = employee.getEmployeeType() != null ? employee.getEmployeeType().toUpperCase() : "";
            Stage stage = (Stage) emailField.getScene().getWindow();

            String fxmlPath = switch (role) {
                case "ADMIN" -> "/com/example/supermarketems/admin-dashboard-view.fxml";
                case "MANAGER" -> "/com/example/supermarketems/manager-dashboard-view.fxml";
                case "PERMANENT", "OUTSOURCE" -> "/com/example/supermarketems/employee-dashboard-view.fxml";
                default -> null;
            };

            if (fxmlPath == null) {
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                statusLabel.setText("Unrecognized role type assigned to user: " + role);
                return;
            }

            // Verify resource existence before loading
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                statusLabel.setText("FXML view file not found: " + fxmlPath);
                System.err.println("=== FXML MISSING ===");
                System.err.println("Could not resolve path: " + fxmlPath);
                System.err.println("Ensure file exists in src/main/resources" + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Dependency injection into view controllers
            switch (role) {
                case "ADMIN" -> {
                    AdminDashboardController controller = loader.getController();
                    controller.setLoggedInEmployee(employee);
                }
                case "MANAGER" -> {
                    ManagerDashboardController controller = loader.getController();
                    controller.setLoggedInEmployee(employee);
                }
                case "PERMANENT", "OUTSOURCE" -> {
                    // ✅ FIXED: Cast to PermanentEmployeeDashboardController
                    PermanentEmployeeDashboardController controller = loader.getController();
                    controller.setLoggedInEmployee(employee);
                }
            }

            stage.setScene(new Scene(root, 1100, 750));
            stage.setTitle("Supermarket EMS - " + role + " Portal");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
            statusLabel.setText("Error loading dashboard view: " + e.getMessage());
        }
    }
}