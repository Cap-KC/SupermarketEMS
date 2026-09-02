package com.example.supermarketems.controller;

import com.example.supermarketems.dao.LeaveRequestDAO;
import com.example.supermarketems.database.DatabaseConnection;
import com.example.supermarketems.model.Employee;
import com.example.supermarketems.model.LeaveRequest;
import com.example.supermarketems.util.SceneNavigator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;

public class LeaveRequestController {

    // Form Controls
    @FXML private TextField requestIdField;
    @FXML private ComboBox<String> employeeComboBox;
    @FXML private ComboBox<String> leaveTypeComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextArea reasonArea;
    @FXML private Button submitButton;
    @FXML private Button clearButton;

    // Optional FX Controls (for simple modal variant support)
    @FXML private ComboBox<String> leaveTypeCombo;
    @FXML private TextArea reasonTextArea;

    // Filters & Search Controls
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterComboBox;

    // Table & Columns
    @FXML private TableView<LeaveRequest> leaveTable;
    @FXML private TableColumn<LeaveRequest, String> colReqId;
    @FXML private TableColumn<LeaveRequest, String> colEmpId;
    @FXML private TableColumn<LeaveRequest, String> colEmpName;
    @FXML private TableColumn<LeaveRequest, String> colType;
    @FXML private TableColumn<LeaveRequest, LocalDate> colStartDate;
    @FXML private TableColumn<LeaveRequest, LocalDate> colEndDate;
    @FXML private TableColumn<LeaveRequest, String> colReason;
    @FXML private TableColumn<LeaveRequest, String> colStatus;
    @FXML private TableColumn<LeaveRequest, String> colApprover;

    // Active Session State (Supports Admin, Manager, Permanent Employee)
    private Employee currentEmployee;

    // Data Holders
    private final LeaveRequestDAO leaveDAO = new LeaveRequestDAO();
    private final ObservableList<LeaveRequest> masterLeaveList = FXCollections.observableArrayList();
    private FilteredList<LeaveRequest> filteredLeaveList;

    /**
     * Receives the active user session when switching views or launching modal dialogs.
     */
    public void setLoggedInEmployee(Employee employee) {
        this.currentEmployee = employee;
        // Pre-select current employee in dropdown if logged in
        if (employeeComboBox != null && currentEmployee != null) {
            String empMatch = currentEmployee.getEmployeeId();
            for (String item : employeeComboBox.getItems()) {
                if (item.startsWith(empMatch)) {
                    employeeComboBox.setValue(item);
                    break;
                }
            }
        }
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        populateComboBoxes();
        loadEmployeesIntoComboBox();
        loadLeaveData();
        setupSearchAndFilters();
    }

