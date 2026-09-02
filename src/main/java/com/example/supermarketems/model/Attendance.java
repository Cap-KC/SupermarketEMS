package com.example.supermarketems.model;

/**
 * Model class representing an employee attendance log.
 * Maps data from the 'time_logs' table joined with 'employees'.
 */
public class Attendance {

    private String attendanceId; // Maps to log_id (VARCHAR)
    private String employeeId;   // Maps to employee_id (VARCHAR)
    private String employeeName; // Joined from employees table
    private String date;         // Extracted DATE(clock_in_time)
    private String clockInTime;  // Extracted TIME(clock_in_time)
    private String clockOutTime; // Extracted TIME(clock_out_time)
    private double workHours;    // Total duration in hours
    private double overtimeHours;// Maps to overtime_hours (DECIMAL)
    private String method;       // Maps to qr_code_token / scan method
    private String overtimeStatus;// UI status: PENDING, APPROVED, REJECTED, NONE

    // Default Constructor
    public Attendance() {
    }

    // Parameterized Constructor
    public Attendance(String attendanceId, String employeeId, String employeeName,
                      String date, String clockInTime, String clockOutTime,
                      double workHours, double overtimeHours, String method, String overtimeStatus) {
        this.attendanceId = attendanceId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.date = date;
        this.clockInTime = clockInTime;
        this.clockOutTime = clockOutTime;
        this.workHours = workHours;
        this.overtimeHours = overtimeHours;
        this.method = method;
        this.overtimeStatus = overtimeStatus;
    }

    // Getters and Setters

    public String getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(String attendanceId) {
        this.attendanceId = attendanceId;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getClockInTime() {
        return clockInTime;
    }

    public void setClockInTime(String clockInTime) {
        this.clockInTime = clockInTime;
    }

    public String getClockOutTime() {
        return clockOutTime;
    }

    public void setClockOutTime(String clockOutTime) {
        this.clockOutTime = clockOutTime;
    }

    public double getWorkHours() {
        return workHours;
    }

    public void setWorkHours(double workHours) {
        this.workHours = workHours;
    }

    public double getOvertimeHours() {
        return overtimeHours;
    }

    public void setOvertimeHours(double overtimeHours) {
        this.overtimeHours = overtimeHours;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getOvertimeStatus() {
        return overtimeStatus;
    }

    public void setOvertimeStatus(String overtimeStatus) {
        this.overtimeStatus = overtimeStatus;
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "attendanceId='" + attendanceId + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", date='" + date + '\'' +
                ", clockInTime='" + clockInTime + '\'' +
                ", clockOutTime='" + clockOutTime + '\'' +
                ", workHours=" + workHours +
                ", overtimeHours=" + overtimeHours +
                ", method='" + method + '\'' +
                ", overtimeStatus='" + overtimeStatus + '\'' +
                '}';
    }
}