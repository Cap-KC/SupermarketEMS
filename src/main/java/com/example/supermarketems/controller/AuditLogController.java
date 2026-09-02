package com.example.supermarketems.controller;

import com.example.supermarketems.dao.AuditLogDAO;
import com.example.supermarketems.model.AuditLog;
import com.example.supermarketems.model.Employee;

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
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AuditLogController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> actionTypeFilterComboBox;
    @FXML private TableView<AuditLog> auditTable;
    @FXML private TableColumn<AuditLog, String> colLogId;
    @FXML private TableColumn<AuditLog, String> colPerformedBy;
    @FXML private TableColumn<AuditLog, String> colActionType;
    @FXML private TableColumn<AuditLog, String> colDescription;
    @FXML private TableColumn<AuditLog, String> colIpAddress;
    @FXML private TableColumn<AuditLog, String> colTimestamp;

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    private final ObservableList<AuditLog> logList = FXCollections.observableArrayList();
    private Employee currentAdmin;

    public void setLoggedInEmployee(Employee employee) {
        this.currentAdmin = employee;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        loadAuditLogs(); // Load data FIRST

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
        if (actionTypeFilterComboBox != null) {
            actionTypeFilterComboBox.setOnAction(e -> applyFilters());
        }
    }

    private void setupTableColumns() {
        colLogId.setCellValueFactory(new PropertyValueFactory<>("logId"));
        colPerformedBy.setCellValueFactory(new PropertyValueFactory<>("performedBy"));
        colActionType.setCellValueFactory(new PropertyValueFactory<>("actionType"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colIpAddress.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        colTimestamp.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        (cell.getValue() != null && cell.getValue().getTimestamp() != null)
                                ? dateFormat.format(cell.getValue().getTimestamp())
                                : ""
                )
        );

        auditTable.setItems(logList);
    }

    private void loadAuditLogs() {
        logList.clear();
        List<AuditLog> logs = auditLogDAO.getAllLogs();

        System.out.println("AuditLogController: Retrieved " + logs.size() + " records from DB.");
        logList.addAll(logs);

        // Dynamically populate filter combo box based on actual database values
        populateDynamicActionTypeFilter(logs);

        // Ensure fresh display
        applyFilters();
    }

    /**
     * Dynamically populates the ComboBox with action types that actually exist in the database.
     */
    private void populateDynamicActionTypeFilter(List<AuditLog> logs) {
        if (actionTypeFilterComboBox != null) {
            Set<String> actionTypes = new HashSet<>();
            actionTypes.add("All");

            for (AuditLog log : logs) {
                if (log.getActionType() != null && !log.getActionType().trim().isEmpty()) {
                    actionTypes.add(log.getActionType().trim());
                }
            }

            String currentSelection = actionTypeFilterComboBox.getValue();
            actionTypeFilterComboBox.setItems(FXCollections.observableArrayList(actionTypes));

            if (currentSelection != null && actionTypes.contains(currentSelection)) {
                actionTypeFilterComboBox.getSelectionModel().select(currentSelection);
            } else {
                actionTypeFilterComboBox.getSelectionModel().select("All");
            }
        }
    }

    private void applyFilters() {
        String keyword = (searchField != null && searchField.getText() != null)
                ? searchField.getText().toLowerCase().trim()
                : "";
        String selectedType = (actionTypeFilterComboBox != null && actionTypeFilterComboBox.getValue() != null)
                ? actionTypeFilterComboBox.getValue()
                : "All";

        ObservableList<AuditLog> filtered = FXCollections.observableArrayList();

        for (AuditLog log : logList) {
            boolean matchesType = selectedType.equalsIgnoreCase("All") ||
                    (log.getActionType() != null && log.getActionType().equalsIgnoreCase(selectedType));

            boolean matchesSearch = keyword.isEmpty() ||
                    (log.getLogId() != null && log.getLogId().toLowerCase().contains(keyword)) ||
                    (log.getPerformedBy() != null && log.getPerformedBy().toLowerCase().contains(keyword)) ||
                    (log.getActionType() != null && log.getActionType().toLowerCase().contains(keyword)) ||
                    (log.getDescription() != null && log.getDescription().toLowerCase().contains(keyword)) ||
                    (log.getIpAddress() != null && log.getIpAddress().toLowerCase().contains(keyword));

            if (matchesType && matchesSearch) {
                filtered.add(log);
            }
        }
        auditTable.setItems(filtered);
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        if (searchField != null) searchField.clear();
        loadAuditLogs();
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/supermarketems/admin-dashboard-view.fxml"));
            Parent root = loader.load();

            Object targetController = loader.getController();
            if (targetController != null && currentAdmin != null) {
                try {
                    Method method = targetController.getClass().getDeclaredMethod("setLoggedInEmployee", Employee.class);
                    method.setAccessible(true);
                    method.invoke(targetController, currentAdmin);
                } catch (NoSuchMethodException ignored) {
                    // Controller does not handle user sessions
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Supermarket EMS - Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}