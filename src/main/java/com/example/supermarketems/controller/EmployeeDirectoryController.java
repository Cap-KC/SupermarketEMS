package com.example.supermarketems.controller;

import com.example.supermarketems.dao.EmployeeDAO;
import com.example.supermarketems.model.Employee;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

public class EmployeeDirectoryController {

    // Session State
    private Employee currentAdmin;

    // Base Fields
    @FXML private Label formTitleLabel;
    @FXML private TextField empIdField;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private DatePicker dateJoinedPicker;
    @FXML private PasswordField passwordField;

    // Type Containers & Specific Fields
    @FXML private VBox permanentBox;
    @FXML private TextField monthlySalaryField;
    @FXML private TextField leaveBalanceField;
    @FXML private TextField benefitsPkgField;

    @FXML private VBox outsourceBox;
    @FXML private TextField hourlyRateField;
    @FXML private DatePicker contractExpiryPicker;
    @FXML private TextField agencyIdField;

    @FXML private VBox managerBox;
    @FXML private TextField deptManagedField;

    @FXML private VBox adminBox;
    @FXML private TextField adminLevelField;

    @FXML private Label statusLabel;
    @FXML private Button saveButton;

    // Table & Search
    @FXML private TextField searchField;
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> colId;
    @FXML private TableColumn<Employee, String> colName;
    @FXML private TableColumn<Employee, String> colEmail;
    @FXML private TableColumn<Employee, String> colPhone;
    @FXML private TableColumn<Employee, String> colType;
    @FXML private TableColumn<Employee, String> colAgency;
    @FXML private TableColumn<Employee, Date> colDate;

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final ObservableList<Employee> masterList = FXCollections.observableArrayList();
    private Employee selectedEmployee = null;

    /**
     * Receives the logged-in admin instance to maintain session state across scenes.
     */
    public void setLoggedInEmployee(Employee employee) {
        this.currentAdmin = employee;
    }

    @FXML
    public void initialize() {
        // Setup dropdown
        typeComboBox.setItems(FXCollections.observableArrayList("Admin", "Manager", "Permanent", "Outsource"));
        typeComboBox.getSelectionModel().select("Permanent");

        // Dynamic UI Toggling based on employee type selection
        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> toggleFormSections(newVal));

        // Setup Table Columns
        colId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colType.setCellValueFactory(new PropertyValueFactory<>("employeeType"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateJoined"));

        // Column for Joined table data (External Agency Name from LEFT JOIN)
        colAgency.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getAgencyName() != null ? cell.getValue().getAgencyName() : "-")
        );

