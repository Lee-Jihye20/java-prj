package com.example.kintai.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class EmployeeTodayStatusDTO {
    private Long userId;
    private String username;
    private String status; 
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private LocalTime startTime; 
    private boolean isOnBreak;
    private boolean isOnLeave;
    private String statusLabel; 

    public EmployeeTodayStatusDTO() {
    }

    public EmployeeTodayStatusDTO(Long userId, String username, String status, 
                                  LocalDateTime checkInTime, LocalDateTime checkOutTime,
                                  LocalTime startTime, boolean isOnBreak, boolean isOnLeave) {
        this.userId = userId;
        this.username = username;
        this.status = status;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.startTime = startTime;
        this.isOnBreak = isOnBreak;
        this.isOnLeave = isOnLeave;
        this.statusLabel = getStatusLabel(status, isOnBreak, isOnLeave);
    }

    private String getStatusLabel(String status, boolean isOnBreak, boolean isOnLeave) {
        switch (status) {
            case "WORKING":
                if (isOnBreak) {
                    return "勤務中（休憩中）";
                } else if (isOnLeave) {
                    return "勤務中（中抜け中）";
                } else {
                    return "勤務中";
                }
            case "COMPLETED":
                return "勤務済み";
            case "NOT_STARTED":
                return "未勤務";
            case "LATE":
                return "遅刻中";
            default:
                return "不明";
        }
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.statusLabel = getStatusLabel(status, isOnBreak, isOnLeave);
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public boolean isOnBreak() {
        return isOnBreak;
    }

    public void setOnBreak(boolean onBreak) {
        isOnBreak = onBreak;
        this.statusLabel = getStatusLabel(status, isOnBreak, isOnLeave);
    }

    public boolean isOnLeave() {
        return isOnLeave;
    }

    public void setOnLeave(boolean onLeave) {
        isOnLeave = onLeave;
        this.statusLabel = getStatusLabel(status, isOnBreak, isOnLeave);
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }
}
