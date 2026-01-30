package com.example.kintai.repository;

import com.example.kintai.entity.SelfEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SelfEvaluationRepository extends JpaRepository<SelfEvaluation, Long> {
    @Query("SELECT e FROM SelfEvaluation e WHERE e.employee.id = :employeeId AND e.yearMonth = :yearMonth")
    Optional<SelfEvaluation> findByEmployeeIdAndYearMonth(@Param("employeeId") Long employeeId, @Param("yearMonth") LocalDate yearMonth);

    @Query("SELECT e FROM SelfEvaluation e WHERE e.employee.id = :employeeId ORDER BY e.yearMonth DESC")
    List<SelfEvaluation> findByEmployeeIdOrderByYearMonthDesc(@Param("employeeId") Long employeeId);

    @Query("SELECT e FROM SelfEvaluation e WHERE e.employee.company.id = :companyId ORDER BY e.yearMonth DESC, e.employee.username")
    List<SelfEvaluation> findByCompanyIdOrderByYearMonthDesc(@Param("companyId") Long companyId);
}
