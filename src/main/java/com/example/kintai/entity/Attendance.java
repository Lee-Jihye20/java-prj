package com.example.kintai.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "check_in")
    private LocalDateTime checkIn;

    @Column(name = "check_out")
    private LocalDateTime checkOut;

    @Column(name = "break_start")
    private LocalDateTime breakStart;

    @Column(name = "break_end")
    private LocalDateTime breakEnd;

    @Column(nullable = false, length = 20)
    private String status = "APPROVED"; // PENDING, APPROVED, REJECTED (通常の打刻は自動承認)

    // Constructors
    public Attendance() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDateTime checkIn) {
        this.checkIn = checkIn;
    }

    public LocalDateTime getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDateTime checkOut) {
        this.checkOut = checkOut;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
}
