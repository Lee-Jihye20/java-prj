package com.example.kintai.service;

import com.example.kintai.entity.Attendance;
import com.example.kintai.entity.BreakRecord;
import com.example.kintai.entity.CompanySettings;
import com.example.kintai.entity.LeaveRecord;
import com.example.kintai.entity.User;
import com.example.kintai.repository.AttendanceRepository;
import com.example.kintai.repository.BreakRecordRepository;
import com.example.kintai.repository.CompanySettingsRepository;
import com.example.kintai.repository.LeaveRecordRepository;
import com.example.kintai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
    private UserRepository userRepository; // To get user details including company ID

    @Autowired
    private SlackNotificationService slackNotificationService;

    @Autowired
    private CompanySettingsRepository companySettingsRepository;

    @Autowired
    private BreakRecordRepository breakRecordRepository;

    @Autowired
    private LeaveRecordRepository leaveRecordRepository;

    /**
     * 出勤打刻
     */
    @Transactional
    public Attendance checkIn(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }
        User user = userOptional.get();

        // 今日の日付範囲で既存の勤怠記録をチェック（1日1回制限）
        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
        List<Attendance> todayAttendances = attendanceRepository.findByUser_IdAndCheckInBetweenOrderByCheckInDesc(
                userId, startOfToday, endOfToday);

        if (!todayAttendances.isEmpty()) {
            throw new IllegalStateException("本日は既に出勤打刻されています。1日1回までです。");
        }

        Attendance attendance = new Attendance();
        attendance.setUser(user); // Set user object directly
        attendance.setCheckIn(LocalDateTime.now());
        attendance.setStatus("APPROVED"); // 通常の打刻は自動承認
        Attendance savedAttendance = attendanceRepository.save(attendance);

        // Slack通知
        String message = String.format("出勤通知: %s さんが %s に出勤しました。",
                user.getUsername(),
                savedAttendance.getCheckIn().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
        slackNotificationService.sendAttendanceNotification(message, user.getCompany().getId());

        return savedAttendance;
    }

    /**
     * 退勤打刻
     */
    @Transactional
    public Attendance checkOut(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }
        User user = userOptional.get();

        // 今日の出勤記録を取得
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

        // Slack通知
        String message = String.format("退勤通知: %s さんが %s に退勤しました。",
                user.getUsername(),
                savedAttendance.getCheckOut().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
        slackNotificationService.sendAttendanceNotification(message, user.getCompany().getId());

        // 残業検知: 勤務時間を計算して1分（テスト用）を超えている場合は通知
        long workMinutes = Duration.between(savedAttendance.getCheckIn(), savedAttendance.getCheckOut()).toMinutes();
        
        // 休憩時間を引く（BreakRecordを使用）
        List<BreakRecord> breakRecords = breakRecordRepository.findByAttendance_IdOrderByBreakStartAsc(savedAttendance.getId());
        long totalBreakMinutes = breakRecords.stream()
                .filter(br -> br.getBreakEnd() != null)
                .mapToLong(BreakRecord::getBreakMinutes)
                .sum();
        workMinutes -= totalBreakMinutes;
        
        // 1分（テスト用）を超えている場合は残業検知として通知
        if (workMinutes > 1) { // テスト用: 1分を超えたら残業検知（本番では480分に戻す）
            double workHours = workMinutes / 60.0;
            double overtimeHours = (workMinutes - 1) / 60.0; // テスト用: 1分を超えた分が残業時間
            
            // Slack通知（管理者チャンネル）- embed形式
            Map<String, Object> attachment = new HashMap<>();
            attachment.put("color", "#e74c3c"); // 赤色（警告）
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

    /**
     * 休憩開始打刻
     */
    @Transactional
    public Attendance startBreak(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }
        User user = userOptional.get();

        // 企業設定を取得
        Optional<CompanySettings> settingsOptional = companySettingsRepository.findByCompanyId(user.getCompany().getId());
        CompanySettings settings = settingsOptional.orElse(new CompanySettings());
        Integer breakCountLimit = settings.getBreakCountLimit() != null ? settings.getBreakCountLimit() : 1;
        String breakInputMode = settings.getBreakInputMode() != null ? settings.getBreakInputMode() : "FREE";

        // 中抜け（通常の休憩）は常に使用可能（昼休憩ボタンは削除済み）

        // 今日の出勤記録を取得
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

        // 既に休憩中の記録があるかチェック
        List<BreakRecord> activeBreaks = breakRecordRepository.findByAttendance_IdAndBreakEndIsNull(attendance.getId());
        if (!activeBreaks.isEmpty()) {
            throw new IllegalStateException("既に休憩中です");
        }

        // 中抜け中でないことを確認（休憩と中抜けは同時に進行できない）
        List<LeaveRecord> activeLeaves = leaveRecordRepository.findByAttendance_IdAndLeaveEndIsNull(attendance.getId());
        if (!activeLeaves.isEmpty()) {
            throw new IllegalStateException("中抜け中です。中抜けを終了してから休憩を開始してください");
        }

        // 休憩回数制限をチェック
        List<BreakRecord> completedBreaks = breakRecordRepository.findByAttendance_IdOrderByBreakStartAsc(attendance.getId());
        completedBreaks = completedBreaks.stream()
                .filter(br -> br.getBreakEnd() != null)
                .toList();

        if (breakCountLimit > 0 && completedBreaks.size() >= breakCountLimit) {
            throw new IllegalStateException("本日の休憩回数制限（" + breakCountLimit + "回）に達しています");
        }

        // 休憩開始時刻を決定（中抜けは常に現在時刻）
        LocalDateTime breakStartTime = LocalDateTime.now();

        // BreakRecordを作成（中抜けは常にFREEタイプ）
        BreakRecord breakRecord = new BreakRecord();
        breakRecord.setAttendance(attendance);
        breakRecord.setBreakStart(breakStartTime);
        breakRecord.setBreakType("FREE");
        breakRecordRepository.save(breakRecord);

        // 後方互換性のため、Attendanceにも設定（既存のコードとの互換性）
        if (attendance.getBreakStart() == null) {
            attendance.setBreakStart(breakStartTime);
        }

        return attendanceRepository.save(attendance);
    }

    /**
     * 休憩終了打刻
     */
    @Transactional
    public Attendance endBreak(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }
        User user = userOptional.get();

        // 企業設定を取得
        Optional<CompanySettings> settingsOptional = companySettingsRepository.findByCompanyId(user.getCompany().getId());
        CompanySettings settings = settingsOptional.orElse(new CompanySettings());
        String breakInputMode = settings.getBreakInputMode() != null ? settings.getBreakInputMode() : "FREE";
        Boolean autoCalculateBreakTime = settings.getAutoCalculateBreakTime() != null ? settings.getAutoCalculateBreakTime() : true;

        // 中抜け（通常の休憩）は常に使用可能（昼休憩ボタンは削除済み）

        // 今日の出勤記録を取得
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

        // 休憩中の記録を取得
        List<BreakRecord> activeBreaks = breakRecordRepository.findByAttendance_IdAndBreakEndIsNull(attendance.getId());
        if (activeBreaks.isEmpty()) {
            throw new IllegalStateException("休憩開始が打刻されていません");
        }

        // 最新の休憩記録を取得
        BreakRecord breakRecord = activeBreaks.get(0);

        // 休憩終了時刻を決定（中抜けは常に現在時刻）
        LocalDateTime breakEndTime = LocalDateTime.now();

        breakRecord.setBreakEnd(breakEndTime);
        breakRecordRepository.save(breakRecord);

        // 後方互換性のため、Attendanceにも設定
        // すべての休憩が終了している場合のみ設定
        List<BreakRecord> allActiveBreaks = breakRecordRepository.findByAttendance_IdAndBreakEndIsNull(attendance.getId());
        if (allActiveBreaks.isEmpty()) {
            // 最後の休憩終了時刻を設定（後方互換性のため）
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

    /**
     * 中抜け開始打刻
     */
    @Transactional
    public Attendance startLeave(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }
        User user = userOptional.get();

        // 企業設定を取得
        Optional<CompanySettings> settingsOptional = companySettingsRepository.findByCompanyId(user.getCompany().getId());
        CompanySettings settings = settingsOptional.orElse(new CompanySettings());
        String leaveDefaultType = settings.getLeaveDefaultType() != null ? settings.getLeaveDefaultType() : "DEDUCTION";

        // 今日の出勤記録を取得
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

        // 既に中抜け中の記録があるかチェック
        List<LeaveRecord> activeLeaves = leaveRecordRepository.findByAttendance_IdAndLeaveEndIsNull(attendance.getId());
        if (!activeLeaves.isEmpty()) {
            throw new IllegalStateException("既に中抜け中です");
        }

        // 休憩中でないことを確認（休憩と中抜けは同時に進行できない）
        List<BreakRecord> activeBreaks = breakRecordRepository.findByAttendance_IdAndBreakEndIsNull(attendance.getId());
        if (!activeBreaks.isEmpty()) {
            throw new IllegalStateException("休憩中です。休憩を終了してから中抜けを開始してください");
        }

        // 中抜け開始時刻を決定
        LocalDateTime leaveStartTime = LocalDateTime.now();

        // LeaveRecordを作成
        LeaveRecord leaveRecord = new LeaveRecord();
        leaveRecord.setAttendance(attendance);
        leaveRecord.setLeaveStart(leaveStartTime);
        leaveRecord.setLeaveType(leaveDefaultType); // デフォルト設定を使用
        leaveRecordRepository.save(leaveRecord);

        return attendanceRepository.save(attendance);
    }

    /**
     * 中抜け終了打刻
     */
    @Transactional
    public Attendance endLeave(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }
        User user = userOptional.get();

        // 今日の出勤記録を取得
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

        // 中抜け中の記録を取得
        List<LeaveRecord> activeLeaves = leaveRecordRepository.findByAttendance_IdAndLeaveEndIsNull(attendance.getId());
        if (activeLeaves.isEmpty()) {
            throw new IllegalStateException("中抜け開始が打刻されていません");
        }

        // 最新の中抜け記録を取得
        LeaveRecord leaveRecord = activeLeaves.get(0);

        // 中抜け終了時刻を決定
        LocalDateTime leaveEndTime = LocalDateTime.now();

        leaveRecord.setLeaveEnd(leaveEndTime);
        leaveRecordRepository.save(leaveRecord);

        return attendanceRepository.save(attendance);
    }

    /**
     * ユーザーの勤怠履歴を取得
     */
    public List<Attendance> getAttendanceHistory(Long userId) {
        return attendanceRepository.findByUser_IdOrderByCheckInDesc(userId);
    }

    /**
     * 勤怠IDで中抜け記録を取得
     */
    public List<LeaveRecord> getLeaveRecordsByAttendanceId(Long attendanceId) {
        return leaveRecordRepository.findByAttendance_IdOrderByLeaveStartAsc(attendanceId);
    }

    /**
     * 期間指定で勤怠履歴を取得
     */
    public List<Attendance> getAttendanceByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return attendanceRepository.findByUser_IdAndCheckInBetweenOrderByCheckInDesc(userId, startDate, endDate);
    }

    /**
     * 現在の勤怠状態を取得
     */
    public Optional<Attendance> getActiveAttendance(Long userId) {
        return attendanceRepository.findByUser_IdAndCheckOutIsNullOrderByCheckInDesc(userId);
    }

    /**
     * 今日の勤怠状態を取得（出勤、休憩中、中抜け中の状態を含む）
     */
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
            List<LeaveRecord> activeLeaves = leaveRecordRepository.findByAttendance_IdAndLeaveEndIsNull(attendance.getId());
            isOnLeave = !activeLeaves.isEmpty();
        }

        return new TodayAttendanceStatus(hasCheckedIn, hasCheckedOut, isOnBreak, isOnLeave);
    }

    /**
     * 今日の勤怠状態を表す内部クラス
     */
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

    /**
     * 勤怠IDで取得
     */
    public Optional<Attendance> getAttendanceById(Long id) {
        return attendanceRepository.findById(id);
    }

    /**
     * すべての勤怠記録を取得 (企業ごと)
     */
    public List<Attendance> findAllByCompanyId(Long companyId) {
        return attendanceRepository.findAllByUser_CompanyId(companyId);
    }

    /**
     * 異常検知: 残業時間が1分を超えているかチェック (企業ごと)
     * 注意: テスト用に1分に設定しています。本番環境では8時間（480分）に戻してください。
     */
    public List<Attendance> detectOvertime(Long companyId) {
        List<Attendance> allAttendances = findAllByCompanyId(companyId);
        return allAttendances.stream()
                .filter(a -> a.getCheckOut() != null)
                .filter(a -> {
                    long workMinutes = Duration.between(a.getCheckIn(), a.getCheckOut()).toMinutes();
                    // 休憩時間を引く（BreakRecordを使用）
                    List<BreakRecord> breakRecords = breakRecordRepository.findByAttendance_IdOrderByBreakStartAsc(a.getId());
                    long totalBreakMinutes = breakRecords.stream()
                            .filter(br -> br.getBreakEnd() != null)
                            .mapToLong(BreakRecord::getBreakMinutes)
                            .sum();
                    workMinutes -= totalBreakMinutes;
                    return workMinutes > 1; // テスト用: 1分を超えたら異常検知（本番では480分に戻す）
                })
                .toList();
    }

    /**
     * 異常検知: 打刻漏れ(退勤未打刻)を検出 (企業ごと)
     */
    public List<Attendance> detectMissingCheckOut(Long companyId) {
        return findAllByCompanyId(companyId).stream()
                .filter(a -> a.getCheckOut() == null && a.getCheckIn() != null)
                .toList();
    }
}
