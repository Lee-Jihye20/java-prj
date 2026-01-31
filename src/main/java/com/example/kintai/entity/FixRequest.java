package com.example.kintai.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fix_request")
public class FixRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attendance_id", nullable = false)
    private Attendance attendance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "request_type", nullable = false, length = 20)
    private String requestType; // CHECK_IN, CHECK_OUT, BREAK_START, BREAK_END, LEAVE_START, LEAVE_END, LEAVE_TYPE

    @Column(name = "new_value")
    private LocalDateTime newValue;

    @Column(name = "new_value_2")
    private LocalDateTime newValue2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_record_id")
    private com.example.kintai.entity.LeaveRecord leaveRecord;

    @Column(name = "new_leave_type", length = 20)
    private String newLeaveType; // DEDUCTION, PAID_LEAVE

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    // Constructors
    public FixRequest() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    public void setUserId(Long userId) {
        if (this.user == null) {
            this.user = new User();
        }
        this.user.setId(userId);
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public LocalDateTime getNewValue() {
        return newValue;
    }

    public void setNewValue(LocalDateTime newValue) {
        this.newValue = newValue;
    }

    public LocalDateTime getNewValue2() {
        return newValue2;
    }

    public void setNewValue2(LocalDateTime newValue2) {
        this.newValue2 = newValue2;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Attendance getAttendance() {
        return attendance;
    }

    public void setAttendance(Attendance attendance) {
        this.attendance = attendance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public com.example.kintai.entity.LeaveRecord getLeaveRecord() {
        return leaveRecord;
    }

    public void setLeaveRecord(com.example.kintai.entity.LeaveRecord leaveRecord) {
        this.leaveRecord = leaveRecord;
    }

    public Long getLeaveRecordId() {
        return leaveRecord != null ? leaveRecord.getId() : null;
    }

    public void setLeaveRecordId(Long leaveRecordId) {
        if (this.leaveRecord == null) {
            this.leaveRecord = new com.example.kintai.entity.LeaveRecord();
        }
        this.leaveRecord.setId(leaveRecordId);
    }

    public String getNewLeaveType() {
        return newLeaveType;
    }

    public void setNewLeaveType(String newLeaveType) {
        this.newLeaveType = newLeaveType;
    }

    /**
     * 修正項目を日本語で取得
     */
    public String getRequestTypeInJapanese() {
        if (requestType == null) {
            return "不明";
        }
        switch (requestType) {
            case "CHECK_IN":
                return "出勤時刻";
            case "CHECK_OUT":
                return "退勤時刻";
            case "BREAK_START":
                return "休憩開始時刻";
            case "BREAK_END":
                return "休憩終了時刻";
            case "LEAVE_START":
                return "中抜け開始時刻";
            case "LEAVE_END":
                return "中抜け終了時刻";
            case "LEAVE_TYPE":
                return "中抜けの扱い";
            case "OVERTIME_APPLICATION":
                return "理由付き残業申請";
            case "CHECK_IN_AND_OUT":
                return "打刻訂正（出勤・退勤）";
            case "BREAK_START_AND_END":
                return "休憩補正（開始・終了）";
            default:
                return requestType;
        }
    }

    /**
     * ステータスを日本語で取得
     */
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

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Long getApprovedByUserId() {
        return approvedBy != null ? approvedBy.getId() : null;
    }

    public void setApprovedByUserId(Long approvedByUserId) {
        if (this.approvedBy == null) {
            this.approvedBy = new User();
        }
        this.approvedBy.setId(approvedByUserId);
    }
}