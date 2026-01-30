package com.example.kintai.repository;

import com.example.kintai.entity.LeaveRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRecordRepository extends JpaRepository<LeaveRecord, Long> {
    List<LeaveRecord> findByAttendance_IdOrderByLeaveStartAsc(Long attendanceId);
    
    List<LeaveRecord> findByAttendance_IdAndLeaveEndIsNull(Long attendanceId);
}
