package com.example.kintai.repository;

import com.example.kintai.entity.FactBasedEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FactBasedEvaluationRepository extends JpaRepository<FactBasedEvaluation, Long> {
    @Query("SELECT e FROM FactBasedEvaluation e WHERE e.employee.id = :employeeId AND e.yearMonth = :yearMonth")
    Optional<FactBasedEvaluation> findByEmployeeIdAndYearMonth(@Param("employeeId") Long employeeId, @Param("yearMonth") LocalDate yearMonth);

    @Query("SELECT e FROM FactBasedEvaluation e WHERE e.employee.id = :employeeId ORDER BY e.yearMonth DESC")
    List<FactBasedEvaluation> findByEmployeeIdOrderByYearMonthDesc(@Param("employeeId") Long employeeId);

    @Query("SELECT e FROM FactBasedEvaluation e WHERE e.employee.company.id = :companyId ORDER BY e.yearMonth DESC, e.employee.username")
    List<FactBasedEvaluation> findByCompanyIdOrderByYearMonthDesc(@Param("companyId") Long companyId);
}
