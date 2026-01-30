package com.example.kintai.service;

import com.example.kintai.entity.Attendance;
import com.example.kintai.entity.FixRequest;
import com.example.kintai.entity.LeaveRecord;
import com.example.kintai.entity.User;
import com.example.kintai.repository.AttendanceRepository;
import com.example.kintai.repository.FixRequestRepository;
import com.example.kintai.repository.LeaveRecordRepository;
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
    private LeaveRecordRepository leaveRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SlackNotificationService slackNotificationService;

    /**
     * 修正依頼を作成
     */
    @Transactional
    public FixRequest createFixRequest(Long attendanceId, Long userId, String requestType,
                                        LocalDateTime newValue, String reason) {
        return createFixRequest(attendanceId, userId, requestType, newValue, null, null, reason);
    }

    /**
     * 修正依頼を作成（中抜け対応版）
     */
    @Transactional
    public FixRequest createFixRequest(Long attendanceId, Long userId, String requestType,
                                        LocalDateTime newValue, Long leaveRecordId, String newLeaveType, String reason) {
        // 勤怠レコードの存在確認
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

        // 中抜け関連の修正依頼の場合、中抜け記録の存在確認
        LeaveRecord leaveRecord = null;
        if (leaveRecordId != null) {
            Optional<LeaveRecord> leaveRecordOptional = leaveRecordRepository.findById(leaveRecordId);
            if (leaveRecordOptional.isEmpty()) {
                throw new IllegalArgumentException("指定された中抜け記録が存在しません");
            }
            leaveRecord = leaveRecordOptional.get();
            // 中抜け記録が該当の勤怠記録に属しているか確認
            if (!leaveRecord.getAttendance().getId().equals(attendanceId)) {
                throw new IllegalArgumentException("指定された中抜け記録はこの勤怠記録に属していません");
            }
        }

        FixRequest fixRequest = new FixRequest();
        fixRequest.setAttendance(attendance);
        fixRequest.setUser(user);
        fixRequest.setRequestType(requestType);
        fixRequest.setNewValue(newValue);
        fixRequest.setLeaveRecord(leaveRecord);
        fixRequest.setNewLeaveType(newLeaveType);
        fixRequest.setReason(reason);
        fixRequest.setStatus("PENDING");
        fixRequest.setCreatedAt(LocalDateTime.now());

        FixRequest savedFixRequest = fixRequestRepository.save(fixRequest);

        // Slack通知 (管理者チャンネル) - embed形式
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("color", "#FFA500"); // オレンジ色
        
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

    /**
     * ユーザーの修正依頼一覧を取得
     */
    public List<FixRequest> getFixRequestsByUserId(Long userId) {
        return fixRequestRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    /**
     * すべての修正依頼を取得(企業ごと)
     */
    public List<FixRequest> findAllByCompanyId(Long companyId) {
        return fixRequestRepository.findAllByUser_CompanyId(companyId);
    }

    /**
     * 保留中の修正依頼を取得 (企業ごと)
     */
    public List<FixRequest> findPendingRequestsByCompanyId(Long companyId) {
        return fixRequestRepository.findByStatusAndUser_CompanyId("PENDING", companyId);
    }

    /**
     * 修正依頼を承認
     */
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

        // 勤怠レコードまたは中抜け記録を更新
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
                        leaveRecordRepository.saveAndFlush(leaveRecord);
                    } else {
                        throw new IllegalArgumentException("中抜け記録または新しい値が指定されていません");
                    }
                    break;
                case "LEAVE_END":
                    if (fixRequest.getLeaveRecord() != null && fixRequest.getNewValue() != null) {
                        LeaveRecord leaveRecord = fixRequest.getLeaveRecord();
                        leaveRecord.setLeaveEnd(fixRequest.getNewValue());
                        leaveRecordRepository.saveAndFlush(leaveRecord);
                    } else {
                        throw new IllegalArgumentException("中抜け記録または新しい値が指定されていません");
                    }
                    break;
                case "LEAVE_TYPE":
                    if (fixRequest.getLeaveRecord() != null && fixRequest.getNewLeaveType() != null) {
                        LeaveRecord leaveRecord = fixRequest.getLeaveRecord();
                        leaveRecord.setLeaveType(fixRequest.getNewLeaveType());
                        leaveRecordRepository.saveAndFlush(leaveRecord);
                    } else {
                        throw new IllegalArgumentException("中抜け記録または新しい扱いが指定されていません");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("無効な修正タイプです: " + fixRequest.getRequestType());
            }
        }

        // 修正依頼のステータスを更新
        fixRequest.setStatus("APPROVED");
        
        // 承認者を記録
        if (approvedByUserId != null) {
            Optional<User> approvedByUser = userRepository.findById(approvedByUserId);
            if (approvedByUser.isPresent()) {
                fixRequest.setApprovedBy(approvedByUser.get());
            }
        }
        
        FixRequest savedFixRequest = fixRequestRepository.saveAndFlush(fixRequest);

        // Slack通知 (ユーザーDM) - embed形式
        User user = savedFixRequest.getUser();
        String mention = user.getSlackUserId() != null ? "<@" + user.getSlackUserId() + "> " : "";
        
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("color", "#36a64f"); // 緑色（承認）
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

    /**
     * 修正依頼を却下
     */
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

        // Slack通知 (ユーザーDM) - embed形式
        User user = savedFixRequest.getUser();
        String mention = user.getSlackUserId() != null ? "<@" + user.getSlackUserId() + "> " : "";
        
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("color", "#D00000"); // 赤色（却下）
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

    /**
     * IDで修正依頼を取得
     */
    public Optional<FixRequest> getFixRequestById(Long id) {
        return fixRequestRepository.findById(id);
    }

    /**
     * 修正タイプを日本語ラベルに変換
     */
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
            default:
                return requestType;
        }
    }
}
