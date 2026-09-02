package com.example.supermarketems.model;

public class Payslip {
    private String payslipId;
    private String employeeId;
    private String payPeriod; // e.g., "March 2026" or "2026-03"
    private double basicSalary;
    private double allowances;
    private double deductions;
    private double netPay;

    // Default Constructor
    public Payslip() {}

    // Parameterized Constructor
    public Payslip(String payslipId, String employeeId, String payPeriod,
                   double basicSalary, double allowances, double deductions, double netPay) {
        this.payslipId = payslipId;
        this.employeeId = employeeId;
        this.payPeriod = payPeriod;
        this.basicSalary = basicSalary;
        this.allowances = allowances;
        this.deductions = deductions;
        this.netPay = netPay;
    }

    // Getters and Setters
    public String getPayslipId() {
        return payslipId;
    }

    public void setPayslipId(String payslipId) {
        this.payslipId = payslipId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getPayPeriod() {
        return payPeriod;
    }

    public void setPayPeriod(String payPeriod) {
        this.payPeriod = payPeriod;
    }

    public double getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(double basicSalary) {
        this.basicSalary = basicSalary;
    }

    public double getAllowances() {
        return allowances;
    }

    public void setAllowances(double allowances) {
        this.allowances = allowances;
    }

    public double getDeductions() {
        return deductions;
    }

    public void setDeductions(double deductions) {
        this.deductions = deductions;
    }

    public double getNetPay() {
        return netPay;
    }

    public void setNetPay(double netPay) {
        this.netPay = netPay;
    }
}