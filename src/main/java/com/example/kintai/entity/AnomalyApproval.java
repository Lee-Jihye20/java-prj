package com.example.kintai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "anomaly_approval")
public class AnomalyApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attendance_id", nullable = false)
    private Attendance attendance;

    @Column(name = "anomaly_type", nullable = false, length = 50)
    private String anomalyType; // OVERTIME, LATE, MISSING_CHECKOUT

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason; // 理由

    @Column(name = "approved", nullable = false)
    private Boolean approved = false; // 承認フラグ

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy; // 承認者

    @Column(name = "approved_at")
    private LocalDateTime approvedAt; // 承認日時

    @Column(name = "adjustment_hours")
    private Double adjustmentHours; // 補正時間（時間単位、負の値も可）

    @Column(name = "adjustment_reason", columnDefinition = "TEXT")
    private String adjustmentReason; // 補正理由

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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

    // Constructors
    public AnomalyApproval() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Attendance getAttendance() {
        return attendance;
    }

    public void setAttendance(Attendance attendance) {
        this.attendance = attendance;
    }

    public Long getAttendanceId() {
        return attendance != null ? attendance.getId() : null;
    }

    public void setAttendanceId(Long attendanceId) {
        if (this.attendance == null) {
            this.attendance = new Attendance();
        }
        this.attendance.setId(attendanceId);
    }

    public String getAnomalyType() {
        return anomalyType;
    }

    public void setAnomalyType(String anomalyType) {
        this.anomalyType = anomalyType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Long getApprovedById() {
        return approvedBy != null ? approvedBy.getId() : null;
    }

    public void setApprovedById(Long approvedById) {
        if (this.approvedBy == null) {
            this.approvedBy = new User();
        }
        this.approvedBy.setId(approvedById);
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public Double getAdjustmentHours() {
        return adjustmentHours;
    }

    public void setAdjustmentHours(Double adjustmentHours) {
        this.adjustmentHours = adjustmentHours;
    }

    public String getAdjustmentReason() {
        return adjustmentReason;
    }

    public void setAdjustmentReason(String adjustmentReason) {
        this.adjustmentReason = adjustmentReason;
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

    /**
     * 異常タイプを日本語で取得
     */
    public String getAnomalyTypeInJapanese() {
        if (anomalyType == null) {
            return "不明";
        }
        switch (anomalyType) {
            case "OVERTIME":
                return "残業超過";
            case "LATE":
                return "遅刻";
            case "MISSING_CHECKOUT":
                return "打刻漏れ";
            default:
                return anomalyType;
        }
    }
}
