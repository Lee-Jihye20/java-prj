package com.example.kintai.service;

import com.example.kintai.dto.WeeklyHighlightDTO;
import com.example.kintai.entity.Attendance;
import com.example.kintai.entity.FactBasedEvaluation;
import com.example.kintai.repository.AttendanceRepository;
import com.example.kintai.repository.FactBasedEvaluationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class HighlightService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private BreakRecordService breakRecordService;

    @Autowired
    private LeaveRecordService leaveRecordService;

    @Autowired
    private FactBasedEvaluationRepository factBasedEvaluationRepository;

    public List<WeeklyHighlightDTO> getPreviousMonthHighlights(Long userId) {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        List<WeeklyHighlightDTO> highlights = new ArrayList<>();

        WeeklyHighlightDTO highlight = new WeeklyHighlightDTO(previousMonth);

        LocalDate firstDayOfPreviousMonth = previousMonth.atDay(1);
        LocalDate lastDayOfPreviousMonth = previousMonth.atEndOfMonth();
        LocalDateTime monthStartDateTime = firstDayOfPreviousMonth.atStartOfDay();
        LocalDateTime monthEndDateTime = lastDayOfPreviousMonth.atTime(23, 59, 59);

        List<Attendance> attendances = attendanceRepository.findByUser_IdAndCheckInBetweenOrderByCheckInDesc(
                userId, monthStartDateTime, monthEndDateTime);

        double totalWorkHours = 0.0;
        double totalOvertimeHours = 0.0;

        for (Attendance attendance : attendances) {
            if (attendance.getCheckIn() != null && attendance.getCheckOut() != null) {
                
                long workMinutes = Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();
                workMinutes -= breakRecordService.getTotalBreakMinutesFromRecordsOnly(attendance);
                workMinutes -= leaveRecordService.getTotalDeductionLeaveMinutes(attendance.getId());

                double workHours = workMinutes / 60.0;
                totalWorkHours += workHours;

                if (workHours > 8.0) {
                    totalOvertimeHours += (workHours - 8.0);
                }
            }
        }

        highlight.setWorkHours(totalWorkHours);
        highlight.setOvertimeHours(totalOvertimeHours);

        Optional<FactBasedEvaluation> evaluation = factBasedEvaluationRepository
                .findByEmployeeIdAndYearMonth(userId, firstDayOfPreviousMonth);
        if (evaluation.isPresent()) {
            highlight.setTotalScore(evaluation.get().getTotalScore().doubleValue());
        }

        highlights.add(highlight);

        return highlights;
    }
}
