package com.example.supermarketems.controller;

import com.example.supermarketems.dao.EmployeeDAO;
import com.example.supermarketems.dao.LeaveRequestDAO;
import com.example.supermarketems.model.Employee;
import com.example.supermarketems.util.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ManagerDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label lblTeamCount;

    @FXML
    private Label lblOnDutyCount;

    @FXML
    private Label lblPendingRequests;

    @FXML
    private TableView<?> shiftTable;

    @FXML
    private TableColumn<?, String> colEmployeeName;

    @FXML
    private TableColumn<?, String> colRole;

    @FXML
    private TableColumn<?, String> colShiftTime;

    @FXML
    private TableColumn<?, String> colStation;

    @FXML
    private TableColumn<?, String> colStatus;

    private Employee currentManager;

    // DAO instances for fetching real-time dashboard data
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final LeaveRequestDAO leaveRequestDAO = new LeaveRequestDAO();

    /**
     * Session injection pattern called when loading this dashboard view
     */
    public void setLoggedInEmployee(Employee employee) {
        this.currentManager = employee;
        if (welcomeLabel != null && employee != null) {
            String dept = employee.getDepartmentManaged() != null ? " - " + employee.getDepartmentManaged() : "";
            welcomeLabel.setText("Welcome, " + employee.getName() + " (Manager" + dept + ")");
        }
        loadDashboardData();
    }

    @FXML
    public void initialize() {
        // Table initializations go here when connecting DAOs
    }

    private void loadDashboardData() {
        // Fetch real-time counts from the database using DAOs
        int totalEmployees = employeeDAO.getTotalEmployeeCount();
        int onDutyEmployees = employeeDAO.getOnDutyCount();
        int pendingLeaves = leaveRequestDAO.getPendingLeaveCount();

        // Update the FXML UI labels dynamically
        lblTeamCount.setText(String.valueOf(totalEmployees));
        lblOnDutyCount.setText(String.valueOf(onDutyEmployees));
        lblPendingRequests.setText(String.valueOf(pendingLeaves));
    }

    @FXML
    private void handleLaunchKiosk(ActionEvent event) {
        SceneNavigator.switchScene(event, "/com/example/supermarketems/attendance-kiosk-view.fxml", "Supermarket EMS - Attendance Terminal", currentManager);
    }

    @FXML
    private void handleNavShifts(ActionEvent event) {
        SceneNavigator.switchScene(event, "/com/example/supermarketems/shift-schedule-view.fxml", "Supermarket EMS - Shift Roster Management", currentManager);
    }

    @FXML
    private void handleNavLeaves(ActionEvent event) {
        SceneNavigator.switchScene(event, "/com/example/supermarketems/leave-request-view.fxml", "Supermarket EMS - Leave Approvals", currentManager);
    }

    @FXML
    private void handleNavAttendance(ActionEvent event) {
        // UPDATED: Points to the manager attendance view created in the previous step
        SceneNavigator.switchScene(event, "/com/example/supermarketems/manager-attendance-view.fxml", "Supermarket EMS - Attendance & Overtime", currentManager);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SceneNavigator.logout(event);
    }
}