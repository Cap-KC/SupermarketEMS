package com.example.supermarketems.dao;

import com.example.supermarketems.database.DatabaseConnection;
import com.example.supermarketems.model.Schedules;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SchedulesDAO {

    /**
     * Fetch all schedules joined with employee (e) and manager (m) details.
     * Uses LEFT JOINs so records with missing/unmatched employee_id or managed_by still display.
     */
    public List<Schedules> getAllSchedules() {
        List<Schedules> list = new ArrayList<>();

        // Changed JOIN to LEFT JOIN for 'e' to prevent hiding rows with missing/unmatched employee records
        String sql = "SELECT s.schedule_id, s.employee_id, e.name AS employee_name, " +
                "s.shift_date, s.start_time, s.end_time, s.department, s.managed_by, " +
                "m.name AS manager_name " +
                "FROM schedules s " +
                "LEFT JOIN employees e ON TRIM(s.employee_id) = TRIM(e.employee_id) " +
                "LEFT JOIN employees m ON TRIM(s.managed_by) = TRIM(m.employee_id) " +
                "ORDER BY s.shift_date DESC, s.start_time ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToSchedules(rs));
            }

            System.out.println("DEBUG [SchedulesDAO]: Successfully loaded " + list.size() + " schedules.");

        } catch (SQLException e) {
            System.err.println("=== ERROR FETCHING SCHEDULES ===");
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Insert a new schedule with both foreign keys.
     */
    public boolean addSchedule(Schedules schedule) {
        String sql = "INSERT INTO schedules (schedule_id, employee_id, shift_date, start_time, end_time, department, managed_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, schedule.getScheduleId());
            stmt.setString(2, schedule.getEmployeeId());

            if (schedule.getShiftDate() != null) {
                stmt.setDate(3, Date.valueOf(schedule.getShiftDate()));
            } else {
                stmt.setNull(3, Types.DATE);
            }

            if (schedule.getStartTime() != null) {
                stmt.setTime(4, Time.valueOf(schedule.getStartTime()));
            } else {
                stmt.setNull(4, Types.TIME);
            }

            if (schedule.getEndTime() != null) {
                stmt.setTime(5, Time.valueOf(schedule.getEndTime()));
            } else {
                stmt.setNull(5, Types.TIME);
            }

            stmt.setString(6, schedule.getDepartment());
            stmt.setString(7, schedule.getManagedBy());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("=== ERROR INSERTING SCHEDULE ===");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update an existing schedule.
     */
    public boolean updateSchedule(Schedules schedule) {
        String sql = "UPDATE schedules SET employee_id = ?, shift_date = ?, start_time = ?, " +
                "end_time = ?, department = ?, managed_by = ? WHERE schedule_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, schedule.getEmployeeId());
            stmt.setDate(2, schedule.getShiftDate() != null ? Date.valueOf(schedule.getShiftDate()) : null);
            stmt.setTime(3, schedule.getStartTime() != null ? Time.valueOf(schedule.getStartTime()) : null);
            stmt.setTime(4, schedule.getEndTime() != null ? Time.valueOf(schedule.getEndTime()) : null);
            stmt.setString(5, schedule.getDepartment());
            stmt.setString(6, schedule.getManagedBy());
            stmt.setString(7, schedule.getScheduleId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("=== ERROR UPDATING SCHEDULE ===");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a schedule by ID.
     */
    public boolean deleteSchedule(String scheduleId) {
        String sql = "DELETE FROM schedules WHERE schedule_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, scheduleId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("=== ERROR DELETING SCHEDULE ===");
            e.printStackTrace();
            return false;
        }
    }

    private Schedules mapResultSetToSchedules(ResultSet rs) throws SQLException {
        Date dateVal = rs.getDate("shift_date");
        Time startTimeVal = rs.getTime("start_time");
        Time endTimeVal = rs.getTime("end_time");

        // Safe fallbacks if joined names return NULL
        String empId = rs.getString("employee_id");
        String empName = rs.getString("employee_name");
        if (empName == null || empName.trim().isEmpty()) {
            empName = empId != null ? empId : "Unknown Employee";
        }

        String mgrId = rs.getString("managed_by");
        String mgrName = rs.getString("manager_name");
        if (mgrName == null || mgrName.trim().isEmpty()) {
            mgrName = mgrId != null ? mgrId : "N/A";
        }

        return new Schedules(
                rs.getString("schedule_id"),
                empId,
                empName,
                dateVal != null ? dateVal.toLocalDate() : null,
                startTimeVal != null ? startTimeVal.toLocalTime() : null,
                endTimeVal != null ? endTimeVal.toLocalTime() : null,
                rs.getString("department"),
                mgrId,
                mgrName
        );
    }
}