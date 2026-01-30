package com.example.kintai.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fact_based_evaluation", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employee_id", "year_month"})
})
public class FactBasedEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @Column(name = "year_month", nullable = false)
    private LocalDate yearMonth; // 年月（月初日）

    @Column(name = "late_count", nullable = false)
    private Integer lateCount = 0; // 遅刻回数

    @Column(name = "application_compliance_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal applicationComplianceRate = BigDecimal.ZERO; // 申請遵守率（0-100）

    @Column(name = "fix_request_count", nullable = false)
    private Integer fixRequestCount = 0; // 打刻修正回数

    @Column(name = "consecutive_work_days", nullable = false)
    private Integer consecutiveWorkDays = 0; // 連続勤務日数

    @Column(name = "overtime_accuracy", nullable = false, precision = 5, scale = 2)
    private BigDecimal overtimeAccuracy = BigDecimal.ZERO; // 残業申請の正確性（0-100）

    @Column(name = "total_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal totalScore = BigDecimal.ZERO; // 総合スコア（0-100）

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getEmployee() {
        return employee;
    }

    public void setEmployee(User employee) {
        this.employee = employee;
    }

    public LocalDate getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(LocalDate yearMonth) {
        this.yearMonth = yearMonth;
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

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getEmployeeId() {
        return employee != null ? employee.getId() : null;
    }
}
