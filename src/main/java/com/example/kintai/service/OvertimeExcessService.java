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

@Service
public class OvertimeExcessService {

    public static final long OVERTIME_EXCESS_THRESHOLD_MINUTES = 540L; 

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

    public long getThresholdMinutes() {
        return OVERTIME_EXCESS_THRESHOLD_MINUTES;
    }

    public boolean isOvertimeExcess(Attendance attendance) {
        if (attendance.getCheckIn() == null || attendance.getCheckOut() == null) {
            return false;
        }
        long workMinutes = getWorkMinutes(attendance);
        return workMinutes > OVERTIME_EXCESS_THRESHOLD_MINUTES;
    }

    public List<Attendance> detectOvertime(Long companyId) {
        List<Attendance> all = attendanceRepository.findAllByUser_CompanyId(companyId);
        return all.stream()
                .filter(a -> a.getCheckOut() != null)
                .filter(this::isOvertimeExcess)
                .toList();
    }

    public List<Attendance> getUnresolvedOvertimeAnomalies(Long companyId) {
        return detectOvertime(companyId).stream()
                .filter(a -> !isResolved(a.getId()))
                .toList();
    }

    public long getUnresolvedOvertimeCount(Long companyId) {
        return detectOvertime(companyId).stream()
                .filter(a -> !isResolved(a.getId()))
                .count();
    }

    public List<Attendance> getUnresolvedOvertimeForUser(Long userId, Long companyId) {
        return detectOvertime(companyId).stream()
                .filter(a -> a.getUser() != null && userId.equals(a.getUser().getId()))
                .filter(a -> !isResolved(a.getId()))
                .toList();
    }

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

    public boolean isResolved(Long attendanceId) {
        return anomalyApprovalRepository.existsByAttendance_IdAndAnomalyTypeAndApprovedTrue(attendanceId, ANOMALY_TYPE_OVERTIME);
    }

    private long getWorkMinutes(Attendance attendance) {
        long total = Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();
        long breaktime = breakRecordService.getTotalBreakMinutesFromRecordsOnly(attendance);
        long leavetime = leaveRecordService.getTotalDeductionLeaveMinutes(attendance.getId());
        total -= breaktime + leavetime;
        return total;
    }
}
