package com.example.kintai.dto;

public class DashboardStatisticsDTO {

    private double todayWorkHours; 
    private double weekWorkHours; 
    private double monthWorkHours; 
    private double monthOvertimeHours; 
    private int monthWorkDays; 
    private String currentStatus; 

    public DashboardStatisticsDTO() {
    }

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
