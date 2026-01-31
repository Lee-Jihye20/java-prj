package com.example.kintai.service;

import com.example.kintai.entity.Attendance;
import com.example.kintai.entity.BreakRecord;
import com.example.kintai.repository.BreakRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class BreakRecordService {

    @Autowired
    private BreakRecordRepository breakRecordRepository;

    public List<BreakRecord> getBreakRecordsByAttendanceId(Long attendanceId) {
        return breakRecordRepository.findByAttendance_IdOrderByBreakStartAsc(attendanceId);
    }

    public List<BreakRecord> getActiveBreaksByAttendanceId(Long attendanceId) {
        return breakRecordRepository.findByAttendance_IdAndBreakEndIsNull(attendanceId);
    }

    public long getTotalBreakMinutesFromRecordsOnly(Attendance attendance) {
        if (attendance == null || attendance.getId() == null) {
            return 0L;
        }
        List<BreakRecord> breakRecords = breakRecordRepository.findByAttendance_IdOrderByBreakStartAsc(attendance.getId());
        return breakRecords.stream()
                .filter(br -> br.getBreakEnd() != null)
                .mapToLong(BreakRecord::getBreakMinutes)
                .sum();
    }

    public long getTotalBreakMinutes(Attendance attendance) {
        if (attendance == null) {
            return 0L;
        }
        if (attendance.getBreakStart() != null && attendance.getBreakEnd() != null) {
            return Duration.between(attendance.getBreakStart(), attendance.getBreakEnd()).toMinutes();
        }
        return getTotalBreakMinutesFromRecordsOnly(attendance);
    }
}