        loadData();
        setupSearch();
    }

    /**
     * Navigates back to the Admin Dashboard scene and passes the current admin object back.
     */
    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/supermarketems/admin-dashboard-view.fxml"));
            Parent root = loader.load();

            // Pass active admin back to dashboard controller
            AdminDashboardController controller = loader.getController();
            if (controller != null && currentAdmin != null) {
                controller.setLoggedInEmployee(currentAdmin);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Supermarket EMS - Admin Dashboard");
            stage.show();

        } catch (IOException e) {
            showStatus("Failed to load Admin Dashboard layout.", true);
            e.printStackTrace();
        }
    }

    private void toggleFormSections(String type) {
        if (type == null) return;

        // Reset visibility
        boolean isOutsource = "Outsource".equalsIgnoreCase(type);
        boolean isManager = "Manager".equalsIgnoreCase(type);
        boolean isAdmin = "Admin".equalsIgnoreCase(type);

        outsourceBox.setVisible(isOutsource);
        outsourceBox.setManaged(isOutsource);

        permanentBox.setVisible(!isOutsource);
        permanentBox.setManaged(!isOutsource);

        managerBox.setVisible(isManager);
        managerBox.setManaged(isManager);

        adminBox.setVisible(isAdmin);
        adminBox.setManaged(isAdmin);
    }

    private void loadData() {
        masterList.clear();
        masterList.addAll(employeeDAO.getAllEmployees());
    }

    private void setupSearch() {
        FilteredList<Employee> filteredList = new FilteredList<>(masterList, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(emp -> {
                if (newVal == null || newVal.trim().isEmpty()) return true;
                String query = newVal.toLowerCase().trim();

                return (emp.getEmployeeId() != null && emp.getEmployeeId().toLowerCase().contains(query))
                        || (emp.getName() != null && emp.getName().toLowerCase().contains(query))
                        || (emp.getEmail() != null && emp.getEmail().toLowerCase().contains(query))
                        || (emp.getEmployeeType() != null && emp.getEmployeeType().toLowerCase().contains(query))
                        || (emp.getAgencyName() != null && emp.getAgencyName().toLowerCase().contains(query));
            });
        });
        employeeTable.setItems(filteredList);
    }

    @FXML
    private void handleSaveEmployee() {
        String id = empIdField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String type = typeComboBox.getValue();
        LocalDate localDate = dateJoinedPicker.getValue();
        String password = passwordField.getText().trim();

        if (id.isEmpty() || name.isEmpty() || email.isEmpty() || localDate == null || password.isEmpty()) {
            showStatus("Please complete all essential fields.", true);
            return;
        }

        boolean isUpdate = (selectedEmployee != null);
        Employee emp = isUpdate ? selectedEmployee : new Employee();

        emp.setEmployeeId(id);
        emp.setName(name);
        emp.setEmail(email);
        emp.setPhoneNumber(phoneField.getText().trim());
        emp.setDateJoined(Date.valueOf(localDate));
        emp.setPasswordHash(password);
        emp.setEmployeeType(type);

        // Populate dynamic properties according to selected category
        try {
            if ("Outsource".equalsIgnoreCase(type)) {
                emp.setHourlyRate(!hourlyRateField.getText().trim().isEmpty() ? new BigDecimal(hourlyRateField.getText().trim()) : null);
                emp.setContractExpiry(contractExpiryPicker.getValue() != null ? Date.valueOf(contractExpiryPicker.getValue()) : null);
                emp.setAgencyId(agencyIdField.getText().trim().isEmpty() ? null : agencyIdField.getText().trim());
                emp.setMonthlySalary(null);
                emp.setLeaveBalance(null);
                emp.setBenefitsPackageId(null);
            } else {
                emp.setMonthlySalary(!monthlySalaryField.getText().trim().isEmpty() ? new BigDecimal(monthlySalaryField.getText().trim()) : null);
                emp.setLeaveBalance(!leaveBalanceField.getText().trim().isEmpty() ? Integer.parseInt(leaveBalanceField.getText().trim()) : null);
                emp.setBenefitsPackageId(benefitsPkgField.getText().trim().isEmpty() ? null : benefitsPkgField.getText().trim());
                emp.setHourlyRate(null);
                emp.setContractExpiry(null);
                emp.setAgencyId(null);
            }

            emp.setDepartmentManaged("Manager".equalsIgnoreCase(type) && !deptManagedField.getText().trim().isEmpty() ? deptManagedField.getText().trim() : null);
            emp.setAdminLevel("Admin".equalsIgnoreCase(type) && !adminLevelField.getText().trim().isEmpty() ? Integer.parseInt(adminLevelField.getText().trim()) : null);

        } catch (NumberFormatException e) {
            showStatus("Please check numeric inputs (Salaries, Rates, Balances, Admin Levels).", true);
            return;
        }

        // Execute SQL statement via DAO
        boolean success = employeeDAO.saveOrUpdate(emp, isUpdate);
        if (success) {
            showStatus(isUpdate ? "Employee updated successfully!" : "Employee added successfully!", false);
            handleClearForm();
            loadData();
            setupSearch();
        } else {
            showStatus("Database operation failed. Verify primary/foreign keys.", true);
        }
    }

    @FXML
    private void handleEditSelected() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Please select an employee to edit.", true);
            return;
        }

        selectedEmployee = selected;
        formTitleLabel.setText("✏️ Edit " + selected.getEmployeeId());

        // Base values
        empIdField.setText(selected.getEmployeeId());
        empIdField.setDisable(true); // Disable primary key editing
        nameField.setText(selected.getName());
        emailField.setText(selected.getEmail());
        phoneField.setText(selected.getPhoneNumber());
        typeComboBox.setValue(selected.getEmployeeType());
        if (selected.getDateJoined() != null) {
            dateJoinedPicker.setValue(selected.getDateJoined().toLocalDate());
        }
        passwordField.setText(selected.getPasswordHash());

        // Type-specific values
        monthlySalaryField.setText(selected.getMonthlySalary() != null ? selected.getMonthlySalary().toString() : "");
        leaveBalanceField.setText(selected.getLeaveBalance() != null ? selected.getLeaveBalance().toString() : "");
        benefitsPkgField.setText(selected.getBenefitsPackageId() != null ? selected.getBenefitsPackageId() : "");

        hourlyRateField.setText(selected.getHourlyRate() != null ? selected.getHourlyRate().toString() : "");
        if (selected.getContractExpiry() != null) {
            contractExpiryPicker.setValue(selected.getContractExpiry().toLocalDate());
        } else {
            contractExpiryPicker.setValue(null);
        }
        agencyIdField.setText(selected.getAgencyId() != null ? selected.getAgencyId() : "");

        deptManagedField.setText(selected.getDepartmentManaged() != null ? selected.getDepartmentManaged() : "");
        adminLevelField.setText(selected.getAdminLevel() != null ? selected.getAdminLevel().toString() : "");

        saveButton.setText("Update Employee");
    }

    @FXML
    private void handleDeleteSelected() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Select an employee from table to delete.", true);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + selected.getName() + " (" + selected.getEmployeeId() + ")?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                boolean deleted = employeeDAO.deleteEmployee(selected.getEmployeeId());
                if (deleted) {
                    showStatus("Deleted " + selected.getEmployeeId(), false);
                    handleClearForm();
                    loadData();
                    setupSearch();
                } else {
                    showStatus("Failed to delete employee. Check database constraints.", true);
                }
            }
        });
    }

    @FXML
    private void handleClearForm() {
        selectedEmployee = null;
        formTitleLabel.setText("➕ Add New Employee");
        empIdField.setDisable(false);
        empIdField.clear();
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        typeComboBox.getSelectionModel().select("Permanent");
        dateJoinedPicker.setValue(LocalDate.now());
        passwordField.clear();

        monthlySalaryField.clear();
        leaveBalanceField.clear();
        benefitsPkgField.clear();

        hourlyRateField.clear();
        contractExpiryPicker.setValue(null);
        agencyIdField.clear();

        deptManagedField.clear();
        adminLevelField.clear();

        saveButton.setText("Save Employee");
        statusLabel.setText("");
    }

    private void showStatus(String msg, boolean isError) {
        statusLabel.setStyle(isError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
        statusLabel.setText(msg);
    }
}