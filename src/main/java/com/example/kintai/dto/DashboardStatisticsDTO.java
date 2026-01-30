package com.example.kintai.dto;

public class DashboardStatisticsDTO {

    private double todayWorkHours; // 今日の勤務時間
    private double weekWorkHours; // 今週の勤務時間
    private double monthWorkHours; // 今月の勤務時間
    private double monthOvertimeHours; // 今月の残業時間
    private int monthWorkDays; // 今月の出勤日数
    private String currentStatus; // 現在の状態（出勤中、退勤済みなど）

    // Constructors
    public DashboardStatisticsDTO() {
    }

    // Getters and Setters
    public double getTodayWorkHours() {
        return todayWorkHours;
    }

    public void setTodayWorkHours(double todayWorkHours) {
        this.todayWorkHours = todayWorkHours;
    }

    public double getWeekWorkHours() {
        return weekWorkHours;
    }

    public void setWeekWorkHours(double weekWorkHours) {
        this.weekWorkHours = weekWorkHours;
    }

    public double getMonthWorkHours() {
        return monthWorkHours;
    }

    public void setMonthWorkHours(double monthWorkHours) {
        this.monthWorkHours = monthWorkHours;
    }

    public double getMonthOvertimeHours() {
        return monthOvertimeHours;
    }

    public void setMonthOvertimeHours(double monthOvertimeHours) {
        this.monthOvertimeHours = monthOvertimeHours;
    }

    public int getMonthWorkDays() {
        return monthWorkDays;
    }

    public void setMonthWorkDays(int monthWorkDays) {
        this.monthWorkDays = monthWorkDays;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }
}
