package com.example.supermarketems.dao;

import com.example.supermarketems.database.DatabaseConnection;
import com.example.supermarketems.model.LeaveRequest;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LeaveRequestDAO {

    /**
     * Fetches the remaining leave balance/days for a specific employee.
     * Resolves 'Cannot resolve method getRemainingLeaveDays'
     */
    public int getRemainingLeaveDays(String employeeId) {
        String sql = "SELECT leave_balance FROM employees WHERE TRIM(employee_id) = TRIM(?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("leave_balance");
                }
            }
        } catch (SQLException e) {
            System.err.println("=== ERROR FETCHING REMAINING LEAVE DAYS ===");
            e.printStackTrace();
        }
        return 0; // Default fallback if not found or on error
    }

    /**
     * Fetch all leave requests joined with employee and approver details.
     */
    public List<LeaveRequest> getAllLeaveRequests() {
        List<LeaveRequest> list = new ArrayList<>();
        String sql = "SELECT l.request_id, l.employee_id, e.name AS employee_name, " +
                "l.leave_type, l.start_date, l.end_date, l.reason, l.status, " +
                "l.approved_by, a.name AS approver_name " +
                "FROM leave_requests l " +
                "LEFT JOIN employees e ON TRIM(l.employee_id) = TRIM(e.employee_id) " +
                "LEFT JOIN employees a ON TRIM(l.approved_by) = TRIM(a.employee_id) " +
                "ORDER BY l.start_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToLeaveRequest(rs));
            }
        } catch (SQLException e) {
            System.err.println("=== ERROR FETCHING LEAVE REQUESTS ===");
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Fetches leave applications for a specific employee by ID.
     */
    public List<LeaveRequest> getLeaveRequestsByEmployeeId(String employeeId) {
        List<LeaveRequest> list = new ArrayList<>();
        String sql = "SELECT l.request_id, l.employee_id, e.name AS employee_name, " +
                "l.leave_type, l.start_date, l.end_date, l.reason, l.status, " +
                "l.approved_by, a.name AS approver_name " +
                "FROM leave_requests l " +
                "LEFT JOIN employees e ON TRIM(l.employee_id) = TRIM(e.employee_id) " +
                "LEFT JOIN employees a ON TRIM(l.approved_by) = TRIM(a.employee_id) " +
                "WHERE TRIM(l.employee_id) = TRIM(?) " +
                "ORDER BY l.start_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLeaveRequest(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("=== ERROR FETCHING EMPLOYEE LEAVE REQUESTS ===");
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Submit a new leave request. Default status is 'Pending'.
     */
    public boolean addLeaveRequest(LeaveRequest leave) {
        String sql = "INSERT INTO leave_requests (request_id, employee_id, leave_type, start_date, end_date, reason, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, leave.getRequestId());
            stmt.setString(2, leave.getEmployeeId());
            stmt.setString(3, leave.getLeaveType()); // ENUM: 'Casual', 'Medical', 'Annual', 'Unpaid'
            stmt.setDate(4, Date.valueOf(leave.getStartDate()));
            stmt.setDate(5, Date.valueOf(leave.getEndDate()));
            stmt.setString(6, leave.getReason());
            stmt.setString(7, leave.getStatus() != null ? leave.getStatus() : "Pending"); // ENUM: 'Pending'

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("=== ERROR INSERTING LEAVE REQUEST ===");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update leave status ('Approved' or 'Rejected') and assign the approver's employee_id.
     */
    public boolean updateLeaveStatus(String requestId, String status, String approvedBy) {
        String sql = "UPDATE leave_requests SET status = ?, approved_by = ? WHERE request_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status); // ENUM: 'Approved' or 'Rejected'
            stmt.setString(2, approvedBy);
            stmt.setString(3, requestId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("=== ERROR UPDATING LEAVE STATUS ===");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a leave request by ID.
     */
    public boolean deleteLeaveRequest(String requestId) {
        String sql = "DELETE FROM leave_requests WHERE request_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, requestId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("=== ERROR DELETING LEAVE REQUEST ===");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fetch count of leave requests currently pending approval.
     */
    public int getPendingLeaveCount() {
        String sql = "SELECT COUNT(*) FROM leave_requests WHERE LOWER(status) = 'pending'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("=== ERROR FETCHING PENDING LEAVE COUNT ===");
            e.printStackTrace();
        }
        return 0;
    }

    private LeaveRequest mapResultSetToLeaveRequest(ResultSet rs) throws SQLException {
        Date startDateVal = rs.getDate("start_date");
        Date endDateVal = rs.getDate("end_date");

        String empId = rs.getString("employee_id");
        String empName = rs.getString("employee_name");
        if (empName == null || empName.trim().isEmpty()) {
            empName = empId != null ? empId : "Unknown";
        }

        String appBy = rs.getString("approved_by");
        String appName = rs.getString("approver_name");
        if (appName == null || appName.trim().isEmpty()) {
            appName = appBy != null ? appBy : "N/A";
        }

        return new LeaveRequest(
                rs.getString("request_id"),
                empId,
                empName,
                rs.getString("leave_type"),
                startDateVal != null ? startDateVal.toLocalDate() : null,
                endDateVal != null ? endDateVal.toLocalDate() : null,
                rs.getString("reason"),
                rs.getString("status"),
                appBy,
                appName
        );
    }
}