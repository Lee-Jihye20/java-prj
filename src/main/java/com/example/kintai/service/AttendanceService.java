package com.example.kintai.service;

import com.example.kintai.entity.Attendance;
import com.example.kintai.entity.BreakRecord;
import com.example.kintai.entity.CompanySettings;
import com.example.kintai.entity.User;
import com.example.kintai.repository.AttendanceRepository;
import com.example.kintai.repository.BreakRecordRepository;
import com.example.kintai.repository.CompanySettingsRepository;
import com.example.kintai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository; 

    @Autowired
    private SlackNotificationService slackNotificationService;

    @Autowired
    private CompanySettingsRepository companySettingsRepository;

    @Autowired
    private BreakRecordRepository breakRecordRepository;

    @Autowired
    private LeaveRecordService leaveRecordService;

    @Autowired
    private OvertimeExcessService overtimeExcessService;

    @Autowired
    private BreakRecordService breakRecordService;

    @Transactional
    public Attendance checkIn(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }
        User user = userOptional.get();

        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
        List<Attendance> todayAttendances = attendanceRepository.findByUser_IdAndCheckInBetweenOrderByCheckInDesc(
                userId, startOfToday, endOfToday);

        if (!todayAttendances.isEmpty()) {
            throw new IllegalStateException("本日は既に出勤打刻されています。1日1回までです。");
        }

        Attendance attendance = new Attendance();
        attendance.setUser(user); 
        attendance.setCheckIn(LocalDateTime.now());
        attendance.setStatus("APPROVED"); 
        Attendance savedAttendance = attendanceRepository.save(attendance);

        String message = String.format("出勤通知: %s さんが %s に出勤しました。",
                user.getUsername(),
                savedAttendance.getCheckIn().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
        slackNotificationService.sendAttendanceNotification(message, user.getCompany().getId());

        return savedAttendance;
    }

    @Transactional
    public Attendance checkOut(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }
        User user = userOptional.get();

        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
        List<Attendance> todayAttendances = attendanceRepository.findByUser_IdAndCheckInBetweenOrderByCheckInDesc(
                userId, startOfToday, endOfToday);

        if (todayAttendances.isEmpty()) {
            throw new IllegalStateException("本日の出勤打刻がされていません");
        }

        Attendance attendance = todayAttendances.get(0);
        if (attendance.getCheckOut() != null) {
            throw new IllegalStateException("既に退勤打刻されています");
        }

        attendance.setCheckOut(LocalDateTime.now());
        Attendance savedAttendance = attendanceRepository.save(attendance);

        String message = String.format("退勤通知: %s さんが %s に退勤しました。",
                user.getUsername(),
                savedAttendance.getCheckOut().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
        slackNotificationService.sendAttendanceNotification(message, user.getCompany().getId());

        if (overtimeExcessService.isOvertimeExcess(savedAttendance)) {
            long workMinutes = Duration.between(savedAttendance.getCheckIn(), savedAttendance.getCheckOut()).toMinutes();
            long totalBreakMinutes = breakRecordService.getTotalBreakMinutes(savedAttendance);
            workMinutes -= totalBreakMinutes;
            long threshold = overtimeExcessService.getThresholdMinutes();
            double workHours = workMinutes / 60.0;
            double overtimeHours = (workMinutes - threshold) / 60.0;
            
            Map<String, Object> attachment = new HashMap<>();
            attachment.put("color", "#e74c3c"); 
            attachment.put("pretext", "⚠️ 残業検知");
            
            List<Map<String, Object>> fields = new ArrayList<>();
            
            Map<String, Object> userField = new HashMap<>();
            userField.put("title", "ユーザー");
            userField.put("value", user.getUsername());
            userField.put("short", true);
            fields.add(userField);
            
            Map<String, Object> dateField = new HashMap<>();
            dateField.put("title", "日付");
            dateField.put("value", savedAttendance.getCheckIn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            dateField.put("short", true);
            fields.add(dateField);
            
            Map<String, Object> workTimeField = new HashMap<>();
            workTimeField.put("title", "勤務時間");
            workTimeField.put("value", String.format("%.1f時間", workHours));
            workTimeField.put("short", true);
            fields.add(workTimeField);
            
            Map<String, Object> overtimeField = new HashMap<>();
            overtimeField.put("title", "残業時間");
            overtimeField.put("value", String.format("%.1f時間", overtimeHours));
            overtimeField.put("short", true);
            fields.add(overtimeField);
            
            Map<String, Object> checkInField = new HashMap<>();
            checkInField.put("title", "出勤時刻");
            checkInField.put("value", savedAttendance.getCheckIn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            checkInField.put("short", true);
            fields.add(checkInField);
            
            Map<String, Object> checkOutField = new HashMap<>();
            checkOutField.put("title", "退勤時刻");
            checkOutField.put("value", savedAttendance.getCheckOut().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            checkOutField.put("short", true);
            fields.add(checkOutField);
            
            attachment.put("fields", fields);
            attachment.put("footer", "勤怠管理システム");
            attachment.put("ts", System.currentTimeMillis() / 1000);
            
            List<Map<String, Object>> attachments = new ArrayList<>();
            attachments.add(attachment);
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("text", "⚠️ 残業検知: " + user.getUsername() + " さんが残業しました");
            payload.put("attachments", attachments);
            
            slackNotificationService.sendAdminNotificationWithAttachment(payload, user.getCompany().getId());
        }

        return savedAttendance;
    }

    @Transactional
    public Attendance startBreak(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }
        User user = userOptional.get();

        Optional<CompanySettings> settingsOptional = companySettingsRepository.findByCompanyId(user.getCompany().getId());
        CompanySettings settings = settingsOptional.orElse(new CompanySettings());
        Integer breakCountLimit = settings.getBreakCountLimit() != null ? settings.getBreakCountLimit() : 1;
        String breakInputMode = settings.getBreakInputMode() != null ? settings.getBreakInputMode() : "FREE";

        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
        List<Attendance> todayAttendances = attendanceRepository.findByUser_IdAndCheckInBetweenOrderByCheckInDesc(
                userId, startOfToday, endOfToday);

        if (todayAttendances.isEmpty()) {
            throw new IllegalStateException("本日の出勤打刻がされていません");
        }

        Attendance attendance = todayAttendances.get(0);
        if (attendance.getCheckOut() != null) {
            throw new IllegalStateException("既に退勤済みです");
        }

        List<BreakRecord> activeBreaks = breakRecordRepository.findByAttendance_IdAndBreakEndIsNull(attendance.getId());
        if (!activeBreaks.isEmpty()) {
            throw new IllegalStateException("既に休憩中です");
        }

        if (leaveRecordService.hasActiveLeave(attendance.getId())) {
            throw new IllegalStateException("中抜け中です。中抜けを終了してから休憩を開始してください");
        }

        List<BreakRecord> completedBreaks = breakRecordRepository.findByAttendance_IdOrderByBreakStartAsc(attendance.getId());
        completedBreaks = completedBreaks.stream()
                .filter(br -> br.getBreakEnd() != null)
                .toList();

        if (breakCountLimit > 0 && completedBreaks.size() >= breakCountLimit) {
            throw new IllegalStateException("本日の休憩回数制限（" + breakCountLimit + "回）に達しています");
        }

        LocalDateTime breakStartTime = LocalDateTime.now();

        BreakRecord breakRecord = new BreakRecord();
        breakRecord.setAttendance(attendance);
        breakRecord.setBreakStart(breakStartTime);
        breakRecord.setBreakType("FREE");
        breakRecordRepository.save(breakRecord);

        if (attendance.getBreakStart() == null) {
            attendance.setBreakStart(breakStartTime);
        }

        return attendanceRepository.save(attendance);
    }

    @Transactional
    public Attendance endBreak(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }
        User user = userOptional.get();

        Optional<CompanySettings> settingsOptional = companySettingsRepository.findByCompanyId(user.getCompany().getId());
        CompanySettings settings = settingsOptional.orElse(new CompanySettings());
        String breakInputMode = settings.getBreakInputMode() != null ? settings.getBreakInputMode() : "FREE";
        Boolean autoCalculateBreakTime = settings.getAutoCalculateBreakTime() != null ? settings.getAutoCalculateBreakTime() : true;

        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
        List<Attendance> todayAttendances = attendanceRepository.findByUser_IdAndCheckInBetweenOrderByCheckInDesc(
                userId, startOfToday, endOfToday);

        if (todayAttendances.isEmpty()) {
            throw new IllegalStateException("本日の出勤打刻がされていません");
        }

        Attendance attendance = todayAttendances.get(0);
        if (attendance.getCheckOut() != null) {
            throw new IllegalStateException("既に退勤済みです");
        }

        List<BreakRecord> activeBreaks = breakRecordRepository.findByAttendance_IdAndBreakEndIsNull(attendance.getId());
        if (activeBreaks.isEmpty()) {
            throw new IllegalStateException("休憩開始が打刻されていません");
        }

        BreakRecord breakRecord = activeBreaks.get(0);

        LocalDateTime breakEndTime = LocalDateTime.now();

        breakRecord.setBreakEnd(breakEndTime);
        breakRecordRepository.save(breakRecord);

        List<BreakRecord> allActiveBreaks = breakRecordRepository.findByAttendance_IdAndBreakEndIsNull(attendance.getId());
        if (allActiveBreaks.isEmpty()) {
            
            List<BreakRecord> allBreaks = breakRecordRepository.findByAttendance_IdOrderByBreakStartAsc(attendance.getId());
            if (!allBreaks.isEmpty()) {
                BreakRecord lastBreak = allBreaks.get(allBreaks.size() - 1);
                if (lastBreak.getBreakEnd() != null) {
                    attendance.setBreakEnd(lastBreak.getBreakEnd());
                }
            }
        }

        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getAttendanceHistory(Long userId) {
        return attendanceRepository.findByUser_IdOrderByCheckInDesc(userId);
    }

    public List<Attendance> getAttendanceByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return attendanceRepository.findByUser_IdAndCheckInBetweenOrderByCheckInDesc(userId, startDate, endDate);
    }

    public Optional<Attendance> getActiveAttendance(Long userId) {
        return attendanceRepository.findByUser_IdAndCheckOutIsNullOrderByCheckInDesc(userId);
    }

    public TodayAttendanceStatus getTodayAttendanceStatus(Long userId) {
        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
        List<Attendance> todayAttendances = attendanceRepository.findByUser_IdAndCheckInBetweenOrderByCheckInDesc(
                userId, startOfToday, endOfToday);

        boolean hasCheckedIn = !todayAttendances.isEmpty();
        boolean hasCheckedOut = hasCheckedIn && todayAttendances.get(0).getCheckOut() != null;
        boolean isOnBreak = false;
        boolean isOnLeave = false;

        if (hasCheckedIn && !hasCheckedOut) {
            Attendance attendance = todayAttendances.get(0);
            List<BreakRecord> activeBreaks = breakRecordRepository.findByAttendance_IdAndBreakEndIsNull(attendance.getId());
            isOnBreak = !activeBreaks.isEmpty();
            isOnLeave = leaveRecordService.hasActiveLeave(attendance.getId());
        }

        return new TodayAttendanceStatus(hasCheckedIn, hasCheckedOut, isOnBreak, isOnLeave);
    }

    public static class TodayAttendanceStatus {
        private final boolean hasCheckedIn;
        private final boolean hasCheckedOut;
        private final boolean isOnBreak;
        private final boolean isOnLeave;

        public TodayAttendanceStatus(boolean hasCheckedIn, boolean hasCheckedOut, boolean isOnBreak, boolean isOnLeave) {
            this.hasCheckedIn = hasCheckedIn;
            this.hasCheckedOut = hasCheckedOut;
            this.isOnBreak = isOnBreak;
            this.isOnLeave = isOnLeave;
        }

        public boolean hasCheckedIn() {
            return hasCheckedIn;
        }

        public boolean hasCheckedOut() {
            return hasCheckedOut;
        }

        public boolean isOnBreak() {
            return isOnBreak;
        }

        public boolean isOnLeave() {
            return isOnLeave;
        }
    }

    public Optional<Attendance> getAttendanceById(Long id) {
        return attendanceRepository.findById(id);
    }

    public List<Attendance> findAllByCompanyId(Long companyId) {
        return attendanceRepository.findAllByUser_CompanyId(companyId);
    }

    public List<Attendance> detectMissingCheckOut(Long companyId) {
        LocalDate today = LocalDate.now();
        return findAllByCompanyId(companyId).stream()
                .filter(a -> a.getCheckOut() == null && a.getCheckIn() != null
                        && a.getCheckIn().toLocalDate().isBefore(today))
                .toList();
    }
}
