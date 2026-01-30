package com.example.kintai.controller;

import com.example.kintai.dto.DashboardStatisticsDTO;
import com.example.kintai.entity.CompanySettings;
import com.example.kintai.entity.User;
import com.example.kintai.repository.CompanySettingsRepository;
import com.example.kintai.service.AttendanceService;
import com.example.kintai.service.DashboardStatisticsService;
import com.example.kintai.service.PermissionService;
import com.example.kintai.service.HighlightService;
import com.example.kintai.dto.WeeklyHighlightDTO;
import com.example.kintai.service.SelfEvaluationService;
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
    private PermissionService permissionService;

    @Autowired
    private HighlightService highlightService;

    @Autowired
    private SelfEvaluationService selfEvaluationService;

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
        // 管理タイプは管理画面へ、従業員タイプは従業員画面へ
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
        // 管理タイプは管理画面のみ表示。従業員画面へ直接アクセスした場合は管理画面へリダイレクト
        if ("ADMIN".equals(user.getRole())) {
            return "redirect:/admin/dashboard";
        }

        // ダッシュボード統計情報を取得

        DashboardStatisticsDTO statistics = dashboardStatisticsService.getDashboardStatistics(user.getId());

        // 休憩設定を取得
        CompanySettings companySettings = companySettingsRepository.findByCompanyId(user.getCompany().getId())
                .orElse(new CompanySettings());

        // 今日の勤怠状態を取得
        AttendanceService.TodayAttendanceStatus todayStatus = attendanceService.getTodayAttendanceStatus(user.getId());

        // 管理者機能にアクセスできる権限があるかチェック
        boolean canAccessAdmin = permissionService.canAccessAdminFeatures(user);

        // 各権限をチェックしてメニュー表示用に追加
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

        // ハイライトデータを取得（前月の週ごとの評価・勤務時間・残業時間）
        // 開発環境なので常に表示
        List<WeeklyHighlightDTO> highlights = highlightService.getPreviousMonthHighlights(user.getId());
        model.addAttribute("highlights", highlights);

        return "employee_dashboard";

    }

    /**
     * 評価ダッシュボード（自己評価・事実ベース評価・比較）
     */
    @GetMapping("/evaluation/dashboard")
    public String evaluationDashboard(Authentication authentication, Model model) {
        User user = getUserFromAuth(authentication);
        YearMonth currentMonth = YearMonth.now();

        // 現在の月の自己評価を取得
        selfEvaluationService.getSelfEvaluation(user.getId(), currentMonth)
                .ifPresent(self -> {
                    model.addAttribute("selfEvaluation", self);
                });

        // 現在の月の事実ベース評価を計算・取得
        var factBasedEval = factBasedEvaluationService.calculateAndSaveMonthlyEvaluation(user.getId(), currentMonth);
        if (factBasedEval != null) {
            model.addAttribute("factBasedEvaluation", factBasedEval);
        }

        // 過去6ヶ月のトレンドを取得
        List<EvaluationTrendDTO> trends = evaluationTrendService.getEmployeeTrend(user.getId(), 6);
        model.addAttribute("trends", trends);
        model.addAttribute("trendAnalysis", evaluationTrendService.analyzeTrend(trends));

        model.addAttribute("username", user.getUsername());
        model.addAttribute("currentMonth", currentMonth);

        return "evaluation_dashboard";
    }

    /**
     * 自己評価を保存
     */
    @PostMapping("/evaluation/self")
    public String saveSelfEvaluation(@RequestParam String rating,
                                     @RequestParam(required = false) String comment,
                                     @RequestParam String yearMonth,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        User user = getUserFromAuth(authentication);
        
        try {
            YearMonth targetMonth = YearMonth.parse(yearMonth);
            selfEvaluationService.saveSelfEvaluation(user.getId(), targetMonth, rating, comment);
            redirectAttributes.addFlashAttribute("successMessage", "自己評価を保存しました");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "自己評価の保存に失敗しました: " + e.getMessage());
        }
        
        return "redirect:/evaluation/dashboard";
    }

    /**
     * 認証からユーザー取得
     */
    private User getUserFromAuth(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }
}
