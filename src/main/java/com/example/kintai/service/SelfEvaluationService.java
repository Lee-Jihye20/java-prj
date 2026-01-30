package com.example.kintai.service;

import com.example.kintai.entity.SelfEvaluation;
import com.example.kintai.entity.User;
import com.example.kintai.repository.SelfEvaluationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class SelfEvaluationService {

    @Autowired
    private SelfEvaluationRepository selfEvaluationRepository;

    /**
     * 自己評価を取得または作成
     */
    @Transactional(readOnly = true)
    public Optional<SelfEvaluation> getSelfEvaluation(Long employeeId, YearMonth yearMonth) {
        LocalDate monthStart = yearMonth.atDay(1);
        return selfEvaluationRepository.findByEmployeeIdAndYearMonth(employeeId, monthStart);
    }

    /**
     * 自己評価を保存
     */
    @Transactional
    public SelfEvaluation saveSelfEvaluation(Long employeeId, YearMonth yearMonth, String rating, String comment) {
        LocalDate monthStart = yearMonth.atDay(1);
        
        Optional<SelfEvaluation> existing = selfEvaluationRepository.findByEmployeeIdAndYearMonth(employeeId, monthStart);
        
        SelfEvaluation evaluation;
        if (existing.isPresent()) {
            evaluation = existing.get();
        } else {
            evaluation = new SelfEvaluation();
            User employee = new User();
            employee.setId(employeeId);
            evaluation.setEmployee(employee);
            evaluation.setYearMonth(monthStart);
        }
        
        evaluation.setRating(rating);
        evaluation.setComment(comment);
        
        return selfEvaluationRepository.save(evaluation);
    }

    /**
     * 従業員の過去の自己評価一覧を取得
     */
    @Transactional(readOnly = true)
    public List<SelfEvaluation> getEmployeeSelfEvaluations(Long employeeId) {
        return selfEvaluationRepository.findByEmployeeIdOrderByYearMonthDesc(employeeId);
    }
}
