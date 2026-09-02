package com.example.supermarketems.util;

import com.example.supermarketems.model.Employee;

public class UserSession {
    private static Employee currentUser;

    public static void setCurrentUser(Employee employee) {
        currentUser = employee;
    }

    public static Employee getCurrentUser() {
        return currentUser;
    }

    public static void cleanUserSession() {
        currentUser = null;
    }

    // --- HELPER METHODS ---

    /**
     * Checks if a session currently exists.
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Helper to verify if logged-in user is an Admin.
     */
    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getEmployeeType());
    }

    /**
     * Helper to verify if logged-in user is a Manager.
     */
    public static boolean isManager() {
        return currentUser != null && "MANAGER".equalsIgnoreCase(currentUser.getEmployeeType());
    }
}