package com.example.supermarketems.dao;

import com.example.supermarketems.database.DatabaseConnection;
import com.example.supermarketems.model.Payslip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PayslipDAO {

    public List<Payslip> getPayslipsByEmployeeId(String employeeId) {
        List<Payslip> list = new ArrayList<>();

        if (employeeId == null || employeeId.trim().isEmpty()) {
            System.err.println("PayslipDAO: Provided employeeId is null or empty.");
            return list;
        }

        // Query mapped directly to payroll_records table
        String sql = "SELECT payroll_id, employee_id, pay_period_start, pay_period_end, " +
                "basic_salary, overtime_pay, allowances, deductions, net_pay " +
                "FROM payroll_records " +
                "WHERE TRIM(employee_id) = TRIM(?) " +
                "ORDER BY pay_period_end DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employeeId.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String payrollId = rs.getString("payroll_id");
                    String empId = rs.getString("employee_id");

                    // Format start and end dates into a display string (e.g., "2026-07-01 to 2026-07-31")
                    String payPeriod = rs.getString("pay_period_start") + " to " + rs.getString("pay_period_end");

                    double basicSalary = rs.getDouble("basic_salary");
                    // Combine standard allowances and overtime pay for total allowances column
                    double totalAllowances = rs.getDouble("allowances") + rs.getDouble("overtime_pay");
                    double deductions = rs.getDouble("deductions");
                    double netPay = rs.getDouble("net_pay");

                    Payslip p = new Payslip(
                            payrollId,
                            empId,
                            payPeriod,
                            basicSalary,
                            totalAllowances,
                            deductions,
                            netPay
                    );
                    list.add(p);
                }
            }
            System.out.println("PayslipDAO: Loaded " + list.size() + " payroll record(s) for Employee: " + employeeId);

        } catch (SQLException e) {
            System.err.println("Error reading payroll_records: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }
}