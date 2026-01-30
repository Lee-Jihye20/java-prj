package com.example.kintai.controller;

import com.example.kintai.dto.MonthlyReportDTO;
import com.example.kintai.entity.User;
import com.example.kintai.service.MonthlyReportService;
import com.example.kintai.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private MonthlyReportService monthlyReportService;

    @Autowired
    private PermissionService permissionService;

    /**
     * 月次レポート表示
     */
    @GetMapping("/monthly")
    public String monthlyReport(@RequestParam(required = false) Integer year,
                                 @RequestParam(required = false) Integer month,
                                 Authentication authentication,
                                 Model model) {
        User user = getUserFromAuth(authentication);

        // デフォルトは当月
        if (year == null || month == null) {
            LocalDate now = LocalDate.now();
            year = now.getYear();
            month = now.getMonthValue();
        }

        MonthlyReportDTO report = monthlyReportService.generateMonthlyReport(user, year, month);

        // 権限チェック：月次レポート閲覧権限が必要
        boolean canViewMonthlyReport = permissionService.hasPermission(user, "VIEW_MONTHLY_REPORT");
        if (!canViewMonthlyReport) {
            return "redirect:/employee/dashboard";
        }

        model.addAttribute("report", report);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("currentYear", year);
        model.addAttribute("currentMonth", month);
        model.addAttribute("isAdmin", permissionService.canAccessAdminFeatures(user));

        return "monthly_report";
    }

    /**
     * 管理者用：全従業員の月次レポート
     */
    @GetMapping("/admin/monthly")
    public String adminMonthlyReport(@RequestParam(required = false) Integer year,
                                      @RequestParam(required = false) Integer month,
                                      Authentication authentication,
                                      Model model) {
        User admin = getUserFromAuth(authentication);

        // 権限チェック：月次レポート閲覧権限が必要
        if (!permissionService.hasPermission(admin, "VIEW_MONTHLY_REPORT")) {
            return "redirect:/employee/dashboard";
        }

        // デフォルトは当月
        if (year == null || month == null) {
            LocalDate now = LocalDate.now();
            year = now.getYear();
            month = now.getMonthValue();
        }

        MonthlyReportDTO report = monthlyReportService.generateMonthlyReportForCompany(admin.getCompany().getId(), year, month);

        model.addAttribute("report", report);
        model.addAttribute("username", admin.getUsername());
        model.addAttribute("currentYear", year);
        model.addAttribute("currentMonth", month);
        model.addAttribute("isAdmin", true); // 管理者用なので常にtrue

        return "monthly_report";
    }

    /**
     * 認証からユーザー取得
     */
    private User getUserFromAuth(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }
}
