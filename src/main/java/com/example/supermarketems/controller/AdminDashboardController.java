package com.example.supermarketems.controller;

import com.example.supermarketems.dao.AttendanceDAO;
import com.example.supermarketems.dao.EmployeeDAO;
import com.example.supermarketems.dao.LeaveRequestDAO;
import com.example.supermarketems.model.Attendance;
import com.example.supermarketems.model.Employee;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

public class AdminDashboardController {

    // --- FXML Bindings for Metric Labels ---
    @FXML private Label welcomeLabel;
    @FXML private Label totalEmployeesLabel;
    @FXML private Label activeShiftLabel;
    @FXML private Label pendingLeavesLabel;

    // --- FXML Bindings for Recent Activity Table ---
    @FXML private TableView<Attendance> timeLogTable;
    @FXML private TableColumn<Attendance, String> colLogId;
    @FXML private TableColumn<Attendance, String> colEmployeeId;
    @FXML private TableColumn<Attendance, String> colClockIn;
    @FXML private TableColumn<Attendance, String> colClockOut;
    @FXML private TableColumn<Attendance, Double> colOvertime;

    private Employee currentAdmin;
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final LeaveRequestDAO leaveDAO = new LeaveRequestDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    /**
     * Called automatically by JavaFX after the FXML file is loaded.
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        loadDashboardMetrics();
    }

    /**
     * Receives the logged-in administrator from LoginController or sub-controllers upon return.
     */
    public void setLoggedInEmployee(Employee employee) {
        this.currentAdmin = employee;
        if (welcomeLabel != null && employee != null) {
            welcomeLabel.setText("Welcome back, " + employee.getName() + " 👋");
        }
        loadDashboardMetrics();
    }

    /**
     * Binds TableColumn properties to the Attendance model getters.
     */
    private void setupTableColumns() {
        if (colLogId != null) colLogId.setCellValueFactory(new PropertyValueFactory<>("attendanceId"));
        if (colEmployeeId != null) colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        if (colClockIn != null) colClockIn.setCellValueFactory(new PropertyValueFactory<>("clockInTime"));
        if (colClockOut != null) colClockOut.setCellValueFactory(new PropertyValueFactory<>("clockOutTime"));
        if (colOvertime != null) colOvertime.setCellValueFactory(new PropertyValueFactory<>("overtimeHours"));
    }

    /**
     * Loads dynamic summary values for metric cards and populates the active shifts table.
     */
    private void loadDashboardMetrics() {
        // 1. Total Employees count
        if (employeeDAO != null && totalEmployeesLabel != null) {
            int totalEmployees = employeeDAO.getAllEmployees().size();
            totalEmployeesLabel.setText(String.valueOf(totalEmployees));
        }

        // 2. Real-time Pending Leaves count
        if (leaveDAO != null && pendingLeavesLabel != null) {
            long pendingCount = leaveDAO.getAllLeaveRequests().stream()
                    .filter(req -> "Pending".equalsIgnoreCase(req.getStatus()))
                    .count();
            pendingLeavesLabel.setText(String.valueOf(pendingCount));
        }

        // 3. Active Shifts metrics and table rendering
        if (attendanceDAO != null) {
            List<Attendance> activeShifts = attendanceDAO.getActiveShifts();

            // Update Counter Card
            if (activeShiftLabel != null) {
                activeShiftLabel.setText(String.valueOf(activeShifts.size()));
            }

            // Populate Table
            if (timeLogTable != null) {
                ObservableList<Attendance> observableList = FXCollections.observableArrayList(activeShifts);
                timeLogTable.setItems(observableList);
                timeLogTable.refresh();
            }
        }
    }

    // ==========================================
    // SIDEBAR NAVIGATION HANDLERS
    // ==========================================

    @FXML
    private void handleNavDashboard(ActionEvent event) {
        loadDashboardMetrics();
    }

    @FXML
    private void handleNavEmployees(ActionEvent event) {
        switchScene(event, "/com/example/supermarketems/employee-directory-view.fxml", "Supermarket EMS - Employee Directory");
    }

    @FXML
    private void handleNavSchedules(ActionEvent event) {
        switchScene(event, "/com/example/supermarketems/shift-schedule-view.fxml", "Supermarket EMS - Shift Schedules Management");
    }

    @FXML
    private void handleNavLeave(ActionEvent event) {
        switchScene(event, "/com/example/supermarketems/leave-request-view.fxml", "Supermarket EMS - Leave Requests Management");
    }

    @FXML
    private void handleNavPayroll(ActionEvent event) {
        switchScene(event, "/com/example/supermarketems/payroll-view.fxml", "Supermarket EMS - Payroll Processing");
    }

    @FXML
    private void handleNavAudit(ActionEvent event) {
        switchScene(event, "/com/example/supermarketems/audit-log-view.fxml", "Supermarket EMS - Audit Logs");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        this.currentAdmin = null;
        switchScene(event, "/com/example/supermarketems/login-view.fxml", "Supermarket EMS - Login");
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    /**
     * Reusable helper to handle scene switching and preserve admin session state dynamically.
     */
    private void switchScene(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object targetController = loader.getController();

            // Safe reflection approach: inspects declared methods and overrides access checks safely
            if (targetController != null && currentAdmin != null) {
                try {
                    Method setEmployeeMethod = targetController.getClass().getDeclaredMethod("setLoggedInEmployee", Employee.class);
                    setEmployeeMethod.setAccessible(true);
                    setEmployeeMethod.invoke(targetController, currentAdmin);
                } catch (NoSuchMethodException ignored) {
                    // Controller does not require admin session injection (e.g. LoginController)
                } catch (Exception e) {
                    System.err.println("Error invoking setLoggedInEmployee on target controller: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            System.err.println("Failed to load layout: " + fxmlPath);
            e.printStackTrace();
        }
    }
}