package com.example.kintai.service;

import com.example.kintai.entity.Company;
import com.example.kintai.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
public class EvaluationSchedulerService {

    @Autowired
    private FactBasedEvaluationService factBasedEvaluationService;

    @Autowired
    private CompanyRepository companyRepository;

    @Scheduled(cron = "0 0 2 1 * ?")
    @Transactional
    public void calculatePreviousMonthEvaluations() {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        
        List<Company> companies = companyRepository.findAll();
        
        for (Company company : companies) {
            
            factBasedEvaluationService.calculateAndSaveAllEmployeesMonthlyEvaluation(
                    company.getId(), previousMonth);
        }
    }

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void recalculateCurrentMonthEvaluations() {
        YearMonth currentMonth = YearMonth.now();
        
        List<Company> companies = companyRepository.findAll();
        
        for (Company company : companies) {
            
            factBasedEvaluationService.calculateAndSaveAllEmployeesMonthlyEvaluation(
                    company.getId(), currentMonth);
        }
    }
}
