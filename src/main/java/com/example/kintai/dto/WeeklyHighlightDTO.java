package com.example.kintai.dto;

import java.time.LocalDate;

public class WeeklyHighlightDTO {
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private String rating; // S, A, B, C, D, or null
    private double workHours;
    private double overtimeHours;

    public WeeklyHighlightDTO() {
    }

    public WeeklyHighlightDTO(LocalDate weekStartDate, LocalDate weekEndDate) {
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        this.workHours = 0.0;
        this.overtimeHours = 0.0;
    }

    // Getters and Setters
    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    public LocalDate getWeekEndDate() {
        return weekEndDate;
    }

    public void setWeekEndDate(LocalDate weekEndDate) {
        this.weekEndDate = weekEndDate;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
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
}
