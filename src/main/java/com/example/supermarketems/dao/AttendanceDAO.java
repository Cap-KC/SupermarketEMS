package com.example.supermarketems.dao;

import com.example.supermarketems.database.DatabaseConnection;
import com.example.supermarketems.model.Attendance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AttendanceDAO {

    /**
     * Alias for isEmployeeClockedIn to resolve controller compilation errors.
     */
    public boolean hasActiveClockIn(String employeeId) {
        return isEmployeeClockedIn(employeeId);
    }

    /**
     * Checks if an employee is currently clocked in.
     */
    public boolean isEmployeeClockedIn(String employeeId) {
        String sql = "SELECT COUNT(*) FROM time_logs " +
                "WHERE employee_id = ? AND clock_out_time IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("=== ERROR CHECKING CLOCK-IN STATUS ===");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Overloaded Clock-In taking 1 argument (defaults method/token to MANUAL).
     */
    public boolean clockIn(String employeeId) {
        return clockIn(employeeId, "MANUAL");
    }

    /**
     * Records a Clock-In entry into time_logs table.
     */
    public boolean clockIn(String employeeId, String method) {
        String sql = "INSERT INTO time_logs (log_id, employee_id, clock_in_time, qr_code_token) " +
                "VALUES (?, ?, CURRENT_TIMESTAMP, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "LOG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            stmt.setString(2, employeeId);
            stmt.setString(3, method != null ? method : "MANUAL");

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("=== ERROR CLOCKING IN EMPLOYEE ===");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Records a Clock-Out entry and updates overtime_hours.
     */
    public boolean clockOut(String employeeId) {
        String sql = "UPDATE time_logs " +
                "SET clock_out_time = CURRENT_TIMESTAMP, " +
                "    overtime_hours = GREATEST(0, ROUND((TIMESTAMPDIFF(MINUTE, clock_in_time, CURRENT_TIMESTAMP) / 60.0) - 8.0, 2)) " +
                "WHERE employee_id = ? AND clock_out_time IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employeeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("=== ERROR CLOCKING OUT EMPLOYEE ===");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fetches attendance and overtime records for a single employee.
     * Fixes 'Cannot resolve method getAttendanceByEmployeeId'.
     */
    public List<Attendance> getAttendanceByEmployeeId(String employeeId) {
        List<Attendance> list = new ArrayList<>();

        String sql = "SELECT t.log_id, t.employee_id, e.name AS employee_name, " +
                "       DATE(t.clock_in_time) AS log_date, " +
                "       TIME(t.clock_in_time) AS clock_in, " +
                "       TIME(t.clock_out_time) AS clock_out, " +
                "       ROUND(TIMESTAMPDIFF(MINUTE, t.clock_in_time, COALESCE(t.clock_out_time, CURRENT_TIMESTAMP)) / 60.0, 2) AS work_hours, " +
                "       COALESCE(t.overtime_hours, 0.0) AS overtime_hours, " +
                "       COALESCE(t.qr_code_token, 'MANUAL') AS scan_method " +
                "FROM time_logs t " +
                "LEFT JOIN employees e ON t.employee_id = e.employee_id " +
                "WHERE t.employee_id = ? " +
                "ORDER BY t.clock_in_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Attendance log = mapResultSetToAttendance(rs);
                    list.add(log);
                }
            }
        } catch (SQLException e) {
            System.err.println("=== ERROR FETCHING EMPLOYEE ATTENDANCE LOGS ===");
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Fetches all attendance and overtime records from time_logs table.
     */
    public List<Attendance> getAllAttendanceLogs() {
        List<Attendance> list = new ArrayList<>();

        String sql = "SELECT t.log_id, t.employee_id, e.name AS employee_name, " +
                "       DATE(t.clock_in_time) AS log_date, " +
                "       TIME(t.clock_in_time) AS clock_in, " +
                "       TIME(t.clock_out_time) AS clock_out, " +
                "       ROUND(TIMESTAMPDIFF(MINUTE, t.clock_in_time, COALESCE(t.clock_out_time, CURRENT_TIMESTAMP)) / 60.0, 2) AS work_hours, " +
                "       COALESCE(t.overtime_hours, 0.0) AS overtime_hours, " +
                "       COALESCE(t.qr_code_token, 'MANUAL') AS scan_method " +
                "FROM time_logs t " +
                "LEFT JOIN employees e ON t.employee_id = e.employee_id " +
                "ORDER BY t.clock_in_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Attendance log = mapResultSetToAttendance(rs);
                list.add(log);
            }
        } catch (SQLException e) {
            System.err.println("=== ERROR FETCHING ALL ATTENDANCE LOGS ===");
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Fetches all currently active shifts (where clock_out_time is NULL).
     */
    public List<Attendance> getActiveShifts() {
        List<Attendance> list = new ArrayList<>();

        String sql = "SELECT t.log_id, t.employee_id, e.name AS employee_name, " +
                "       DATE(t.clock_in_time) AS log_date, " +
                "       TIME(t.clock_in_time) AS clock_in, " +
                "       COALESCE(t.qr_code_token, 'MANUAL') AS scan_method " +
                "FROM time_logs t " +
                "LEFT JOIN employees e ON t.employee_id = e.employee_id " +
                "WHERE t.clock_out_time IS NULL " +
                "ORDER BY t.clock_in_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Attendance log = new Attendance();
                log.setAttendanceId(rs.getString("log_id"));
                log.setEmployeeId(rs.getString("employee_id"));
                log.setEmployeeName(rs.getString("employee_name") != null ? rs.getString("employee_name") : "N/A");
                log.setDate(rs.getString("log_date"));
                log.setClockInTime(rs.getString("clock_in") != null ? rs.getString("clock_in") : "--:--");
                log.setClockOutTime("Active Shift");
                log.setMethod(rs.getString("scan_method"));

                list.add(log);
            }
        } catch (SQLException e) {
            System.err.println("=== ERROR FETCHING ACTIVE SHIFTS ===");
            e.printStackTrace();
        }
        return list;
    }

    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance log = new Attendance();
        log.setAttendanceId(rs.getString("log_id"));
        log.setEmployeeId(rs.getString("employee_id"));
        log.setEmployeeName(rs.getString("employee_name") != null ? rs.getString("employee_name") : "N/A");
        log.setDate(rs.getString("log_date"));
        log.setClockInTime(rs.getString("clock_in") != null ? rs.getString("clock_in") : "--:--");
        log.setClockOutTime(rs.getString("clock_out") != null ? rs.getString("clock_out") : "Active Shift");
        log.setWorkHours(rs.getDouble("work_hours"));
        log.setOvertimeHours(rs.getDouble("overtime_hours"));
        log.setMethod(rs.getString("scan_method"));

        if (log.getOvertimeHours() > 0) {
            log.setOvertimeStatus("PENDING");
        } else {
            log.setOvertimeStatus("NONE");
        }
        return log;
    }
}