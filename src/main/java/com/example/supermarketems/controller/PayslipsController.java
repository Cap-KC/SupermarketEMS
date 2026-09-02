package com.example.supermarketems.controller;

import com.example.supermarketems.dao.PayslipDAO;
import com.example.supermarketems.model.Employee;
import com.example.supermarketems.model.Payslip;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;

public class PayslipsController {

    @FXML private Label employeeNameLabel;
    @FXML private TableView<Payslip> payslipTable;
    @FXML private TableColumn<Payslip, String> colPayPeriod;
    @FXML private TableColumn<Payslip, Double> colBasicSalary;
    @FXML private TableColumn<Payslip, Double> colAllowances;
    @FXML private TableColumn<Payslip, Double> colDeductions;
    @FXML private TableColumn<Payslip, Double> colNetPay;

    private Employee loggedInEmployee;
    private final PayslipDAO payslipDAO = new PayslipDAO();
    private final ObservableList<Payslip> payslipList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
    }

    /**
     * Receives active employee session from dashboard scene / modal router.
     */
    public void setLoggedInEmployee(Employee employee) {
        this.loggedInEmployee = employee;
        if (employee != null) {
            if (employeeNameLabel != null) {
                employeeNameLabel.setText("Payment records for " + employee.getName() + " (" + employee.getEmployeeId() + ")");
            }
            loadPayslipData();
        }
    }

    private void setupTableColumns() {
        if (colPayPeriod != null) colPayPeriod.setCellValueFactory(new PropertyValueFactory<>("payPeriod"));
        if (colBasicSalary != null) colBasicSalary.setCellValueFactory(new PropertyValueFactory<>("basicSalary"));
        if (colAllowances != null) colAllowances.setCellValueFactory(new PropertyValueFactory<>("allowances"));
        if (colDeductions != null) colDeductions.setCellValueFactory(new PropertyValueFactory<>("deductions"));
        if (colNetPay != null) colNetPay.setCellValueFactory(new PropertyValueFactory<>("netPay"));
    }

    private void loadPayslipData() {
        if (loggedInEmployee == null || payslipTable == null) return;

        payslipList.clear();

        // Fetch real records from MySQL database
        payslipList.addAll(payslipDAO.getPayslipsByEmployeeId(loggedInEmployee.getEmployeeId()));

        payslipTable.setItems(payslipList);
    }

    @FXML
    private void handleDownloadPdf(ActionEvent event) {
        Payslip selected = payslipTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a payslip record to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Payslip Summary");
        fileChooser.setInitialFileName("Payslip_" + selected.getEmployeeId() + "_" + selected.getPayPeriod().replace(" ", "_") + ".txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("=========================================");
                writer.println("           PAYSLIP STATEMENT             ");
                writer.println("=========================================");
                writer.println("Employee ID : " + selected.getEmployeeId());
                writer.println("Employee Name: " + (loggedInEmployee != null ? loggedInEmployee.getName() : "N/A"));
                writer.println("Pay Period   : " + selected.getPayPeriod());
                writer.println("-----------------------------------------");
                writer.println("Basic Salary : $" + String.format("%.2f", selected.getBasicSalary()));
                writer.println("Allowances   : $" + String.format("%.2f", selected.getAllowances()));
                writer.println("Deductions   : $" + String.format("%.2f", selected.getDeductions()));
                writer.println("-----------------------------------------");
                writer.println("NET PAY      : $" + String.format("%.2f", selected.getNetPay()));
                writer.println("=========================================");

                showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Payslip exported to: " + file.getAbsolutePath());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Export Failed", "Could not save file: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if (stage != null) {
            stage.close();
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