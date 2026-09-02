package com.example.supermarketems.controller;

import com.example.supermarketems.dao.AttendanceDAO;
import com.example.supermarketems.model.Attendance;
import com.example.supermarketems.model.Employee;
import com.example.supermarketems.util.SceneNavigator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.util.List;

public class ManagerAttendanceController {

    @FXML private TableView<Attendance> tblAttendance;
    @FXML private TableColumn<Attendance, String> colEmpId;
    @FXML private TableColumn<Attendance, String> colEmpName;
    @FXML private TableColumn<Attendance, String> colDate;
    @FXML private TableColumn<Attendance, String> colClockIn;
    @FXML private TableColumn<Attendance, String> colClockOut;
    @FXML private TableColumn<Attendance, Double> colWorkHours;
    @FXML private TableColumn<Attendance, Double> colOvertimeHours;
    @FXML private TableColumn<Attendance, String> colMethod;
    @FXML private TableColumn<Attendance, String> colOtStatus;

    @FXML private TextField txtSearchEmployee;
    @FXML private DatePicker dateFilter;
    @FXML private ComboBox<String> cmbStatusFilter;

    private Employee loggedInEmployee;
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private ObservableList<Attendance> attendanceList = FXCollections.observableArrayList();

    public void setLoggedInEmployee(Employee employee) {
        this.loggedInEmployee = employee;
        loadAttendanceData();
    }

    @FXML
    public void initialize() {
        // Map columns to model getters
        colEmpId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colEmpName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colClockIn.setCellValueFactory(new PropertyValueFactory<>("clockInTime"));
        colClockOut.setCellValueFactory(new PropertyValueFactory<>("clockOutTime"));
        colWorkHours.setCellValueFactory(new PropertyValueFactory<>("workHours"));
        colOvertimeHours.setCellValueFactory(new PropertyValueFactory<>("overtimeHours"));
        colMethod.setCellValueFactory(new PropertyValueFactory<>("method"));
        colOtStatus.setCellValueFactory(new PropertyValueFactory<>("overtimeStatus"));

        if (cmbStatusFilter != null) {
            cmbStatusFilter.setItems(FXCollections.observableArrayList("ALL", "PENDING", "APPROVED", "REJECTED", "NONE"));
            cmbStatusFilter.getSelectionModel().selectFirst();
        }
    }

    private void loadAttendanceData() {
        List<Attendance> logs = attendanceDAO.getAllAttendanceLogs();
        attendanceList = FXCollections.observableArrayList(logs);
        tblAttendance.setItems(attendanceList);

        if (attendanceList.isEmpty()) {
            tblAttendance.setPlaceholder(new Label("No time logs recorded in the database yet."));
        }
    }

    @FXML
    private void handleFilterLogs() {
        String search = txtSearchEmployee.getText() != null ? txtSearchEmployee.getText().toLowerCase().trim() : "";
        LocalDate selectedDate = dateFilter.getValue();
        String selectedStatus = cmbStatusFilter.getValue();

        ObservableList<Attendance> filtered = attendanceList.filtered(log -> {
            boolean matchesSearch = search.isEmpty()
                    || (log.getEmployeeId() != null && log.getEmployeeId().toLowerCase().contains(search))
                    || (log.getEmployeeName() != null && log.getEmployeeName().toLowerCase().contains(search));

            boolean matchesDate = (selectedDate == null)
                    || (log.getDate() != null && log.getDate().equals(selectedDate.toString()));

            boolean matchesStatus = selectedStatus == null || "ALL".equalsIgnoreCase(selectedStatus)
                    || (log.getOvertimeStatus() != null && log.getOvertimeStatus().equalsIgnoreCase(selectedStatus));

            return matchesSearch && matchesDate && matchesStatus;
        });

        tblAttendance.setItems(filtered);
    }

    @FXML
    private void handleResetFilters() {
        txtSearchEmployee.clear();
        dateFilter.setValue(null);
        if (cmbStatusFilter != null) {
            cmbStatusFilter.getSelectionModel().selectFirst();
        }
        tblAttendance.setItems(attendanceList);
    }

    @FXML
    private void handleApproveOvertime() {
        Attendance selected = tblAttendance.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an attendance record to approve.");
            return;
        }
        selected.setOvertimeStatus("APPROVED");
        tblAttendance.refresh();
        showAlert(Alert.AlertType.INFORMATION, "Success", "Overtime status updated to APPROVED.");
    }

    @FXML
    private void handleRejectOvertime() {
        Attendance selected = tblAttendance.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an attendance record to reject.");
            return;
        }
        selected.setOvertimeStatus("REJECTED");
        tblAttendance.refresh();
        showAlert(Alert.AlertType.INFORMATION, "Success", "Overtime status updated to REJECTED.");
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        SceneNavigator.navigateToDashboard(event, loggedInEmployee);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}