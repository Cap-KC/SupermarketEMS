package com.example.supermarketems.util;

import com.example.supermarketems.model.Employee;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class SceneNavigator {

    public static void navigateToDashboard(ActionEvent event, Employee employee) {
        if (employee == null) {
            System.err.println("[SceneNavigator Warning] Session employee is null. Defaulting to Admin Dashboard.");
            switchScene(event, "/com/example/supermarketems/admin-dashboard-view.fxml", "Supermarket EMS - Admin Portal", null);
            return;
        }

        String role = employee.getEmployeeType() != null ? employee.getEmployeeType().toUpperCase().trim() : "";

        if ("MANAGER".equals(role)) {
            switchScene(event, "/com/example/supermarketems/manager-dashboard-view.fxml", "Supermarket EMS - Manager Portal", employee);
        } else if ("ADMIN".equals(role)) {
            switchScene(event, "/com/example/supermarketems/admin-dashboard-view.fxml", "Supermarket EMS - Admin Portal", employee);
        } else {
            switchScene(event, "/com/example/supermarketems/employee-dashboard-view.fxml", "Supermarket EMS - Employee Portal", employee);
        }
    }

    public static void switchScene(ActionEvent event, String fxmlPath, String title, Employee employee) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Clean up resources (e.g. webcam thread) from the current controller before switching
            if (stage.getScene() != null && stage.getScene().getRoot() != null) {
                Object currentController = stage.getScene().getUserData();
                cleanupController(currentController);
            }

            URL resource = SceneNavigator.class.getResource(fxmlPath);
            if (resource == null) {
                System.err.println("[SceneNavigator Error] Cannot find FXML file at path: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            // Inject the employee session into the target controller if present
            Object targetController = loader.getController();
            if (targetController != null && employee != null) {
                try {
                    targetController.getClass().getMethod("setLoggedInEmployee", Employee.class).invoke(targetController, employee);
                } catch (NoSuchMethodException e) {
                    // Target controller does not need setLoggedInEmployee - safe to ignore
                }
            }

            // Create new scene and store controller reference in scene userData for future cleanup
            Scene scene = new Scene(root);
            scene.setUserData(targetController);

            // Set stage close listener to release background hardware resources when window 'X' is clicked
            stage.setOnCloseRequest(windowEvent -> cleanupController(targetController));

            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("[SceneNavigator Error] Failed to load layout file: " + fxmlPath);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[SceneNavigator Error] Unexpected error during navigation:");
            e.printStackTrace();
        }
    }

    /**
     * Safely triggers cleanup methods (such as stopWebcam()) on controller departure or app close
     */
    private static void cleanupController(Object controller) {
        if (controller == null) return;

        try {
            controller.getClass().getMethod("stopWebcam").invoke(controller);
        } catch (NoSuchMethodException e) {
            // Controller has no webcam resources to clean up - normal operation
        } catch (Exception e) {
            System.err.println("[SceneNavigator Warning] Failed to invoke stopWebcam on controller: " + e.getMessage());
        }
    }

    public static void logout(ActionEvent event) {
        switchScene(event, "/com/example/supermarketems/login-view.fxml", "Supermarket EMS - Login", null);
    }
}