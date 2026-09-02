package com.example.supermarketems.model;

public class Payroll {
    private String payrollId;
    private String employeeId;
    private String employeeName;
    private String payPeriod;
    private double basicSalary;
    private double allowances;
    private double deductions;
    private double netPay;
    private String status; // e.g., "Pending", "Paid"

    public Payroll(String payrollId, String employeeId, String employeeName,
                   String payPeriod, double basicSalary, double allowances,
                   double deductions, double netPay, String status) {
        this.payrollId = payrollId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.payPeriod = payPeriod;
        this.basicSalary = basicSalary;
        this.allowances = allowances;
        this.deductions = deductions;
        this.netPay = netPay;
        this.status = status;
    }

    // Getters and Setters for all fields...
}