package com.example.kintai.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MonthlyReportDTO {

    private String username;
    private int year;
    private int month;
    private int workDays; 
    private long totalWorkMinutes; 
    private long totalOvertimeMinutes; 
    private long totalBreakMinutes; 
    private double averageWorkHours; 
    private List<DailyReportDTO> dailyReports; 

    public MonthlyReportDTO() {
        this.dailyReports = new ArrayList<>();
    }

    public MonthlyReportDTO(String username, int year, int month) {
        this.username = username;
        this.year = year;
        this.month = month;
        this.dailyReports = new ArrayList<>();
    }

    public double getTotalWorkHours() {
        return totalWorkMinutes / 60.0;
    }

    public double getTotalOvertimeHours() {
        return totalOvertimeMinutes / 60.0;
    }

    public double getTotalBreakHours() {
        return totalBreakMinutes / 60.0;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getWorkDays() {
        return workDays;
    }

    public void setWorkDays(int workDays) {
        this.workDays = workDays;
    }

    public long getTotalWorkMinutes() {
        return totalWorkMinutes;
    }

    public void setTotalWorkMinutes(long totalWorkMinutes) {
        this.totalWorkMinutes = totalWorkMinutes;
    }

    public long getTotalOvertimeMinutes() {
        return totalOvertimeMinutes;
    }

    public void setTotalOvertimeMinutes(long totalOvertimeMinutes) {
        this.totalOvertimeMinutes = totalOvertimeMinutes;
    }

    public long getTotalBreakMinutes() {
        return totalBreakMinutes;
    }

    public void setTotalBreakMinutes(long totalBreakMinutes) {
        this.totalBreakMinutes = totalBreakMinutes;
    }

    public double getAverageWorkHours() {
        return averageWorkHours;
    }

    public void setAverageWorkHours(double averageWorkHours) {
        this.averageWorkHours = averageWorkHours;
    }

    public List<DailyReportDTO> getDailyReports() {
        return dailyReports;
    }

    public void setDailyReports(List<DailyReportDTO> dailyReports) {
        this.dailyReports = dailyReports;
    }

    public static class DailyReportDTO {
        private LocalDate date;
        private long workMinutes;
        private long overtimeMinutes;
        private long breakMinutes;
        private String status;

        public DailyReportDTO() {
        }

        public DailyReportDTO(LocalDate date, long workMinutes, long overtimeMinutes, long breakMinutes, String status) {
            this.date = date;
            this.workMinutes = workMinutes;
            this.overtimeMinutes = overtimeMinutes;
            this.breakMinutes = breakMinutes;
            this.status = status;
        }

        public double getWorkHours() {
            return workMinutes / 60.0;
        }

        public double getOvertimeHours() {
            return overtimeMinutes / 60.0;
        }

        public double getBreakHours() {
            return breakMinutes / 60.0;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public long getWorkMinutes() {
            return workMinutes;
        }

        public void setWorkMinutes(long workMinutes) {
            this.workMinutes = workMinutes;
        }

        public long getOvertimeMinutes() {
            return overtimeMinutes;
        }

        public void setOvertimeMinutes(long overtimeMinutes) {
            this.overtimeMinutes = overtimeMinutes;
        }

        public long getBreakMinutes() {
            return breakMinutes;
        }

        public void setBreakMinutes(long breakMinutes) {
            this.breakMinutes = breakMinutes;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getStatusInJapanese() {
            if (status == null) {
                return "不明";
            }
            switch (status) {
                case "PENDING":
                    return "保留中";
                case "APPROVED":
                    return "承認済み";
                case "REJECTED":
                    return "却下";
                default:
                    return status;
            }
        }
    }
}
