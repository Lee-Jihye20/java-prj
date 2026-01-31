package com.example.kintai.controller;

import com.example.kintai.entity.User;
import com.example.kintai.repository.UserRepository;
import com.example.kintai.service.ExportService;
import com.example.kintai.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/export")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionService permissionService;

    @GetMapping("/page")
    public String exportPage(Authentication authentication, Model model) {
        User user = getUserFromAuth(authentication);
        
        if (!permissionService.hasPermission(user, "EXPORT_DATA")) {
            return "redirect:/employee/dashboard";
        }

        List<User> companyUsers = userRepository.findAllByCompanyId(user.getCompany().getId())
                .stream()
                .filter(u -> !"ADMIN".equals(u.getRole()))
                .collect(java.util.stream.Collectors.toList());
        
        List<User> adminUsers = userRepository.findAllByCompanyId(user.getCompany().getId())
                .stream()
                .filter(u -> "ADMIN".equals(u.getRole()))
                .collect(java.util.stream.Collectors.toList());
        
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int currentMonth = today.getMonthValue();

        List<Integer> years = new java.util.ArrayList<>();
        for (int year = 2000; year <= 2099; year++) {
            years.add(year);
        }
        
        List<Integer> months = new java.util.ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            months.add(month);
        }

        model.addAttribute("username", user.getUsername());
        model.addAttribute("users", companyUsers);
        model.addAttribute("adminUsers", adminUsers);
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("years", years);
        model.addAttribute("months", months);
        model.addAttribute("today", today);
        model.addAttribute("oneMonthAgo", today.minusMonths(1));
        
        return "export_page";
    }

    @GetMapping("/daily")
    public ResponseEntity<ByteArrayResource> exportDaily(
            @RequestParam Long userId,
            @RequestParam String date,
            Authentication authentication) {
        try {
            User user = getUserFromAuth(authentication);
            
            if (!permissionService.hasPermission(user, "EXPORT_DATA")) {
                return ResponseEntity.status(403).build();
            }

            User targetUser = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));
            if (!targetUser.getCompany().getId().equals(user.getCompany().getId())) {
                return ResponseEntity.status(403).build();
            }

            LocalDate exportDate = LocalDate.parse(date, DateTimeFormatter.ISO_DATE);
            byte[] data = exportService.exportDailyAttendance(userId, exportDate);

            ByteArrayResource resource = new ByteArrayResource(data);
            String filename = String.format("日別勤怠_%s_%s.xlsx",
                    targetUser.getUsername(),
                    exportDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            String encodedFilename = java.net.URLEncoder.encode(filename, java.nio.charset.StandardCharsets.UTF_8)
                    .replace("+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s", filename, encodedFilename))
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(data.length)
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/monthly")
    public ResponseEntity<ByteArrayResource> exportMonthly(
            @RequestParam Long userId,
            @RequestParam Integer year,
            @RequestParam Integer month,
            Authentication authentication) {
        try {
            User user = getUserFromAuth(authentication);
            
            if (!permissionService.hasPermission(user, "EXPORT_DATA")) {
                return ResponseEntity.status(403).build();
            }

            User targetUser = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));
            if (!targetUser.getCompany().getId().equals(user.getCompany().getId())) {
                return ResponseEntity.status(403).build();
            }

            byte[] data = exportService.exportMonthlySummary(userId, year, month);

            ByteArrayResource resource = new ByteArrayResource(data);
            String filename = String.format("月次集計_%s_%04d%02d.xlsx",
                    targetUser.getUsername(), year, month);
            String encodedFilename = java.net.URLEncoder.encode(filename, java.nio.charset.StandardCharsets.UTF_8)
                    .replace("+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s", filename, encodedFilename))
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(data.length)
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/log")
    public ResponseEntity<ByteArrayResource> exportLog(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operationType,
            Authentication authentication) {
        try {
            User user = getUserFromAuth(authentication);
            
            if (!permissionService.hasPermission(user, "EXPORT_DATA")) {
                return ResponseEntity.status(403).build();
            }

            if (startDate == null || startDate.trim().isEmpty() || endDate == null || endDate.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            LocalDate start = LocalDate.parse(startDate.trim(), DateTimeFormatter.ISO_DATE);
            LocalDate end = LocalDate.parse(endDate.trim(), DateTimeFormatter.ISO_DATE);

            byte[] data = exportService.exportAttendanceLog(user.getCompany().getId(), start, end, username, operationType);

            ByteArrayResource resource = new ByteArrayResource(data);
            String filename = String.format("打刻ログ_%s_%s.xlsx",
                    start.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                    end.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            String encodedFilename = java.net.URLEncoder.encode(filename, java.nio.charset.StandardCharsets.UTF_8)
                    .replace("+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s", filename, encodedFilename))
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(data.length)
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/admin-log")
    public ResponseEntity<ByteArrayResource> exportAdminLog(
            @RequestParam Long adminId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            Authentication authentication) {
        try {
            User user = getUserFromAuth(authentication);
            
            if (!permissionService.hasPermission(user, "EXPORT_DATA")) {
                return ResponseEntity.status(403).build();
            }

            User targetAdmin = userRepository.findById(adminId)
                    .orElseThrow(() -> new IllegalArgumentException("管理者が見つかりません"));
            if (!targetAdmin.getCompany().getId().equals(user.getCompany().getId())) {
                return ResponseEntity.status(403).build();
            }
            
            if (!"ADMIN".equals(targetAdmin.getRole())) {
                return ResponseEntity.status(400).build();
            }

            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);

            byte[] data = exportService.exportAdminLog(adminId, start, end);

            ByteArrayResource resource = new ByteArrayResource(data);
            String filename = String.format("管理者ログ_%s_%s_%s.xlsx",
                    targetAdmin.getUsername(),
                    start.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                    end.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            String encodedFilename = java.net.URLEncoder.encode(filename, java.nio.charset.StandardCharsets.UTF_8)
                    .replace("+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s", filename, encodedFilename))
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(data.length)
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    private User getUserFromAuth(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }
}
