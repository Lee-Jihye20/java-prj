package com.example.kintai.dto;

import java.time.YearMonth;

public class WeeklyHighlightDTO {
    private YearMonth yearMonth;
    private String rating; // S, A, B, C, D, or null (事実ベース評価の総合スコアから算出)
    private double workHours;
    private double overtimeHours;
    private double totalScore; // 事実ベース評価の総合スコア

    public WeeklyHighlightDTO() {
    }

    public WeeklyHighlightDTO(YearMonth yearMonth) {
        this.yearMonth = yearMonth;
        this.workHours = 0.0;
        this.overtimeHours = 0.0;
        this.totalScore = 0.0;
    }

    // Getters and Setters
    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(YearMonth yearMonth) {
        this.yearMonth = yearMonth;
    }
    
    // 後方互換性のため（テンプレートで使用中）
    public java.time.LocalDate getWeekStartDate() {
        return yearMonth != null ? yearMonth.atDay(1) : null;
    }
    
    public java.time.LocalDate getWeekEndDate() {
        return yearMonth != null ? yearMonth.atEndOfMonth() : null;
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

    public double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
        // 総合スコアから評価を算出（100点満点）
        if (totalScore >= 90) {
            this.rating = "S";
        } else if (totalScore >= 80) {
            this.rating = "A";
        } else if (totalScore >= 70) {
            this.rating = "B";
        } else if (totalScore >= 60) {
            this.rating = "C";
        } else {
            this.rating = "D";
        }
    }
}
