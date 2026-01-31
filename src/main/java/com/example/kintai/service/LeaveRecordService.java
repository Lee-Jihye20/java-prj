package com.example.kintai.service;

import com.example.kintai.entity.Attendance;
import com.example.kintai.entity.CompanySettings;
import com.example.kintai.entity.LeaveRecord;
import com.example.kintai.entity.User;
import com.example.kintai.repository.AttendanceRepository;
import com.example.kintai.repository.CompanySettingsRepository;
import com.example.kintai.repository.LeaveRecordRepository;
import com.example.kintai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 中抜け（LeaveRecord）の時間管理を一括で行うサービス。
 * 中抜け一覧取得・控除時間合計・開始・終了・修正依頼用の取得・保存をここに集約する。
 */
@Service
public class LeaveRecordService {

    /** 控除タイプ（実働から引く）。実働計算ではこのタイプのみ控除する。 */
    public static final String LEAVE_TYPE_DEDUCTION = "DEDUCTION";

    /** 有給タイプ（実働から引かない） */
    public static final String LEAVE_TYPE_PAID_LEAVE = "PAID_LEAVE";

    @Autowired
    private LeaveRecordRepository leaveRecordRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanySettingsRepository companySettingsRepository;

    @Autowired
    private BreakRecordService breakRecordService;

    /**
     * 勤怠に紐づく中抜け記録を開始時刻昇順で返す。
     */
    public List<LeaveRecord> getLeaveRecordsByAttendanceId(Long attendanceId) {
        return leaveRecordRepository.findByAttendance_IdOrderByLeaveStartAsc(attendanceId);
    }

    /**
     * 終了打刻が未登録の中抜け（中抜け中）を返す。
     */
    public List<LeaveRecord> getActiveLeavesByAttendanceId(Long attendanceId) {
        return leaveRecordRepository.findByAttendance_IdAndLeaveEndIsNull(attendanceId);
    }

    /**
     * 指定勤怠に中抜け中の記録があるかどうか。
     */
    public boolean hasActiveLeave(Long attendanceId) {
        return !getActiveLeavesByAttendanceId(attendanceId).isEmpty();
    }

    /**
     * 控除対象の中抜け時間の合計（分）。DEDUCTION かつ終了打刻済みのみ。実働計算用。
     */
    public long getTotalDeductionLeaveMinutes(Long attendanceId) {
        List<LeaveRecord> leaveRecords = leaveRecordRepository.findByAttendance_IdOrderByLeaveStartAsc(attendanceId);
        return leaveRecords.stream()
                .filter(lr -> lr.getLeaveEnd() != null && LEAVE_TYPE_DEDUCTION.equals(lr.getLeaveType()))
                .mapToLong(LeaveRecord::getLeaveMinutes)
                .sum();
    }

    /**
     * IDで中抜け記録を1件取得。修正依頼の紐付け用。
     */
    public Optional<LeaveRecord> getLeaveRecordById(Long id) {
        return leaveRecordRepository.findById(id);
    }

    /**
     * 中抜け記録を保存。修正依頼承認時の更新用。
     */
    @Transactional
    public LeaveRecord save(LeaveRecord leaveRecord) {
        return leaveRecordRepository.saveAndFlush(leaveRecord);
    }

    /**
     * 中抜け開始打刻
     */
    @Transactional
    public Attendance startLeave(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        CompanySettings settings = companySettingsRepository.findByCompanyId(user.getCompany().getId())
                .orElse(new CompanySettings());
        String leaveDefaultType = settings.getLeaveDefaultType() != null ? settings.getLeaveDefaultType() : LEAVE_TYPE_DEDUCTION;

        Attendance attendance = getTodayAttendanceOrThrow(userId);

        if (attendance.getCheckOut() != null) {
            throw new IllegalStateException("既に退勤済みです");
        }
        if (hasActiveLeave(attendance.getId())) {
            throw new IllegalStateException("既に中抜け中です");
        }
        if (!breakRecordService.getActiveBreaksByAttendanceId(attendance.getId()).isEmpty()) {
            throw new IllegalStateException("休憩中です。休憩を終了してから中抜けを開始してください");
        }

        LeaveRecord leaveRecord = new LeaveRecord();
        leaveRecord.setAttendance(attendance);
        leaveRecord.setLeaveStart(LocalDateTime.now());
        leaveRecord.setLeaveType(leaveDefaultType);
        leaveRecordRepository.save(leaveRecord);

        return attendanceRepository.save(attendance);
    }

    /**
     * 中抜け終了打刻
     */
    @Transactional
    public Attendance endLeave(Long userId) {
        Attendance attendance = getTodayAttendanceOrThrow(userId);

        if (attendance.getCheckOut() != null) {
            throw new IllegalStateException("既に退勤済みです");
        }

        List<LeaveRecord> activeLeaves = getActiveLeavesByAttendanceId(attendance.getId());
        if (activeLeaves.isEmpty()) {
            throw new IllegalStateException("中抜け開始が打刻されていません");
        }

        LeaveRecord leaveRecord = activeLeaves.get(0);
        leaveRecord.setLeaveEnd(LocalDateTime.now());
        leaveRecordRepository.save(leaveRecord);

        return attendanceRepository.save(attendance);
    }

    private Attendance getTodayAttendanceOrThrow(Long userId) {
        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
        List<Attendance> todayAttendances = attendanceRepository.findByUser_IdAndCheckInBetweenOrderByCheckInDesc(
                userId, startOfToday, endOfToday);
        if (todayAttendances.isEmpty()) {
            throw new IllegalStateException("本日の出勤打刻がされていません");
        }
        return todayAttendances.get(0);
    }
}
