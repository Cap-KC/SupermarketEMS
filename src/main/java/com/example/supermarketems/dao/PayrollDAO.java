package com.example.supermarketems.dao;

import com.example.supermarketems.model.PayrollRecord;
import com.example.supermarketems.database.DatabaseConnection; // Adjust import to your DB connection class

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PayrollDAO {

    /**
     * Retrieve all payroll records from payroll_records table
     */
    public List<PayrollRecord> getAllPayrollRecords() {
        List<PayrollRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM payroll_records ORDER BY pay_period_end DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                records.add(mapResultSetToPayroll(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching payroll records: " + e.getMessage());
            e.printStackTrace();
        }
        return records;
    }

    /**
     * Insert a new payroll record
     */
    public boolean savePayrollRecord(PayrollRecord record) {
        String sql = "INSERT INTO payroll_records (payroll_id, employee_id, pay_period_start, pay_period_end, " +
                "basic_salary, overtime_pay, allowances, deductions, net_pay, payment_status, payment_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, record.getPayrollId());
            stmt.setString(2, record.getEmployeeId());
            stmt.setDate(3, record.getPayPeriodStart());
            stmt.setDate(4, record.getPayPeriodEnd());
            stmt.setDouble(5, record.getBasicSalary());
            stmt.setDouble(6, record.getOvertimePay());
            stmt.setDouble(7, record.getAllowances());
            stmt.setDouble(8, record.getDeductions());
            stmt.setDouble(9, record.getNetPay());
            stmt.setString(10, record.getPaymentStatus());
            stmt.setTimestamp(11, record.getPaymentDate());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting payroll record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update payment status and timestamp (e.g., Marking as 'Paid')
     */
    public boolean updatePaymentStatus(String payrollId, String newStatus) {
        String sql = "UPDATE payroll_records SET payment_status = ?, payment_date = ? WHERE payroll_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setTimestamp(2, "Paid".equalsIgnoreCase(newStatus) ? new Timestamp(System.currentTimeMillis()) : null);
            stmt.setString(3, payrollId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a payroll record by payroll_id
     */
    public boolean deletePayrollRecord(String payrollId) {
        String sql = "DELETE FROM payroll_records WHERE payroll_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, payrollId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting payroll record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Helper method to map SQL ResultSet to PayrollRecord object
     */
    private PayrollRecord mapResultSetToPayroll(ResultSet rs) throws SQLException {
        return new PayrollRecord(
                rs.getString("payroll_id"),
                rs.getString("employee_id"),
                rs.getDate("pay_period_start"),
                rs.getDate("pay_period_end"),
                rs.getDouble("basic_salary"),
                rs.getDouble("overtime_pay"),
                rs.getDouble("allowances"),
                rs.getDouble("deductions"),
                rs.getDouble("net_pay"),
                rs.getString("payment_status"),
                rs.getTimestamp("payment_date")
        );
    }
}