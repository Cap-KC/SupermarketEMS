package com.example.supermarketems.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/supermarket_ems";
    private static final String USER = "root"; //  MySQL username
    private static final String PASSWORD = ""; //  MySQL password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // --- TEST METHOD ---
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("✅ Connected to MySQL database successfully!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Connection failed!");
            e.printStackTrace();
        }
    }
}
