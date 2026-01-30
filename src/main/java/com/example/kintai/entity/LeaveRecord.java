package com.example.kintai.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "leave_record")
public class LeaveRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attendance_id", nullable = false)
    private Attendance attendance;

    @Column(name = "leave_start", nullable = false)
    private LocalDateTime leaveStart;

    @Column(name = "leave_end")
    private LocalDateTime leaveEnd;

    @Column(name = "leave_type", length = 20)
    private String leaveType = "DEDUCTION"; // DEDUCTION（控除）, PAID_LEAVE（有給）

    // Constructors
    public LeaveRecord() {
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

    public LocalDateTime getLeaveStart() {
        return leaveStart;
    }

    public void setLeaveStart(LocalDateTime leaveStart) {
        this.leaveStart = leaveStart;
    }

    public LocalDateTime getLeaveEnd() {
        return leaveEnd;
    }

    public void setLeaveEnd(LocalDateTime leaveEnd) {
        this.leaveEnd = leaveEnd;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    /**
     * 中抜け時間を分単位で取得
     */
    public long getLeaveMinutes() {
        if (leaveStart == null || leaveEnd == null) {
            return 0;
        }
        return java.time.Duration.between(leaveStart, leaveEnd).toMinutes();
    }
}
