package com.example.kintai.controller;

import com.example.kintai.entity.Attendance;
import com.example.kintai.entity.BreakRecord;
import com.example.kintai.entity.User;
import com.example.kintai.service.AttendanceService;
import com.example.kintai.service.BreakRecordService;
import com.example.kintai.service.LeaveRecordService;
import com.example.kintai.service.PermissionService;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/calendar")
public class CalendarController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private BreakRecordService breakRecordService;

    @Autowired
    private LeaveRecordService leaveRecordService;

    @Autowired
    private PermissionService permissionService;

    /**
     * カレンダー表示（従業員用：自分の勤怠のみ）
     */
    @GetMapping
    public String calendar(@RequestParam(required = false) Integer year,
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

        // カレンダーデータを作成
        YearMonth yearMonth = YearMonth.of(year, month);
        List<Attendance> attendances = attendanceService.getAttendanceByDateRange(
                user.getId(),
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

                // 休憩記録を取得（BreakRecordService に集約）
                List<BreakRecord> breakRecords = breakRecordService.getBreakRecordsByAttendanceId(attendance.getId());
                calendarDay.setBreakRecords(breakRecords);

                // 実働時間を計算（出退勤差 − 休憩 − 中抜け（控除））
                if (attendance.getCheckOut() != null) {
                    long workMinutes = Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();
                    workMinutes -= breakRecordService.getTotalBreakMinutesFromRecordsOnly(attendance);
                    workMinutes -= leaveRecordService.getTotalDeductionLeaveMinutes(attendance.getId());

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

        model.addAttribute("username", user.getUsername());
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("weeks", weeks);
        model.addAttribute("prevMonth", month == 1 ? 12 : month - 1);
        model.addAttribute("prevYear", month == 1 ? year - 1 : year);
        model.addAttribute("nextMonth", month == 12 ? 1 : month + 1);
        model.addAttribute("nextYear", month == 12 ? year + 1 : year);

        return "attendance_calendar";
    }

    /**
     * 勤怠詳細情報を取得（API）- 従業員用（自分の勤怠のみ）
     */
    @GetMapping("/api/attendance/{attendanceId}/details")
    @ResponseBody
    public Map<String, Object> getAttendanceDetails(@PathVariable Long attendanceId,
                                                     Authentication authentication) {
        User user = getUserFromAuth(authentication);
        
        Optional<Attendance> attendanceOptional = attendanceService.getAttendanceById(attendanceId);
        if (attendanceOptional.isEmpty()) {
            throw new IllegalArgumentException("勤怠記録が見つかりません");
        }
        
        Attendance attendance = attendanceOptional.get();
        
        // 権限チェック：自分の勤怠のみ閲覧可能
        if (!attendance.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("権限がありません");
        }
        
        // 休憩記録を取得（BreakRecordService に集約）
        List<BreakRecord> breakRecords = breakRecordService.getBreakRecordsByAttendanceId(attendanceId);
        
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
        
        // 実働時間を計算（出退勤差 − 休憩 − 中抜け（控除））
        if (attendance.getCheckIn() != null && attendance.getCheckOut() != null) {
            long workMinutes = Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();
            workMinutes -= breakRecordService.getTotalBreakMinutesFromRecordsOnly(attendance);
            workMinutes -= leaveRecordService.getTotalDeductionLeaveMinutes(attendanceId);
            result.put("workHours", workMinutes / 60.0);
        }
        
        return result;
    }

    /**
     * 認証からユーザー取得
     */
    private User getUserFromAuth(Authentication authentication) {
        return (User) authentication.getPrincipal();
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
}
