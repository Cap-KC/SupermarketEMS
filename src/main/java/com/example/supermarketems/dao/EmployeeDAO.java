package com.example.supermarketems.dao;

import com.example.supermarketems.database.DatabaseConnection;
import com.example.supermarketems.model.Employee;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    // 1. READ ALL: Fetch all employees with agency names (via LEFT JOIN)
    public List<Employee> getAllEmployees() {
        List<Employee> employeeList = new ArrayList<>();

        String sql = "SELECT e.*, a.agency_name " +
                "FROM employees e " +
                "LEFT JOIN external_agencies a ON e.agency_id = a.agency_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                employeeList.add(mapResultSetToEmployee(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employeeList;
    }

    // 2. READ BY ID: Fetch employee by employee_id
    public Employee getEmployeeById(String employeeId) {
        String sql = "SELECT e.*, a.agency_name " +
                "FROM employees e " +
                "LEFT JOIN external_agencies a ON e.agency_id = a.agency_id " +
                "WHERE e.employee_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 3. READ BY EMAIL: Fetch employee by email for validation
    public Employee getEmployeeByEmail(String email) {
        String sql = "SELECT e.*, a.agency_name " +
                "FROM employees e " +
                "LEFT JOIN external_agencies a ON e.agency_id = a.agency_id " +
                "WHERE e.email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 4. AUTHENTICATE / LOGIN
    public Employee authenticate(String email, String password) {
        String sql = "SELECT e.*, a.agency_name " +
                "FROM employees e " +
                "LEFT JOIN external_agencies a ON e.agency_id = a.agency_id " +
                "WHERE e.email = ? AND e.password_hash = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 5. CREATE / UPDATE: Save new employee or edit existing record
    public boolean saveOrUpdate(Employee emp, boolean isUpdate) {
        String sql;
        if (isUpdate) {
            sql = "UPDATE employees SET name=?, email=?, phone_number=?, employee_type=?, " +
                    "date_joined=?, password_hash=?, monthly_salary=?, leave_balance=?, " +
                    "benefits_package_id=?, hourly_rate=?, contract_expiry=?, agency_id=?, " +
                    "department_managed=?, admin_level=? WHERE employee_id=?";
        } else {
            sql = "INSERT INTO employees (name, email, phone_number, employee_type, " +
                    "date_joined, password_hash, monthly_salary, leave_balance, " +
                    "benefits_package_id, hourly_rate, contract_expiry, agency_id, " +
                    "department_managed, admin_level, employee_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, emp.getName());
            stmt.setString(2, emp.getEmail());
            stmt.setString(3, emp.getPhoneNumber());
            stmt.setString(4, emp.getEmployeeType());
            stmt.setDate(5, emp.getDateJoined());
            stmt.setString(6, emp.getPasswordHash());

            // Handle Nullable Numeric/Object SQL Parameters
            if (emp.getMonthlySalary() != null) stmt.setBigDecimal(7, emp.getMonthlySalary());
            else stmt.setNull(7, Types.DECIMAL);

            if (emp.getLeaveBalance() != null) stmt.setInt(8, emp.getLeaveBalance());
            else stmt.setNull(8, Types.INTEGER);

            stmt.setString(9, emp.getBenefitsPackageId());

            if (emp.getHourlyRate() != null) stmt.setBigDecimal(10, emp.getHourlyRate());
            else stmt.setNull(10, Types.DECIMAL);

            stmt.setDate(11, emp.getContractExpiry());
            stmt.setString(12, emp.getAgencyId());
            stmt.setString(13, emp.getDepartmentManaged());

            if (emp.getAdminLevel() != null) stmt.setInt(14, emp.getAdminLevel());
            else stmt.setNull(14, Types.INTEGER);

            stmt.setString(15, emp.getEmployeeId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 6. DELETE: Remove employee by ID
    public boolean deleteEmployee(String employeeId) {
        String sql = "DELETE FROM employees WHERE employee_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employeeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 7. COUNT: Get total employee count
    public int getTotalEmployeeCount() {
        String sql = "SELECT COUNT(*) FROM employees";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching total employee count: " + e.getMessage());
        }
        return 0;
    }

    // 8. COUNT: Get currently clocked in / on duty employee count
    public int getOnDutyCount() {
        // Queries time_logs for active clock-ins (clock_out_time IS NULL) on today's date
        String sql = "SELECT COUNT(DISTINCT employee_id) FROM time_logs " +
                "WHERE clock_out_time IS NULL AND DATE(clock_in_time) = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Explicitly set today's local system date
            stmt.setDate(1, Date.valueOf(LocalDate.now()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching on-duty count: " + e.getMessage());
        }
        return 0;
    }

    // Helper method to map ResultSet rows into Employee objects
    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        Employee emp = new Employee();

        // Base fields
        emp.setEmployeeId(rs.getString("employee_id"));
        emp.setName(rs.getString("name"));
        emp.setEmail(rs.getString("email"));
        emp.setPhoneNumber(rs.getString("phone_number"));
        emp.setDateJoined(rs.getDate("date_joined"));
        emp.setPasswordHash(rs.getString("password_hash"));
        emp.setEmployeeType(rs.getString("employee_type"));

        // Permanent fields
        emp.setMonthlySalary(rs.getBigDecimal("monthly_salary"));
        emp.setLeaveBalance((Integer) rs.getObject("leave_balance"));
        emp.setBenefitsPackageId(rs.getString("benefits_package_id"));

        // Outsource fields
        emp.setHourlyRate(rs.getBigDecimal("hourly_rate"));
        emp.setContractExpiry(rs.getDate("contract_expiry"));
        emp.setAgencyId(rs.getString("agency_id"));
        emp.setAgencyName(rs.getString("agency_name")); // From LEFT JOIN

        // Role-specific fields
        emp.setDepartmentManaged(rs.getString("department_managed"));
        emp.setAdminLevel((Integer) rs.getObject("admin_level"));

        return emp;
    }
}