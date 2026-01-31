package com.example.kintai.service;

import com.example.kintai.dto.EvaluationTrendDTO;
import com.example.kintai.repository.FactBasedEvaluationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class EvaluationTrendService {

    @Autowired
    private FactBasedEvaluationRepository factBasedEvaluationRepository;

    @Transactional(readOnly = true)
    public List<EvaluationTrendDTO> getEmployeeTrend(Long employeeId, int months) {
        List<EvaluationTrendDTO> trends = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();

        for (int i = 0; i < months; i++) {
            YearMonth targetMonth = currentMonth.minusMonths(i);
            LocalDate monthStart = targetMonth.atDay(1);

            EvaluationTrendDTO trend = new EvaluationTrendDTO(monthStart);

            factBasedEvaluationRepository.findByEmployeeIdAndYearMonth(employeeId, monthStart)
                    .ifPresent(eval -> {
                        trend.setTotalScore(eval.getTotalScore());
                        trend.setLateCount(eval.getLateCount());
                        trend.setApplicationComplianceRate(eval.getApplicationComplianceRate());
                        trend.setFixRequestCount(eval.getFixRequestCount());
                        trend.setConsecutiveWorkDays(eval.getConsecutiveWorkDays());
                        trend.setOvertimeAccuracy(eval.getOvertimeAccuracy());
                    });

            trends.add(trend);
        }

        return trends;
    }

    public String analyzeTrend(List<EvaluationTrendDTO> trends) {
        if (trends.size() < 2) {
            return "データ不足";
        }

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
