package com.example.kintai.service;

import com.example.kintai.dto.EvaluationTrendDTO;
import com.example.kintai.entity.*;
import com.example.kintai.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EvaluationTrendService {

    @Autowired
    private FactBasedEvaluationRepository factBasedEvaluationRepository;

    @Autowired
    private SelfEvaluationRepository selfEvaluationRepository;

    @Autowired
    private WeeklyEvaluationRepository weeklyEvaluationRepository;

    /**
     * 従業員の月別評価トレンドを取得
     */
    @Transactional(readOnly = true)
    public List<EvaluationTrendDTO> getEmployeeTrend(Long employeeId, int months) {
        List<EvaluationTrendDTO> trends = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();

        for (int i = 0; i < months; i++) {
            YearMonth targetMonth = currentMonth.minusMonths(i);
            LocalDate monthStart = targetMonth.atDay(1);

            EvaluationTrendDTO trend = new EvaluationTrendDTO(monthStart);

            // 事実ベース評価を取得
            factBasedEvaluationRepository.findByEmployeeIdAndYearMonth(employeeId, monthStart)
                    .ifPresent(eval -> {
                        trend.setTotalScore(eval.getTotalScore());
                        trend.setLateCount(eval.getLateCount());
                        trend.setApplicationComplianceRate(eval.getApplicationComplianceRate());
                        trend.setFixRequestCount(eval.getFixRequestCount());
                        trend.setConsecutiveWorkDays(eval.getConsecutiveWorkDays());
                        trend.setOvertimeAccuracy(eval.getOvertimeAccuracy());
                    });

            // 自己評価を取得
            selfEvaluationRepository.findByEmployeeIdAndYearMonth(employeeId, monthStart)
                    .ifPresent(self -> trend.setSelfRating(self.getRating()));

            // 管理者評価を取得（その月の週次評価から平均を計算）
            List<WeeklyEvaluation> weeklyEvals = weeklyEvaluationRepository
                    .findByEmployeeIdOrderByWeekStartDateDesc(employeeId);
            
            List<WeeklyEvaluation> monthEvals = weeklyEvals.stream()
                    .filter(we -> {
                        LocalDate weekStart = we.getWeekStartDate();
                        return weekStart.getYear() == targetMonth.getYear() &&
                               weekStart.getMonth() == targetMonth.getMonth();
                    })
                    .toList();

            if (!monthEvals.isEmpty()) {
                // 週次評価の平均を計算（簡易版：最も多い評価を使用）
                Map<String, Long> ratingCount = monthEvals.stream()
                        .collect(Collectors.groupingBy(WeeklyEvaluation::getRating, Collectors.counting()));
                
                String mostCommonRating = ratingCount.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(null);
                
                trend.setAdminRating(mostCommonRating);
            }

            trends.add(trend);
        }

        return trends;
    }

    /**
     * トレンドの改善・悪化を判定
     */
    public String analyzeTrend(List<EvaluationTrendDTO> trends) {
        if (trends.size() < 2) {
            return "データ不足";
        }

        // 最新のスコアと過去のスコアを比較
        EvaluationTrendDTO latest = trends.get(0);
        EvaluationTrendDTO previous = trends.get(1);

        if (latest.getTotalScore() == null || previous.getTotalScore() == null) {
            return "データ不足";
        }

        double latestScore = latest.getTotalScore().doubleValue();
        double previousScore = previous.getTotalScore().doubleValue();
        double difference = latestScore - previousScore;

        if (difference > 5) {
            return "改善";
        } else if (difference < -5) {
            return "悪化";
        } else {
            return "横ばい";
        }
    }
}
