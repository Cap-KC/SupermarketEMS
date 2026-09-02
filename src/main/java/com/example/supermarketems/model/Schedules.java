package com.example.supermarketems.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Schedules {
    private String scheduleId;
    private String employeeId;    // Foreign key to employees.employee_id
    private String employeeName;  // Retrieved via SQL JOIN on employees e
    private LocalDate shiftDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String department;
    private String managedBy;    // Foreign key to employees.employee_id (Manager ID)
    private String managerName;  // Retrieved via SQL JOIN on employees m

    // Default Constructor
    public Schedules() {}

    // Full Constructor
    public Schedules(String scheduleId, String employeeId, String employeeName,
                     LocalDate shiftDate, LocalTime startTime, LocalTime endTime,
                     String department, String managedBy, String managerName) {
        this.scheduleId = scheduleId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.shiftDate = shiftDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.department = department;
        this.managedBy = managedBy;
        this.managerName = managerName;
    }

    // --- Getters and Setters ---
    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getManagedBy() {
        return managedBy;
    }

    public void setManagedBy(String managedBy) {
        this.managedBy = managedBy;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }
}