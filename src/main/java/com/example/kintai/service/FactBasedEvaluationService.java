package com.example.kintai.service;

import com.example.kintai.entity.*;
import com.example.kintai.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class FactBasedEvaluationService {

    @Autowired
    private FactBasedEvaluationRepository factBasedEvaluationRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private FixRequestRepository fixRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BreakRecordService breakRecordService;

    @Autowired
    private LeaveRecordService leaveRecordService;

    @Transactional
    public FactBasedEvaluation calculateAndSaveMonthlyEvaluation(Long employeeId, YearMonth yearMonth) {
        User employee = userRepository.findById(employeeId).orElse(null);
        if (employee == null) {
            return null;
        }

        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();
        LocalDateTime startDateTime = monthStart.atStartOfDay();
        LocalDateTime endDateTime = monthEnd.atTime(23, 59, 59);

        List<Attendance> attendances = attendanceRepository.findByUser_IdAndCheckInBetweenOrderByCheckInDesc(
                employeeId, startDateTime, endDateTime);

        List<FixRequest> fixRequests = fixRequestRepository.findByUser_IdOrderByCreatedAtDesc(employeeId);
        List<FixRequest> monthFixRequests = fixRequests.stream()
                .filter(fr -> fr.getCreatedAt().isAfter(startDateTime.minusSeconds(1)) && 
                             fr.getCreatedAt().isBefore(endDateTime.plusSeconds(1)))
                .toList();

        int lateCount = calculateLateCount(attendances, employee);
        BigDecimal applicationComplianceRate = calculateApplicationComplianceRate(monthFixRequests);
        int fixRequestCount = monthFixRequests.size();
        int consecutiveWorkDays = calculateConsecutiveWorkDays(attendances, monthStart, monthEnd);
        BigDecimal overtimeAccuracy = calculateOvertimeAccuracy(attendances, employee);

        BigDecimal totalScore = calculateTotalScore(
                lateCount, applicationComplianceRate, fixRequestCount, consecutiveWorkDays, overtimeAccuracy);

        Optional<FactBasedEvaluation> existing = factBasedEvaluationRepository
                .findByEmployeeIdAndYearMonth(employeeId, monthStart);
        
        FactBasedEvaluation evaluation;
        if (existing.isPresent()) {
            evaluation = existing.get();
        } else {
            evaluation = new FactBasedEvaluation();
            evaluation.setEmployee(employee);
            evaluation.setYearMonth(monthStart);
        }

        evaluation.setLateCount(lateCount);
        evaluation.setApplicationComplianceRate(applicationComplianceRate);
        evaluation.setFixRequestCount(fixRequestCount);
        evaluation.setConsecutiveWorkDays(consecutiveWorkDays);
        evaluation.setOvertimeAccuracy(overtimeAccuracy);
        evaluation.setTotalScore(totalScore);

        return factBasedEvaluationRepository.save(evaluation);
    }

    private int calculateLateCount(List<Attendance> attendances, User employee) {
        if (employee.getStartTime() == null) {
            return 0; 
        }

        LocalTime startTime = employee.getStartTime();
        int lateCount = 0;

        for (Attendance attendance : attendances) {
            if (attendance.getCheckIn() != null) {
                LocalTime checkInTime = attendance.getCheckIn().toLocalTime();
                
                if (checkInTime.isAfter(startTime)) {
                    lateCount++;
                }
            }
        }

        return lateCount;
    }

    private BigDecimal calculateApplicationComplianceRate(List<FixRequest> fixRequests) {
        if (fixRequests.isEmpty()) {
            return BigDecimal.valueOf(100.0); 
        }

        long approvedCount = fixRequests.stream()
                .filter(fr -> "APPROVED".equals(fr.getStatus()))
                .count();

        double rate = (double) approvedCount / fixRequests.size() * 100.0;
        return BigDecimal.valueOf(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private int calculateConsecutiveWorkDays(List<Attendance> attendances, LocalDate monthStart, LocalDate monthEnd) {
        if (attendances.isEmpty()) {
            return 0;
        }

        List<LocalDate> workDates = attendances.stream()
                .filter(a -> a.getCheckIn() != null)
                .map(a -> a.getCheckIn().toLocalDate())
                .distinct()
                .sorted()
                .toList();

        if (workDates.isEmpty()) {
            return 0;
        }

        int maxConsecutive = 1;
        int currentConsecutive = 1;
        LocalDate prevDate = workDates.get(workDates.size() - 1);

        for (int i = workDates.size() - 2; i >= 0; i--) {
            LocalDate currentDate = workDates.get(i);
            if (prevDate.minusDays(1).equals(currentDate)) {
                currentConsecutive++;
                maxConsecutive = Math.max(maxConsecutive, currentConsecutive);
            } else {
                currentConsecutive = 1;
            }
            prevDate = currentDate;
        }

        return maxConsecutive;
    }

    private BigDecimal calculateOvertimeAccuracy(List<Attendance> attendances, User employee) {
        if (attendances.isEmpty()) {
            return BigDecimal.valueOf(100.0);
        }

        int validOvertimeDays = 0;
        int totalDays = 0;

        for (Attendance attendance : attendances) {
            if (attendance.getCheckIn() != null && attendance.getCheckOut() != null) {
                totalDays++;
                long minutes = java.time.Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();
                
                long breakMinutes = breakRecordService.getTotalBreakMinutesFromRecordsOnly(attendance);
                minutes -= breakMinutes;
                
                long leaveMinutes = leaveRecordService.getTotalDeductionLeaveMinutes(attendance.getId());
                minutes -= leaveMinutes;
                
                double hours = minutes / 60.0;

                if (hours > 8.0) {
                    
                    validOvertimeDays++;
                }
            }
        }

        if (totalDays == 0) {
            return BigDecimal.valueOf(100.0);
        }

        double accuracy = (double) validOvertimeDays / totalDays * 100.0;
        return BigDecimal.valueOf(accuracy).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTotalScore(int lateCount, BigDecimal applicationComplianceRate,
                                          int fixRequestCount, int consecutiveWorkDays,
                                          BigDecimal overtimeAccuracy) {
        
        BigDecimal lateScore = BigDecimal.valueOf(Math.max(0, 100 - lateCount * 10));

        BigDecimal complianceScore = applicationComplianceRate;

        BigDecimal fixScore = BigDecimal.valueOf(Math.max(0, 100 - fixRequestCount * 5));

        BigDecimal consecutiveScore = BigDecimal.valueOf(Math.min(20, consecutiveWorkDays * 2));

        BigDecimal overtimeScore = overtimeAccuracy;

        BigDecimal total = lateScore
                .add(complianceScore)
                .add(fixScore)
                .add(consecutiveScore)
                .add(overtimeScore);

        return total.divide(BigDecimal.valueOf(5), 2, RoundingMode.HALF_UP);
    }

    @Transactional
    public void calculateAndSaveAllEmployeesMonthlyEvaluation(Long companyId, YearMonth yearMonth) {
        List<User> employees = userRepository.findAllByCompanyId(companyId);
        for (User employee : employees) {
            
            if ("ADMIN".equals(employee.getRole())) {
                continue;
            }
            calculateAndSaveMonthlyEvaluation(employee.getId(), yearMonth);
        }
    }

}
