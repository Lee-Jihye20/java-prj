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

@Service
public class LeaveRecordService {

    public static final String LEAVE_TYPE_DEDUCTION = "DEDUCTION";

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

    public List<LeaveRecord> getLeaveRecordsByAttendanceId(Long attendanceId) {
        return leaveRecordRepository.findByAttendance_IdOrderByLeaveStartAsc(attendanceId);
    }

    public List<LeaveRecord> getActiveLeavesByAttendanceId(Long attendanceId) {
        return leaveRecordRepository.findByAttendance_IdAndLeaveEndIsNull(attendanceId);
    }

    public boolean hasActiveLeave(Long attendanceId) {
        return !getActiveLeavesByAttendanceId(attendanceId).isEmpty();
    }

    public long getTotalDeductionLeaveMinutes(Long attendanceId) {
        List<LeaveRecord> leaveRecords = leaveRecordRepository.findByAttendance_IdOrderByLeaveStartAsc(attendanceId);
        return leaveRecords.stream()
                .filter(lr -> lr.getLeaveEnd() != null && LEAVE_TYPE_DEDUCTION.equals(lr.getLeaveType()))
                .mapToLong(LeaveRecord::getLeaveMinutes)
                .sum();
    }

    public Optional<LeaveRecord> getLeaveRecordById(Long id) {
        return leaveRecordRepository.findById(id);
    }

    @Transactional
    public LeaveRecord save(LeaveRecord leaveRecord) {
        return leaveRecordRepository.saveAndFlush(leaveRecord);
    }

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
