package com.example.supermarketems.controller;

import com.example.supermarketems.dao.AttendanceDAO;
import com.example.supermarketems.dao.LeaveRequestDAO;
import com.example.supermarketems.model.Attendance;
import com.example.supermarketems.model.Employee;
import com.example.supermarketems.model.LeaveRequest;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PermanentEmployeeDashboardController {

    // --- Header & Profile ---
    @FXML private Label welcomeLabel;
    @FXML private Label empIdLabel;
    @FXML private Label designationLabel;

    // --- Metric Cards ---
    @FXML private Label statusLabel;
    @FXML private Label monthlyHoursLabel;
    @FXML private Label remainingLeaveLabel;

    // --- Action Buttons ---
    @FXML private Button clockInBtn;
    @FXML private Button clockOutBtn;

    // --- Recent Attendance Table ---
    @FXML private TableView<Attendance> attendanceTable;
    @FXML private TableColumn<Attendance, String> colClockIn;
    @FXML private TableColumn<Attendance, String> colClockOut;
    @FXML private TableColumn<Attendance, Double> colOvertime;

    // --- Pending Leave Table ---
    @FXML private TableView<LeaveRequest> leaveTable;
    @FXML private TableColumn<LeaveRequest, String> colLeaveType;
    @FXML private TableColumn<LeaveRequest, String> colStartDate;
    @FXML private TableColumn<LeaveRequest, String> colEndDate;
    @FXML private TableColumn<LeaveRequest, String> colStatus;

    private Employee loggedInEmployee;
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final LeaveRequestDAO leaveDAO = new LeaveRequestDAO();

    @FXML
    public void initialize() {
        setupTables();
    }

    /**
     * Injects the logged-in permanent employee from LoginController.
     */
    public void setLoggedInEmployee(Employee employee) {
        this.loggedInEmployee = employee;
        if (employee != null) {
            welcomeLabel.setText("Welcome Back 👋 " + employee.getName());
            if (empIdLabel != null) empIdLabel.setText("ID: " + employee.getEmployeeId());
            if (designationLabel != null) designationLabel.setText("Role: " + (employee.getDesignation() != null ? employee.getDesignation() : "Permanent Staff"));

            loadDashboardData();
        }
    }

    private void setupTables() {
        // Attendance Table Property Bindings
        if (colClockIn != null) colClockIn.setCellValueFactory(new PropertyValueFactory<>("clockInTime"));
        if (colClockOut != null) colClockOut.setCellValueFactory(new PropertyValueFactory<>("clockOutTime"));
        if (colOvertime != null) colOvertime.setCellValueFactory(new PropertyValueFactory<>("overtimeHours"));

        // Leave Table Property Bindings
        if (colLeaveType != null) colLeaveType.setCellValueFactory(new PropertyValueFactory<>("leaveType"));
        if (colStartDate != null) colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        if (colEndDate != null) colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        if (colStatus != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadDashboardData() {
        if (loggedInEmployee == null) return;

        String empId = loggedInEmployee.getEmployeeId();

        // 1. Load Attendance Records
        List<Attendance> myLogs = attendanceDAO.getAttendanceByEmployeeId(empId);
        if (attendanceTable != null && myLogs != null) {
            attendanceTable.setItems(FXCollections.observableArrayList(myLogs));
        }

        // Check if currently clocked in
        boolean isClockedIn = attendanceDAO.hasActiveClockIn(empId);
        updateClockButtons(isClockedIn);

        // 2. Load Leave Requests
        List<LeaveRequest> myLeaves = leaveDAO.getLeaveRequestsByEmployeeId(empId);
        if (leaveTable != null && myLeaves != null) {
            leaveTable.setItems(FXCollections.observableArrayList(myLeaves));
        }

        // 3. Update Metric Cards
        if (statusLabel != null) {
            statusLabel.setText(isClockedIn ? "ON SHIFT" : "OFF SHIFT");
            statusLabel.setStyle(isClockedIn ? "-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #16a34a;"
                    : "-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #0284c7;");
        }

        if (remainingLeaveLabel != null) {
            remainingLeaveLabel.setText(String.valueOf(leaveDAO.getRemainingLeaveDays(empId)));
        }
    }

    private void updateClockButtons(boolean isClockedIn) {
        if (clockInBtn != null) clockInBtn.setDisable(isClockedIn);
        if (clockOutBtn != null) clockOutBtn.setDisable(!isClockedIn);
    }

    // ==========================================
    // ACTION HANDLERS
    // ==========================================

    @FXML
    private void handleClockIn(ActionEvent event) {
        if (loggedInEmployee == null) return;

        boolean success = attendanceDAO.clockIn(loggedInEmployee.getEmployeeId());
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Clock In Successful", "You have clocked in at " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            loadDashboardData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Clock In Failed", "Unable to record clock-in time.");
        }
    }

    @FXML
    private void handleClockOut(ActionEvent event) {
        if (loggedInEmployee == null) return;

        boolean success = attendanceDAO.clockOut(loggedInEmployee.getEmployeeId());
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Clock Out Successful", "You have clocked out successfully.");
            loadDashboardData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Clock Out Failed", "Unable to record clock-out time.");
        }
    }

    @FXML
    private void handleRequestLeave(ActionEvent event) {
        openModalDialog(event, "/com/example/supermarketems/employee-leave-request-view.fxml", "Apply for Leave");
        loadDashboardData(); // Refresh tables upon modal close
    }

    @FXML
    private void handleViewPayslips(ActionEvent event) {
        openModalDialog(event, "/com/example/supermarketems/employee-payslips-view.fxml", "My Payslips");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        this.loggedInEmployee = null;
        switchScene(event, "/com/example/supermarketems/login-view.fxml", "Supermarket EMS - Login");
    }

    private void openModalDialog(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object targetController = loader.getController();
            if (targetController != null && loggedInEmployee != null) {
                try {
                    var setEmployeeMethod = targetController.getClass().getDeclaredMethod("setLoggedInEmployee", Employee.class);
                    setEmployeeMethod.setAccessible(true);
                    setEmployeeMethod.invoke(targetController, loggedInEmployee);
                } catch (Exception ignored) {}
            }

            Stage parentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(parentStage);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Loading View", "Could not load " + fxmlPath + "\nEnsure file exists in resources.");
        }
    }

    private void switchScene(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 750));
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Switching Scene", "Could not load " + fxmlPath);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}