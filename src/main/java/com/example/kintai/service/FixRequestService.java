package com.example.kintai.service;

import com.example.kintai.entity.AnomalyApproval;
import com.example.kintai.entity.Attendance;
import com.example.kintai.entity.FixRequest;
import com.example.kintai.entity.LeaveRecord;
import com.example.kintai.entity.User;
import com.example.kintai.repository.AnomalyApprovalRepository;
import com.example.kintai.repository.AttendanceRepository;
import com.example.kintai.repository.FixRequestRepository;
import com.example.kintai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FixRequestService {

    @Autowired
    private FixRequestRepository fixRequestRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private LeaveRecordService leaveRecordService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnomalyApprovalRepository anomalyApprovalRepository;

    @Autowired
    private OvertimeExcessService overtimeExcessService;

    @Autowired
    private SlackNotificationService slackNotificationService;

    @Transactional
    public FixRequest createFixRequest(Long attendanceId, Long userId, String requestType,
                                        LocalDateTime newValue, String reason) {
        return createFixRequest(attendanceId, userId, requestType, newValue, null, null, reason);
    }

    @Transactional
    public FixRequest createFixRequest(Long attendanceId, Long userId, String requestType,
                                        LocalDateTime newValue, Long leaveRecordId, String newLeaveType, String reason) {
        return createFixRequest(attendanceId, userId, requestType, newValue, null, leaveRecordId, newLeaveType, reason);
    }

    @Transactional
    public FixRequest createFixRequest(Long attendanceId, Long userId, String requestType,
                                        LocalDateTime newValue, LocalDateTime newValue2, Long leaveRecordId, String newLeaveType, String reason) {
        
        Optional<Attendance> attendanceOptional = attendanceRepository.findById(attendanceId);
        if (attendanceOptional.isEmpty()) {
            throw new IllegalArgumentException("指定された勤怠レコードが存在しません");
        }
        Attendance attendance = attendanceOptional.get();

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("ユーザーが見つかりません");
        }
        User user = userOptional.get();

        LeaveRecord leaveRecord = null;
        if (leaveRecordId != null) {
            leaveRecord = leaveRecordService.getLeaveRecordById(leaveRecordId)
                    .orElseThrow(() -> new IllegalArgumentException("指定された中抜け記録が存在しません"));
            if (!leaveRecord.getAttendance().getId().equals(attendanceId)) {
                throw new IllegalArgumentException("指定された中抜け記録はこの勤怠記録に属していません");
            }
        }

        FixRequest fixRequest = new FixRequest();
        fixRequest.setAttendance(attendance);
        fixRequest.setUser(user);
        fixRequest.setRequestType(requestType);
        fixRequest.setNewValue(newValue);
        fixRequest.setNewValue2(newValue2);
        fixRequest.setLeaveRecord(leaveRecord);
        fixRequest.setNewLeaveType(newLeaveType);
        fixRequest.setReason(reason);
        fixRequest.setStatus("PENDING");
        fixRequest.setCreatedAt(LocalDateTime.now());

        FixRequest savedFixRequest = fixRequestRepository.save(fixRequest);

        Map<String, Object> attachment = new HashMap<>();
        attachment.put("color", "#FFA500"); 
        
        List<Map<String, Object>> fields = new ArrayList<>();
        
        Map<String, Object> userField = new HashMap<>();
        userField.put("title", "ユーザー");
        userField.put("value", user.getUsername() + " (" + user.getCompany().getName() + ")");
        userField.put("short", true);
        fields.add(userField);
        
        Map<String, Object> typeField = new HashMap<>();
        typeField.put("title", "修正タイプ");
        typeField.put("value", getRequestTypeLabel(requestType));
        typeField.put("short", true);
        fields.add(typeField);
        
        Map<String, Object> newValueField = new HashMap<>();
        if ("LEAVE_TYPE".equals(requestType)) {
            newValueField.put("title", "新しい値");
            newValueField.put("value", "DEDUCTION".equals(newLeaveType) ? "控除" : "有給");
            newValueField.put("short", true);
        } else if ("CHECK_IN_AND_OUT".equals(requestType) && newValue != null && newValue2 != null) {
            newValueField.put("title", "新しい値");
            newValueField.put("value", "出勤: " + newValue.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + ", 退勤: " + newValue2.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            newValueField.put("short", true);
        } else if ("BREAK_START_AND_END".equals(requestType) && newValue != null && newValue2 != null) {
            newValueField.put("title", "新しい値");
            newValueField.put("value", "開始: " + newValue.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + ", 終了: " + newValue2.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            newValueField.put("short", true);
        } else if (newValue != null) {
            newValueField.put("title", "新しい値");
            newValueField.put("value", newValue.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            newValueField.put("short", true);
        } else {
            newValueField.put("title", "新しい値");
            newValueField.put("value", "-");
            newValueField.put("short", true);
        }
        fields.add(newValueField);
        
        Map<String, Object> reasonField = new HashMap<>();
        reasonField.put("title", "理由");
        reasonField.put("value", reason);
        reasonField.put("short", false);
        fields.add(reasonField);
        
        attachment.put("fields", fields);
        attachment.put("footer", "勤怠管理システム");
        attachment.put("ts", System.currentTimeMillis() / 1000);
        
        List<Map<String, Object>> attachments = new ArrayList<>();
        attachments.add(attachment);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("text", "新しい修正依頼が提出されました");
        payload.put("attachments", attachments);
        
        slackNotificationService.sendAdminNotificationWithAttachment(payload, user.getCompany().getId());

        return savedFixRequest;
    }

    public List<FixRequest> getFixRequestsByUserId(Long userId) {
        return fixRequestRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    public List<FixRequest> findAllByCompanyId(Long companyId) {
        return fixRequestRepository.findAllByUser_CompanyId(companyId);
    }

    public List<FixRequest> findPendingRequestsByCompanyId(Long companyId) {
        return fixRequestRepository.findByStatusAndUser_CompanyId("PENDING", companyId);
    }

    @Transactional
    public FixRequest approveFixRequest(Long requestId, Long approvedByUserId) {
        Optional<FixRequest> optionalRequest = fixRequestRepository.findById(requestId);
        if (optionalRequest.isEmpty()) {
            throw new IllegalArgumentException("修正依頼が見つかりません");
        }

        FixRequest fixRequest = optionalRequest.get();
        if (!"PENDING".equals(fixRequest.getStatus())) {
            throw new IllegalStateException("この修正依頼は既に処理されています");
        }

        Optional<Attendance> optionalAttendance = attendanceRepository.findById(fixRequest.getAttendanceId());
        if (optionalAttendance.isPresent()) {
            Attendance attendance = optionalAttendance.get();

            switch (fixRequest.getRequestType()) {
                case "CHECK_IN":
                    attendance.setCheckIn(fixRequest.getNewValue());
                    attendanceRepository.saveAndFlush(attendance);
                    break;
                case "CHECK_OUT":
                    attendance.setCheckOut(fixRequest.getNewValue());
                    attendanceRepository.saveAndFlush(attendance);
                    break;
                case "BREAK_START":
                    attendance.setBreakStart(fixRequest.getNewValue());
                    attendanceRepository.saveAndFlush(attendance);
                    break;
                case "BREAK_END":
                    attendance.setBreakEnd(fixRequest.getNewValue());
                    attendanceRepository.saveAndFlush(attendance);
                    break;
                case "LEAVE_START":
                    if (fixRequest.getLeaveRecord() != null && fixRequest.getNewValue() != null) {
                        LeaveRecord leaveRecord = fixRequest.getLeaveRecord();
                        leaveRecord.setLeaveStart(fixRequest.getNewValue());
                        leaveRecordService.save(leaveRecord);
                    } else {
                        throw new IllegalArgumentException("中抜け記録または新しい値が指定されていません");
                    }
                    break;
                case "LEAVE_END":
                    if (fixRequest.getLeaveRecord() != null && fixRequest.getNewValue() != null) {
                        LeaveRecord leaveRecord = fixRequest.getLeaveRecord();
                        leaveRecord.setLeaveEnd(fixRequest.getNewValue());
                        leaveRecordService.save(leaveRecord);
                    } else {
                        throw new IllegalArgumentException("中抜け記録または新しい値が指定されていません");
                    }
                    break;
                case "LEAVE_TYPE":
                    if (fixRequest.getLeaveRecord() != null && fixRequest.getNewLeaveType() != null) {
                        LeaveRecord leaveRecord = fixRequest.getLeaveRecord();
                        leaveRecord.setLeaveType(fixRequest.getNewLeaveType());
                        leaveRecordService.save(leaveRecord);
                    } else {
                        throw new IllegalArgumentException("中抜け記録または新しい扱いが指定されていません");
                    }
                    break;
                case "OVERTIME_APPLICATION":
                    
                    break;
                case "CHECK_IN_AND_OUT":
                    if (fixRequest.getNewValue() != null && fixRequest.getNewValue2() != null) {
                        attendance.setCheckIn(fixRequest.getNewValue());
                        attendance.setCheckOut(fixRequest.getNewValue2());
                        attendanceRepository.saveAndFlush(attendance);
                    } else {
                        throw new IllegalArgumentException("出勤・退勤の両方の値が指定されていません");
                    }
                    break;
                case "BREAK_START_AND_END":
                    if (fixRequest.getNewValue() != null && fixRequest.getNewValue2() != null) {
                        attendance.setBreakStart(fixRequest.getNewValue());
                        attendance.setBreakEnd(fixRequest.getNewValue2());
                        attendanceRepository.saveAndFlush(attendance);
                    } else {
                        throw new IllegalArgumentException("休憩開始・終了の両方の値が指定されていません");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("無効な修正タイプです: " + fixRequest.getRequestType());
            }
        }

        fixRequest.setStatus("APPROVED");
        
        if (approvedByUserId != null) {
            Optional<User> approvedByUser = userRepository.findById(approvedByUserId);
            if (approvedByUser.isPresent()) {
                fixRequest.setApprovedBy(approvedByUser.get());
            }
        }
        
        FixRequest savedFixRequest = fixRequestRepository.saveAndFlush(fixRequest);

        markAnomalyResolvedByFixRequest(savedFixRequest.getAttendanceId(), savedFixRequest.getRequestType(), approvedByUserId);

        User user = savedFixRequest.getUser();
        String mention = user.getSlackUserId() != null ? "<@" + user.getSlackUserId() + "> " : "";
        
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("color", "#36a64f"); 
        attachment.put("pretext", mention + "修正依頼が承認されました");
        
        List<Map<String, Object>> fields = new ArrayList<>();
        
        Map<String, Object> typeField = new HashMap<>();
        typeField.put("title", "修正タイプ");
        typeField.put("value", getRequestTypeLabel(savedFixRequest.getRequestType()));
        typeField.put("short", true);
        fields.add(typeField);
        
        Map<String, Object> newValueField = new HashMap<>();
        if ("LEAVE_TYPE".equals(savedFixRequest.getRequestType())) {
            newValueField.put("title", "新しい値");
            newValueField.put("value", "DEDUCTION".equals(savedFixRequest.getNewLeaveType()) ? "控除" : "有給");
            newValueField.put("short", true);
        } else if ("CHECK_IN_AND_OUT".equals(savedFixRequest.getRequestType()) && savedFixRequest.getNewValue() != null && savedFixRequest.getNewValue2() != null) {
            newValueField.put("title", "新しい値");
            newValueField.put("value", "出勤: " + savedFixRequest.getNewValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + ", 退勤: " + savedFixRequest.getNewValue2().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            newValueField.put("short", true);
        } else if ("BREAK_START_AND_END".equals(savedFixRequest.getRequestType()) && savedFixRequest.getNewValue() != null && savedFixRequest.getNewValue2() != null) {
            newValueField.put("title", "新しい値");
            newValueField.put("value", "開始: " + savedFixRequest.getNewValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + ", 終了: " + savedFixRequest.getNewValue2().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            newValueField.put("short", true);
        } else if (savedFixRequest.getNewValue() != null) {
            newValueField.put("title", "新しい値");
            newValueField.put("value", savedFixRequest.getNewValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            newValueField.put("short", true);
        } else {
            newValueField.put("title", "新しい値");
            newValueField.put("value", "-");
            newValueField.put("short", true);
        }
        fields.add(newValueField);
        
        Map<String, Object> reasonField = new HashMap<>();
        reasonField.put("title", "理由");
        reasonField.put("value", savedFixRequest.getReason());
        reasonField.put("short", false);
        fields.add(reasonField);
        
        attachment.put("fields", fields);
        attachment.put("footer", "勤怠管理システム");
        attachment.put("ts", System.currentTimeMillis() / 1000);
        
        List<Map<String, Object>> attachments = new ArrayList<>();
        attachments.add(attachment);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("attachments", attachments);
        
        slackNotificationService.sendUserDM(payload, savedFixRequest.getUser().getId());

        return savedFixRequest;
    }

    @Transactional
    public FixRequest rejectFixRequest(Long requestId) {
        Optional<FixRequest> optionalRequest = fixRequestRepository.findById(requestId);
        if (optionalRequest.isEmpty()) {
            throw new IllegalArgumentException("修正依頼が見つかりません");
        }

        FixRequest fixRequest = optionalRequest.get();
        if (!"PENDING".equals(fixRequest.getStatus())) {
            throw new IllegalStateException("この修正依頼は既に処理されています");
        }

        fixRequest.setStatus("REJECTED");
        FixRequest savedFixRequest = fixRequestRepository.saveAndFlush(fixRequest);

        User user = savedFixRequest.getUser();
        String mention = user.getSlackUserId() != null ? "<@" + user.getSlackUserId() + "> " : "";
        
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("color", "#D00000"); 
        attachment.put("pretext", mention + "修正依頼が却下されました");
        
        List<Map<String, Object>> fields = new ArrayList<>();
        
        Map<String, Object> typeField = new HashMap<>();
        typeField.put("title", "修正タイプ");
        typeField.put("value", getRequestTypeLabel(savedFixRequest.getRequestType()));
        typeField.put("short", true);
        fields.add(typeField);
        
        Map<String, Object> reasonField = new HashMap<>();
        reasonField.put("title", "理由");
        reasonField.put("value", savedFixRequest.getReason());
        reasonField.put("short", false);
        fields.add(reasonField);
        
        attachment.put("fields", fields);
        attachment.put("footer", "勤怠管理システム");
        attachment.put("ts", System.currentTimeMillis() / 1000);
        
        List<Map<String, Object>> attachments = new ArrayList<>();
        attachments.add(attachment);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("text", mention + "修正依頼が却下されました");
        payload.put("attachments", attachments);
        
        slackNotificationService.sendUserDM(payload, savedFixRequest.getUser().getId());

        return savedFixRequest;
    }

    public Optional<FixRequest> getFixRequestById(Long id) {
        return fixRequestRepository.findById(id);
    }

    private void markAnomalyResolvedByFixRequest(Long attendanceId, String requestType, Long approvedByUserId) {
        
        if ("CHECK_IN".equals(requestType) || "CHECK_OUT".equals(requestType)
                || "BREAK_START".equals(requestType) || "BREAK_END".equals(requestType)
                || "CHECK_IN_AND_OUT".equals(requestType) || "BREAK_START_AND_END".equals(requestType)
                || "OVERTIME_APPLICATION".equals(requestType)) {
            overtimeExcessService.markResolved(attendanceId, approvedByUserId);
        }
        
        if ("CHECK_OUT".equals(requestType)) {
            markAnomalyResolved(attendanceId, "MISSING_CHECKOUT", approvedByUserId);
        }
    }

    private void markAnomalyResolved(Long attendanceId, String anomalyType, Long approvedByUserId) {
        Optional<AnomalyApproval> existing = anomalyApprovalRepository.findFirstByAttendance_IdAndAnomalyType(attendanceId, anomalyType);
        User approver = null;
        if (approvedByUserId != null) {
            approver = userRepository.findById(approvedByUserId).orElse(null);
        }
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
            approval.setAnomalyType(anomalyType);
            approval.setApproved(true);
            approval.setApprovedAt(LocalDateTime.now());
            approval.setApprovedBy(approver);
            approval.setReason("修正依頼承認により解決");
            anomalyApprovalRepository.saveAndFlush(approval);
        }
    }

    private String getRequestTypeLabel(String requestType) {
        switch (requestType) {
            case "CHECK_IN":
                return "出勤時刻";
            case "CHECK_OUT":
                return "退勤時刻";
            case "BREAK_START":
                return "休憩開始時刻";
            case "BREAK_END":
                return "休憩終了時刻";
            case "LEAVE_START":
                return "中抜け開始時刻";
            case "LEAVE_END":
                return "中抜け終了時刻";
            case "LEAVE_TYPE":
                return "中抜けの扱い";
            case "OVERTIME_APPLICATION":
                return "理由付き残業申請";
            case "CHECK_IN_AND_OUT":
                return "打刻訂正（出勤・退勤）";
            case "BREAK_START_AND_END":
                return "休憩補正（開始・終了）";
            default:
                return requestType;
        }
    }
}
