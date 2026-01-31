package com.example.kintai.service;

import com.example.kintai.entity.Attendance;
import com.example.kintai.entity.BreakRecord;
import com.example.kintai.repository.BreakRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * 休憩時間（BreakRecord）の計算と取得を一括で行うサービス。
 * 休憩合計（分）の算出と、勤怠に紐づく休憩一覧の取得をここに集約する。
 */
@Service
public class BreakRecordService {

    @Autowired
    private BreakRecordRepository breakRecordRepository;

    /**
     * 勤怠に紐づく休憩記録を開始時刻昇順で返す。
     */
    public List<BreakRecord> getBreakRecordsByAttendanceId(Long attendanceId) {
        return breakRecordRepository.findByAttendance_IdOrderByBreakStartAsc(attendanceId);
    }

    /**
     * 終了打刻が未登録の休憩（休憩中）を返す。
     */
    public List<BreakRecord> getActiveBreaksByAttendanceId(Long attendanceId) {
        return breakRecordRepository.findByAttendance_IdAndBreakEndIsNull(attendanceId);
    }

    /**
     * 休憩時間合計（分）。BreakRecord のみで計算。
     * 終了打刻済み（breakEnd != null）のレコードだけを合計。月次レポート・カレンダー・残業超過判定と同じ計算。
     * レコードなし・終了未打刻のみの場合は 0。
     */
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

    /**
     * 休憩時間合計（分）。Attendance.breakStart/breakEnd が両方ある場合はそれを優先し、なければ BreakRecord の合計を使用。
     * 休憩0（レコードなし or 終了未打刻のみ）のときは 0。
     */
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
