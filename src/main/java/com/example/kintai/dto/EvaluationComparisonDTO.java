package com.example.kintai.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EvaluationComparisonDTO {
    private Long employeeId;
    private String employeeName;
    private LocalDate yearMonth;
    
    private BigDecimal factBasedScore;
    private Integer lateCount;
    private BigDecimal applicationComplianceRate;
    private Integer fixRequestCount;
    private Integer consecutiveWorkDays;
    private BigDecimal overtimeAccuracy;
    
    private String selfRating;
    private String selfComment;
    
    private String adminRating;
    private String adminComment;
    
    private String ratingDifference; 
    private boolean hasDifference; 

    public EvaluationComparisonDTO() {
    }

    public EvaluationComparisonDTO(Long employeeId, String employeeName, LocalDate yearMonth) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.yearMonth = yearMonth;
        this.factBasedScore = BigDecimal.ZERO;
        this.hasDifference = false;
    }

    public void calculateDifference() {
        if (selfRating == null || adminRating == null) {
            hasDifference = false;
            ratingDifference = null;
            return;
        }

        if (!selfRating.equals(adminRating)) {
            hasDifference = true;
            ratingDifference = selfRating + " → " + adminRating;
        } else {
            hasDifference = false;
            ratingDifference = null;
        }
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public LocalDate getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(LocalDate yearMonth) {
        this.yearMonth = yearMonth;
    }

    public BigDecimal getFactBasedScore() {
        return factBasedScore;
    }

    public void setFactBasedScore(BigDecimal factBasedScore) {
        this.factBasedScore = factBasedScore;
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
        calculateDifference();
    }

    public String getSelfComment() {
        return selfComment;
    }

    public void setSelfComment(String selfComment) {
        this.selfComment = selfComment;
    }

    public String getAdminRating() {
        return adminRating;
    }

    public void setAdminRating(String adminRating) {
        this.adminRating = adminRating;
        calculateDifference();
    }

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }

    public String getRatingDifference() {
        return ratingDifference;
    }

    public void setRatingDifference(String ratingDifference) {
        this.ratingDifference = ratingDifference;
    }

    public boolean isHasDifference() {
        return hasDifference;
    }

    public void setHasDifference(boolean hasDifference) {
        this.hasDifference = hasDifference;
    }
}
