package com.example.supermarketems.model;

import java.sql.Date;
import java.sql.Timestamp;

public class PayrollRecord {
    private String payrollId;
    private String employeeId;
    private Date payPeriodStart;
    private Date payPeriodEnd;
    private double basicSalary;
    private double overtimePay;
    private double allowances;
    private double deductions;
    private double netPay;
    private String paymentStatus; // 'Draft', 'Processed', 'Paid'
    private Timestamp paymentDate;

    // Full Constructor
    public PayrollRecord(String payrollId, String employeeId, Date payPeriodStart, Date payPeriodEnd,
                         double basicSalary, double overtimePay, double allowances, double deductions,
                         double netPay, String paymentStatus, Timestamp paymentDate) {
        this.payrollId = payrollId;
        this.employeeId = employeeId;
        this.payPeriodStart = payPeriodStart;
        this.payPeriodEnd = payPeriodEnd;
        this.basicSalary = basicSalary;
        this.overtimePay = overtimePay;
        this.allowances = allowances;
        this.deductions = deductions;
        this.netPay = netPay;
        this.paymentStatus = paymentStatus;
        this.paymentDate = paymentDate;
    }

    // Getters and Setters
    public String getPayrollId() { return payrollId; }
    public void setPayrollId(String payrollId) { this.payrollId = payrollId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public Date getPayPeriodStart() { return payPeriodStart; }
    public void setPayPeriodStart(Date payPeriodStart) { this.payPeriodStart = payPeriodStart; }

    public Date getPayPeriodEnd() { return payPeriodEnd; }
    public void setPayPeriodEnd(Date payPeriodEnd) { this.payPeriodEnd = payPeriodEnd; }

    public double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }

    public double getOvertimePay() { return overtimePay; }
    public void setOvertimePay(double overtimePay) { this.overtimePay = overtimePay; }

    public double getAllowances() { return allowances; }
    public void setAllowances(double allowances) { this.allowances = allowances; }

    public double getDeductions() { return deductions; }
    public void setDeductions(double deductions) { this.deductions = deductions; }

    public double getNetPay() { return netPay; }
    public void setNetPay(double netPay) { this.netPay = netPay; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Timestamp getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Timestamp paymentDate) { this.paymentDate = paymentDate; }
}