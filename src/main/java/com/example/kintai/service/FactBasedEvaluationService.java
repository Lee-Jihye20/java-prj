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

    /**
     * 月別の事実ベース評価を計算・保存
     */
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

        // その月の勤怠記録を取得
        List<Attendance> attendances = attendanceRepository.findByUser_IdAndCheckInBetweenOrderByCheckInDesc(
                employeeId, startDateTime, endDateTime);

        // その月の修正依頼を取得
        List<FixRequest> fixRequests = fixRequestRepository.findByUser_IdOrderByCreatedAtDesc(employeeId);
        List<FixRequest> monthFixRequests = fixRequests.stream()
                .filter(fr -> fr.getCreatedAt().isAfter(startDateTime.minusSeconds(1)) && 
                             fr.getCreatedAt().isBefore(endDateTime.plusSeconds(1)))
                .toList();

        // 各指標を計算
        int lateCount = calculateLateCount(attendances, employee);
        BigDecimal applicationComplianceRate = calculateApplicationComplianceRate(monthFixRequests);
        int fixRequestCount = monthFixRequests.size();
        int consecutiveWorkDays = calculateConsecutiveWorkDays(attendances, monthStart, monthEnd);
        BigDecimal overtimeAccuracy = calculateOvertimeAccuracy(attendances, employee);

        // 総合スコアを計算（各指標を100点満点で正規化）
        BigDecimal totalScore = calculateTotalScore(
                lateCount, applicationComplianceRate, fixRequestCount, consecutiveWorkDays, overtimeAccuracy);

        // 既存の評価を取得または新規作成
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

        // 値を設定
        evaluation.setLateCount(lateCount);
        evaluation.setApplicationComplianceRate(applicationComplianceRate);
        evaluation.setFixRequestCount(fixRequestCount);
        evaluation.setConsecutiveWorkDays(consecutiveWorkDays);
        evaluation.setOvertimeAccuracy(overtimeAccuracy);
        evaluation.setTotalScore(totalScore);

        return factBasedEvaluationRepository.save(evaluation);
    }

    /**
     * 遅刻回数を計算
     */
    private int calculateLateCount(List<Attendance> attendances, User employee) {
        if (employee.getStartTime() == null) {
            return 0; // 始業時間が設定されていない場合は遅刻なし
        }

        LocalTime startTime = employee.getStartTime();
        int lateCount = 0;

        for (Attendance attendance : attendances) {
            if (attendance.getCheckIn() != null) {
                LocalTime checkInTime = attendance.getCheckIn().toLocalTime();
                // 始業時間より遅い場合は遅刻
                if (checkInTime.isAfter(startTime)) {
                    lateCount++;
                }
            }
        }

        return lateCount;
    }

    /**
     * 申請遵守率を計算（修正依頼の承認率）
     */
    private BigDecimal calculateApplicationComplianceRate(List<FixRequest> fixRequests) {
        if (fixRequests.isEmpty()) {
            return BigDecimal.valueOf(100.0); // 申請がない場合は100%
        }

        long approvedCount = fixRequests.stream()
                .filter(fr -> "APPROVED".equals(fr.getStatus()))
                .count();

        double rate = (double) approvedCount / fixRequests.size() * 100.0;
        return BigDecimal.valueOf(rate).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 連続勤務日数を計算
     */
    private int calculateConsecutiveWorkDays(List<Attendance> attendances, LocalDate monthStart, LocalDate monthEnd) {
        if (attendances.isEmpty()) {
            return 0;
        }

        // 月内の出勤日を取得
        List<LocalDate> workDates = attendances.stream()
                .filter(a -> a.getCheckIn() != null)
                .map(a -> a.getCheckIn().toLocalDate())
                .distinct()
                .sorted()
                .toList();

        if (workDates.isEmpty()) {
            return 0;
        }

        // 連続勤務日数を計算（月末から逆算）
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

    /**
     * 残業申請の正確性を計算（残業時間の申請と実際の比較）
     * 簡易版：残業時間が適切に記録されているかを確認
     */
    private BigDecimal calculateOvertimeAccuracy(List<Attendance> attendances, User employee) {
        if (attendances.isEmpty()) {
            return BigDecimal.valueOf(100.0);
        }

        // 1日8時間を超える勤務時間を残業とみなす
        int validOvertimeDays = 0;
        int totalDays = 0;

        for (Attendance attendance : attendances) {
            if (attendance.getCheckIn() != null && attendance.getCheckOut() != null) {
                totalDays++;
                long minutes = java.time.Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();
                double hours = minutes / 60.0;

                // 休憩時間を考慮（簡易版：1時間と仮定）
                hours -= 1.0;

                if (hours > 8.0) {
                    // 残業がある場合、適切に記録されているか確認
                    // ここでは簡易的に、check_outが記録されていれば正確とみなす
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

    /**
     * 総合スコアを計算
     */
    private BigDecimal calculateTotalScore(int lateCount, BigDecimal applicationComplianceRate,
                                          int fixRequestCount, int consecutiveWorkDays,
                                          BigDecimal overtimeAccuracy) {
        // 各指標を100点満点で正規化
        // 遅刻回数：0回=100点、1回=90点、2回=80点...（10点減点）
        BigDecimal lateScore = BigDecimal.valueOf(Math.max(0, 100 - lateCount * 10));

        // 申請遵守率：そのまま使用
        BigDecimal complianceScore = applicationComplianceRate;

        // 打刻修正回数：0回=100点、1回=95点、2回=90点...（5点減点）
        BigDecimal fixScore = BigDecimal.valueOf(Math.max(0, 100 - fixRequestCount * 5));

        // 連続勤務日数：日数に応じて加点（最大20点）
        BigDecimal consecutiveScore = BigDecimal.valueOf(Math.min(20, consecutiveWorkDays * 2));

        // 残業申請の正確性：そのまま使用
        BigDecimal overtimeScore = overtimeAccuracy;

        // 重み付け平均（簡易版：等重み）
        BigDecimal total = lateScore
                .add(complianceScore)
                .add(fixScore)
                .add(consecutiveScore)
                .add(overtimeScore);

        return total.divide(BigDecimal.valueOf(5), 2, RoundingMode.HALF_UP);
    }
}
