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

    public MonthlyReportDTO generateMonthlyReport(User user, int year, int month) {
        MonthlyReportDTO report = new MonthlyReportDTO(user.getUsername(), year, month);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Attendance> attendances = attendanceRepository.findByUser_IdAndCheckInBetweenOrderByCheckInDesc(
                user.getId(), startDate, endDate);

        long totalWorkMinutes = 0;
        long totalOvertimeMinutes = 0;
        long totalBreakMinutes = 0;
        int workDays = 0;

        for (Attendance attendance : attendances) {
            if (attendance.getCheckIn() == null) {
                continue;
            }

            workDays++;

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

            MonthlyReportDTO.DailyReportDTO dailyReport = new MonthlyReportDTO.DailyReportDTO(
                    attendance.getCheckIn().toLocalDate(),
                    workMinutes,
                    overtimeMinutes,
                    breakMinutes,
                    attendance.getStatus()
            );
            report.getDailyReports().add(dailyReport);
        }

        report.setWorkDays(workDays);
        report.setTotalWorkMinutes(totalWorkMinutes);
        report.setTotalOvertimeMinutes(totalOvertimeMinutes);
        report.setTotalBreakMinutes(totalBreakMinutes);

        if (workDays > 0) {
            report.setAverageWorkHours((totalWorkMinutes / 60.0) / workDays);
        }

        return report;
    }

    public MonthlyReportDTO generateMonthlyReportForCompany(Long companyId, int year, int month) {
        MonthlyReportDTO report = new MonthlyReportDTO("全従業員", year, month);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Long> userIdsInCompany = userRepository.findAllByCompanyId(companyId)
                .stream()
                .map(User::getId)
                .toList();

        List<Attendance> allAttendances = attendanceRepository.findAllByUser_CompanyId(companyId).stream()
                .filter(a -> a.getCheckIn() != null)
                .filter(a -> userIdsInCompany.contains(a.getUserId())) 
                .filter(a -> !a.getCheckIn().isBefore(startDate) && !a.getCheckIn().isAfter(endDate))
                .toList();

        long totalWorkMinutes = 0;
        long totalOvertimeMinutes = 0;
        long totalBreakMinutes = 0;
        int workDays = 0; 

        for (Attendance attendance : allAttendances) {
            if (attendance.getCheckIn() == null) {
                continue;
            }
            workDays++; 

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

            MonthlyReportDTO.DailyReportDTO dailyReport = new MonthlyReportDTO.DailyReportDTO(
                    attendance.getCheckIn().toLocalDate(),
                    workMinutes,
                    overtimeMinutes,
                    breakMinutes,
                    attendance.getStatus()
            );
            report.getDailyReports().add(dailyReport);
        }

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
