package com.example.supermarketems.model;

import java.math.BigDecimal;
import java.sql.Date;

public class Employee {
    private String employeeId;
    private String name;
    private String email;
    private String phoneNumber;
    Date dateJoined;
    private String passwordHash;
    private String employeeType; // Admin, Manager, Permanent, Outsource

    // Permanent specific
    private BigDecimal monthlySalary;
    private Integer leaveBalance;
    private String benefitsPackageId;

    // Outsource specific
    private BigDecimal hourlyRate;
    private Date contractExpiry;
    private String agencyId;
    private String agencyName; // Populated via SQL JOIN with external_agencies

    // Manager specific
    private String departmentManaged;

    // Admin specific
    private Integer adminLevel;

    // Constructors
    public Employee() {}

    public Employee(String employeeId, String name, String email, String phoneNumber,
                    Date dateJoined, String passwordHash, String employeeType) {
        this.employeeId = employeeId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateJoined = dateJoined;
        this.passwordHash = passwordHash;
        this.employeeType = employeeType;
    }

    // --- ALIAS METHODS FOR UI DASHBOARDS ---

    /**
     * Alias for employeeType to resolve 'Cannot resolve method getRole()'
     */
    public String getRole() {
        return employeeType != null ? employeeType : "Permanent";
    }

    /**
     * Alias for leaveBalance to resolve 'Cannot resolve method getLeaveAllowance()'
     */
    public int getLeaveAllowance() {
        return leaveBalance != null ? leaveBalance : 0;
    }

    /**
     * Alias for employeeType / department to resolve 'Cannot resolve method getDesignation()'
     */
    public String getDesignation() {
        if (departmentManaged != null && !departmentManaged.isBlank()) {
            return employeeType + " (" + departmentManaged + ")";
        }
        return employeeType != null ? employeeType : "Permanent Staff";
    }

    public void setDesignation(String designation) {
        this.employeeType = designation;
    }

    // --- STANDARD GETTERS AND SETTERS ---

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Date getDateJoined() { return dateJoined; }
    public void setDateJoined(Date dateJoined) { this.dateJoined = dateJoined; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getEmployeeType() { return employeeType; }
    public void setEmployeeType(String employeeType) { this.employeeType = employeeType; }

    public BigDecimal getMonthlySalary() { return monthlySalary; }
    public void setMonthlySalary(BigDecimal monthlySalary) { this.monthlySalary = monthlySalary; }

    public Integer getLeaveBalance() { return leaveBalance; }
    public void setLeaveBalance(Integer leaveBalance) { this.leaveBalance = leaveBalance; }

    public String getBenefitsPackageId() { return benefitsPackageId; }
    public void setBenefitsPackageId(String benefitsPackageId) { this.benefitsPackageId = benefitsPackageId; }

    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }

    public Date getContractExpiry() { return contractExpiry; }
    public void setContractExpiry(Date contractExpiry) { this.contractExpiry = contractExpiry; }

    public String getAgencyId() { return agencyId; }
    public void setAgencyId(String agencyId) { this.agencyId = agencyId; }

    public String getAgencyName() { return agencyName; }
    public void setAgencyName(String agencyName) { this.agencyName = agencyName; }

    public String getDepartmentManaged() { return departmentManaged; }
    public void setDepartmentManaged(String departmentManaged) { this.departmentManaged = departmentManaged; }

    public Integer getAdminLevel() { return adminLevel; }
    public void setAdminLevel(Integer adminLevel) { this.adminLevel = adminLevel; }
}