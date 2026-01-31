package com.example.kintai.controller;

import com.example.kintai.dto.DashboardStatisticsDTO;
import com.example.kintai.entity.Attendance;
import com.example.kintai.entity.CompanySettings;
import com.example.kintai.entity.User;
import com.example.kintai.repository.AnomalyApprovalRepository;
import com.example.kintai.repository.CompanySettingsRepository;
import com.example.kintai.service.AttendanceService;
import com.example.kintai.service.DashboardStatisticsService;
import com.example.kintai.service.OvertimeExcessService;
import com.example.kintai.service.PermissionService;
import com.example.kintai.service.HighlightService;
import com.example.kintai.dto.WeeklyHighlightDTO;
import com.example.kintai.service.EvaluationTrendService;
import com.example.kintai.service.FactBasedEvaluationService;
import com.example.kintai.dto.EvaluationTrendDTO;
import com.example.kintai.dto.EvaluationComparisonDTO;
import java.time.YearMonth;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller

public class DashboardController {

    @Autowired

    private DashboardStatisticsService dashboardStatisticsService;

    @Autowired
    private CompanySettingsRepository companySettingsRepository;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private OvertimeExcessService overtimeExcessService;

    @Autowired
    private AnomalyApprovalRepository anomalyApprovalRepository;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private HighlightService highlightService;

    @Autowired
    private EvaluationTrendService evaluationTrendService;

    @Autowired
    private FactBasedEvaluationService factBasedEvaluationService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        User user = getUserFromAuth(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        
        if ("ADMIN".equals(user.getRole())) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/employee/dashboard";
    }

    @GetMapping("/employee/dashboard")
    public String employeeDashboard(Authentication authentication, Model model) {
        User user = getUserFromAuth(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        
        if ("ADMIN".equals(user.getRole())) {
            return "redirect:/admin/dashboard";
        }

        DashboardStatisticsDTO statistics = dashboardStatisticsService.getDashboardStatistics(user.getId());

        CompanySettings companySettings = companySettingsRepository.findByCompanyId(user.getCompany().getId())
                .orElse(new CompanySettings());

        AttendanceService.TodayAttendanceStatus todayStatus = attendanceService.getTodayAttendanceStatus(user.getId());

        boolean canAccessAdmin = permissionService.canAccessAdminFeatures(user);

        boolean canViewUsers = permissionService.hasPermission(user, "VIEW_USER_LIST");
        boolean canManageUsers = permissionService.hasPermission(user, "MANAGE_USER");
        boolean canManageRoles = permissionService.hasPermission(user, "MANAGE_ROLE");
        boolean canApproveFixRequests = permissionService.hasPermission(user, "APPROVE_FIX_REQUEST");
        boolean canViewLogs = permissionService.hasPermission(user, "VIEW_LOG");
        boolean canExport = permissionService.hasPermission(user, "EXPORT_DATA");
        boolean canManageSettings = permissionService.hasPermission(user, "MANAGE_COMPANY_SETTINGS");
        boolean canViewReports = permissionService.hasPermission(user, "VIEW_MONTHLY_REPORT");

        model.addAttribute("statistics", statistics);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("breakInputMode", companySettings.getBreakInputMode() != null ? companySettings.getBreakInputMode() : "FREE");
        model.addAttribute("hasCheckedIn", todayStatus.hasCheckedIn());
        model.addAttribute("hasCheckedOut", todayStatus.hasCheckedOut());
        model.addAttribute("isOnBreak", todayStatus.isOnBreak());
        model.addAttribute("isOnLeave", todayStatus.isOnLeave());
        model.addAttribute("canAccessAdmin", canAccessAdmin);
        model.addAttribute("canViewUsers", canViewUsers);
        model.addAttribute("canManageUsers", canManageUsers);
        model.addAttribute("canManageRoles", canManageRoles);
        model.addAttribute("canApproveFixRequests", canApproveFixRequests);
        model.addAttribute("canViewLogs", canViewLogs);
        model.addAttribute("canExport", canExport);
        model.addAttribute("canManageSettings", canManageSettings);
        model.addAttribute("canViewReports", canViewReports);

        List<WeeklyHighlightDTO> highlights = highlightService.getPreviousMonthHighlights(user.getId());
        model.addAttribute("highlights", highlights);

        List<Attendance> myOvertimeExcess = overtimeExcessService.getUnresolvedOvertimeForUser(user.getId(), user.getCompany().getId());
        model.addAttribute("overtimeExcessAttendances", myOvertimeExcess);

        List<Attendance> companyMissingCheckout = attendanceService.detectMissingCheckOut(user.getCompany().getId());
        List<Attendance> myMissingCheckout = companyMissingCheckout.stream()
                .filter(a -> a.getUser() != null && user.getId().equals(a.getUser().getId()))
                .filter(a -> !anomalyApprovalRepository.existsByAttendance_IdAndAnomalyTypeAndApprovedTrue(a.getId(), "MISSING_CHECKOUT"))
                .toList();
        model.addAttribute("missingCheckoutAttendances", myMissingCheckout);

        return "employee_dashboard";

    }

    @GetMapping("/evaluation/dashboard")
    public String evaluationDashboard(Authentication authentication, Model model) {
        User user = getUserFromAuth(authentication);
        YearMonth currentMonth = YearMonth.now();

        var factBasedEval = factBasedEvaluationService.calculateAndSaveMonthlyEvaluation(user.getId(), currentMonth);
        if (factBasedEval != null) {
            model.addAttribute("factBasedEvaluation", factBasedEval);
        }

        List<EvaluationTrendDTO> trends = evaluationTrendService.getEmployeeTrend(user.getId(), 6);
        model.addAttribute("trends", trends);
        model.addAttribute("trendAnalysis", evaluationTrendService.analyzeTrend(trends));

        model.addAttribute("username", user.getUsername());
        model.addAttribute("currentMonth", currentMonth);

        return "evaluation_dashboard";
    }

    private User getUserFromAuth(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }
}
