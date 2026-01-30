package com.example.kintai.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "break_record")
public class BreakRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attendance_id", nullable = false)
    private Attendance attendance;

    @Column(name = "break_start", nullable = false)
    private LocalDateTime breakStart;

    @Column(name = "break_end")
    private LocalDateTime breakEnd;

    @Column(name = "break_type", length = 20)
    private String breakType = "FREE"; // FREE, FIXED_LUNCH

    // Constructors
    public BreakRecord() {
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

    public LocalDateTime getBreakStart() {
        return breakStart;
    }

    public void setBreakStart(LocalDateTime breakStart) {
        this.breakStart = breakStart;
    }

    public LocalDateTime getBreakEnd() {
        return breakEnd;
    }

    public void setBreakEnd(LocalDateTime breakEnd) {
        this.breakEnd = breakEnd;
    }

    public String getBreakType() {
        return breakType;
    }

    public void setBreakType(String breakType) {
        this.breakType = breakType;
    }

    /**
     * 休憩時間を分単位で取得
     */
    public long getBreakMinutes() {
        if (breakStart == null || breakEnd == null) {
            return 0;
        }
        return java.time.Duration.between(breakStart, breakEnd).toMinutes();
    }
}

