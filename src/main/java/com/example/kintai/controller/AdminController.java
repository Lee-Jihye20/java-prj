package com.example.kintai.controller;

import com.example.kintai.entity.AnomalyApproval;
import com.example.kintai.entity.Attendance;
import com.example.kintai.entity.BreakRecord;
import com.example.kintai.entity.CompanySettings;
import com.example.kintai.entity.FixRequest;
import com.example.kintai.entity.Permission;
import com.example.kintai.entity.Role;
import com.example.kintai.entity.User;
import com.example.kintai.dto.EmployeeTodayStatusDTO;
import com.example.kintai.repository.AnomalyApprovalRepository;
import com.example.kintai.repository.CompanyRepository;
import com.example.kintai.repository.CompanySettingsRepository;
import com.example.kintai.repository.UserRepository;
import com.example.kintai.repository.AttendanceRepository;
import com.example.kintai.service.AdminActionLogService;
import com.example.kintai.service.AttendanceService;
import com.example.kintai.service.BreakRecordService;
import com.example.kintai.service.LeaveRecordService;
import com.example.kintai.service.OvertimeExcessService;
import com.example.kintai.service.FixRequestService;
import com.example.kintai.service.RoleService;
import com.example.kintai.service.PermissionService;
import com.example.kintai.service.MonthlyReportService;
import com.example.kintai.repository.FactBasedEvaluationRepository;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private FixRequestService fixRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanySettingsRepository companySettingsRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private BreakRecordService breakRecordService;

    @Autowired
    private LeaveRecordService leaveRecordService;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private OvertimeExcessService overtimeExcessService;

    @Autowired
    private AnomalyApprovalRepository anomalyApprovalRepository;

    @Autowired
    private AdminActionLogService adminActionLogService;

    @Autowired
    private MonthlyReportService monthlyReportService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private FactBasedEvaluationRepository factBasedEvaluationRepository;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return "redirect:/login";
        }
        
        User user = getUserFromAuth(authentication);
        
        if (user == null) {
            return "redirect:/login";
        }
        
        if (!permissionService.canAccessAdminFeatures(user)) {
            return "redirect:/employee/dashboard";
        }
        
        Long companyId = user.getCompany().getId();

        List<User> companyUsers = userRepository.findAllByCompanyId(companyId);
        Map<Long, String> userMap = new HashMap<>();
        for (User u : companyUsers) {
            userMap.put(u.getId(), u.getUsername());
        }

        List<FixRequest> pendingRequests = fixRequestService.findPendingRequestsByCompanyId(companyId);

        long unresolvedOvertime = overtimeExcessService.getUnresolvedOvertimeCount(companyId);
        List<Attendance> missingCheckoutAnomalies = attendanceService.detectMissingCheckOut(companyId);
        long unresolvedMissing = missingCheckoutAnomalies.stream()
                .filter(att -> !anomalyApprovalRepository.existsByAttendance_IdAndAnomalyTypeAndApprovedTrue(att.getId(), "MISSING_CHECKOUT"))
                .count();
        int unresolvedAnomalyCount = (int) (unresolvedOvertime + unresolvedMissing);

        List<EmployeeTodayStatusDTO> todayStatusList = getTodayEmployeeStatuses(companyUsers);
        model.addAttribute("todayStatusList", todayStatusList);
        
        List<EmployeeTodayStatusDTO> workingOrCompletedList = new ArrayList<>();
        List<EmployeeTodayStatusDTO> notStartedOrLateList = new ArrayList<>();
        for (EmployeeTodayStatusDTO status : todayStatusList) {
            if ("WORKING".equals(status.getStatus()) || "COMPLETED".equals(status.getStatus())) {
                workingOrCompletedList.add(status);
            } else if ("NOT_STARTED".equals(status.getStatus()) || "LATE".equals(status.getStatus())) {
                notStartedOrLateList.add(status);
            }
        }
        model.addAttribute("workingOrCompletedList", workingOrCompletedList);
        model.addAttribute("notStartedOrLateList", notStartedOrLateList);
        model.addAttribute("unresolvedAnomalyCount", unresolvedAnomalyCount);

        boolean canViewUsers = permissionService.hasPermission(user, "VIEW_USER_LIST");
        boolean canManageUsers = permissionService.hasPermission(user, "MANAGE_USER");
        boolean canManageRoles = permissionService.hasPermission(user, "MANAGE_ROLE");
        boolean canApproveFixRequests = permissionService.hasPermission(user, "APPROVE_FIX_REQUEST");
        boolean canViewLogs = permissionService.hasPermission(user, "VIEW_LOG");
        boolean canExport = permissionService.hasPermission(user, "EXPORT_DATA");
        boolean canManageSettings = permissionService.hasPermission(user, "MANAGE_COMPANY_SETTINGS");
        boolean canViewReports = permissionService.hasPermission(user, "VIEW_MONTHLY_REPORT");

        model.addAttribute("username", user.getUsername());
        model.addAttribute("userMap", userMap);
        model.addAttribute("pendingRequestsCount", pendingRequests.size());
        model.addAttribute("canViewUsers", canViewUsers);
        model.addAttribute("canManageUsers", canManageUsers);
        model.addAttribute("canManageRoles", canManageRoles);
        model.addAttribute("canApproveFixRequests", canApproveFixRequests);
        model.addAttribute("canViewLogs", canViewLogs);
        model.addAttribute("canExport", canExport);
        model.addAttribute("canManageSettings", canManageSettings);
        model.addAttribute("canViewReports", canViewReports);
        return "admin_dashboard";
    }

    @GetMapping("/fix-requests")
    public String fixRequests(Authentication authentication, Model model,
                              @RequestParam(required = false) String success,
                              @RequestParam(required = false) String error,
                              @RequestParam(required = false) String requestType,
                              @RequestParam(required = false) String status) {
        User admin = getUserFromAuth(authentication);
        
        if (!permissionService.hasPermission(admin, "APPROVE_FIX_REQUEST")) {
            return "redirect:/employee/dashboard";
        }
        
        List<FixRequest> allRequests = fixRequestService.findAllByCompanyId(admin.getCompany().getId());
        if (requestType != null && !requestType.trim().isEmpty()) {
            allRequests = allRequests.stream()
                    .filter(r -> requestType.trim().equals(r.getRequestType()))
                    .toList();
        }
        if (status != null && !status.trim().isEmpty()) {
            allRequests = allRequests.stream()
                    .filter(r -> status.trim().equals(r.getStatus()))
                    .toList();
        }

        model.addAttribute("username", admin.getUsername());
        model.addAttribute("fixRequests", allRequests);
        model.addAttribute("fixRequestListPath", "/admin/fix-requests");
        model.addAttribute("searchRequestType", requestType != null ? requestType : "");
        model.addAttribute("searchStatus", status != null ? status : "");
        
        if (success != null) {
            if ("approved".equals(success)) {
                model.addAttribute("successMessage", "修正依頼を承認しました");
            } else if ("rejected".equals(success)) {
                model.addAttribute("successMessage", "修正依頼を却下しました");
            }
        }
        
        if (error != null) {
            model.addAttribute("errorMessage", "エラー: " + error);
        }
        
        return "fix_request_list";
    }

    @PostMapping("/fix-request/approve/{id}")
    public String approveFixRequest(@PathVariable Long id, Authentication authentication) {
        try {
            User admin = getUserFromAuth(authentication);
            
            if (!permissionService.hasPermission(admin, "APPROVE_FIX_REQUEST")) {
                return "redirect:/employee/dashboard";
            }
            
            fixRequestService.approveFixRequest(id, admin.getId());
            adminActionLogService.log(admin, "APPROVE_FIX_REQUEST", "FIX_REQUEST", id, "修正依頼ID: " + id + " を承諾");
            return "redirect:/admin/fix-requests?success=approved";
        } catch (Exception e) {
            return "redirect:/admin/fix-requests?error=" + e.getMessage();
        }
    }

    @PostMapping("/fix-request/reject/{id}")
    public String rejectFixRequest(@PathVariable Long id, Authentication authentication) {
        try {
            User admin = getUserFromAuth(authentication);
            
            if (!permissionService.hasPermission(admin, "APPROVE_FIX_REQUEST")) {
                return "redirect:/employee/dashboard";
            }
            
            fixRequestService.rejectFixRequest(id);
            adminActionLogService.log(admin, "REJECT_FIX_REQUEST", "FIX_REQUEST", id, "修正依頼ID: " + id + " を却下");
            return "redirect:/admin/fix-requests?success=rejected";
        } catch (Exception e) {
            return "redirect:/admin/fix-requests?error=" + e.getMessage();
        }
    }

    @GetMapping("/anomaly-detection")
    public String anomalyDetection(Authentication authentication, Model model) {
        User admin = getUserFromAuth(authentication);
        
        if (!permissionService.hasPermission(admin, "VIEW_USER_LIST")) {
            return "redirect:/employee/dashboard";
        }
        
        Long companyId = admin.getCompany().getId();
        List<Attendance> overtimeList = overtimeExcessService.getUnresolvedOvertimeAnomalies(companyId);
        List<Attendance> missingCheckout = attendanceService.detectMissingCheckOut(companyId);

        List<Attendance> missingCheckoutList = missingCheckout.stream()
                .filter(att -> !anomalyApprovalRepository.existsByAttendance_IdAndAnomalyTypeAndApprovedTrue(att.getId(), "MISSING_CHECKOUT"))
                .toList();

        List<User> companyUsers = userRepository.findAllByCompanyId(companyId);
        Map<Long, String> userMap = new HashMap<>();
        for (User u : companyUsers) {
            userMap.put(u.getId(), u.getUsername());
        }

        Map<Long, Map<String, Object>> anomalyApprovalMap = new HashMap<>();
        for (Attendance att : overtimeList) {
            Optional<AnomalyApproval> approval = anomalyApprovalRepository.findFirstByAttendance_IdAndAnomalyType(att.getId(), OvertimeExcessService.ANOMALY_TYPE_OVERTIME);
            if (approval.isPresent()) {
                AnomalyApproval a = approval.get();
                Map<String, Object> approvalData = new HashMap<>();
                approvalData.put("reason", a.getReason());
                approvalData.put("approved", a.getApproved());
                approvalData.put("adjustmentHours", a.getAdjustmentHours());
                approvalData.put("adjustmentReason", a.getAdjustmentReason());
                anomalyApprovalMap.put(att.getId(), approvalData);
            }
        }
        for (Attendance att : missingCheckoutList) {
            Optional<AnomalyApproval> approval = anomalyApprovalRepository.findFirstByAttendance_IdAndAnomalyType(att.getId(), "MISSING_CHECKOUT");
            if (approval.isPresent()) {
                AnomalyApproval a = approval.get();
                Map<String, Object> approvalData = new HashMap<>();
                approvalData.put("reason", a.getReason());
                approvalData.put("approved", a.getApproved());
                approvalData.put("adjustmentHours", a.getAdjustmentHours());
                approvalData.put("adjustmentReason", a.getAdjustmentReason());
                anomalyApprovalMap.put(att.getId(), approvalData);
            }
        }

        model.addAttribute("username", admin.getUsername());
        model.addAttribute("overtimeList", overtimeList);
        model.addAttribute("missingCheckoutList", missingCheckoutList);
        model.addAttribute("userMap", userMap);
        model.addAttribute("anomalyApprovalMap", anomalyApprovalMap);
        return "anomaly_detection";
    }

    @PostMapping("/anomaly/approval")
    public String saveAnomalyApproval(@RequestParam Long attendanceId,
                                      @RequestParam Long userId,
                                      @RequestParam String anomalyType,
                                      @RequestParam String reason,
                                      @RequestParam Boolean approved,
                                      @RequestParam(required = false) Double adjustmentHours,
                                      @RequestParam(required = false) String adjustmentReason,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        User admin = getUserFromAuth(authentication);
        
        if (!permissionService.hasPermission(admin, "APPROVE_FIX_REQUEST")) {
            redirectAttributes.addFlashAttribute("errorMessage", "権限がありません");
            return "redirect:/employee/dashboard";
        }
        
        try {
            
            Optional<Attendance> attendanceOpt = attendanceRepository.findById(attendanceId);
            if (attendanceOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "勤怠記録が見つかりません");
                return "redirect:/admin/anomaly-detection";
            }
            
            Attendance attendance = attendanceOpt.get();
            
            Optional<AnomalyApproval> existingApproval = anomalyApprovalRepository.findByAttendance_Id(attendanceId);
            AnomalyApproval approval;
            
            if (existingApproval.isPresent()) {
                approval = existingApproval.get();
            } else {
                approval = new AnomalyApproval();
                approval.setAttendance(attendance);
                approval.setAnomalyType(anomalyType);
            }
            
            approval.setReason(reason);
            approval.setApproved(approved);
            
            if (approved) {
                approval.setApprovedBy(admin);
                approval.setApprovedAt(LocalDateTime.now());
            } else {
                approval.setApprovedBy(null);
                approval.setApprovedAt(null);
            }
            
            approval.setAdjustmentHours(adjustmentHours);
            approval.setAdjustmentReason(adjustmentReason);
            
            anomalyApprovalRepository.save(approval);
            adminActionLogService.log(admin, "ANOMALY_APPROVAL", "ANOMALY_APPROVAL", attendanceId,
                (approved ? "承認" : "未承認") + " 異常タイプ: " + anomalyType + (reason != null && !reason.isEmpty() ? " 理由: " + reason : ""));
            
            redirectAttributes.addFlashAttribute("successMessage", "理由・承認・補正を保存しました");
            return "redirect:/admin/anomaly-detection";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "保存に失敗しました: " + e.getMessage());
            return "redirect:/admin/anomaly-detection";
        }
    }

    @GetMapping("/settings")
    public String settings(Authentication authentication, Model model) {
        User admin = getUserFromAuth(authentication);
        
        boolean canManageSettings = permissionService.hasPermission(admin, "MANAGE_COMPANY_SETTINGS");
        boolean canManageUsers = permissionService.hasPermission(admin, "MANAGE_USER");
        
        if (!canManageSettings && !canManageUsers) {
            return "redirect:/employee/dashboard";
        }
        
        Long companyId = admin.getCompany().getId();
        
        CompanySettings breakSettings = companySettingsRepository.findByCompanyId(companyId)
                .orElse(new CompanySettings());
        
        List<User> companyUsers = new ArrayList<>();
        if (canManageUsers) {
            companyUsers = userRepository.findAllByCompanyId(companyId);
        }

        model.addAttribute("username", admin.getUsername());
        model.addAttribute("breakSettings", breakSettings);
        model.addAttribute("users", companyUsers);
        model.addAttribute("canManageSettings", canManageSettings);
        model.addAttribute("canManageUsers", canManageUsers);
        return "settings";
    }

    @GetMapping("/break-settings")
    public String breakSettings(Authentication authentication, Model model) {
        return "redirect:/admin/settings";
    }

    @PostMapping("/break-settings")
    public String updateBreakSettings(Authentication authentication,
                                      @RequestParam(required = false) Integer breakCountLimit,
                                      @RequestParam(required = false) String breakInputMode,
                                      @RequestParam(required = false) Boolean autoCalculateBreakTime,
                                      @RequestParam(required = false) String lunchBreakStartTime,
                                      @RequestParam(required = false) String lunchBreakEndTime,
                                      @RequestParam(required = false) String leaveDefaultType,
                                      RedirectAttributes redirectAttributes) {
        User admin = getUserFromAuth(authentication);
        
        if (!permissionService.hasPermission(admin, "MANAGE_COMPANY_SETTINGS")) {
            redirectAttributes.addFlashAttribute("errorMessage", "権限がありません");
            return "redirect:/employee/dashboard";
        }
        CompanySettings settings = companySettingsRepository.findByCompanyId(admin.getCompany().getId())
                .orElse(new CompanySettings());

        if (settings.getCompany() == null) {
            settings.setCompany(admin.getCompany());
        }

        if (breakCountLimit != null) {
            settings.setBreakCountLimit(breakCountLimit);
        }
        if (breakInputMode != null) {
            settings.setBreakInputMode(breakInputMode);
        }
        if (autoCalculateBreakTime != null) {
            settings.setAutoCalculateBreakTime(autoCalculateBreakTime);
        }
        if (lunchBreakStartTime != null && !lunchBreakStartTime.isEmpty()) {
            settings.setLunchBreakStartTime(LocalTime.parse(lunchBreakStartTime));
        } else {
            settings.setLunchBreakStartTime(null);
        }
        if (lunchBreakEndTime != null && !lunchBreakEndTime.isEmpty()) {
            settings.setLunchBreakEndTime(LocalTime.parse(lunchBreakEndTime));
        } else {
            settings.setLunchBreakEndTime(null);
        }
        if (leaveDefaultType != null) {
            settings.setLeaveDefaultType(leaveDefaultType);
        }

        companySettingsRepository.save(settings);
        redirectAttributes.addFlashAttribute("successMessage", "休憩設定が更新されました");
        return "redirect:/admin/settings";
    }

    @PostMapping("/slack-settings")
    public String updateSlackSettings(Authentication authentication,
                                      @RequestParam(required = false) Boolean slackNotificationEnabled,
                                      @RequestParam(required = false) String logSlackWebhookUrl,
                                      @RequestParam(required = false) String alertSlackWebhookUrl,
                                      RedirectAttributes redirectAttributes) {
        User admin = getUserFromAuth(authentication);
        
        if (!permissionService.hasPermission(admin, "MANAGE_COMPANY_SETTINGS")) {
            redirectAttributes.addFlashAttribute("errorMessage", "権限がありません");
            return "redirect:/employee/dashboard";
        }
        
        CompanySettings settings = companySettingsRepository.findByCompanyId(admin.getCompany().getId())
                .orElse(new CompanySettings());

        if (settings.getCompany() == null) {
            settings.setCompany(admin.getCompany());
        }

        if (slackNotificationEnabled != null) {
            settings.setSlackNotificationEnabled(slackNotificationEnabled);
        }
        if (logSlackWebhookUrl != null) {
            settings.setLogSlackWebhookUrl(logSlackWebhookUrl.isEmpty() ? null : logSlackWebhookUrl);
        }
        if (alertSlackWebhookUrl != null) {
            settings.setAlertSlackWebhookUrl(alertSlackWebhookUrl.isEmpty() ? null : alertSlackWebhookUrl);
        }

        companySettingsRepository.save(settings);
        redirectAttributes.addFlashAttribute("successMessage", "Slack通知設定が更新されました");
        return "redirect:/admin/settings";
    }

    @GetMapping("/users")
    public String userList(@RequestParam(required = false) String search,
                           Authentication authentication, Model model) {
        User admin = getUserFromAuth(authentication);
        
        if (!permissionService.hasPermission(admin, "VIEW_USER_LIST")) {
            return "redirect:/employee/dashboard";
        }
        Long companyId = admin.getCompany().getId();
        
        List<User> companyUsers = userRepository.findAllByCompanyId(companyId);
        
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            companyUsers = companyUsers.stream()
                    .filter(user -> user.getUsername().toLowerCase().contains(searchLower))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        String companyCode = companyRepository.findById(companyId)
                .map(com.example.kintai.entity.Company::getCompanyCode)
                .orElse("");

        List<Role> roles = roleService.getRolesByCompanyId(companyId);

        int currentYear = java.time.Year.now().getValue();
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        
        Map<Long, com.example.kintai.dto.MonthlyReportDTO> userMonthlyStats = new HashMap<>();
        for (User user : companyUsers) {
            if (!"ADMIN".equals(user.getRole())) {
                try {
                    com.example.kintai.dto.MonthlyReportDTO report = monthlyReportService.generateMonthlyReport(
                            user, currentYear, currentMonth);
                    userMonthlyStats.put(user.getId(), report);
                } catch (Exception e) {
                    
                }
            }
        }
        
        model.addAttribute("username", admin.getUsername());
        model.addAttribute("users", companyUsers);
        model.addAttribute("companyCode", companyCode);
        model.addAttribute("roles", roles);
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("userMonthlyStats", userMonthlyStats);
        model.addAttribute("search", search != null ? search : "");
        return "user_list";
    }

    @GetMapping("/my-actions")
    public String myActions(Authentication authentication, Model model) {
        User admin = getUserFromAuth(authentication);
        if (admin == null) {
            return "redirect:/login";
        }
        if (!"ADMIN".equals(admin.getRole())) {
            return "redirect:/employee/dashboard";
        }
        List<com.example.kintai.entity.AdminActionLog> actions = adminActionLogService.findRecentByAdminId(admin.getId());
        model.addAttribute("username", admin.getUsername());
        model.addAttribute("actions", actions);
        return "admin_action_history";
    }

    @GetMapping("/users/{userId}/actions")
    public String userActions(@PathVariable Long userId, Authentication authentication, Model model) {
        User admin = getUserFromAuth(authentication);
        if (admin == null) {
            return "redirect:/login";
        }
        if (!permissionService.hasPermission(admin, "VIEW_USER_LIST")) {
            return "redirect:/employee/dashboard";
        }
        Optional<User> targetOptional = userRepository.findById(userId);
        if (targetOptional.isEmpty()) {
            return "redirect:/admin/users?error=ユーザーが見つかりません";
        }
        User target = targetOptional.get();
        if (!target.getCompany().getId().equals(admin.getCompany().getId())) {
            return "redirect:/admin/users?error=権限がありません";
        }
        if (!"ADMIN".equals(target.getRole())) {
            return "redirect:/admin/users?error=管理者のみ操作履歴を表示できます";
        }
        List<com.example.kintai.entity.AdminActionLog> actions = adminActionLogService.findRecentByAdminId(target.getId());
        model.addAttribute("username", admin.getUsername());
        model.addAttribute("actions", actions);
        model.addAttribute("targetUsername", target.getUsername());
        return "admin_action_history";
    }

    @GetMapping("/users/create")
    public String createUserForm(Authentication authentication, Model model) {
        User admin = getUserFromAuth(authentication);
        
        if (!permissionService.hasPermission(admin, "MANAGE_USER")) {
            return "redirect:/employee/dashboard";
        }
        
        Long companyId = admin.getCompany().getId();
        
        String companyCode = companyRepository.findById(companyId)
                .map(com.example.kintai.entity.Company::getCompanyCode)
                .orElse("");
        
        List<Role> roles = roleService.getRolesByCompanyId(companyId);
        List<Permission> permissions = roleService.getAllPermissions();
        
        model.addAttribute("username", admin.getUsername());
        model.addAttribute("companyCode", companyCode);
        model.addAttribute("roles", roles);
        model.addAttribute("permissions", permissions);
        return "create_user";
    }

    @PostMapping("/users/create")
    public String createUser(Authentication authentication,
                             @RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String role,
                             @RequestParam(required = false) String slackWebhookUrl,
                             @RequestParam(required = false) String slackUserId,
                             RedirectAttributes redirectAttributes) {
        User admin = getUserFromAuth(authentication);
        
        if (!permissionService.hasPermission(admin, "MANAGE_USER")) {
            redirectAttributes.addFlashAttribute("errorMessage", "権限がありません");
            return "redirect:/employee/dashboard";
        }
        
        Optional<User> existingUser = userRepository.findByUsernameAndCompanyId(username, admin.getCompany().getId());
        if (existingUser.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "このユーザー名は既に使用されています");
            return "redirect:/admin/users/create";
        }

        if (!"EMPLOYEE".equals(role) && !"ADMIN".equals(role)) {
            redirectAttributes.addFlashAttribute("errorMessage", "無効な権限が指定されました");
            return "redirect:/admin/users/create";
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password); 
        newUser.setRole(role);
        newUser.setCompany(admin.getCompany());
        
        newUser.setWorkType("FULLTIME");
        newUser.setStartTime(LocalTime.of(9, 0));
        if (slackWebhookUrl != null && !slackWebhookUrl.isEmpty()) {
            newUser.setSlackWebhookUrl(slackWebhookUrl);
        }
        if (slackUserId != null && !slackUserId.isEmpty()) {
            newUser.setSlackUserId(slackUserId);
        }

        userRepository.save(newUser);
        adminActionLogService.log(admin, "CREATE_USER", "USER", newUser.getId(), "ユーザー: " + username);
        
        Long companyId = admin.getCompany().getId();
        String companyCode = companyRepository.findById(companyId)
                .map(com.example.kintai.entity.Company::getCompanyCode)
                .orElse("");
        redirectAttributes.addFlashAttribute("successMessage", 
            "社員アカウントを作成しました。ログインには企業コード「" + companyCode + "」が必要です。");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/role")
    public String updateUserRole(@PathVariable Long userId,
                                  @RequestParam String role,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        User admin = getUserFromAuth(authentication);
        
        if (!permissionService.hasPermission(admin, "MANAGE_USER")) {
            redirectAttributes.addFlashAttribute("errorMessage", "権限がありません");
            return "redirect:/employee/dashboard";
        }
        
        if (!"EMPLOYEE".equals(role) && !"ADMIN".equals(role)) {
            redirectAttributes.addFlashAttribute("errorMessage", "無効な権限が指定されました");
            return "redirect:/admin/users";
        }
        
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "ユーザーが見つかりません");
            return "redirect:/admin/users";
        }
        
        User user = optionalUser.get();
        
        if (!user.getCompany().getId().equals(admin.getCompany().getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "このユーザーの権限を変更する権限がありません");
            return "redirect:/admin/users";
        }
        
        user.setRole(role);
        userRepository.save(user);
        
        redirectAttributes.addFlashAttribute("successMessage", 
            "ユーザー「" + user.getUsername() + "」の権限を「" + 
            ("ADMIN".equals(role) ? "管理者" : "従業員") + "」に変更しました");
        return "redirect:/admin/users";
    }

    @GetMapping("/roles")
    public String roleList(Authentication authentication, Model model) {
        User admin = getUserFromAuth(authentication);
        
        if (!permissionService.hasPermission(admin, "MANAGE_ROLE")) {
            return "redirect:/employee/dashboard";
        }
        
        Long companyId = admin.getCompany().getId();
        
        List<Role> roles = roleService.getRolesByCompanyId(companyId);
        List<Permission> permissions = roleService.getAllPermissions();
        
        Map<String, List<Permission>> permissionsByCategory = new HashMap<>();
        for (Permission permission : permissions) {
            String category = permission.getCategory();
            permissionsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(permission);
        }
        
        model.addAttribute("username", admin.getUsername());
        model.addAttribute("roles", roles);
        model.addAttribute("permissions", permissions);
        model.addAttribute("permissionsByCategory", permissionsByCategory);
        return "role_list";
    }

    @PostMapping("/roles/create")
    public String createRole(@RequestParam String name,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) List<Long> permissionIds,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            User admin = getUserFromAuth(authentication);
            
            if (!permissionService.hasPermission(admin, "MANAGE_ROLE")) {
                redirectAttributes.addFlashAttribute("errorMessage", "権限がありません");
                return "redirect:/employee/dashboard";
            }
            
            Long companyId = admin.getCompany().getId();
            
            Set<Long> permissionSet = permissionIds != null ? new HashSet<>(permissionIds) : new HashSet<>();
            roleService.createRole(name, description, companyId, permissionSet);
            
            redirectAttributes.addFlashAttribute("successMessage", "ロール「" + name + "」を作成しました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/roles/{roleId}/update")
    public String updateRole(@PathVariable Long roleId,
                             @RequestParam String name,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) List<Long> permissionIds,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            User admin = getUserFromAuth(authentication);
            
            if (!permissionService.hasPermission(admin, "MANAGE_ROLE")) {
                redirectAttributes.addFlashAttribute("errorMessage", "権限がありません");
                return "redirect:/employee/dashboard";
            }
            
            Set<Long> permissionSet = permissionIds != null ? new HashSet<>(permissionIds) : new HashSet<>();
            roleService.updateRole(roleId, name, description, permissionSet);
            
            redirectAttributes.addFlashAttribute("successMessage", "ロール「" + name + "」を更新しました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/roles";
    }

    @PostMapping("/roles/{roleId}/delete")
    public String deleteRole(@PathVariable Long roleId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            User admin = getUserFromAuth(authentication);
            
            if (!permissionService.hasPermission(admin, "MANAGE_ROLE")) {
                redirectAttributes.addFlashAttribute("errorMessage", "権限がありません");
                return "redirect:/employee/dashboard";
            }
            
            roleService.deleteRole(roleId);
            redirectAttributes.addFlashAttribute("successMessage", "ロールを削除しました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/roles";
    }

    @PostMapping("/users/{userId}/roles")
    public String assignRolesToUser(@PathVariable Long userId,
                                    @RequestParam(required = false) String role,
                                    @RequestParam(required = false) List<Long> roleIds,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            User admin = getUserFromAuth(authentication);
            
            if (!permissionService.hasPermission(admin, "MANAGE_USER")) {
                redirectAttributes.addFlashAttribute("errorMessage", "権限がありません");
                return "redirect:/employee/dashboard";
            }
            
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "ユーザーが見つかりません");
                return "redirect:/admin/users";
            }
            
            User user = optionalUser.get();
            
            if (!user.getCompany().getId().equals(admin.getCompany().getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "このユーザーのロールを変更する権限がありません");
                return "redirect:/admin/users";
            }
            
            if (role != null && !role.isEmpty()) {
                if (!"EMPLOYEE".equals(role) && !"ADMIN".equals(role)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "無効な権限が指定されました");
                    return "redirect:/admin/users";
                }
                user.setRole(role);
            }
            
            Set<Long> roleSet = roleIds != null ? new HashSet<>(roleIds) : new HashSet<>();
            roleService.updateUserRoles(userId, roleSet);
            
            userRepository.save(user);
            adminActionLogService.log(admin, "UPDATE_ROLES", "USER", userId, "ユーザー: " + user.getUsername() + " のロールを更新");
            
            redirectAttributes.addFlashAttribute("successMessage", "ユーザー「" + user.getUsername() + "」のロールを更新しました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{userId}/calendar")
    public String userCalendar(@PathVariable Long userId,
                                @RequestParam(required = false) Integer year,
                                @RequestParam(required = false) Integer month,
                                Authentication authentication,
                                Model model) {
        User admin = getUserFromAuth(authentication);
        
        if (!permissionService.hasPermission(admin, "VIEW_USER_LIST")) {
            return "redirect:/employee/dashboard";
        }
        
        Optional<User> targetUserOptional = userRepository.findById(userId);
        if (targetUserOptional.isEmpty()) {
            return "redirect:/admin/users?error=ユーザーが見つかりません";
        }
        
        User targetUser = targetUserOptional.get();
        if (!targetUser.getCompany().getId().equals(admin.getCompany().getId())) {
            return "redirect:/admin/users?error=権限がありません";
        }

        if (year == null || month == null) {
            LocalDate now = LocalDate.now();
            year = now.getYear();
            month = now.getMonthValue();
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        List<Attendance> attendances = attendanceService.getAttendanceByDateRange(
                userId,
                yearMonth.atDay(1).atStartOfDay(),
                yearMonth.atEndOfMonth().atTime(23, 59, 59)
        );

        Map<Integer, CalendarDay> calendarDays = new HashMap<>();
        for (Attendance attendance : attendances) {
            if (attendance.getCheckIn() != null) {
                int day = attendance.getCheckIn().getDayOfMonth();
                CalendarDay calendarDay = new CalendarDay();
                calendarDay.setDay(day);
                calendarDay.setAttendance(attendance);

                List<BreakRecord> breakRecords = breakRecordService.getBreakRecordsByAttendanceId(attendance.getId());
                calendarDay.setBreakRecords(breakRecords);

                if (attendance.getCheckOut() != null) {
                    long workMinutes = Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();
                    workMinutes -= breakRecordService.getTotalBreakMinutesFromRecordsOnly(attendance);
                    workMinutes -= leaveRecordService.getTotalDeductionLeaveMinutes(attendance.getId());

                    calendarDay.setWorkHours(workMinutes / 60.0);

                    if (workMinutes > 480) { 
                        calendarDay.setStatus("overtime");
                    } else {
                        calendarDay.setStatus("normal");
                    }
                } else {
                    calendarDay.setStatus("incomplete"); 
                }

                calendarDays.put(day, calendarDay);
            }
        }

        int daysInMonth = yearMonth.lengthOfMonth();
        int firstDayOfWeek = yearMonth.atDay(1).getDayOfWeek().getValue(); 

        List<List<CalendarDay>> weeks = new ArrayList<>();
        List<CalendarDay> week = new ArrayList<>();

        for (int i = 1; i < firstDayOfWeek; i++) {
            week.add(null);
        }

        for (int day = 1; day <= daysInMonth; day++) {
            CalendarDay calendarDay = calendarDays.getOrDefault(day, new CalendarDay());
            if (calendarDay.getDay() == 0) {
                calendarDay.setDay(day);
            }
            week.add(calendarDay);

            if ((firstDayOfWeek + day - 1) % 7 == 0) {
                weeks.add(week);
                week = new ArrayList<>();
            }
        }

        if (!week.isEmpty()) {
            while (week.size() < 7) {
                week.add(null);
            }
            weeks.add(week);
        }

        model.addAttribute("username", admin.getUsername());
        model.addAttribute("targetUsername", targetUser.getUsername());
        model.addAttribute("targetUserId", userId);
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("weeks", weeks);
        model.addAttribute("prevMonth", month == 1 ? 12 : month - 1);
        model.addAttribute("prevYear", month == 1 ? year - 1 : year);
        model.addAttribute("nextMonth", month == 12 ? 1 : month + 1);
        model.addAttribute("nextYear", month == 12 ? year + 1 : year);

        return "admin_user_calendar";
    }

    public static class CalendarDay {
        private int day;
        private Attendance attendance;
        private double workHours;
        private String status; 
        private List<BreakRecord> breakRecords = new ArrayList<>();

        public CalendarDay() {
        }

        public int getDay() {
            return day;
        }

        public void setDay(int day) {
            this.day = day;
        }

        public Attendance getAttendance() {
            return attendance;
        }

        public void setAttendance(Attendance attendance) {
            this.attendance = attendance;
        }

        public double getWorkHours() {
            return workHours;
        }

        public void setWorkHours(double workHours) {
            this.workHours = workHours;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<BreakRecord> getBreakRecords() {
            return breakRecords;
        }

        public void setBreakRecords(List<BreakRecord> breakRecords) {
            this.breakRecords = breakRecords != null ? breakRecords : new ArrayList<>();
        }

        public boolean hasAttendance() {
            return attendance != null;
        }
    }

    @GetMapping("/api/attendance/{attendanceId}/details")
    @ResponseBody
    public Map<String, Object> getAttendanceDetails(@PathVariable Long attendanceId,
                                                     Authentication authentication) {
        User admin = getUserFromAuth(authentication);
        
        if (!permissionService.hasPermission(admin, "VIEW_USER_LIST")) {
            throw new IllegalArgumentException("権限がありません");
        }
        
        Optional<Attendance> attendanceOptional = attendanceService.getAttendanceById(attendanceId);
        if (attendanceOptional.isEmpty()) {
            throw new IllegalArgumentException("勤怠記録が見つかりません");
        }
        
        Attendance attendance = attendanceOptional.get();
        
        if (!attendance.getUser().getCompany().getId().equals(admin.getCompany().getId())) {
            throw new IllegalArgumentException("権限がありません");
        }
        
        List<BreakRecord> breakRecords = breakRecordService.getBreakRecordsByAttendanceId(attendanceId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("checkIn", attendance.getCheckIn() != null ? 
            attendance.getCheckIn().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null);
        result.put("checkOut", attendance.getCheckOut() != null ? 
            attendance.getCheckOut().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null);
        
        List<Map<String, Object>> breaks = new ArrayList<>();
        for (BreakRecord br : breakRecords) {
            Map<String, Object> breakInfo = new HashMap<>();
            breakInfo.put("start", br.getBreakStart() != null ? 
                br.getBreakStart().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null);
            breakInfo.put("end", br.getBreakEnd() != null ? 
                br.getBreakEnd().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null);
            breakInfo.put("type", br.getBreakType());
            breakInfo.put("minutes", br.getBreakMinutes());
            breaks.add(breakInfo);
        }
        result.put("breaks", breaks);
        
        if (attendance.getCheckIn() != null && attendance.getCheckOut() != null) {
            long workMinutes = Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();
            workMinutes -= breakRecordService.getTotalBreakMinutesFromRecordsOnly(attendance);
            workMinutes -= leaveRecordService.getTotalDeductionLeaveMinutes(attendanceId);
            result.put("workHours", workMinutes / 60.0);
        }
        
        return result;
    }

    private Map<String, Object> calculateMonthlySummary(Long userId, int year, int month) {
        Map<String, Object> summary = new HashMap<>();
        
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Attendance> attendances = attendanceService.getAttendanceByDateRange(
                userId, startDate, endDate);

        long totalWorkMinutes = 0;
        long totalOvertimeMinutes = 0;
        long totalBreakMinutes = 0;
        int workDays = 0;

        for (Attendance attendance : attendances) {
            if (attendance.getCheckIn() == null) {
                continue;
            }

            workDays++;

            long workMinutes = 0;
            long overtimeMinutes = 0;
            long breakMinutes = 0;

            if (attendance.getCheckOut() != null) {
                workMinutes = Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();
                breakMinutes = breakRecordService.getTotalBreakMinutesFromRecordsOnly(attendance);
                workMinutes -= breakMinutes;

                if (workMinutes > 480) {
                    overtimeMinutes = workMinutes - 480;
                }
            }

            totalWorkMinutes += workMinutes;
            totalOvertimeMinutes += overtimeMinutes;
            totalBreakMinutes += breakMinutes;
        }

        summary.put("workDays", workDays);
        summary.put("totalWorkHours", totalWorkMinutes / 60.0);
        summary.put("totalBreakHours", totalBreakMinutes / 60.0);
        summary.put("totalOvertimeHours", totalOvertimeMinutes / 60.0);

        return summary;
    }

    @GetMapping("/evaluations/monthly")
    public String monthlyEvaluations(@RequestParam(required = false) String yearMonth,
                                     @RequestParam(required = false) String search,
                                     Authentication authentication, Model model) {
        User admin = getUserFromAuth(authentication);
        
        if (!permissionService.hasPermission(admin, "VIEW_USER_LIST")) {
            return "redirect:/employee/dashboard";
        }
        
        Long companyId = admin.getCompany().getId();
        
        YearMonth selectedYearMonth;
        if (yearMonth != null && !yearMonth.trim().isEmpty()) {
            try {
                selectedYearMonth = YearMonth.parse(yearMonth.trim());
            } catch (Exception e) {
                selectedYearMonth = YearMonth.now();
            }
        } else {
            selectedYearMonth = YearMonth.now();
        }
        
        LocalDate monthStart = selectedYearMonth.atDay(1);
        
        List<com.example.kintai.entity.FactBasedEvaluation> evaluations = 
                factBasedEvaluationRepository.findByCompanyIdOrderByYearMonthDesc(companyId);
        
        List<com.example.kintai.entity.FactBasedEvaluation> monthEvaluations = evaluations.stream()
                .filter(eval -> eval.getYearMonth().equals(monthStart))
                .toList();
        
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.trim().toLowerCase();
            monthEvaluations = monthEvaluations.stream()
                    .filter(eval -> eval.getEmployee() != null && 
                            eval.getEmployee().getUsername().toLowerCase().contains(searchLower))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        List<YearMonth> availableMonths = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();
        for (int i = 0; i < 12; i++) {
            availableMonths.add(currentMonth.minusMonths(i));
        }
        
        model.addAttribute("username", admin.getUsername());
        model.addAttribute("evaluations", monthEvaluations);
        model.addAttribute("selectedYearMonth", selectedYearMonth);
        model.addAttribute("selectedYearMonthStr", selectedYearMonth.toString());
        model.addAttribute("availableMonths", availableMonths);
        model.addAttribute("search", search != null ? search : "");
        
        return "monthly_evaluation_list";
    }

    @PostMapping("/users/{userId}/work-settings")
    public String updateWorkSettings(@PathVariable Long userId,
                                     @RequestParam String workType,
                                     @RequestParam(required = false) String startTime,
                                     @RequestParam(required = false) String coreTimeStart,
                                     @RequestParam(required = false) String coreTimeEnd,
                                     @RequestParam(required = false) String redirectTo,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        User admin = getUserFromAuth(authentication);
        
        if (!permissionService.hasPermission(admin, "MANAGE_USER")) {
            redirectAttributes.addFlashAttribute("errorMessage", "権限がありません");
            return "redirect:/employee/dashboard";
        }
        
        try {
            
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "ユーザーが見つかりません");
                return "redirect:/admin/users";
            }
            
            User user = optionalUser.get();
            
            if (!user.getCompany().getId().equals(admin.getCompany().getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "権限がありません");
                return "redirect:/admin/users";
            }
            
            if (!"FULLTIME".equals(workType) && !"FLEX".equals(workType)) {
                redirectAttributes.addFlashAttribute("errorMessage", "無効な勤務形態が指定されました");
                return "redirect:/admin/users";
            }
            
            user.setWorkType(workType);
            
            if ("FULLTIME".equals(workType)) {
                if (startTime != null && !startTime.trim().isEmpty()) {
                    try {
                        LocalTime startTimeParsed = LocalTime.parse(startTime.trim());
                        user.setStartTime(startTimeParsed);
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("errorMessage", "無効な始業時間が指定されました");
                        return "users".equals(redirectTo) ? "redirect:/admin/users" : "redirect:/admin/settings";
                    }
                } else {
                    
                    user.setStartTime(LocalTime.of(9, 0));
                }
                
                user.setCoreTimeStart(null);
                user.setCoreTimeEnd(null);
            } else if ("FLEX".equals(workType)) {
                
                user.setStartTime(null);
                
                if (coreTimeStart != null && !coreTimeStart.trim().isEmpty()) {
                    try {
                        LocalTime coreTimeStartParsed = LocalTime.parse(coreTimeStart.trim());
                        user.setCoreTimeStart(coreTimeStartParsed);
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("errorMessage", "無効なコアタイム開始時刻が指定されました");
                        return "users".equals(redirectTo) ? "redirect:/admin/users" : "redirect:/admin/settings";
                    }
                } else {
                    user.setCoreTimeStart(null);
                }
                
                if (coreTimeEnd != null && !coreTimeEnd.trim().isEmpty()) {
                    try {
                        LocalTime coreTimeEndParsed = LocalTime.parse(coreTimeEnd.trim());
                        user.setCoreTimeEnd(coreTimeEndParsed);
                    } catch (Exception e) {
                        redirectAttributes.addFlashAttribute("errorMessage", "無効なコアタイム終了時刻が指定されました");
                        return "users".equals(redirectTo) ? "redirect:/admin/users" : "redirect:/admin/settings";
                    }
                } else {
                    user.setCoreTimeEnd(null);
                }
            }
            
            userRepository.save(user);
            
            redirectAttributes.addFlashAttribute("successMessage", "勤務設定を更新しました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "勤務設定の更新に失敗しました: " + e.getMessage());
        }
        
        if ("users".equals(redirectTo)) {
            return "redirect:/admin/users";
        }
        return "redirect:/admin/settings";
    }

    @PostMapping("/users/{userId}/slack-id")
    public String updateSlackUserId(@PathVariable Long userId,
                                     @RequestParam(required = false) String slackUserId,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        User admin = getUserFromAuth(authentication);
        
        if (!permissionService.hasPermission(admin, "MANAGE_USER")) {
            redirectAttributes.addFlashAttribute("errorMessage", "権限がありません");
            return "redirect:/employee/dashboard";
        }
        
        try {
            
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "ユーザーが見つかりません");
                return "redirect:/admin/users";
            }
            
            User user = optionalUser.get();
            
            if (!user.getCompany().getId().equals(admin.getCompany().getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "権限がありません");
                return "redirect:/admin/users";
            }
            
            if (slackUserId != null && !slackUserId.trim().isEmpty()) {
                user.setSlackUserId(slackUserId.trim());
            } else {
                user.setSlackUserId(null);
            }
            
            userRepository.save(user);
            
            redirectAttributes.addFlashAttribute("successMessage", "SlackIDを更新しました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "エラーが発生しました: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }

    private List<EmployeeTodayStatusDTO> getTodayEmployeeStatuses(List<User> companyUsers) {
        List<EmployeeTodayStatusDTO> statusList = new ArrayList<>();
        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
        LocalTime currentTime = LocalTime.now();

        for (User employee : companyUsers) {
            
            if ("ADMIN".equals(employee.getRole())) {
                continue;
            }

            List<Attendance> todayAttendances = attendanceRepository.findByUser_IdAndCheckInBetweenOrderByCheckInDesc(
                    employee.getId(), startOfToday, endOfToday);

            boolean hasCheckedIn = !todayAttendances.isEmpty();
            boolean hasCheckedOut = hasCheckedIn && todayAttendances.get(0).getCheckOut() != null;
            boolean isOnBreak = false;
            boolean isOnLeave = false;
            LocalDateTime checkInTime = null;
            LocalDateTime checkOutTime = null;

            if (hasCheckedIn) {
                Attendance attendance = todayAttendances.get(0);
                checkInTime = attendance.getCheckIn();
                checkOutTime = attendance.getCheckOut();

                if (!hasCheckedOut) {
                    
                    List<BreakRecord> activeBreaks = breakRecordService.getActiveBreaksByAttendanceId(attendance.getId());
                    isOnBreak = !activeBreaks.isEmpty();

                    isOnLeave = leaveRecordService.hasActiveLeave(attendance.getId());
                }
            }

            LocalTime startTime = null;
            if ("FULLTIME".equals(employee.getWorkType()) || employee.getWorkType() == null) {
                startTime = employee.getStartTime() != null ? employee.getStartTime() : LocalTime.of(9, 0);
            } else if ("FLEX".equals(employee.getWorkType())) {
                startTime = employee.getCoreTimeStart() != null ? employee.getCoreTimeStart() : LocalTime.of(10, 0);
            }

            String status;
            if (hasCheckedOut) {
                status = "COMPLETED"; 
            } else if (hasCheckedIn) {
                status = "WORKING"; 
            } else {
                
                if (startTime != null && currentTime.isAfter(startTime)) {
                    status = "LATE"; 
                } else {
                    status = "NOT_STARTED"; 
                }
            }

            EmployeeTodayStatusDTO statusDTO = new EmployeeTodayStatusDTO(
                    employee.getId(),
                    employee.getUsername(),
                    status,
                    checkInTime,
                    checkOutTime,
                    startTime,
                    isOnBreak,
                    isOnLeave
            );

            statusList.add(statusDTO);
        }

        return statusList;
    }

    private User getUserFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        try {
            return (User) authentication.getPrincipal();
        } catch (ClassCastException e) {
            return null;
        }
    }
}
