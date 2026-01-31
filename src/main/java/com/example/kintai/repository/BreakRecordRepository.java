package com.example.kintai.repository;

import com.example.kintai.entity.BreakRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BreakRecordRepository extends JpaRepository<BreakRecord, Long> {
    List<BreakRecord> findByAttendance_IdOrderByBreakStartAsc(Long attendanceId);
    
    List<BreakRecord> findByAttendance_IdAndBreakEndIsNull(Long attendanceId);
}
