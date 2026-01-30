package com.example.kintai.repository;

import com.example.kintai.entity.WeeklyEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyEvaluationRepository extends JpaRepository<WeeklyEvaluation, Long> {
    @Query("SELECT e FROM WeeklyEvaluation e WHERE e.employee.id = :employeeId AND e.evaluator.id = :evaluatorId AND e.weekStartDate = :weekStartDate")
    Optional<WeeklyEvaluation> findByEmployeeIdAndEvaluatorIdAndWeekStartDate(
            @Param("employeeId") Long employeeId, @Param("evaluatorId") Long evaluatorId, @Param("weekStartDate") LocalDate weekStartDate);

    @Query("SELECT e FROM WeeklyEvaluation e WHERE e.employee.id = :employeeId ORDER BY e.weekStartDate DESC")
    List<WeeklyEvaluation> findByEmployeeIdOrderByWeekStartDateDesc(@Param("employeeId") Long employeeId);

    @Query("SELECT e FROM WeeklyEvaluation e WHERE e.evaluator.id = :evaluatorId ORDER BY e.weekStartDate DESC")
    List<WeeklyEvaluation> findByEvaluatorIdOrderByWeekStartDateDesc(@Param("evaluatorId") Long evaluatorId);

    @Query("SELECT e FROM WeeklyEvaluation e WHERE e.employee.company.id = :companyId ORDER BY e.weekStartDate DESC, e.employee.username")
    List<WeeklyEvaluation> findByCompanyIdOrderByWeekStartDateDesc(@Param("companyId") Long companyId);

    @Query("SELECT e FROM WeeklyEvaluation e WHERE e.employee.id = :employeeId AND e.weekStartDate = :weekStartDate")
    Optional<WeeklyEvaluation> findByEmployeeIdAndWeekStartDate(
            @Param("employeeId") Long employeeId, @Param("weekStartDate") LocalDate weekStartDate);

    @Query("SELECT e FROM WeeklyEvaluation e WHERE e.employee.company.id = :companyId " +
           "AND e.weekStartDate >= :monthStart AND e.weekStartDate < :monthEnd " +
           "ORDER BY e.employee.username, e.weekStartDate")
    List<WeeklyEvaluation> findByCompanyIdAndMonth(
            @Param("companyId") Long companyId,
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd);
}
