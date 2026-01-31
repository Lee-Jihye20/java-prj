package com.example.kintai.service;

import com.example.kintai.entity.AnomalyApproval;
import com.example.kintai.entity.Attendance;
import com.example.kintai.entity.User;
import com.example.kintai.repository.AnomalyApprovalRepository;
import com.example.kintai.repository.AttendanceRepository;
import com.example.kintai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 残業超過の計算と扱いを一括で行うサービス。
 * 閾値（9時間）、検出、未解決一覧取得、解決済みマークをここに集約する。
 */
@Service
public class OvertimeExcessService {

    /** 残業超過の閾値（分）。実働がこの値を超えると「残業超過」異常として扱う。 */
    public static final long OVERTIME_EXCESS_THRESHOLD_MINUTES = 540L; // 9時間

    /** 異常種別（AnomalyApproval 用） */
    public static final String ANOMALY_TYPE_OVERTIME = "OVERTIME";

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private BreakRecordService breakRecordService;
    @Autowired
    private LeaveRecordService leaveRecordService;

    @Autowired
    private AnomalyApprovalRepository anomalyApprovalRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 閾値（分）を返す。Slack通知などのメッセージ用。
     */
    public long getThresholdMinutes() {
        return OVERTIME_EXCESS_THRESHOLD_MINUTES;
    }

    /**
     * 指定勤怠が残業超過かどうか判定する。
     * 実働 = 出退勤差 − BreakRecord の休憩。休憩は月次レポート・カレンダーと同じく BreakRecord のみで計算。
     */
    public boolean isOvertimeExcess(Attendance attendance) {
        if (attendance.getCheckIn() == null || attendance.getCheckOut() == null) {
            return false;
        }
        long workMinutes = getWorkMinutes(attendance);
        return workMinutes > OVERTIME_EXCESS_THRESHOLD_MINUTES;
    }

    /**
     * 会社内の残業超過となっている勤怠をすべて返す（解決済み・未解決の区別なし）。
     */
    public List<Attendance> detectOvertime(Long companyId) {
        List<Attendance> all = attendanceRepository.findAllByUser_CompanyId(companyId);
        return all.stream()
                .filter(a -> a.getCheckOut() != null)
                .filter(this::isOvertimeExcess)
                .toList();
    }

    /**
     * 未解決の残業超過のみ返す（管理者用・異常検知一覧）。
     */
    public List<Attendance> getUnresolvedOvertimeAnomalies(Long companyId) {
        return detectOvertime(companyId).stream()
                .filter(a -> !isResolved(a.getId()))
                .toList();
    }

    /**
     * 未解決の残業超過件数。管理者ダッシュボードの「未解決異常数」用。
     */
    public long getUnresolvedOvertimeCount(Long companyId) {
        return detectOvertime(companyId).stream()
                .filter(a -> !isResolved(a.getId()))
                .count();
    }

    /**
     * 指定ユーザーの未解決残業超過一覧（従業員ダッシュボード・修正依頼案内用）。
     */
    public List<Attendance> getUnresolvedOvertimeForUser(Long userId, Long companyId) {
        return detectOvertime(companyId).stream()
                .filter(a -> a.getUser() != null && userId.equals(a.getUser().getId()))
                .filter(a -> !isResolved(a.getId()))
                .toList();
    }

    /**
     * 残業超過を解決済みにマークする。修正依頼承認時などに呼ぶ。
     */
    public void markResolved(Long attendanceId, Long approvedByUserId) {
        Optional<AnomalyApproval> existing = anomalyApprovalRepository.findFirstByAttendance_IdAndAnomalyType(attendanceId, ANOMALY_TYPE_OVERTIME);
        User approver = approvedByUserId != null ? userRepository.findById(approvedByUserId).orElse(null) : null;
        if (existing.isPresent()) {
            AnomalyApproval a = existing.get();
            a.setApproved(true);
            a.setApprovedAt(LocalDateTime.now());
            a.setApprovedBy(approver);
            anomalyApprovalRepository.saveAndFlush(a);
        } else {
            Optional<Attendance> attOpt = attendanceRepository.findById(attendanceId);
            if (attOpt.isEmpty()) {
                return;
            }
            AnomalyApproval approval = new AnomalyApproval();
            approval.setAttendance(attOpt.get());
            approval.setAnomalyType(ANOMALY_TYPE_OVERTIME);
            approval.setApproved(true);
            approval.setApprovedAt(LocalDateTime.now());
            approval.setApprovedBy(approver);
            approval.setReason("修正依頼承認により解決");
            anomalyApprovalRepository.saveAndFlush(approval);
        }
    }

    /**
     * 指定勤怠が残業超過として既に解決済みかどうか。
     */
    public boolean isResolved(Long attendanceId) {
        return anomalyApprovalRepository.existsByAttendance_IdAndAnomalyTypeAndApprovedTrue(attendanceId, ANOMALY_TYPE_OVERTIME);
    }

    /**
     * 実働分数（出退勤差 − 休憩）。休憩は BreakRecordService で BreakRecord のみから計算。
     */
    private long getWorkMinutes(Attendance attendance) {
        long total = Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();
        long breaktime = breakRecordService.getTotalBreakMinutesFromRecordsOnly(attendance);
        long leavetime = leaveRecordService.getTotalDeductionLeaveMinutes(attendance.getId());
        total -= breaktime + leavetime;
        return total;
    }
}