    private void setupTableColumns() {
        if (leaveTable == null) return;

        if (colReqId != null) colReqId.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        if (colEmpId != null) colEmpId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        if (colEmpName != null) colEmpName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        if (colType != null) colType.setCellValueFactory(new PropertyValueFactory<>("leaveType"));
        if (colStartDate != null) colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        if (colEndDate != null) colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        if (colReason != null) colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        if (colStatus != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        if (colApprover != null) {
            colApprover.setCellValueFactory(cellData -> {
                String name = cellData.getValue().getApproverName();
                return new SimpleStringProperty(name != null ? name : "N/A");
            });
        }
    }

    private void populateComboBoxes() {
        ObservableList<String> types = FXCollections.observableArrayList(
                "Casual Leave", "Sick Leave", "Annual Leave", "Unpaid Leave"
        );

        if (leaveTypeComboBox != null) leaveTypeComboBox.setItems(types);
        if (leaveTypeCombo != null) leaveTypeCombo.setItems(types);

        if (statusFilterComboBox != null) {
            statusFilterComboBox.setItems(FXCollections.observableArrayList("All", "Pending", "Approved", "Rejected"));
            statusFilterComboBox.setValue("All");
        }
    }

    private void loadEmployeesIntoComboBox() {
        if (employeeComboBox == null) return;

        ObservableList<String> employees = FXCollections.observableArrayList();
        String sql = "SELECT employee_id, name FROM employees ORDER BY employee_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String empOption = rs.getString("employee_id") + " - " + rs.getString("name");
                employees.add(empOption);
            }
            employeeComboBox.setItems(employees);

        } catch (Exception e) {
            System.err.println("Error fetching employees for drop-down: " + e.getMessage());
        }
    }

    private void loadLeaveData() {
        if (leaveTable == null) return;

        masterLeaveList.clear();
        masterLeaveList.addAll(leaveDAO.getAllLeaveRequests());

        if (filteredLeaveList == null) {
            filteredLeaveList = new FilteredList<>(masterLeaveList, p -> true);
            leaveTable.setItems(filteredLeaveList);
        }
    }

    private void setupSearchAndFilters() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
        if (statusFilterComboBox != null) {
            statusFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
    }

    private void applyFilters() {
        if (filteredLeaveList == null) return;

        String searchText = (searchField != null && searchField.getText() != null) ? searchField.getText().toLowerCase().trim() : "";
        String selectedStatus = statusFilterComboBox != null ? statusFilterComboBox.getValue() : "All";

        filteredLeaveList.setPredicate(request -> {
            boolean matchesStatus = "All".equalsIgnoreCase(selectedStatus) || selectedStatus == null
                    || request.getStatus().equalsIgnoreCase(selectedStatus);

            boolean matchesSearch = searchText.isEmpty()
                    || (request.getRequestId() != null && request.getRequestId().toLowerCase().contains(searchText))
                    || (request.getEmployeeId() != null && request.getEmployeeId().toLowerCase().contains(searchText))
                    || (request.getEmployeeName() != null && request.getEmployeeName().toLowerCase().contains(searchText));

            return matchesStatus && matchesSearch;
        });
    }

    // ==========================================
    // ACTION HANDLERS
    // ==========================================

    @FXML
    private void handleSubmit(ActionEvent event) {
        handleSubmit();

        // If loaded inside a modal popup window, auto-close on success
        if (event != null && event.getSource() instanceof Node node) {
            Stage stage = (Stage) node.getScene().getWindow();
            if (stage != null && stage.getOwner() != null) {
                stage.close();
            }
        }
    }

    @FXML
    private void handleSubmit() {
        if (!validateForm()) return;

        String reqId = (requestIdField != null && !requestIdField.getText().trim().isEmpty())
                ? requestIdField.getText().trim()
                : "REQ-" + System.currentTimeMillis() % 10000;

        String empId = getSelectedEmployeeId();
        String type = (leaveTypeComboBox != null && leaveTypeComboBox.getValue() != null)
                ? leaveTypeComboBox.getValue()
                : (leaveTypeCombo != null ? leaveTypeCombo.getValue() : "Casual Leave");

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        String reason = "";
        if (reasonArea != null && reasonArea.getText() != null) {
            reason = reasonArea.getText().trim();
        } else if (reasonTextArea != null && reasonTextArea.getText() != null) {
            reason = reasonTextArea.getText().trim();
        }

        LeaveRequest newRequest = new LeaveRequest(reqId, empId, null, type, startDate, endDate, reason, "Pending", null, null);

        if (leaveDAO.addLeaveRequest(newRequest)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Leave request submitted successfully.");
            handleClear();
            loadLeaveData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to submit leave request.");
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeOrNavigateBack(event);
    }

    @FXML
    private void handleClear(ActionEvent event) {
        handleClear();
    }

    @FXML
    private void handleClear() {
        if (requestIdField != null) requestIdField.clear();
        if (employeeComboBox != null) employeeComboBox.setValue(null);
        if (leaveTypeComboBox != null) leaveTypeComboBox.setValue(null);
        if (leaveTypeCombo != null) leaveTypeCombo.setValue(null);
        if (startDatePicker != null) startDatePicker.setValue(null);
        if (endDatePicker != null) endDatePicker.setValue(null);
        if (reasonArea != null) reasonArea.clear();
        if (reasonTextArea != null) reasonTextArea.clear();
    }

    @FXML
    private void handleApprove() {
        updateSelectedLeaveStatus("Approved");
    }

    @FXML
    private void handleReject() {
        updateSelectedLeaveStatus("Rejected");
    }

    private void updateSelectedLeaveStatus(String status) {
        if (leaveTable == null) return;

        LeaveRequest selected = leaveTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a leave request from the table first.");
            return;
        }

        String currentApproverId = (currentEmployee != null) ? currentEmployee.getEmployeeId() : "ADM001";

        if (leaveDAO.updateLeaveStatus(selected.getRequestId(), status, currentApproverId)) {
            showAlert(Alert.AlertType.INFORMATION, "Status Updated", "Request " + selected.getRequestId() + " marked as " + status);
            loadLeaveData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update leave status.");
        }
    }

    @FXML
    private void handleDelete() {
        if (leaveTable == null) return;

        LeaveRequest selected = leaveTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a leave request to delete.");
            return;
        }

        if (leaveDAO.deleteLeaveRequest(selected.getRequestId())) {
            showAlert(Alert.AlertType.INFORMATION, "Deleted", "Leave request deleted successfully.");
            loadLeaveData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete leave request.");
        }
    }

    @FXML
    private void handleRefresh() {
        loadLeaveData();
    }

    // ==========================================
    // NAVIGATION & UTILS
    // ==========================================

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        closeOrNavigateBack(event);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SceneNavigator.logout(event);
    }

    private void closeOrNavigateBack(ActionEvent event) {
        if (event != null && event.getSource() instanceof Node node) {
            Stage stage = (Stage) node.getScene().getWindow();
            // If running inside a modal window (dialog)
            if (stage != null && stage.getOwner() != null) {
                stage.close();
                return;
            }
        }
        // Fallback to primary screen navigation
        SceneNavigator.navigateToDashboard(event, currentEmployee);
    }

    private String getSelectedEmployeeId() {
        if (employeeComboBox != null && employeeComboBox.getValue() != null) {
            return employeeComboBox.getValue().split(" - ")[0].trim();
        }
        if (currentEmployee != null) {
            return currentEmployee.getEmployeeId();
        }
        return "EMP000";
    }

    private boolean validateForm() {
        ComboBox<String> activeTypeCombo = leaveTypeComboBox != null ? leaveTypeComboBox : leaveTypeCombo;

        if (activeTypeCombo == null || activeTypeCombo.getValue() == null ||
                startDatePicker == null || startDatePicker.getValue() == null ||
                endDatePicker == null || endDatePicker.getValue() == null) {

            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill in all required fields (*).");
            return false;
        }

        if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "End date cannot be before start date.");
            return false;
        }

        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}