package com.example.supermarketems.model;

import java.sql.Timestamp;

public class AuditLog {
    private String logId;
    private String performedBy;
    private String actionType;
    private String description;
    private String ipAddress;
    private Timestamp timestamp;

    public AuditLog(String logId, String performedBy, String actionType, String description, String ipAddress, Timestamp timestamp) {
        this.logId = logId;
        this.performedBy = performedBy;
        this.actionType = actionType;
        this.description = description;
        this.ipAddress = ipAddress;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}