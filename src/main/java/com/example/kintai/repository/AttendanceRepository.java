package com.example.kintai.repository;

import com.example.kintai.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByUser_IdOrderByCheckInDesc(Long userId);

    List<Attendance> findByUser_IdAndCheckInBetweenOrderByCheckInDesc(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    Optional<Attendance> findByUser_IdAndCheckOutIsNullOrderByCheckInDesc(Long userId);

    List<Attendance> findByCheckOutIsNull();

    List<Attendance> findAllByUser_CompanyId(Long companyId);
}
