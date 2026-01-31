package com.example.kintai.dto;

import java.time.LocalDateTime;

public class AttendanceLogDTO {
    private LocalDateTime timestamp;
    private String username;
    private String operationType;
    private String value;
    private String note;

    public AttendanceLogDTO() {
    }

    public AttendanceLogDTO(LocalDateTime timestamp, String username, String operationType, String value, String note) {
        this.timestamp = timestamp;
        this.username = username;
        this.operationType = operationType;
        this.value = value;
        this.note = note;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
