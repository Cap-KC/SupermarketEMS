package com.example.supermarketems.dao;

import com.example.supermarketems.model.AuditLog;
import com.example.supermarketems.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuditLogDAO {

    /**
     * Utility method to log any action across the system safely.
     */
    public static boolean log(String performedBy, String actionType, String description, String ipAddress) {
        String sql = "INSERT INTO audit_logs (log_id, performed_by, action_type, description, ip_address, timestamp) " +
                "VALUES (?, ?, ?, ?, ?, NOW())";

        String logId = "LOG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String safeUser = (performedBy == null || performedBy.trim().isEmpty()) ? "SYSTEM" : performedBy.trim();
        String safeIp = (ipAddress == null || ipAddress.trim().isEmpty()) ? "127.0.0.1" : ipAddress.trim();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, logId);
            stmt.setString(2, safeUser);
            stmt.setString(3, actionType);
            stmt.setString(4, description);
            stmt.setString(5, safeIp);

            int rowsAffected = stmt.executeUpdate();
            System.out.println("AuditLogDAO: Successfully logged action [" + actionType + "] by " + safeUser);
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("AuditLogDAO SQL Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fetch all audit logs ordered from newest to oldest.
     * Made static so controllers can call AuditLogDAO.getAllLogs() directly.
     */
    public static List<AuditLog> getAllLogs() {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT log_id, performed_by, action_type, description, ip_address, timestamp " +
                "FROM audit_logs ORDER BY timestamp DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                logs.add(new AuditLog(
                        rs.getString("log_id"),
                        rs.getString("performed_by"),
                        rs.getString("action_type"),
                        rs.getString("description"),
                        rs.getString("ip_address"),
                        rs.getTimestamp("timestamp")
                ));
            }
            System.out.println("AuditLogDAO: Retained " + logs.size() + " audit log entries from database.");

        } catch (SQLException e) {
            System.err.println("AuditLogDAO Fetch Error: " + e.getMessage());
            e.printStackTrace();
        }
        return logs;
    }
}