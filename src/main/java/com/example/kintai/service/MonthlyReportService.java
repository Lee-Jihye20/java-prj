package com.example.kintai.service;

import com.example.kintai.dto.MonthlyReportDTO;
import com.example.kintai.entity.Attendance;
import com.example.kintai.entity.User;
import com.example.kintai.repository.AttendanceRepository;
import com.example.kintai.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class MonthlyReportService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BreakRecordService breakRecordService;

    /**
     * 月次レポートを生成
     */
    public MonthlyReportDTO generateMonthlyReport(User user, int year, int month) {
        MonthlyReportDTO report = new MonthlyReportDTO(user.getUsername(), year, month);

        // 月の開始日と終了日を計算
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // 該当月の勤怠データを取得
        List<Attendance> attendances = attendanceRepository.findByUser_IdAndCheckInBetweenOrderByCheckInDesc(
                user.getId(), startDate, endDate);

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
                breakMinutes = breakRecordService.getTotalBreakMinutesFromRecordsOnly(attendance);
                workMinutes -= breakMinutes;

                // 残業時間を計算（8時間 = 480分を超えた分）
                if (workMinutes > 480) {
                    overtimeMinutes = workMinutes - 480;
                }
            }

            totalWorkMinutes += workMinutes;
            totalOvertimeMinutes += overtimeMinutes;
            totalBreakMinutes += breakMinutes;

            // 日別レポートを追加
            MonthlyReportDTO.DailyReportDTO dailyReport = new MonthlyReportDTO.DailyReportDTO(
                    attendance.getCheckIn().toLocalDate(),
                    workMinutes,
                    overtimeMinutes,
                    breakMinutes,
                    attendance.getStatus()
            );
            report.getDailyReports().add(dailyReport);
        }

        // 集計結果を設定
        report.setWorkDays(workDays);
        report.setTotalWorkMinutes(totalWorkMinutes);
        report.setTotalOvertimeMinutes(totalOvertimeMinutes);
        report.setTotalBreakMinutes(totalBreakMinutes);

        // 平均勤務時間を計算
        if (workDays > 0) {
            report.setAverageWorkHours((totalWorkMinutes / 60.0) / workDays);
        }

        return report;
    }

    /**
     * 企業ごとの月次レポートを生成
     */
    public MonthlyReportDTO generateMonthlyReportForCompany(Long companyId, int year, int month) {
        MonthlyReportDTO report = new MonthlyReportDTO("全従業員", year, month);

        // 月の開始日と終了日を計算
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // 該当企業のユーザーIDリストを取得
        List<Long> userIdsInCompany = userRepository.findAllByCompanyId(companyId)
                .stream()
                .map(User::getId)
                .toList();

        // 該当月の全勤怠データを取得 (企業に属するユーザーのみ)
        List<Attendance> allAttendances = attendanceRepository.findAllByUser_CompanyId(companyId).stream()
                .filter(a -> a.getCheckIn() != null)
                .filter(a -> userIdsInCompany.contains(a.getUserId())) // ユーザーIDでフィルタリング
                .filter(a -> !a.getCheckIn().isBefore(startDate) && !a.getCheckIn().isAfter(endDate))
                .toList();

        long totalWorkMinutes = 0;
        long totalOvertimeMinutes = 0;
        long totalBreakMinutes = 0;
        int workDays = 0; // 実際に出勤があった日数をカウント

        // 各勤怠レコードを処理
        for (Attendance attendance : allAttendances) {
            if (attendance.getCheckIn() == null) {
                continue;
            }
            workDays++; // checkInがあるものを出勤日としてカウント

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

            // 日別レポートを追加
            MonthlyReportDTO.DailyReportDTO dailyReport = new MonthlyReportDTO.DailyReportDTO(
                    attendance.getCheckIn().toLocalDate(),
                    workMinutes,
                    overtimeMinutes,
                    breakMinutes,
                    attendance.getStatus()
            );
            report.getDailyReports().add(dailyReport);
        }

        // 日付順にソート
        report.getDailyReports().sort((d1, d2) -> d1.getDate().compareTo(d2.getDate()));

        report.setWorkDays(workDays);
        report.setTotalWorkMinutes(totalWorkMinutes);
        report.setTotalOvertimeMinutes(totalOvertimeMinutes);
        report.setTotalBreakMinutes(totalBreakMinutes);

        if (workDays > 0) {
            report.setAverageWorkHours((totalWorkMinutes / 60.0) / workDays);
        }

        return report;
    }
}
