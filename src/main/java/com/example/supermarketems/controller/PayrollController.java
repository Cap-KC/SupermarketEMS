package com.example.supermarketems.controller;

import com.example.supermarketems.dao.EmployeeDAO;
import com.example.supermarketems.dao.PayrollDAO;
import com.example.supermarketems.model.Employee;
import com.example.supermarketems.model.PayrollRecord;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PayrollController {

    // --- FXML Bindings for Input Form ---
    @FXML private TextField payrollIdField;
    @FXML private ComboBox<String> employeeComboBox;
    @FXML private ComboBox<String> payPeriodComboBox;
    @FXML private TextField basicSalaryField;
    @FXML private TextField overtimeHoursField;
    @FXML private TextField allowancesField;
    @FXML private TextField deductionsField;
    @FXML private Label netPayLabel;

    // --- FXML Bindings for Table & Controls ---
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private TableView<PayrollRecord> payrollTable;
    @FXML private TableColumn<PayrollRecord, String> colPayrollId;
    @FXML private TableColumn<PayrollRecord, String> colEmpId;
    @FXML private TableColumn<PayrollRecord, String> colEmpName;
    @FXML private TableColumn<PayrollRecord, String> colPayPeriod;
    @FXML private TableColumn<PayrollRecord, Double> colBasic;
    @FXML private TableColumn<PayrollRecord, Double> colAllowances;
    @FXML private TableColumn<PayrollRecord, Double> colDeductions;
    @FXML private TableColumn<PayrollRecord, Double> colNetPay;
    @FXML private TableColumn<PayrollRecord, String> colStatus;

    // --- Service / DAO Objects & Session State ---
    private final PayrollDAO payrollDAO = new PayrollDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final ObservableList<PayrollRecord> payrollList = FXCollections.observableArrayList();
    private final Map<String, String> employeeMap = new HashMap<>(); // Maps "Name (ID)" -> ID
    private Employee currentAdmin;

    /**
     * Session management method called when navigating between views.
     */
    public void setLoggedInEmployee(Employee employee) {
        this.currentAdmin = employee;
    }

    /**
     * Called automatically by JavaFX when the FXML file is loaded.
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        populateEmployeeComboBox();
        populatePayPeriodComboBox();
        populateStatusFilterComboBox();
        loadPayrollData();

        // Listen for table selections to fill the form for viewing/editing
        payrollTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFormWithSelection(newSelection);
            }
        });

        // Search field dynamic listener
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> filterTable(newValue));
        }
    }

    // ==========================================
    // FORM CALCULATIONS & DATABASE OPERATIONS
    // ==========================================

    @FXML
    private void handleCalculate(ActionEvent event) {
        calculateNetPay();
    }

    private double calculateNetPay() {
        try {
            // Validate Basic Salary (Required Field)
            String basicText = basicSalaryField != null ? basicSalaryField.getText() : "";
            if (basicText == null || basicText.trim().isEmpty()) {
                showWarningAlert("Input Required", "Please enter a valid Basic Salary.");
                return 0.0;
            }

            double basic = parseDoubleOrDefault(basicText, 0.0);

            // Optional Fields: Default to 0.0 safely if empty or invalid
            String otText = overtimeHoursField != null ? overtimeHoursField.getText() : "";
            String allowText = allowancesField != null ? allowancesField.getText() : "";
            String deductText = deductionsField != null ? deductionsField.getText() : "";

            double overtimeHours = parseDoubleOrDefault(otText, 0.0);
            double allowances = parseDoubleOrDefault(allowText, 0.0);
            double deductions = parseDoubleOrDefault(deductText, 0.0);

            // Standard overtime calculation assumption: Basic Salary / 160 hrs * 1.5 * overtimeHours
            double hourlyRate = basic > 0 ? (basic / 160.0) : 0.0;
            double overtimePay = hourlyRate * 1.5 * overtimeHours;

            double netPay = basic + overtimePay + allowances - deductions;

            if (netPayLabel != null) {
                netPayLabel.setText(String.format("$%.2f", netPay));
            }
            return netPay;
        } catch (Exception e) {
            showErrorAlert("Invalid Numbers", "Please enter valid numerical values for salary, hours, allowances, and deductions.");
            return 0.0;
        }
    }

    @FXML
    private void handleProcessPayroll(ActionEvent event) {
        String payrollId = payrollIdField.getText().trim();
        String selectedEmp = employeeComboBox.getValue();
        String selectedPeriod = payPeriodComboBox.getValue();

        if (payrollId.isEmpty() || selectedEmp == null || selectedPeriod == null) {
            showErrorAlert("Validation Error", "Payroll ID, Employee, and Pay Period are required fields.");
            return;
        }

        String employeeId = employeeMap.get(selectedEmp);
        double basic = parseDoubleOrDefault(basicSalaryField.getText(), 0.0);
        double overtimeHours = parseDoubleOrDefault(overtimeHoursField.getText(), 0.0);
        double hourlyRate = basic > 0 ? (basic / 160.0) : 0.0;
        double overtimePay = hourlyRate * 1.5 * overtimeHours;

        double allowances = parseDoubleOrDefault(allowancesField.getText(), 0.0);
        double deductions = parseDoubleOrDefault(deductionsField.getText(), 0.0);
        double netPay = calculateNetPay();

        // Infer pay period start and end dates from current month
        LocalDate now = LocalDate.now();
        Date startDate = Date.valueOf(now.withDayOfMonth(1));
        Date endDate = Date.valueOf(now.withDayOfMonth(now.lengthOfMonth()));

        PayrollRecord newRecord = new PayrollRecord(
                payrollId, employeeId, startDate, endDate,
                basic, overtimePay, allowances, deductions, netPay,
                "Draft", null
        );

        boolean success = payrollDAO.savePayrollRecord(newRecord);

        if (success) {
            showInfoAlert("Success", "Payroll record successfully processed and saved.");
            loadPayrollData();
            handleClear(null);
        } else {
            showErrorAlert("Database Error", "Failed to save payroll record. Verify if Payroll ID already exists.");
        }
    }

    @FXML
    private void handleMarkPaid(ActionEvent event) {
        PayrollRecord selected = payrollTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("No Selection", "Please select a payroll record from the table to mark as Paid.");
            return;
        }

        boolean success = payrollDAO.updatePaymentStatus(selected.getPayrollId(), "Paid");
        if (success) {
            showInfoAlert("Success", "Payroll status updated to 'Paid'.");
            loadPayrollData();
        } else {
            showErrorAlert("Error", "Could not update status in database.");
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        PayrollRecord selected = payrollTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("No Selection", "Please select a record from the table to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Payroll Record " + selected.getPayrollId() + "?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = payrollDAO.deletePayrollRecord(selected.getPayrollId());
            if (success) {
                showInfoAlert("Deleted", "Payroll record deleted successfully.");
                loadPayrollData();
                handleClear(null);
            } else {
                showErrorAlert("Error", "Failed to delete payroll record.");
            }
        }
    }

    @FXML
    private void handleGeneratePayslip(ActionEvent event) {
        PayrollRecord selected = payrollTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("No Selection", "Please select a record from the table to generate payslip.");
            return;
        }
        showInfoAlert("Export Payslip", "Payslip generated for " + selected.getPayrollId() + ". (PDF feature stub)");
    }

    @FXML
    private void handleClear(ActionEvent event) {
        payrollIdField.clear();
        employeeComboBox.getSelectionModel().clearSelection();
        payPeriodComboBox.getSelectionModel().clearSelection();
        basicSalaryField.clear();
        overtimeHoursField.clear();
        allowancesField.clear();
        deductionsField.clear();
        if (netPayLabel != null) netPayLabel.setText("$0.00");
        payrollTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadPayrollData();
        if (searchField != null) searchField.clear();
        if (statusFilterComboBox != null) statusFilterComboBox.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        switchScene(event, "/com/example/supermarketems/admin-dashboard-view.fxml", "Supermarket EMS - Admin Dashboard");
    }

    // ==========================================
    // HELPER & INITIALIZATION METHODS
    // ==========================================

    private void setupTableColumns() {
        colPayrollId.setCellValueFactory(new PropertyValueFactory<>("payrollId"));
        colEmpId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));

        // Dynamically match Employee ID to Employee Name
        colEmpName.setCellValueFactory(cellData -> {
            String empId = cellData.getValue().getEmployeeId();
            Employee emp = employeeDAO.getEmployeeById(empId);
            return new SimpleStringProperty(emp != null ? emp.getName() : empId);
        });

        colPayPeriod.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPayPeriodStart() + " to " + cellData.getValue().getPayPeriodEnd())
        );

        colBasic.setCellValueFactory(new PropertyValueFactory<>("basicSalary"));
        colAllowances.setCellValueFactory(new PropertyValueFactory<>("allowances"));
        colDeductions.setCellValueFactory(new PropertyValueFactory<>("deductions"));
        colNetPay.setCellValueFactory(new PropertyValueFactory<>("netPay"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));

        payrollTable.setItems(payrollList);
    }

    private void loadPayrollData() {
        payrollList.clear();
        List<PayrollRecord> records = payrollDAO.getAllPayrollRecords();
        payrollList.addAll(records);
    }

    private void populateEmployeeComboBox() {
        employeeComboBox.getItems().clear();
        employeeMap.clear();
        List<Employee> employees = employeeDAO.getAllEmployees();
        for (Employee emp : employees) {
            String displayKey = emp.getName() + " (" + emp.getEmployeeId() + ")";
            employeeMap.put(displayKey, emp.getEmployeeId());
            employeeComboBox.getItems().add(displayKey);
        }
    }

    private void populatePayPeriodComboBox() {
        payPeriodComboBox.setItems(FXCollections.observableArrayList(
                "January 2026", "February 2026", "March 2026", "April 2026",
                "May 2026", "June 2026", "July 2026", "August 2026"
        ));
    }

    private void populateStatusFilterComboBox() {
        if (statusFilterComboBox != null) {
            statusFilterComboBox.setItems(FXCollections.observableArrayList("Draft", "Processed", "Paid"));
            statusFilterComboBox.setOnAction(e -> filterTableByStatus(statusFilterComboBox.getValue()));
        }
    }

    private void populateFormWithSelection(PayrollRecord record) {
        payrollIdField.setText(record.getPayrollId());

        // Match combobox by Employee ID
        employeeMap.forEach((key, val) -> {
            if (val.equals(record.getEmployeeId())) {
                employeeComboBox.setValue(key);
            }
        });

        basicSalaryField.setText(String.valueOf(record.getBasicSalary()));
        allowancesField.setText(String.valueOf(record.getAllowances()));
        deductionsField.setText(String.valueOf(record.getDeductions()));
        if (netPayLabel != null) netPayLabel.setText(String.format("$%.2f", record.getNetPay()));
    }

    private void filterTable(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            payrollTable.setItems(payrollList);
            return;
        }

        ObservableList<PayrollRecord> filtered = FXCollections.observableArrayList();
        for (PayrollRecord rec : payrollList) {
            if (rec.getPayrollId().toLowerCase().contains(keyword.toLowerCase()) ||
                    rec.getEmployeeId().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(rec);
            }
        }
        payrollTable.setItems(filtered);
    }

    private void filterTableByStatus(String status) {
        if (status == null || status.isEmpty()) {
            payrollTable.setItems(payrollList);
            return;
        }

        ObservableList<PayrollRecord> filtered = FXCollections.observableArrayList();
        for (PayrollRecord rec : payrollList) {
            if (status.equalsIgnoreCase(rec.getPaymentStatus())) {
                filtered.add(rec);
            }
        }
        payrollTable.setItems(filtered);
    }

    /**
     * Safely parses double values. Prevents crashes from empty or invalid strings.
     */
    private double parseDoubleOrDefault(String text, double defaultValue) {
        if (text == null || text.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private void switchScene(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object targetController = loader.getController();
            if (targetController != null && currentAdmin != null) {
                try {
                    Method method = targetController.getClass().getMethod("setLoggedInEmployee", Employee.class);
                    method.invoke(targetController, currentAdmin);
                } catch (NoSuchMethodException ignored) {}
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Could not load view: " + fxmlPath);
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}