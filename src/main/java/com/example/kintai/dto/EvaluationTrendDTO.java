package com.example.kintai.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EvaluationTrendDTO {
    private LocalDate yearMonth;
    private BigDecimal totalScore;
    private Integer lateCount;
    private BigDecimal applicationComplianceRate;
    private Integer fixRequestCount;
    private Integer consecutiveWorkDays;
    private BigDecimal overtimeAccuracy;
    private String selfRating; 
    private String adminRating; 

    public EvaluationTrendDTO() {
    }

    public EvaluationTrendDTO(LocalDate yearMonth) {
        this.yearMonth = yearMonth;
        this.totalScore = BigDecimal.ZERO;
        this.lateCount = 0;
        this.applicationComplianceRate = BigDecimal.ZERO;
        this.fixRequestCount = 0;
        this.consecutiveWorkDays = 0;
        this.overtimeAccuracy = BigDecimal.ZERO;
    }

    public LocalDate getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(LocalDate yearMonth) {
        this.yearMonth = yearMonth;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getLateCount() {
        return lateCount;
    }

    public void setLateCount(Integer lateCount) {
        this.lateCount = lateCount;
    }

    public BigDecimal getApplicationComplianceRate() {
        return applicationComplianceRate;
    }

    public void setApplicationComplianceRate(BigDecimal applicationComplianceRate) {
        this.applicationComplianceRate = applicationComplianceRate;
    }

    public Integer getFixRequestCount() {
        return fixRequestCount;
    }

    public void setFixRequestCount(Integer fixRequestCount) {
        this.fixRequestCount = fixRequestCount;
    }

    public Integer getConsecutiveWorkDays() {
        return consecutiveWorkDays;
    }

    public void setConsecutiveWorkDays(Integer consecutiveWorkDays) {
        this.consecutiveWorkDays = consecutiveWorkDays;
    }

    public BigDecimal getOvertimeAccuracy() {
        return overtimeAccuracy;
    }

    public void setOvertimeAccuracy(BigDecimal overtimeAccuracy) {
        this.overtimeAccuracy = overtimeAccuracy;
    }

    public String getSelfRating() {
        return selfRating;
    }

    public void setSelfRating(String selfRating) {
        this.selfRating = selfRating;
    }

    public String getAdminRating() {
        return adminRating;
    }

    public void setAdminRating(String adminRating) {
        this.adminRating = adminRating;
    }
}
