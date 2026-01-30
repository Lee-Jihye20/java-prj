package com.example.kintai.controller;

import com.example.kintai.dto.AttendanceLogDTO;
import com.example.kintai.entity.Attendance;
import com.example.kintai.entity.FixRequest;
import com.example.kintai.entity.User;
import com.example.kintai.service.AttendanceService;
import com.example.kintai.service.ExportService;
import com.example.kintai.service.FixRequestService;
import com.example.kintai.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private FixRequestService fixRequestService;

    @Autowired
    private ExportService exportService;

    @Autowired
    private PermissionService permissionService;

    /**
     * 出勤打刻
     */
    @PostMapping("/check-in")
    public String checkIn(Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = getUserFromAuth(authentication);
        try {
            attendanceService.checkIn(user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "出勤打刻が完了しました");
            return "redirect:/employee/dashboard";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/employee/dashboard";
        }
    }

    /**
     * 退勤打刻
     */
    @PostMapping("/check-out")
    public String checkOut(Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = getUserFromAuth(authentication);
        try {
            attendanceService.checkOut(user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "退勤打刻が完了しました");
            return "redirect:/employee/dashboard";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/employee/dashboard";
        }
    }

    /**
     * 休憩開始打刻
     */
    @PostMapping("/break-start")
    public String breakStart(Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = getUserFromAuth(authentication);
        try {
            attendanceService.startBreak(user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "休憩開始打刻が完了しました");
            return "redirect:/employee/dashboard";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/employee/dashboard";
        }
    }

    /**
     * 休憩終了打刻
     */
    @PostMapping("/break-end")
    public String breakEnd(Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = getUserFromAuth(authentication);
        try {
            attendanceService.endBreak(user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "休憩終了打刻が完了しました");
            return "redirect:/employee/dashboard";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/employee/dashboard";
        }
    }

    /**
     * 中抜け開始打刻
     */
    @PostMapping("/leave-start")
    public String leaveStart(Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = getUserFromAuth(authentication);
        try {
            attendanceService.startLeave(user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "中抜け開始打刻が完了しました");
            return "redirect:/employee/dashboard";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/employee/dashboard";
        }
    }

    /**
     * 中抜け終了打刻
     */
    @PostMapping("/leave-end")
    public String leaveEnd(Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = getUserFromAuth(authentication);
        try {
            attendanceService.endLeave(user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "中抜け終了打刻が完了しました");
            return "redirect:/employee/dashboard";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/employee/dashboard";
        }
    }

    /**
     * 勤怠履歴表示
     */
    @GetMapping("/history")
    public String history(Authentication authentication, Model model) {
        User user = getUserFromAuth(authentication);
        List<Attendance> attendances = attendanceService.getAttendanceHistory(user.getId());
        
        // 各勤怠記録に対して中抜け情報を取得
        java.util.Map<Long, java.util.List<com.example.kintai.entity.LeaveRecord>> leaveRecordsMap = 
            new java.util.HashMap<>();
        for (Attendance attendance : attendances) {
            java.util.List<com.example.kintai.entity.LeaveRecord> leaveRecords = 
                attendanceService.getLeaveRecordsByAttendanceId(attendance.getId());
            leaveRecordsMap.put(attendance.getId(), leaveRecords);
        }
        
        model.addAttribute("attendances", attendances);
        model.addAttribute("leaveRecordsMap", leaveRecordsMap);
        model.addAttribute("username", user.getUsername());
        return "attendance_history";
    }

    /**
     * 修正依頼フォーム表示
     */
    @GetMapping("/fix-request/{attendanceId}")
    public String fixRequestForm(@PathVariable Long attendanceId,
                                   Authentication authentication,
                                   Model model) {
        User user = getUserFromAuth(authentication);
        Optional<Attendance> attendance = attendanceService.getAttendanceById(attendanceId);

        if (attendance.isEmpty() || !attendance.get().getUserId().equals(user.getId())) {
            return "redirect:/attendance/history?error=invalid";
        }

        // 中抜け記録を取得
        List<com.example.kintai.entity.LeaveRecord> leaveRecords = 
            attendanceService.getLeaveRecordsByAttendanceId(attendance.get().getId());

        model.addAttribute("attendance", attendance.get());
        model.addAttribute("leaveRecords", leaveRecords);
        model.addAttribute("username", user.getUsername());
        return "fix_request_form";
    }

    /**
     * 修正依頼送信
     */
    @PostMapping("/fix-request")
    public String submitFixRequest(@RequestParam Long attendanceId,
                                     @RequestParam String requestType,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newValue,
                                     @RequestParam(required = false) Long leaveRecordId,
                                     @RequestParam(required = false) String newLeaveType,
                                     @RequestParam String reason,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        User user = getUserFromAuth(authentication);
        try {
            // 時刻の修正依頼の場合、日付のバリデーション（2000年～2099年）
            if (newValue != null && (newValue.getYear() < 2000 || newValue.getYear() > 2099)) {
                redirectAttributes.addFlashAttribute("errorMessage", "年は2000年から2099年の範囲で入力してください");
                return "redirect:/attendance/history";
            }

            // 時刻の修正依頼と中抜けの扱いの変更依頼の両方がない場合はエラー
            if (newValue == null && newLeaveType == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "修正後の値が指定されていません");
                return "redirect:/attendance/history";
            }

            fixRequestService.createFixRequest(attendanceId, user.getId(), requestType, newValue, leaveRecordId, newLeaveType, reason);
            redirectAttributes.addFlashAttribute("successMessage", "修正依頼を送信しました");
            return "redirect:/attendance/fix-request-list";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/attendance/history";
        }
    }

    /**
     * 修正依頼一覧
     */
    @GetMapping("/fix-request-list")
    public String fixRequestList(Authentication authentication, Model model) {
        User user = getUserFromAuth(authentication);
        List<FixRequest> fixRequests = fixRequestService.getFixRequestsByUserId(user.getId());
        model.addAttribute("fixRequests", fixRequests);
        model.addAttribute("username", user.getUsername());
        return "fix_request_list";
    }

    /**
     * ログページ表示（管理者のみ）
     */
    @GetMapping("/logs")
    public String logsPage(Authentication authentication, 
                           @RequestParam(required = false) String startDate,
                           @RequestParam(required = false) String endDate,
                           Model model) {
        User user = getUserFromAuth(authentication);
        
        // 権限チェック：ログ閲覧権限が必要
        if (!permissionService.hasPermission(user, "VIEW_LOG")) {
            return "redirect:/employee/dashboard";
        }
        
        // デフォルトの期間（過去30日間）
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : end.minusDays(30);
        
        // 打刻ログを取得（全社員）
        List<AttendanceLogDTO> logs = exportService.getAttendanceLogs(user.getCompany().getId(), start, end);
        
        model.addAttribute("logs", logs);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("companyId", user.getCompany().getId());
        
        return "logs_page";
    }

    /**
     * 認証からユーザー取得
     */
    private User getUserFromAuth(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }
}
