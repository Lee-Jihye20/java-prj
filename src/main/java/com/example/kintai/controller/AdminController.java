package com.example.kintai.controller;

import com.example.kintai.entity.AnomalyApproval;
import com.example.kintai.entity.Attendance;
import com.example.kintai.entity.BreakRecord;
import com.example.kintai.entity.CompanySettings;
import com.example.kintai.entity.FixRequest;
import com.example.kintai.entity.Permission;
import com.example.kintai.entity.Role;
import com.example.kintai.entity.User;
import com.example.kintai.entity.WeeklyEvaluation;
import com.example.kintai.dto.MonthlyEvaluationSummaryDTO;
import com.example.kintai.dto.EmployeeTodayStatusDTO;
import com.example.kintai.repository.AnomalyApprovalRepository;
import com.example.kintai.repository.BreakRecordRepository;
import com.example.kintai.repository.CompanyRepository;
import com.example.kintai.repository.CompanySettingsRepository;
import com.example.kintai.repository.UserRepository;
import com.example.kintai.repository.LeaveRecordRepository;
import com.example.kintai.repository.AttendanceRepository;
import com.example.kintai.entity.LeaveRecord;
import com.example.kintai.service.AdminActionLogService;
import com.example.kintai.service.AttendanceService;
import com.example.kintai.service.FixRequestService;
import com.example.kintai.service.RoleService;
import com.example.kintai.service.PermissionService;
import com.example.kintai.service.WeeklyEvaluationService;
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
    private BreakRecordRepository breakRecordRepository;

    @Autowired
    private LeaveRecordRepository leaveRecordRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AnomalyApprovalRepository anomalyApprovalRepository;

    @Autowired
    private AdminActionLogService adminActionLogService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private WeeklyEvaluationService weeklyEvaluationService;

    /**
     * 管理者ダッシュボード（権限ベースのアクセス制御）
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return "redirect:/login";
        }
        
        User user = getUserFromAuth(authentication);
        
        if (user == null) {
            return "redirect:/login";
        }
        
        // 管理者機能にアクセスできる権限があるかチェック
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

        // 未解決の異常検知数を取得
        List<Attendance> overtimeAnomalies = attendanceService.detectOvertime(companyId);
        List<Attendance> missingCheckoutAnomalies = attendanceService.detectMissingCheckOut(companyId);
        int unresolvedAnomalyCount = overtimeAnomalies.size() + missingCheckoutAnomalies.size();

        // 今日の全社員の勤務状況を取得
        List<EmployeeTodayStatusDTO> todayStatusList = getTodayEmployeeStatuses(companyUsers);
        model.addAttribute("todayStatusList", todayStatusList);
        
        // フィルタリングしたリストを作成
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

        // 権限情報をモデルに追加
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

    /**
     * 修正依頼一覧(管理者)
     */
    @GetMapping("/fix-requests")
    public String fixRequests(Authentication authentication, Model model,
                              @RequestParam(required = false) String success,
                              @RequestParam(required = false) String error) {
        User admin = getUserFromAuth(authentication);
        
        // 権限チェック：修正依頼承認権限が必要
        if (!permissionService.hasPermission(admin, "APPROVE_FIX_REQUEST")) {
            return "redirect:/employee/dashboard";
        }
        
        List<FixRequest> allRequests = fixRequestService.findAllByCompanyId(admin.getCompany().getId());

        model.addAttribute("username", admin.getUsername());
        model.addAttribute("fixRequests", allRequests);
        
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

    /**
     * 修正依頼承認
     */
    @PostMapping("/fix-request/approve/{id}")
    public String approveFixRequest(@PathVariable Long id, Authentication authentication) {
        try {
            User admin = getUserFromAuth(authentication);
            
            // 権限チェック：修正依頼承認権限が必要
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

    /**
     * 修正依頼却下
     */
    @PostMapping("/fix-request/reject/{id}")
    public String rejectFixRequest(@PathVariable Long id, Authentication authentication) {
        try {
            User admin = getUserFromAuth(authentication);
            
            // 権限チェック：修正依頼承認権限が必要
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

    /**
     * 異常検知画面
     */
    @GetMapping("/anomaly-detection")
    public String anomalyDetection(Authentication authentication, Model model) {
        User admin = getUserFromAuth(authentication);
        
        // 権限チェック：ユーザー一覧閲覧権限が必要
        if (!permissionService.hasPermission(admin, "VIEW_USER_LIST")) {
            return "redirect:/employee/dashboard";
        }
        
        Long companyId = admin.getCompany().getId();
        List<Attendance> overtime = attendanceService.detectOvertime(companyId);
        List<Attendance> missingCheckout = attendanceService.detectMissingCheckOut(companyId);

        // ユーザー名マップを作成
        List<User> companyUsers = userRepository.findAllByCompanyId(companyId);
        Map<Long, String> userMap = new HashMap<>();
        for (User u : companyUsers) {
            userMap.put(u.getId(), u.getUsername());
        }

        // 既存の承認情報を取得
        Map<Long, Map<String, Object>> anomalyApprovalMap = new HashMap<>();
        for (Attendance att : overtime) {
            Optional<AnomalyApproval> approval = anomalyApprovalRepository.findByAttendance_Id(att.getId());
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
        for (Attendance att : missingCheckout) {
            Optional<AnomalyApproval> approval = anomalyApprovalRepository.findByAttendance_Id(att.getId());
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
        model.addAttribute("overtimeList", overtime);
        model.addAttribute("missingCheckoutList", missingCheckout);
        model.addAttribute("userMap", userMap);
        model.addAttribute("anomalyApprovalMap", anomalyApprovalMap);
        return "anomaly_detection";
    }

    /**
     * 異常検知の理由・承認・補正を保存
     */
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
        
        // 権限チェック：修正依頼承認権限が必要
        if (!permissionService.hasPermission(admin, "APPROVE_FIX_REQUEST")) {
            redirectAttributes.addFlashAttribute("errorMessage", "権限がありません");
            return "redirect:/employee/dashboard";
        }
        
        try {
            // 勤怠記録を取得
            Optional<Attendance> attendanceOpt = attendanceRepository.findById(attendanceId);
            if (attendanceOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "勤怠記録が見つかりません");
                return "redirect:/admin/anomaly-detection";
            }
            
            Attendance attendance = attendanceOpt.get();
            
            // 既存の承認情報を取得または新規作成
            Optional<AnomalyApproval> existingApproval = anomalyApprovalRepository.findByAttendance_Id(attendanceId);
            AnomalyApproval approval;
            
            if (existingApproval.isPresent()) {
                approval = existingApproval.get();
            } else {
                approval = new AnomalyApproval();
                approval.setAttendance(attendance);
                approval.setAnomalyType(anomalyType);
            }
            
            // 承認情報を更新
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

    /**
     * 統合設定画面
     */
    @GetMapping("/settings")
    public String settings(Authentication authentication, Model model) {
        User admin = getUserFromAuth(authentication);
        
        // 権限チェック：企業設定管理権限またはユーザー管理権限が必要
        boolean canManageSettings = permissionService.hasPermission(admin, "MANAGE_COMPANY_SETTINGS");
        boolean canManageUsers = permissionService.hasPermission(admin, "MANAGE_USER");
        
        if (!canManageSettings && !canManageUsers) {
            return "redirect:/employee/dashboard";
        }
        
        Long companyId = admin.getCompany().getId();
        
        // 休憩設定を取得
        CompanySettings breakSettings = companySettingsRepository.findByCompanyId(companyId)
                .orElse(new CompanySettings());
        
        // 全ユーザーを取得（勤務設定用）
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

    /**
     * 休憩設定画面（後方互換性のため残すが、設定ページにリダイレクト）
     */
    @GetMapping("/break-settings")
    public String breakSettings(Authentication authentication, Model model) {
        return "redirect:/admin/settings";
    }

    /**
     * 休憩設定更新
     */
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
        
        // 権限チェック：企業設定管理権限が必要
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

    /**
     * Slack通知設定更新
     */
    @PostMapping("/slack-settings")
    public String updateSlackSettings(Authentication authentication,
                                      @RequestParam(required = false) Boolean slackNotificationEnabled,
                                      @RequestParam(required = false) String logSlackWebhookUrl,
                                      @RequestParam(required = false) String alertSlackWebhookUrl,
                                      RedirectAttributes redirectAttributes) {
        User admin = getUserFromAuth(authentication);
        
        // 権限チェック：企業設定管理権限が必要
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

    /**
     * 社員アカウント一覧
     */
    @GetMapping("/users")
    public String userList(Authentication authentication, Model model) {
        User admin = getUserFromAuth(authentication);
        
        // ユーザー一覧閲覧権限をチェック
        if (!permissionService.hasPermission(admin, "VIEW_USER_LIST")) {
            return "redirect:/employee/dashboard";
        }
        Long companyId = admin.getCompany().getId();
        
        // Companyエンティティを確実にロードするため、明示的に取得
        List<User> companyUsers = userRepository.findAllByCompanyId(companyId);
        
        // 企業コードを取得（ログイン時に必要）- CompanyRepositoryから直接取得
        String companyCode = companyRepository.findById(companyId)
                .map(com.example.kintai.entity.Company::getCompanyCode)
                .orElse("");

        // ロール情報を取得
        List<Role> roles = roleService.getRolesByCompanyId(companyId);

        int currentYear = java.time.Year.now().getValue();
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        
        model.addAttribute("username", admin.getUsername());
        model.addAttribute("users", companyUsers);
        model.addAttribute("companyCode", companyCode);
        model.addAttribute("roles", roles);
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("currentMonth", currentMonth);
        return "user_list";
    }

    /**
     * 管理者の操作履歴（ログ）- 管理者アカウントのみ
     */
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

    /**
     * 指定管理者の操作履歴（ログ）表示 - 社員一覧の「ログ」ボタンから
     */
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

    /**
     * 社員アカウント作成画面
     */
    @GetMapping("/users/create")
    public String createUserForm(Authentication authentication, Model model) {
        User admin = getUserFromAuth(authentication);
        
        // 権限チェック：ユーザー管理権限が必要
        if (!permissionService.hasPermission(admin, "MANAGE_USER")) {
            return "redirect:/employee/dashboard";
        }
        
        Long companyId = admin.getCompany().getId();
        
        // 企業コードを取得（ログイン時に必要）- CompanyRepositoryから直接取得
        String companyCode = companyRepository.findById(companyId)
                .map(com.example.kintai.entity.Company::getCompanyCode)
                .orElse("");
        
        // ロール情報を取得
        List<Role> roles = roleService.getRolesByCompanyId(companyId);
        List<Permission> permissions = roleService.getAllPermissions();
        
        model.addAttribute("username", admin.getUsername());
        model.addAttribute("companyCode", companyCode);
        model.addAttribute("roles", roles);
        model.addAttribute("permissions", permissions);
        return "create_user";
    }

    /**
     * 社員アカウント作成
     */
    @PostMapping("/users/create")
    public String createUser(Authentication authentication,
                             @RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String role,
                             @RequestParam(required = false) String slackWebhookUrl,
                             @RequestParam(required = false) String slackUserId,
                             RedirectAttributes redirectAttributes) {
        User admin = getUserFromAuth(authentication);
        
        // 権限チェック：ユーザー管理権限が必要
        if (!permissionService.hasPermission(admin, "MANAGE_USER")) {
            redirectAttributes.addFlashAttribute("errorMessage", "権限がありません");
            return "redirect:/employee/dashboard";
        }
        
        // 同じ企業内でユーザー名の重複チェック
        Optional<User> existingUser = userRepository.findByUsernameAndCompanyId(username, admin.getCompany().getId());
        if (existingUser.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "このユーザー名は既に使用されています");
            return "redirect:/admin/users/create";
        }

        // 権限のバリデーション
        if (!"EMPLOYEE".equals(role) && !"ADMIN".equals(role)) {
            redirectAttributes.addFlashAttribute("errorMessage", "無効な権限が指定されました");
            return "redirect:/admin/users/create";
        }

        // 新しいユーザーを作成
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password); // 現在は平文で保存（将来的にはBCryptを使用すべき）
        newUser.setRole(role);
        newUser.setCompany(admin.getCompany());
        // デフォルトでフルタイム、始業時間9時に設定
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
        
        // 企業コードを含む成功メッセージ - CompanyRepositoryから直接取得
        Long companyId = admin.getCompany().getId();
        String companyCode = companyRepository.findById(companyId)
                .map(com.example.kintai.entity.Company::getCompanyCode)
                .orElse("");
        redirectAttributes.addFlashAttribute("successMessage", 
            "社員アカウントを作成しました。ログインには企業コード「" + companyCode + "」が必要です。");
        return "redirect:/admin/users";
    }

    /**
     * ユーザー権限変更
     */
    @PostMapping("/users/{userId}/role")
    public String updateUserRole(@PathVariable Long userId,
                                  @RequestParam String role,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        User admin = getUserFromAuth(authentication);
        
        // 権限チェック：ユーザー管理権限が必要
        if (!permissionService.hasPermission(admin, "MANAGE_USER")) {
            redirectAttributes.addFlashAttribute("errorMessage", "権限がありません");
            return "redirect:/employee/dashboard";
        }
        
        // 権限のバリデーション
        if (!"EMPLOYEE".equals(role) && !"ADMIN".equals(role)) {
            redirectAttributes.addFlashAttribute("errorMessage", "無効な権限が指定されました");
            return "redirect:/admin/users";
        }
        
        // ユーザーを取得
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "ユーザーが見つかりません");
            return "redirect:/admin/users";
        }
        
        User user = optionalUser.get();
        
        // 同じ企業に属しているか確認
        if (!user.getCompany().getId().equals(admin.getCompany().getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "このユーザーの権限を変更する権限がありません");
            return "redirect:/admin/users";
        }
        
        // 権限を更新
        user.setRole(role);
        userRepository.save(user);
        
        redirectAttributes.addFlashAttribute("successMessage", 
            "ユーザー「" + user.getUsername() + "」の権限を「" + 
            ("ADMIN".equals(role) ? "管理者" : "従業員") + "」に変更しました");
        return "redirect:/admin/users";
    }

    /**
     * ロール一覧・管理画面
     */
    @GetMapping("/roles")
    public String roleList(Authentication authentication, Model model) {
        User admin = getUserFromAuth(authentication);
        
        // 権限チェック：ロール管理権限が必要
        if (!permissionService.hasPermission(admin, "MANAGE_ROLE")) {
            return "redirect:/employee/dashboard";
        }
        
        Long companyId = admin.getCompany().getId();
        
        List<Role> roles = roleService.getRolesByCompanyId(companyId);
        List<Permission> permissions = roleService.getAllPermissions();
        
        // カテゴリごとに権限をグループ化
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

    /**
     * ロール作成
     */
    @PostMapping("/roles/create")
    public String createRole(@RequestParam String name,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) List<Long> permissionIds,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            User admin = getUserFromAuth(authentication);
            
            // 権限チェック：ロール管理権限が必要
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

    /**
     * ロール更新
     */
    @PostMapping("/roles/{roleId}/update")
    public String updateRole(@PathVariable Long roleId,
                             @RequestParam String name,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) List<Long> permissionIds,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            User admin = getUserFromAuth(authentication);
            
            // 権限チェック：ロール管理権限が必要
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

    /**
     * ロール削除
     */
    @PostMapping("/roles/{roleId}/delete")
    public String deleteRole(@PathVariable Long roleId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            User admin = getUserFromAuth(authentication);
            
            // 権限チェック：ロール管理権限が必要
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

    /**
     * ユーザーにロールを割り当て（基本ロールとカスタムロールの両方）
     */
    @PostMapping("/users/{userId}/roles")
    public String assignRolesToUser(@PathVariable Long userId,
                                    @RequestParam(required = false) String role,
                                    @RequestParam(required = false) List<Long> roleIds,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            User admin = getUserFromAuth(authentication);
            
            // 権限チェック：ユーザー管理権限が必要
            if (!permissionService.hasPermission(admin, "MANAGE_USER")) {
                redirectAttributes.addFlashAttribute("errorMessage", "権限がありません");
                return "redirect:/employee/dashboard";
            }
            
            // ユーザーを取得
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "ユーザーが見つかりません");
                return "redirect:/admin/users";
            }
            
            User user = optionalUser.get();
            
            // 同じ企業に属しているか確認
            if (!user.getCompany().getId().equals(admin.getCompany().getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "このユーザーのロールを変更する権限がありません");
                return "redirect:/admin/users";
            }
            
            // 基本ロールを更新
            if (role != null && !role.isEmpty()) {
                if (!"EMPLOYEE".equals(role) && !"ADMIN".equals(role)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "無効な権限が指定されました");
                    return "redirect:/admin/users";
                }
                user.setRole(role);
            }
            
            // カスタムロールを更新
            Set<Long> roleSet = roleIds != null ? new HashSet<>(roleIds) : new HashSet<>();
            roleService.updateUserRoles(userId, roleSet);
            
            // ユーザーを保存
            userRepository.save(user);
            adminActionLogService.log(admin, "UPDATE_ROLES", "USER", userId, "ユーザー: " + user.getUsername() + " のロールを更新");
            
            redirectAttributes.addFlashAttribute("successMessage", "ユーザー「" + user.getUsername() + "」のロールを更新しました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * 管理者用：特定ユーザーの勤怠カレンダー表示
     */
    @GetMapping("/users/{userId}/calendar")
    public String userCalendar(@PathVariable Long userId,
                                @RequestParam(required = false) Integer year,
                                @RequestParam(required = false) Integer month,
                                Authentication authentication,
                                Model model) {
        User admin = getUserFromAuth(authentication);
        
        // 権限チェック：ユーザー一覧閲覧権限が必要
        if (!permissionService.hasPermission(admin, "VIEW_USER_LIST")) {
            return "redirect:/employee/dashboard";
        }
        
        // ユーザーが同じ企業に属しているか確認
        Optional<User> targetUserOptional = userRepository.findById(userId);
        if (targetUserOptional.isEmpty()) {
            return "redirect:/admin/users?error=ユーザーが見つかりません";
        }
        
        User targetUser = targetUserOptional.get();
        if (!targetUser.getCompany().getId().equals(admin.getCompany().getId())) {
            return "redirect:/admin/users?error=権限がありません";
        }

        // デフォルトは当月
        if (year == null || month == null) {
            LocalDate now = LocalDate.now();
            year = now.getYear();
            month = now.getMonthValue();
        }

        // カレンダーデータを作成
        YearMonth yearMonth = YearMonth.of(year, month);
        List<Attendance> attendances = attendanceService.getAttendanceByDateRange(
                userId,
                yearMonth.atDay(1).atStartOfDay(),
                yearMonth.atEndOfMonth().atTime(23, 59, 59)
        );

        // 日付ごとの勤怠データをマップに変換
        Map<Integer, CalendarDay> calendarDays = new HashMap<>();
        for (Attendance attendance : attendances) {
            if (attendance.getCheckIn() != null) {
                int day = attendance.getCheckIn().getDayOfMonth();
                CalendarDay calendarDay = new CalendarDay();
                calendarDay.setDay(day);
                calendarDay.setAttendance(attendance);

                // 休憩記録を取得
                List<BreakRecord> breakRecords = breakRecordRepository.findByAttendance_IdOrderByBreakStartAsc(attendance.getId());
                calendarDay.setBreakRecords(breakRecords);

                // 勤務時間を計算
                if (attendance.getCheckOut() != null) {
                    long workMinutes = Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();

                    // 休憩時間を引く（BreakRecordを使用）
                    long totalBreakMinutes = breakRecords.stream()
                            .filter(br -> br.getBreakEnd() != null)
                            .mapToLong(BreakRecord::getBreakMinutes)
                            .sum();
                    workMinutes -= totalBreakMinutes;

                    calendarDay.setWorkHours(workMinutes / 60.0);

                    // ステータスを設定（残業かどうか）
                    if (workMinutes > 480) { // 8時間以上
                        calendarDay.setStatus("overtime");
                    } else {
                        calendarDay.setStatus("normal");
                    }
                } else {
                    calendarDay.setStatus("incomplete"); // 退勤未打刻
                }

                calendarDays.put(day, calendarDay);
            }
        }

        // カレンダーグリッドを作成
        int daysInMonth = yearMonth.lengthOfMonth();
        int firstDayOfWeek = yearMonth.atDay(1).getDayOfWeek().getValue(); // 1=月曜, 7=日曜

        List<List<CalendarDay>> weeks = new ArrayList<>();
        List<CalendarDay> week = new ArrayList<>();

        // 最初の週の空白を埋める
        for (int i = 1; i < firstDayOfWeek; i++) {
            week.add(null);
        }

        // 日付を配置
        for (int day = 1; day <= daysInMonth; day++) {
            CalendarDay calendarDay = calendarDays.getOrDefault(day, new CalendarDay());
            if (calendarDay.getDay() == 0) {
                calendarDay.setDay(day);
            }
            week.add(calendarDay);

            // 週の終わり（日曜日）
            if ((firstDayOfWeek + day - 1) % 7 == 0) {
                weeks.add(week);
                week = new ArrayList<>();
            }
        }

        // 最後の週に空白を追加
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

    /**
     * カレンダーの日付情報を保持するクラス
     */
    public static class CalendarDay {
        private int day;
        private Attendance attendance;
        private double workHours;
        private String status; // normal, overtime, incomplete, absent
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

    /**
     * 勤怠詳細情報を取得（API）- 管理者用
     */
    @GetMapping("/api/attendance/{attendanceId}/details")
    @ResponseBody
    public Map<String, Object> getAttendanceDetails(@PathVariable Long attendanceId,
                                                     Authentication authentication) {
        User admin = getUserFromAuth(authentication);
        
        // 権限チェック：ユーザー一覧閲覧権限が必要
        if (!permissionService.hasPermission(admin, "VIEW_USER_LIST")) {
            throw new IllegalArgumentException("権限がありません");
        }
        
        Optional<Attendance> attendanceOptional = attendanceService.getAttendanceById(attendanceId);
        if (attendanceOptional.isEmpty()) {
            throw new IllegalArgumentException("勤怠記録が見つかりません");
        }
        
        Attendance attendance = attendanceOptional.get();
        
        // 権限チェック：管理者は同じ企業に属しているユーザーの勤怠を閲覧可能
        if (!attendance.getUser().getCompany().getId().equals(admin.getCompany().getId())) {
            throw new IllegalArgumentException("権限がありません");
        }
        
        // 休憩記録を取得
        List<BreakRecord> breakRecords = breakRecordRepository.findByAttendance_IdOrderByBreakStartAsc(attendanceId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("checkIn", attendance.getCheckIn() != null ? 
            attendance.getCheckIn().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null);
        result.put("checkOut", attendance.getCheckOut() != null ? 
            attendance.getCheckOut().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null);
        
        // 休憩情報をリスト形式で返す
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
        
        // 勤務時間を計算
        if (attendance.getCheckIn() != null && attendance.getCheckOut() != null) {
            long workMinutes = Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();
            long totalBreakMinutes = breakRecords.stream()
                    .filter(br -> br.getBreakEnd() != null)
                    .mapToLong(BreakRecord::getBreakMinutes)
                    .sum();
            workMinutes -= totalBreakMinutes;
            result.put("workHours", workMinutes / 60.0);
        }
        
        return result;
    }

    /**
     * ユーザーの月次サマリーを計算
     */
    private Map<String, Object> calculateMonthlySummary(Long userId, int year, int month) {
        Map<String, Object> summary = new HashMap<>();
        
        // 月の開始日と終了日を計算
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // 該当月の勤怠データを取得
        List<Attendance> attendances = attendanceService.getAttendanceByDateRange(
                userId, startDate, endDate);

        long totalWorkMinutes = 0;
        long totalOvertimeMinutes = 0;
        long totalBreakMinutes = 0;
        int workDays = 0;

        // 各勤怠レコードを処理
        for (Attendance attendance : attendances) {
            if (attendance.getCheckIn() == null) {
                continue;
            }

            workDays++;

            // 勤務時間を計算
            long workMinutes = 0;
            long overtimeMinutes = 0;
            long breakMinutes = 0;

            if (attendance.getCheckOut() != null) {
                workMinutes = Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();

                // 休憩時間を引く（BreakRecordを使用）
                List<BreakRecord> breakRecords = breakRecordRepository.findByAttendance_IdOrderByBreakStartAsc(attendance.getId());
                breakMinutes = breakRecords.stream()
                        .filter(br -> br.getBreakEnd() != null)
                        .mapToLong(BreakRecord::getBreakMinutes)
                        .sum();
                workMinutes -= breakMinutes;

                // 残業時間を計算（8時間 = 480分を超えた分）
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

    /**
     * 評価管理画面
     */
    @GetMapping("/evaluations")
    public String evaluations(@RequestParam(required = false) String selectedWeekStart,
                               Authentication authentication, Model model) {
        User admin = getUserFromAuth(authentication);
        
        // 権限チェック：ユーザー一覧閲覧権限が必要
        if (!permissionService.hasPermission(admin, "VIEW_USER_LIST")) {
            return "redirect:/employee/dashboard";
        }
        
        Long companyId = admin.getCompany().getId();
        List<User> companyUsers = userRepository.findAllByCompanyId(companyId);
        
        // 選択された週の開始日を取得（デフォルトは今週）
        LocalDate today = LocalDate.now();
        LocalDate weekStart;
        if (selectedWeekStart != null && !selectedWeekStart.trim().isEmpty()) {
            try {
                weekStart = LocalDate.parse(selectedWeekStart.trim());
            } catch (Exception e) {
                weekStart = weeklyEvaluationService.getWeekStartDate(today);
            }
        } else {
            weekStart = weeklyEvaluationService.getWeekStartDate(today);
        }
        LocalDate weekEnd = weeklyEvaluationService.getWeekEndDate(weekStart);
        
        // 過去の評価一覧を取得
        List<WeeklyEvaluation> pastEvaluations = weeklyEvaluationService.getCompanyEvaluations(companyId);
        
        // 選択された週の評価状況を取得
        Map<Long, WeeklyEvaluation> selectedWeekEvaluations = new HashMap<>();
        for (User user : companyUsers) {
            if (!"ADMIN".equals(user.getRole())) {
                Optional<WeeklyEvaluation> eval = weeklyEvaluationService.getEvaluationByEmployeeAndWeek(
                        user.getId(), weekStart);
                if (eval.isPresent()) {
                    selectedWeekEvaluations.put(user.getId(), eval.get());
                }
            }
        }
        
        // 選択可能な週のリストを生成（過去12週間）
        List<LocalDate> availableWeeks = new ArrayList<>();
        LocalDate currentWeek = weeklyEvaluationService.getWeekStartDate(today);
        for (int i = 0; i < 12; i++) {
            availableWeeks.add(currentWeek.minusWeeks(i));
        }
        
        model.addAttribute("username", admin.getUsername());
        model.addAttribute("users", companyUsers);
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("weekEnd", weekEnd);
        model.addAttribute("selectedWeekStart", weekStart.toString());
        model.addAttribute("availableWeeks", availableWeeks);
        model.addAttribute("pastEvaluations", pastEvaluations);
        model.addAttribute("currentWeekEvaluations", selectedWeekEvaluations);
        
        return "evaluation_list";
    }

    /**
     * 評価を保存
     */
    @PostMapping("/evaluations/save")
    public String saveEvaluation(@RequestParam Long employeeId,
                                 @RequestParam String rating,
                                 @RequestParam(required = false) String comment,
                                 @RequestParam String weekStartDate,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        User admin = getUserFromAuth(authentication);
        
        // 権限チェック：ユーザー一覧閲覧権限が必要
        if (!permissionService.hasPermission(admin, "VIEW_USER_LIST")) {
            redirectAttributes.addFlashAttribute("errorMessage", "権限がありません");
            return "redirect:/employee/dashboard";
        }
        
        try {
            LocalDate weekStart = LocalDate.parse(weekStartDate);
            
            weeklyEvaluationService.saveEvaluation(employeeId, admin.getId(), weekStart, rating, comment);
            
            redirectAttributes.addFlashAttribute("successMessage", "評価を保存しました");
            redirectAttributes.addAttribute("selectedWeekStart", weekStart.toString());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "評価の保存に失敗しました: " + e.getMessage());
        }
        
        return "redirect:/admin/evaluations";
    }

    /**
     * 月別評価一覧画面
     */
    @GetMapping("/evaluations/monthly")
    public String monthlyEvaluations(@RequestParam(required = false) String yearMonth,
                                      Authentication authentication, Model model) {
        User admin = getUserFromAuth(authentication);
        
        // 権限チェック：ユーザー一覧閲覧権限が必要
        if (!permissionService.hasPermission(admin, "VIEW_USER_LIST")) {
            return "redirect:/employee/dashboard";
        }
        
        Long companyId = admin.getCompany().getId();
        
        // 選択された年月を取得（デフォルトは今月）
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
        
        // 月別評価サマリーを取得
        List<MonthlyEvaluationSummaryDTO> summaries = weeklyEvaluationService.getMonthlyEvaluationSummaries(
                companyId, selectedYearMonth);
        
        // 選択可能な年月のリストを生成（過去12ヶ月）
        List<YearMonth> availableMonths = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();
        for (int i = 0; i < 12; i++) {
            availableMonths.add(currentMonth.minusMonths(i));
        }
        
        model.addAttribute("username", admin.getUsername());
        model.addAttribute("summaries", summaries);
        model.addAttribute("selectedYearMonth", selectedYearMonth);
        model.addAttribute("selectedYearMonthStr", selectedYearMonth.toString());
        model.addAttribute("availableMonths", availableMonths);
        
        return "monthly_evaluation_list";
    }

    /**
     * ユーザーの勤務設定を更新
     */
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
        
        // 権限チェック：ユーザー管理権限が必要
        if (!permissionService.hasPermission(admin, "MANAGE_USER")) {
            redirectAttributes.addFlashAttribute("errorMessage", "権限がありません");
            return "redirect:/employee/dashboard";
        }
        
        try {
            // ユーザーを取得
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "ユーザーが見つかりません");
                return "redirect:/admin/users";
            }
            
            User user = optionalUser.get();
            
            // 同じ企業に属しているか確認
            if (!user.getCompany().getId().equals(admin.getCompany().getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "権限がありません");
                return "redirect:/admin/users";
            }
            
            // 勤務形態を設定
            if (!"FULLTIME".equals(workType) && !"FLEX".equals(workType)) {
                redirectAttributes.addFlashAttribute("errorMessage", "無効な勤務形態が指定されました");
                return "redirect:/admin/users";
            }
            
            user.setWorkType(workType);
            
            // フルタイムの場合のみ始業時間を設定
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
                    // デフォルト値として9時を設定
                    user.setStartTime(LocalTime.of(9, 0));
                }
                // フルタイムの場合はコアタイムをnullに設定
                user.setCoreTimeStart(null);
                user.setCoreTimeEnd(null);
            } else if ("FLEX".equals(workType)) {
                // フレックスの場合は始業時間をnullに設定
                user.setStartTime(null);
                
                // コアタイムを設定
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

    /**
     * 今日の全社員の勤務状況を取得
     */
    private List<EmployeeTodayStatusDTO> getTodayEmployeeStatuses(List<User> companyUsers) {
        List<EmployeeTodayStatusDTO> statusList = new ArrayList<>();
        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
        LocalTime currentTime = LocalTime.now();

        for (User employee : companyUsers) {
            // 管理者は除外
            if ("ADMIN".equals(employee.getRole())) {
                continue;
            }

            // 今日の勤怠記録を取得
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
                    // 休憩中かチェック
                    List<BreakRecord> activeBreaks = breakRecordRepository.findByAttendance_IdAndBreakEndIsNull(attendance.getId());
                    isOnBreak = !activeBreaks.isEmpty();

                    // 中抜け中かチェック
                    List<LeaveRecord> activeLeaves = leaveRecordRepository.findByAttendance_IdAndLeaveEndIsNull(attendance.getId());
                    isOnLeave = !activeLeaves.isEmpty();
                }
            }

            // 始業時間を取得（フルタイムの場合はstartTime、フレックスの場合はcoreTimeStart）
            LocalTime startTime = null;
            if ("FULLTIME".equals(employee.getWorkType()) || employee.getWorkType() == null) {
                startTime = employee.getStartTime() != null ? employee.getStartTime() : LocalTime.of(9, 0);
            } else if ("FLEX".equals(employee.getWorkType())) {
                startTime = employee.getCoreTimeStart() != null ? employee.getCoreTimeStart() : LocalTime.of(10, 0);
            }

            // 状態を判定
            String status;
            if (hasCheckedOut) {
                status = "COMPLETED"; // 勤務済み
            } else if (hasCheckedIn) {
                status = "WORKING"; // 勤務中
            } else {
                // 未出勤の場合、遅刻判定
                if (startTime != null && currentTime.isAfter(startTime)) {
                    status = "LATE"; // 遅刻中
                } else {
                    status = "NOT_STARTED"; // 未勤務
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

    /**
     * 認証からユーザー取得
     */
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
