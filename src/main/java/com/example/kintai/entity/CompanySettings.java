package com.example.kintai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;

@Entity
@Table(name = "company_settings")
public class CompanySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    private Company company;

    @Column(name = "admin_slack_webhook_url")
    private String adminSlackWebhookUrl;

    @Column(name = "attendance_slack_webhook_url")
    private String attendanceSlackWebhookUrl;

    // Slack通知設定
    @Column(name = "slack_notification_enabled")
    private Boolean slackNotificationEnabled = false;

    @Column(name = "log_slack_webhook_url")
    private String logSlackWebhookUrl;

    @Column(name = "alert_slack_webhook_url")
    private String alertSlackWebhookUrl;

    // 休憩設定
    @Column(name = "break_count_limit")
    private Integer breakCountLimit = 1; // 1回, 2回, -1(無制限)

    @Column(name = "break_input_mode", length = 20)
    private String breakInputMode = "FREE"; // FREE, FIXED_LUNCH

    @Column(name = "auto_calculate_break_time")
    private Boolean autoCalculateBreakTime = true; // ON/OFF

    @Column(name = "lunch_break_start_time")
    private LocalTime lunchBreakStartTime; // 昼休憩開始時刻（例: 12:00）

    @Column(name = "lunch_break_end_time")
    private LocalTime lunchBreakEndTime; // 昼休憩終了時刻（例: 13:00）

    // 中抜け設定
    @Column(name = "leave_default_type", length = 20)
    private String leaveDefaultType = "DEDUCTION"; // DEDUCTION（控除）, PAID_LEAVE（有給）

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getAdminSlackWebhookUrl() {
        return adminSlackWebhookUrl;
    }

    public void setAdminSlackWebhookUrl(String adminSlackWebhookUrl) {
        this.adminSlackWebhookUrl = adminSlackWebhookUrl;
    }

    public String getAttendanceSlackWebhookUrl() {
        return attendanceSlackWebhookUrl;
    }

    public void setAttendanceSlackWebhookUrl(String attendanceSlackWebhookUrl) {
        this.attendanceSlackWebhookUrl = attendanceSlackWebhookUrl;
    }

    public Integer getBreakCountLimit() {
        return breakCountLimit;
    }

    public void setBreakCountLimit(Integer breakCountLimit) {
        this.breakCountLimit = breakCountLimit;
    }

    public String getBreakInputMode() {
        return breakInputMode;
    }

    public void setBreakInputMode(String breakInputMode) {
        this.breakInputMode = breakInputMode;
    }

    public Boolean getAutoCalculateBreakTime() {
        return autoCalculateBreakTime;
    }

    public void setAutoCalculateBreakTime(Boolean autoCalculateBreakTime) {
        this.autoCalculateBreakTime = autoCalculateBreakTime;
    }

    public LocalTime getLunchBreakStartTime() {
        return lunchBreakStartTime;
    }

    public void setLunchBreakStartTime(LocalTime lunchBreakStartTime) {
        this.lunchBreakStartTime = lunchBreakStartTime;
    }

    public LocalTime getLunchBreakEndTime() {
        return lunchBreakEndTime;
    }

    public void setLunchBreakEndTime(LocalTime lunchBreakEndTime) {
        this.lunchBreakEndTime = lunchBreakEndTime;
    }

    public String getLeaveDefaultType() {
        return leaveDefaultType;
    }

    public void setLeaveDefaultType(String leaveDefaultType) {
        this.leaveDefaultType = leaveDefaultType;
    }

    public Boolean getSlackNotificationEnabled() {
        return slackNotificationEnabled;
    }

    public void setSlackNotificationEnabled(Boolean slackNotificationEnabled) {
        this.slackNotificationEnabled = slackNotificationEnabled;
    }

    public String getLogSlackWebhookUrl() {
        return logSlackWebhookUrl;
    }

    public void setLogSlackWebhookUrl(String logSlackWebhookUrl) {
        this.logSlackWebhookUrl = logSlackWebhookUrl;
    }

    public String getAlertSlackWebhookUrl() {
        return alertSlackWebhookUrl;
    }

    public void setAlertSlackWebhookUrl(String alertSlackWebhookUrl) {
        this.alertSlackWebhookUrl = alertSlackWebhookUrl;
    }
}