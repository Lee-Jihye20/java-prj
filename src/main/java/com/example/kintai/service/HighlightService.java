package com.example.kintai.service;

import com.example.kintai.dto.WeeklyHighlightDTO;
import com.example.kintai.entity.Attendance;
import com.example.kintai.entity.BreakRecord;
import com.example.kintai.entity.LeaveRecord;
import com.example.kintai.entity.WeeklyEvaluation;
import com.example.kintai.repository.AttendanceRepository;
import com.example.kintai.repository.BreakRecordRepository;
import com.example.kintai.repository.LeaveRecordRepository;
import com.example.kintai.repository.WeeklyEvaluationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class HighlightService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private BreakRecordRepository breakRecordRepository;

    @Autowired
    private LeaveRecordRepository leaveRecordRepository;

    @Autowired
    private WeeklyEvaluationRepository evaluationRepository;

    /**
     * 週の開始日を取得（月曜日）
     */
    private LocalDate getWeekStartDate(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        int daysToSubtract = dayOfWeek.getValue() - DayOfWeek.MONDAY.getValue();
        if (daysToSubtract < 0) {
            daysToSubtract += 7;
        }
        return date.minusDays(daysToSubtract);
    }

    /**
     * 週の終了日を取得（日曜日）
     */
    private LocalDate getWeekEndDate(LocalDate date) {
        LocalDate weekStart = getWeekStartDate(date);
        return weekStart.plusDays(6);
    }

    /**
     * 前月の週ごとのハイライトデータを取得
     */
    public List<WeeklyHighlightDTO> getPreviousMonthHighlights(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfCurrentMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfPreviousMonth = firstDayOfCurrentMonth.minusDays(1);
        LocalDate firstDayOfPreviousMonth = lastDayOfPreviousMonth.withDayOfMonth(1);

        List<WeeklyHighlightDTO> highlights = new ArrayList<>();

        // 前月の各週を処理
        LocalDate currentDate = firstDayOfPreviousMonth;
        while (!currentDate.isAfter(lastDayOfPreviousMonth)) {
            LocalDate calculatedWeekStart = getWeekStartDate(currentDate);
            LocalDate calculatedWeekEnd = getWeekEndDate(currentDate);

            // 前月の範囲内の週のみを処理
            final LocalDate weekStart = calculatedWeekStart.isBefore(firstDayOfPreviousMonth) 
                    ? firstDayOfPreviousMonth : calculatedWeekStart;
            final LocalDate weekEnd = calculatedWeekEnd.isAfter(lastDayOfPreviousMonth) 
                    ? lastDayOfPreviousMonth : calculatedWeekEnd;

            // 既に処理した週をスキップ
            boolean alreadyProcessed = highlights.stream()
                    .anyMatch(h -> h.getWeekStartDate().equals(weekStart));
            if (alreadyProcessed) {
                currentDate = weekEnd.plusDays(1);
                continue;
            }

            WeeklyHighlightDTO highlight = new WeeklyHighlightDTO(weekStart, weekEnd);

            // 評価を取得
            Optional<WeeklyEvaluation> evaluation = evaluationRepository.findByEmployeeIdAndWeekStartDate(
                    userId, weekStart);
            if (evaluation.isPresent()) {
                highlight.setRating(evaluation.get().getRating());
            }

            // 勤務時間と残業時間を計算
            LocalDateTime weekStartDateTime = weekStart.atStartOfDay();
            LocalDateTime weekEndDateTime = weekEnd.atTime(23, 59, 59);
            List<Attendance> attendances = attendanceRepository.findByUser_IdAndCheckInBetweenOrderByCheckInDesc(
                    userId, weekStartDateTime, weekEndDateTime);

            double totalWorkHours = 0.0;
            double totalOvertimeHours = 0.0;

            for (Attendance attendance : attendances) {
                if (attendance.getCheckIn() != null && attendance.getCheckOut() != null) {
                    // 勤務時間を計算
                    long workMinutes = Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();

                    // 休憩時間を引く
                    List<BreakRecord> breakRecords = breakRecordRepository.findByAttendance_IdOrderByBreakStartAsc(attendance.getId());
                    long totalBreakMinutes = breakRecords.stream()
                            .filter(br -> br.getBreakEnd() != null)
                            .mapToLong(BreakRecord::getBreakMinutes)
                            .sum();
                    workMinutes -= totalBreakMinutes;

                    // 中抜け時間を引く（控除の場合）
                    List<LeaveRecord> leaveRecords = leaveRecordRepository.findByAttendance_IdOrderByLeaveStartAsc(attendance.getId());
                    long totalLeaveMinutes = leaveRecords.stream()
                            .filter(lr -> lr.getLeaveEnd() != null && "DEDUCTION".equals(lr.getLeaveType()))
                            .mapToLong(lr -> Duration.between(lr.getLeaveStart(), lr.getLeaveEnd()).toMinutes())
                            .sum();
                    workMinutes -= totalLeaveMinutes;

                    double workHours = workMinutes / 60.0;
                    totalWorkHours += workHours;

                    // 残業時間を計算（1日8時間を超える分）
                    if (workHours > 8.0) {
                        totalOvertimeHours += (workHours - 8.0);
                    }
                }
            }

            highlight.setWorkHours(totalWorkHours);
            highlight.setOvertimeHours(totalOvertimeHours);

            highlights.add(highlight);

            // 次の週へ
            currentDate = weekEnd.plusDays(1);
        }

        return highlights;
    }
}
