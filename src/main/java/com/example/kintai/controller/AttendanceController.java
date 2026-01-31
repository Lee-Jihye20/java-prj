package com.example.kintai.controller;

import com.example.kintai.dto.AttendanceLogDTO;
import com.example.kintai.entity.Attendance;
import com.example.kintai.entity.FixRequest;
import com.example.kintai.entity.User;
import com.example.kintai.service.AttendanceService;
import com.example.kintai.service.BreakRecordService;
import com.example.kintai.service.LeaveRecordService;
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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private BreakRecordService breakRecordService;

    @Autowired
    private LeaveRecordService leaveRecordService;

    @Autowired
    private FixRequestService fixRequestService;

    @Autowired
    private ExportService exportService;

    @Autowired
    private PermissionService permissionService;

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

    @PostMapping("/leave-start")
    public String leaveStart(Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = getUserFromAuth(authentication);
        try {
            leaveRecordService.startLeave(user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "中抜け開始打刻が完了しました");
            return "redirect:/employee/dashboard";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/employee/dashboard";
        }
    }

    @PostMapping("/leave-end")
    public String leaveEnd(Authentication authentication, RedirectAttributes redirectAttributes) {
        User user = getUserFromAuth(authentication);
        try {
            leaveRecordService.endLeave(user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "中抜け終了打刻が完了しました");
            return "redirect:/employee/dashboard";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/employee/dashboard";
        }
    }

    @GetMapping("/history")
    public String history(Authentication authentication, Model model) {
        User user = getUserFromAuth(authentication);
        List<Attendance> attendances = attendanceService.getAttendanceHistory(user.getId());

        Map<Long, List<com.example.kintai.entity.LeaveRecord>> leaveRecordsMap = new HashMap<>();
        
        Map<Long, List<com.example.kintai.entity.BreakRecord>> breakRecordsMap = new HashMap<>();
        
        Map<Long, Double> workHoursMap = new HashMap<>();
        for (Attendance attendance : attendances) {
            leaveRecordsMap.put(attendance.getId(), leaveRecordService.getLeaveRecordsByAttendanceId(attendance.getId()));
            breakRecordsMap.put(attendance.getId(), breakRecordService.getBreakRecordsByAttendanceId(attendance.getId()));
            if (attendance.getCheckIn() != null && attendance.getCheckOut() != null) {
                long workMinutes = Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();
                workMinutes -= breakRecordService.getTotalBreakMinutesFromRecordsOnly(attendance);
                workMinutes -= leaveRecordService.getTotalDeductionLeaveMinutes(attendance.getId());
                workHoursMap.put(attendance.getId(), workMinutes / 60.0);
            } else {
                workHoursMap.put(attendance.getId(), null);
            }
        }

        model.addAttribute("attendances", attendances);
        model.addAttribute("leaveRecordsMap", leaveRecordsMap);
        model.addAttribute("breakRecordsMap", breakRecordsMap);
        model.addAttribute("workHoursMap", workHoursMap);
        model.addAttribute("username", user.getUsername());
        return "attendance_history";
    }

    @GetMapping("/fix-request/{attendanceId}")
    public String fixRequestForm(@PathVariable Long attendanceId,
                                   @RequestParam(required = false) String overtime,
                                   @RequestParam(required = false) String missingCheckout,
                                   Authentication authentication,
                                   Model model) {
        User user = getUserFromAuth(authentication);
        Optional<Attendance> attendance = attendanceService.getAttendanceById(attendanceId);

        if (attendance.isEmpty() || !attendance.get().getUserId().equals(user.getId())) {
            return "redirect:/attendance/history?error=invalid";
        }

        List<com.example.kintai.entity.LeaveRecord> leaveRecords = 
            leaveRecordService.getLeaveRecordsByAttendanceId(attendance.get().getId());

        model.addAttribute("attendance", attendance.get());
        model.addAttribute("leaveRecords", leaveRecords);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("isOvertimeExcess", "1".equals(overtime));
        model.addAttribute("isMissingCheckout", "1".equals(missingCheckout));
        return "fix_request_form";
    }

    @PostMapping("/fix-request")
    public String submitFixRequest(@RequestParam Long attendanceId,
                                     @RequestParam String requestType,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newValue,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newValue2,
                                     @RequestParam(required = false) Long leaveRecordId,
                                     @RequestParam(required = false) String newLeaveType,
                                     @RequestParam String reason,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        User user = getUserFromAuth(authentication);
        try {
            
            if (newValue != null && (newValue.getYear() < 2000 || newValue.getYear() > 2099)) {
                redirectAttributes.addFlashAttribute("errorMessage", "年は2000年から2099年の範囲で入力してください");
                return "redirect:/attendance/history";
            }
            if (newValue2 != null && (newValue2.getYear() < 2000 || newValue2.getYear() > 2099)) {
                redirectAttributes.addFlashAttribute("errorMessage", "年は2000年から2099年の範囲で入力してください");
                return "redirect:/attendance/history";
            }

            if ("OVERTIME_APPLICATION".equals(requestType)) {
                fixRequestService.createFixRequest(attendanceId, user.getId(), requestType, null, leaveRecordId, newLeaveType, reason);
                redirectAttributes.addFlashAttribute("successMessage", "修正依頼を送信しました");
                return "redirect:/attendance/fix-request-list";
            }
            
            if ("CHECK_IN_AND_OUT".equals(requestType) || "BREAK_START_AND_END".equals(requestType)) {
                if (newValue == null || newValue2 == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "開始・終了の両方を入力してください");
                    return "redirect:/attendance/history";
                }
                fixRequestService.createFixRequest(attendanceId, user.getId(), requestType, newValue, newValue2, leaveRecordId, newLeaveType, reason);
                redirectAttributes.addFlashAttribute("successMessage", "修正依頼を送信しました");
                return "redirect:/attendance/fix-request-list";
            }
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

    @GetMapping("/fix-request-list")
    public String fixRequestList(Authentication authentication, Model model,
                                  @RequestParam(required = false) String requestType,
                                  @RequestParam(required = false) String status) {
        User user = getUserFromAuth(authentication);
        List<FixRequest> fixRequests = fixRequestService.getFixRequestsByUserId(user.getId());
        if (requestType != null && !requestType.trim().isEmpty()) {
            fixRequests = fixRequests.stream()
                    .filter(r -> requestType.trim().equals(r.getRequestType()))
                    .toList();
        }
        if (status != null && !status.trim().isEmpty()) {
            fixRequests = fixRequests.stream()
                    .filter(r -> status.trim().equals(r.getStatus()))
                    .toList();
        }
        model.addAttribute("fixRequests", fixRequests);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("fixRequestListPath", "/attendance/fix-request-list");
        model.addAttribute("searchRequestType", requestType != null ? requestType : "");
        model.addAttribute("searchStatus", status != null ? status : "");
        return "fix_request_list";
    }

    @GetMapping("/logs")
    public String logsPage(Authentication authentication, 
                           @RequestParam(required = false) String startDate,
                           @RequestParam(required = false) String endDate,
                           Model model) {
        User user = getUserFromAuth(authentication);
        
        if (!permissionService.hasPermission(user, "VIEW_LOG")) {
            return "redirect:/employee/dashboard";
        }
        
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : end.minusDays(30);
        
        List<AttendanceLogDTO> logs = exportService.getAttendanceLogs(user.getCompany().getId(), start, end);
        
        model.addAttribute("logs", logs);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("companyId", user.getCompany().getId());
        
        return "logs_page";
    }

    private User getUserFromAuth(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }
}
