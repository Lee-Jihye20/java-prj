package com.example.kintai.service;

import com.example.kintai.entity.Company;
import com.example.kintai.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

/**
 * 評価の自動計算スケジューラー
 * 毎月1日の午前2時に前月の評価を自動計算・保存
 */
@Service
public class EvaluationSchedulerService {

    @Autowired
    private FactBasedEvaluationService factBasedEvaluationService;

    @Autowired
    private CompanyRepository companyRepository;

    /**
     * 毎月1日の午前2時に前月の評価を自動計算・保存
     * cron形式: 秒 分 時 日 月 曜日
     * "0 0 2 1 * ?" = 毎月1日の午前2時0分0秒
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    @Transactional
    public void calculatePreviousMonthEvaluations() {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        
        // 全企業を取得
        List<Company> companies = companyRepository.findAll();
        
        for (Company company : companies) {
            // 各企業の全従業員の前月評価を計算・保存
            factBasedEvaluationService.calculateAndSaveAllEmployeesMonthlyEvaluation(
                    company.getId(), previousMonth);
        }
    }

    /**
     * 毎日の午前3時に当月の評価を再計算（データ更新に対応）
     * 開発・テスト用にも使用可能
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void recalculateCurrentMonthEvaluations() {
        YearMonth currentMonth = YearMonth.now();
        
        // 全企業を取得
        List<Company> companies = companyRepository.findAll();
        
        for (Company company : companies) {
            // 各企業の全従業員の当月評価を再計算・保存
            factBasedEvaluationService.calculateAndSaveAllEmployeesMonthlyEvaluation(
                    company.getId(), currentMonth);
        }
    }
}

