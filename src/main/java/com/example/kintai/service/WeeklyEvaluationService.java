package com.example.kintai.service;

import com.example.kintai.dto.MonthlyEvaluationSummaryDTO;
import com.example.kintai.dto.WeeklyEvaluationDTO;
import com.example.kintai.entity.User;
import com.example.kintai.entity.WeeklyEvaluation;
import com.example.kintai.repository.WeeklyEvaluationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WeeklyEvaluationService {

    @Autowired
    private WeeklyEvaluationRepository evaluationRepository;

    /**
     * 週の開始日を取得（月曜日）
     */
    public LocalDate getWeekStartDate(LocalDate date) {
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
    public LocalDate getWeekEndDate(LocalDate date) {
        LocalDate weekStart = getWeekStartDate(date);
        return weekStart.plusDays(6);
    }

    /**
     * 今週の評価を取得または作成
     */
    @Transactional
    public WeeklyEvaluation getOrCreateCurrentWeekEvaluation(Long employeeId, Long evaluatorId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = getWeekStartDate(today);
        
        Optional<WeeklyEvaluation> existing = evaluationRepository.findByEmployeeIdAndEvaluatorIdAndWeekStartDate(
                employeeId, evaluatorId, weekStart);
        
        if (existing.isPresent()) {
            return existing.get();
        }
        
        WeeklyEvaluation evaluation = new WeeklyEvaluation();
        evaluation.setWeekStartDate(weekStart);
        evaluation.setWeekEndDate(getWeekEndDate(today));
        
        User employee = new User();
        employee.setId(employeeId);
        evaluation.setEmployee(employee);
        
        User evaluator = new User();
        evaluator.setId(evaluatorId);
        evaluation.setEvaluator(evaluator);
        
        evaluation.setRating("B"); // デフォルト評価
        
        return evaluationRepository.save(evaluation);
    }

    /**
     * 評価を保存または更新
     */
    @Transactional
    public WeeklyEvaluation saveEvaluation(Long employeeId, Long evaluatorId, LocalDate weekStartDate, 
                                           String rating, String comment) {
        Optional<WeeklyEvaluation> existing = evaluationRepository.findByEmployeeIdAndEvaluatorIdAndWeekStartDate(
                employeeId, evaluatorId, weekStartDate);
        
        WeeklyEvaluation evaluation;
        if (existing.isPresent()) {
            evaluation = existing.get();
        } else {
            evaluation = new WeeklyEvaluation();
            User employee = new User();
            employee.setId(employeeId);
            evaluation.setEmployee(employee);
            
            User evaluator = new User();
            evaluator.setId(evaluatorId);
            evaluation.setEvaluator(evaluator);
            
            evaluation.setWeekStartDate(weekStartDate);
            evaluation.setWeekEndDate(getWeekEndDate(weekStartDate));
        }
        
        evaluation.setRating(rating);
        evaluation.setComment(comment);
        
        return evaluationRepository.save(evaluation);
    }

    /**
     * 従業員の過去の評価一覧を取得
     */
    public List<WeeklyEvaluation> getEmployeeEvaluations(Long employeeId) {
        return evaluationRepository.findByEmployeeIdOrderByWeekStartDateDesc(employeeId);
    }

    /**
     * 評価者が評価した一覧を取得
     */
    public List<WeeklyEvaluation> getEvaluatorEvaluations(Long evaluatorId) {
        return evaluationRepository.findByEvaluatorIdOrderByWeekStartDateDesc(evaluatorId);
    }

    /**
     * 企業の全評価を取得
     */
    public List<WeeklyEvaluation> getCompanyEvaluations(Long companyId) {
        return evaluationRepository.findByCompanyIdOrderByWeekStartDateDesc(companyId);
    }

    /**
     * 特定の週の評価を取得
     */
    public Optional<WeeklyEvaluation> getEvaluationByEmployeeAndWeek(Long employeeId, LocalDate weekStartDate) {
        return evaluationRepository.findByEmployeeIdAndWeekStartDate(employeeId, weekStartDate);
    }

    /**
     * 月別評価サマリーを取得
     */
    @Transactional(readOnly = true)
    public List<MonthlyEvaluationSummaryDTO> getMonthlyEvaluationSummaries(Long companyId, YearMonth yearMonth) {
        // 月の開始日と終了日を取得
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth().plusDays(1);

        // その月の評価を取得
        List<WeeklyEvaluation> evaluations = evaluationRepository.findByCompanyIdAndMonth(companyId, monthStart, monthEnd);

        // 従業員ごとにグループ化
        Map<Long, List<WeeklyEvaluation>> evaluationsByEmployee = evaluations.stream()
                .collect(Collectors.groupingBy(e -> e.getEmployee().getId()));

        // 従業員情報を取得
        List<User> employees = evaluations.stream()
                .map(WeeklyEvaluation::getEmployee)
                .distinct()
                .collect(Collectors.toList());

        // DTOに変換
        List<MonthlyEvaluationSummaryDTO> summaries = new ArrayList<>();
        for (User employee : employees) {
            if ("ADMIN".equals(employee.getRole())) {
                continue; // 管理者は除外
            }

            MonthlyEvaluationSummaryDTO summary = new MonthlyEvaluationSummaryDTO(
                    employee.getId(),
                    employee.getUsername(),
                    yearMonth
            );

            List<WeeklyEvaluation> employeeEvaluations = evaluationsByEmployee.getOrDefault(employee.getId(), new ArrayList<>());
            List<WeeklyEvaluationDTO> weeklyDTOs = employeeEvaluations.stream()
                    .map(eval -> new WeeklyEvaluationDTO(
                            eval.getWeekStartDate(),
                            eval.getWeekEndDate(),
                            eval.getRating(),
                            eval.getComment(),
                            eval.getEvaluator().getUsername()
                    ))
                    .collect(Collectors.toList());

            summary.setWeeklyEvaluations(weeklyDTOs);
            summaries.add(summary);
        }

        // 従業員名でソート
        summaries.sort((a, b) -> a.getEmployeeName().compareTo(b.getEmployeeName()));

        return summaries;
    }
}
