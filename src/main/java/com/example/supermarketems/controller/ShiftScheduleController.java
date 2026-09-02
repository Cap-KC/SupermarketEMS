package com.example.supermarketems.controller;

import com.example.supermarketems.dao.EmployeeDAO;
import com.example.supermarketems.dao.SchedulesDAO;
import com.example.supermarketems.model.Employee;
import com.example.supermarketems.model.Schedules;
import com.example.supermarketems.util.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class ShiftScheduleController {

    // --- Form Panel Bindings ---
    @FXML private Label formTitleLabel;
    @FXML private TextField scheduleIdField;
    @FXML private ComboBox<Employee> employeeComboBox; // Dropdown to pick assigned employee
    @FXML private DatePicker shiftDatePicker;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private TextField departmentField;
    @FXML private TextField managedByField; // Read-only: shows logged-in user
    @FXML private Label statusLabel;
    @FXML private Button saveButton;

    // --- Table & Filter Bindings ---
    @FXML private TextField searchField;
    @FXML private DatePicker filterStartDatePicker;
    @FXML private DatePicker filterEndDatePicker;

    @FXML private TableView<Schedules> shiftTable;
    @FXML private TableColumn<Schedules, String> colScheduleId;
    @FXML private TableColumn<Schedules, String> colEmployeeId;
    @FXML private TableColumn<Schedules, String> colEmployeeName;
    @FXML private TableColumn<Schedules, LocalDate> colDate;
    @FXML private TableColumn<Schedules, LocalTime> colStartTime;
    @FXML private TableColumn<Schedules, LocalTime> colEndTime;
    @FXML private TableColumn<Schedules, String> colDepartment;
    @FXML private TableColumn<Schedules, String> colManagedBy; // Displays Manager's Name

    // --- State & DAOs ---
    private Employee currentEmployee;
    private final SchedulesDAO schedulesDAO = new SchedulesDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    private final ObservableList<Schedules> masterShiftList = FXCollections.observableArrayList();
    private FilteredList<Schedules> filteredShiftList;
    private Schedules selectedShiftForEdit = null;

    @FXML
    public void initialize() {
        setupTableColumns();
        populateEmployeeDropdown();
        loadShiftData();

        // Setup filterable list for live search
        filteredShiftList = new FilteredList<>(masterShiftList, p -> true);
        shiftTable.setItems(filteredShiftList);

        // Attach listener for dynamic search bar
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    /**
     * Receives active logged-in employee (Admin or Manager) context.
     */
    public void setLoggedInEmployee(Employee employee) {
        this.currentEmployee = employee;
        if (this.currentEmployee != null && managedByField != null) {
            managedByField.setText(this.currentEmployee.getEmployeeId() + " - " + this.currentEmployee.getName());
            managedByField.setEditable(false);
        }
    }

    // ==========================================
    // INITIALIZATION HELPERS
    // ==========================================

    private void setupTableColumns() {
        colScheduleId.setCellValueFactory(new PropertyValueFactory<>("scheduleId"));
        if (colEmployeeId != null) {
            colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        }
        if (colEmployeeName != null) {
            colEmployeeName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        }
        colDate.setCellValueFactory(new PropertyValueFactory<>("shiftDate"));
        colStartTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));

        // Binds to managerName returned by SQL JOIN
        colManagedBy.setCellValueFactory(new PropertyValueFactory<>("managerName"));
    }

    private void populateEmployeeDropdown() {
        if (employeeComboBox == null) return;

        try {
            ObservableList<Employee> employees = FXCollections.observableArrayList(employeeDAO.getAllEmployees());
            employeeComboBox.setItems(employees);

            // Display "EMP-ID - Full Name" in dropdown items and selected value
            employeeComboBox.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Employee item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getEmployeeId() + " - " + item.getName());
                    }
                }
            });
            employeeComboBox.setButtonCell(employeeComboBox.getCellFactory().call(null));
        } catch (Exception e) {
            showStatus("Failed to populate employee dropdown list.", true);
        }
    }

    private void loadShiftData() {
        try {
            masterShiftList.setAll(schedulesDAO.getAllSchedules());
        } catch (Exception e) {
            showStatus("Error loading shift schedules.", true);
        }
    }

    // ==========================================
    // FORM ACTION HANDLERS
    // ==========================================

    @FXML
    private void handleSaveShift(ActionEvent event) {
        if (!validateForm()) return;

        try {
            Employee selectedEmp = employeeComboBox.getValue();
            String scheduleId = scheduleIdField.getText().trim();
            if (scheduleId.isEmpty()) {
                scheduleId = "SCH-" + (System.currentTimeMillis() % 1000000);
            }

            LocalDate date = shiftDatePicker.getValue();
            LocalTime startTime = LocalTime.parse(startTimeField.getText().trim());
            LocalTime endTime = LocalTime.parse(endTimeField.getText().trim());
            String department = departmentField.getText().trim();

            // Store current user's employee_id for foreign key constraint
            String managerId = (currentEmployee != null) ? currentEmployee.getEmployeeId() : null;
            String managerName = (currentEmployee != null) ? currentEmployee.getName() : null;

            if (selectedShiftForEdit == null) {
                // CREATE NEW SCHEDULE
                Schedules newSchedule = new Schedules(
                        scheduleId,
                        selectedEmp.getEmployeeId(),
                        selectedEmp.getName(),
                        date,
                        startTime,
                        endTime,
                        department,
                        managerId,
                        managerName
                );

                if (schedulesDAO.addSchedule(newSchedule)) {
                    showStatus("Schedule added successfully!", false);
                    loadShiftData();
                    handleClearForm(null);
                } else {
                    showStatus("Failed to save schedule to database.", true);
                }
            } else {
                // UPDATE EXISTING SCHEDULE
                selectedShiftForEdit.setEmployeeId(selectedEmp.getEmployeeId());
                selectedShiftForEdit.setEmployeeName(selectedEmp.getName());
                selectedShiftForEdit.setShiftDate(date);
                selectedShiftForEdit.setStartTime(startTime);
                selectedShiftForEdit.setEndTime(endTime);
                selectedShiftForEdit.setDepartment(department);
                if (managerId != null) {
                    selectedShiftForEdit.setManagedBy(managerId);
                    selectedShiftForEdit.setManagerName(managerName);
                }

                if (schedulesDAO.updateSchedule(selectedShiftForEdit)) {
                    showStatus("Schedule updated successfully!", false);
                    loadShiftData();
                    handleClearForm(null);
                } else {
                    showStatus("Failed to update schedule.", true);
                }
            }

        } catch (DateTimeParseException e) {
            showStatus("Invalid time format! Use HH:mm (e.g., 08:30 or 16:00).", true);
        }
    }

    @FXML
    private void handleEditSelected(ActionEvent event) {
        Schedules selected = shiftTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Please select a schedule from the table to edit.", true);
            return;
        }

        selectedShiftForEdit = selected;
        formTitleLabel.setText("✏️ Edit Schedule (ID: " + selected.getScheduleId() + ")");

        scheduleIdField.setText(selected.getScheduleId());
        scheduleIdField.setDisable(true); // Primary key disabled during edits

        // Match selected employee in ComboBox
        if (employeeComboBox != null) {
            for (Employee emp : employeeComboBox.getItems()) {
                if (emp.getEmployeeId() != null && emp.getEmployeeId().equals(selected.getEmployeeId())) {
                    employeeComboBox.setValue(emp);
                    break;
                }
            }
        }

        shiftDatePicker.setValue(selected.getShiftDate());
        startTimeField.setText(selected.getStartTime() != null ? selected.getStartTime().toString() : "");
        endTimeField.setText(selected.getEndTime() != null ? selected.getEndTime().toString() : "");
        departmentField.setText(selected.getDepartment());

        saveButton.setText("Update Schedule");
    }

    @FXML
    private void handleDeleteSelected(ActionEvent event) {
        Schedules selected = shiftTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Select a schedule from the table to delete.", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Schedule?");
        confirm.setContentText("Are you sure you want to delete Schedule ID " + selected.getScheduleId() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (schedulesDAO.deleteSchedule(selected.getScheduleId())) {
                showStatus("Schedule deleted.", false);
                loadShiftData();
                handleClearForm(null);
            } else {
                showStatus("Failed to delete schedule.", true);
            }
        }
    }

    @FXML
    private void handleClearForm(ActionEvent event) {
        selectedShiftForEdit = null;
        formTitleLabel.setText("📅 Assign Schedule");
        scheduleIdField.clear();
        scheduleIdField.setDisable(false);
        if (employeeComboBox != null) {
            employeeComboBox.setValue(null);
        }
        shiftDatePicker.setValue(null);
        startTimeField.clear();
        endTimeField.clear();
        departmentField.clear();
        if (currentEmployee != null && managedByField != null) {
            managedByField.setText(currentEmployee.getEmployeeId() + " - " + currentEmployee.getName());
        }
        saveButton.setText("Save Schedule");
        statusLabel.setText("");
    }

    // ==========================================
    // FILTERING & SEARCH
    // ==========================================

    @FXML
    private void handleFilterByDate(ActionEvent event) {
        applyFilters();
    }

    @FXML
    private void handleResetFilter(ActionEvent event) {
        filterStartDatePicker.setValue(null);
        filterEndDatePicker.setValue(null);
        searchField.clear();
        filteredShiftList.setPredicate(p -> true);
    }

    private void applyFilters() {
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        LocalDate startDate = filterStartDatePicker.getValue();
        LocalDate endDate = filterEndDatePicker.getValue();

        filteredShiftList.setPredicate(schedule -> {
            // Text search across ID, Employee Name, Employee ID, Department, and Manager
            boolean matchesSearch = searchText.isEmpty()
                    || (schedule.getScheduleId() != null && schedule.getScheduleId().toLowerCase().contains(searchText))
                    || (schedule.getEmployeeId() != null && schedule.getEmployeeId().toLowerCase().contains(searchText))
                    || (schedule.getEmployeeName() != null && schedule.getEmployeeName().toLowerCase().contains(searchText))
                    || (schedule.getDepartment() != null && schedule.getDepartment().toLowerCase().contains(searchText))
                    || (schedule.getManagerName() != null && schedule.getManagerName().toLowerCase().contains(searchText));

            // Date range matching
            boolean matchesDateRange = true;
            if (schedule.getShiftDate() != null) {
                if (startDate != null && schedule.getShiftDate().isBefore(startDate)) {
                    matchesDateRange = false;
                }
                if (endDate != null && schedule.getShiftDate().isAfter(endDate)) {
                    matchesDateRange = false;
                }
            }

            return matchesSearch && matchesDateRange;
        });
    }

    // ==========================================
    // NAVIGATION & UTILS
    // ==========================================

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        // Dynamically routes based on role (Manager -> Manager Portal, Admin -> Admin Portal)
        SceneNavigator.navigateToDashboard(event, currentEmployee);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SceneNavigator.logout(event);
    }

    private boolean validateForm() {
        if (selectedShiftForEdit == null && scheduleIdField.getText().trim().isEmpty()) {
            showStatus("Please enter a Schedule ID.", true);
            return false;
        }
        if (employeeComboBox != null && employeeComboBox.getValue() == null) {
            showStatus("Please select an employee.", true);
            return false;
        }
        if (shiftDatePicker.getValue() == null) {
            showStatus("Please pick a shift date.", true);
            return false;
        }
        if (startTimeField.getText().trim().isEmpty() || endTimeField.getText().trim().isEmpty()) {
            showStatus("Please specify both start and end times.", true);
            return false;
        }
        return true;
    }

    private void showStatus(String text, boolean isError) {
        statusLabel.setText(text);
        statusLabel.setTextFill(isError ? Color.RED : Color.GREEN);
    }
}