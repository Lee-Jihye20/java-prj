package com.example.kintai.service;

import com.example.kintai.dto.DashboardStatisticsDTO;
import com.example.kintai.entity.Attendance;
import com.example.kintai.entity.BreakRecord;
import com.example.kintai.repository.AttendanceRepository;
import com.example.kintai.repository.BreakRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class DashboardStatisticsService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private BreakRecordRepository breakRecordRepository;

    /**
     * ダッシュボード統計情報を取得
     */
    public DashboardStatisticsDTO getDashboardStatistics(Long userId) {
        DashboardStatisticsDTO stats = new DashboardStatisticsDTO();

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(23, 59, 59);

        // 今日の勤怠
        List<Attendance> todayAttendances = attendanceRepository.findByUser_IdAndCheckInBetweenOrderByCheckInDesc(
                userId, startOfToday, endOfToday);

        if (!todayAttendances.isEmpty()) {
            Attendance todayAttendance = todayAttendances.get(0);
            stats.setTodayWorkHours(calculateWorkHours(todayAttendance));

            // 現在の状態
            if (todayAttendance.getCheckOut() != null) {
                stats.setCurrentStatus("退勤済み");
            } else {
                // BreakRecordを使用して休憩中かチェック
                List<BreakRecord> activeBreaks = breakRecordRepository.findByAttendance_IdAndBreakEndIsNull(todayAttendance.getId());
                if (!activeBreaks.isEmpty()) {
                    stats.setCurrentStatus("休憩中");
                } else if (todayAttendance.getCheckIn() != null) {
                    stats.setCurrentStatus("勤務中");
                }
            }
        } else {
            stats.setCurrentStatus("未出勤");
        }

        // 今週の勤怠
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY));
        List<Attendance> weekAttendances = attendanceRepository.findByUser_IdAndCheckInBetweenOrderByCheckInDesc(
                userId, startOfWeek.atStartOfDay(), endOfWeek.atTime(23, 59, 59));

        double weekWorkHours = 0;
        for (Attendance attendance : weekAttendances) {
            weekWorkHours += calculateWorkHours(attendance);
        }
        stats.setWeekWorkHours(weekWorkHours);

        // 今月の勤怠
        LocalDate startOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        List<Attendance> monthAttendances = attendanceRepository.findByUser_IdAndCheckInBetweenOrderByCheckInDesc(
                userId, startOfMonth.atStartOfDay(), endOfMonth.atTime(23, 59, 59));

        double monthWorkHours = 0;
        double monthOvertimeHours = 0;
        int workDays = monthAttendances.size();

        for (Attendance attendance : monthAttendances) {
            double workHours = calculateWorkHours(attendance);
            monthWorkHours += workHours;

            // 残業時間（8時間超過分）
            if (workHours > 8) {
                monthOvertimeHours += (workHours - 8);
            }
        }

        stats.setMonthWorkHours(monthWorkHours);
        stats.setMonthOvertimeHours(monthOvertimeHours);
        stats.setMonthWorkDays(workDays);

        return stats;
    }

    /**
     * 勤務時間を計算（時間単位）
     */
    private double calculateWorkHours(Attendance attendance) {
        if (attendance.getCheckIn() == null) {
            return 0;
        }

        LocalDateTime checkOut = attendance.getCheckOut();
        if (checkOut == null) {
            checkOut = LocalDateTime.now(); // 未退勤の場合は現在時刻を使用
        }

        long workMinutes = Duration.between(attendance.getCheckIn(), checkOut).toMinutes();

        // 休憩時間を引く（BreakRecordを使用）
        List<BreakRecord> breakRecords = breakRecordRepository.findByAttendance_IdOrderByBreakStartAsc(attendance.getId());
        long totalBreakMinutes = breakRecords.stream()
                .filter(br -> br.getBreakEnd() != null)
                .mapToLong(BreakRecord::getBreakMinutes)
                .sum();
        workMinutes -= totalBreakMinutes;

        return workMinutes / 60.0;
    }
}
