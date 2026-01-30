package com.example.kintai.service;

import com.example.kintai.dto.AttendanceLogDTO;
import com.example.kintai.entity.Attendance;
import com.example.kintai.entity.BreakRecord;
import com.example.kintai.entity.FixRequest;
import com.example.kintai.entity.LeaveRecord;
import com.example.kintai.entity.User;
import com.example.kintai.repository.AttendanceRepository;
import com.example.kintai.repository.BreakRecordRepository;
import com.example.kintai.repository.FixRequestRepository;
import com.example.kintai.repository.LeaveRecordRepository;
import com.example.kintai.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExportService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private BreakRecordRepository breakRecordRepository;

    @Autowired
    private LeaveRecordRepository leaveRecordRepository;

    @Autowired
    private FixRequestRepository fixRequestRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 日別勤怠データをXLSX形式で出力（1人×1日）
     * 列名: 日付, 出勤, 退勤, 休憩時間, 実働時間, 残業時間, 備考
     */
    public byte[] exportDailyAttendance(Long userId, LocalDate date) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("日別勤怠");

        // ヘッダー行のスタイル
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dateTimeStyle = createDateTimeStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle timeStyle = createTimeStyle(workbook);

        // ヘッダー行を作成
        Row headerRow = sheet.createRow(0);
        String[] headers = {"日付", "出勤", "退勤", "休憩時間", "実働時間", "残業時間", "備考"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // データ行を作成
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        List<Attendance> attendances = attendanceRepository.findByUser_IdAndCheckInBetweenOrderByCheckInDesc(
                userId, startOfDay, endOfDay);

        int rowNum = 1;
        for (Attendance attendance : attendances) {
            Row row = sheet.createRow(rowNum++);

            // 日付（Excel日付型として設定）
            Cell dateCell = row.createCell(0);
            if (attendance.getCheckIn() != null) {
                LocalDate localDate = attendance.getCheckIn().toLocalDate();
                Date dateValue = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                dateCell.setCellValue(dateValue);
                dateCell.setCellStyle(dateStyle);
            }

            // 出勤（Excel日時型として設定）
            Cell checkInCell = row.createCell(1);
            if (attendance.getCheckIn() != null) {
                Date checkInDate = Date.from(attendance.getCheckIn().atZone(ZoneId.systemDefault()).toInstant());
                checkInCell.setCellValue(checkInDate);
                checkInCell.setCellStyle(dateTimeStyle);
            }

            // 退勤（Excel日時型として設定）
            Cell checkOutCell = row.createCell(2);
            if (attendance.getCheckOut() != null) {
                Date checkOutDate = Date.from(attendance.getCheckOut().atZone(ZoneId.systemDefault()).toInstant());
                checkOutCell.setCellValue(checkOutDate);
                checkOutCell.setCellStyle(dateTimeStyle);
            }

            // 休憩時間（分）
            List<BreakRecord> breakRecords = breakRecordRepository.findByAttendance_IdOrderByBreakStartAsc(attendance.getId());
            long totalBreakMinutes = breakRecords.stream()
                    .filter(br -> br.getBreakEnd() != null)
                    .mapToLong(BreakRecord::getBreakMinutes)
                    .sum();
            Cell breakCell = row.createCell(3);
            breakCell.setCellValue(totalBreakMinutes / 60.0); // 時間単位
            breakCell.setCellStyle(timeStyle);

            // 実働時間（時間）
            double workHours = 0.0;
            if (attendance.getCheckIn() != null && attendance.getCheckOut() != null) {
                long workMinutes = Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();
                workMinutes -= totalBreakMinutes;
                workHours = workMinutes / 60.0;
            }
            Cell workCell = row.createCell(4);
            workCell.setCellValue(workHours);
            workCell.setCellStyle(timeStyle);

            // 残業時間（時間、8時間超過分）
            double overtimeHours = 0.0;
            if (workHours > 8.0) {
                overtimeHours = workHours - 8.0;
            }
            Cell overtimeCell = row.createCell(5);
            overtimeCell.setCellValue(overtimeHours);
            overtimeCell.setCellStyle(timeStyle);

            // 備考
            Cell noteCell = row.createCell(6);
            StringBuilder notes = new StringBuilder();
            if (attendance.getCheckOut() == null) {
                notes.append("退勤未打刻; ");
            }
            // 修正依頼の情報を追加
            List<FixRequest> fixRequests = fixRequestRepository.findByUser_IdOrderByCreatedAtDesc(attendance.getUserId());
            fixRequests.stream()
                    .filter(fr -> fr.getAttendanceId().equals(attendance.getId()))
                    .forEach(fr -> {
                        String requestTypeJapanese = convertRequestTypeToJapanese(fr.getRequestType());
                        if ("APPROVED".equals(fr.getStatus())) {
                            notes.append("修正承認済み(").append(requestTypeJapanese).append("); ");
                        } else if ("REJECTED".equals(fr.getStatus())) {
                            notes.append("修正却下(").append(requestTypeJapanese).append("); ");
                        } else if ("PENDING".equals(fr.getStatus())) {
                            notes.append("修正依頼中(").append(requestTypeJapanese).append("); ");
                        }
                    });
            noteCell.setCellValue(notes.toString().trim());
        }

        // 列幅を自動調整
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000); // 少し余裕を持たせる
        }

        // ワークブックをバイト配列に変換
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    /**
     * 月次集計データをXLSX形式で出力（1人×1か月）
     * 列名: ユーザー名, 年月, 勤務日数, 総勤務時間, 総休憩時間, 総残業時間
     */
    public byte[] exportMonthlySummary(Long userId, int year, int month) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("月次集計");

        // スタイル
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);
        CellStyle timeStyle = createTimeStyle(workbook);

        // ヘッダー行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ユーザー名", "年月", "勤務日数", "総勤務時間", "総休憩時間", "総残業時間"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // データ行
        User user = userRepository.findById(userId).orElseThrow();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Attendance> attendances = attendanceRepository.findByUser_IdAndCheckInBetweenOrderByCheckInDesc(
                userId, startDate, endDate);

        Row dataRow = sheet.createRow(1);

        // ユーザー名
        Cell nameCell = dataRow.createCell(0);
        nameCell.setCellValue(user.getUsername());

        // 年月
        Cell yearMonthCell = dataRow.createCell(1);
        yearMonthCell.setCellValue(yearMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月")));

        // 集計計算
        int workDays = 0;
        double totalWorkHours = 0.0;
        double totalBreakHours = 0.0;
        double totalOvertimeHours = 0.0;

        for (Attendance attendance : attendances) {
            if (attendance.getCheckIn() == null) {
                continue;
            }

            workDays++;

            if (attendance.getCheckOut() != null) {
                long workMinutes = Duration.between(attendance.getCheckIn(), attendance.getCheckOut()).toMinutes();

                // 休憩時間を計算
                List<BreakRecord> breakRecords = breakRecordRepository.findByAttendance_IdOrderByBreakStartAsc(attendance.getId());
                long totalBreakMinutes = breakRecords.stream()
                        .filter(br -> br.getBreakEnd() != null)
                        .mapToLong(BreakRecord::getBreakMinutes)
                        .sum();
                workMinutes -= totalBreakMinutes;

                totalWorkHours += workMinutes / 60.0;
                totalBreakHours += totalBreakMinutes / 60.0;

                // 残業時間（8時間超過分）
                if (workMinutes > 480) {
                    totalOvertimeHours += (workMinutes - 480) / 60.0;
                }
            }
        }

        // 勤務日数
        Cell workDaysCell = dataRow.createCell(2);
        workDaysCell.setCellValue(workDays);
        workDaysCell.setCellStyle(numberStyle);

        // 総勤務時間
        Cell totalWorkCell = dataRow.createCell(3);
        totalWorkCell.setCellValue(totalWorkHours);
        totalWorkCell.setCellStyle(timeStyle);

        // 総休憩時間
        Cell totalBreakCell = dataRow.createCell(4);
        totalBreakCell.setCellValue(totalBreakHours);
        totalBreakCell.setCellStyle(timeStyle);

        // 総残業時間
        Cell totalOvertimeCell = dataRow.createCell(5);
        totalOvertimeCell.setCellValue(totalOvertimeHours);
        totalOvertimeCell.setCellStyle(timeStyle);

        // 列幅を自動調整
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }

        // ワークブックをバイト配列に変換
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    /**
     * 打刻ログデータを取得（表示用）
     */
    public List<AttendanceLogDTO> getAttendanceLogs(Long companyId, LocalDate startDate, LocalDate endDate) {
        List<AttendanceLogDTO> logs = new java.util.ArrayList<>();
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        // 全ユーザーを取得
        List<User> users = userRepository.findAllByCompanyId(companyId);
        
        // 出勤・退勤ログ
        List<Attendance> attendances = attendanceRepository.findAllByUser_CompanyId(companyId).stream()
                .filter(a -> {
                    boolean checkInInRange = a.getCheckIn() != null && 
                            !a.getCheckIn().isBefore(startDateTime) && 
                            !a.getCheckIn().isAfter(endDateTime);
                    boolean checkOutInRange = a.getCheckOut() != null && 
                            !a.getCheckOut().isBefore(startDateTime) && 
                            !a.getCheckOut().isAfter(endDateTime);
                    return checkInInRange || checkOutInRange;
                })
                .sorted((a1, a2) -> {
                    LocalDateTime time1 = a1.getCheckIn() != null ? a1.getCheckIn() : 
                            (a1.getCheckOut() != null ? a1.getCheckOut() : LocalDateTime.MIN);
                    LocalDateTime time2 = a2.getCheckIn() != null ? a2.getCheckIn() : 
                            (a2.getCheckOut() != null ? a2.getCheckOut() : LocalDateTime.MIN);
                    return time1.compareTo(time2);
                })
                .collect(Collectors.toList());

        for (Attendance attendance : attendances) {
            User user = users.stream()
                    .filter(u -> u.getId().equals(attendance.getUserId()))
                    .findFirst()
                    .orElse(null);
            if (user == null) continue;

            // 出勤ログ
            if (attendance.getCheckIn() != null) {
                boolean checkInInRange = !attendance.getCheckIn().isBefore(startDateTime) && 
                        !attendance.getCheckIn().isAfter(endDateTime);
                if (checkInInRange) {
                    logs.add(new AttendanceLogDTO(
                        attendance.getCheckIn(),
                        user.getUsername(),
                        "出勤",
                        attendance.getCheckIn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        "自動記録"
                    ));
                }
            }

            // 退勤ログ
            if (attendance.getCheckOut() != null) {
                boolean checkOutInRange = !attendance.getCheckOut().isBefore(startDateTime) && 
                        !attendance.getCheckOut().isAfter(endDateTime);
                if (checkOutInRange) {
                    logs.add(new AttendanceLogDTO(
                        attendance.getCheckOut(),
                        user.getUsername(),
                        "退勤",
                        attendance.getCheckOut().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        "自動記録"
                    ));
                }
            }

            // 休憩ログ
            List<BreakRecord> breakRecords = breakRecordRepository.findByAttendance_IdOrderByBreakStartAsc(attendance.getId());
            for (BreakRecord breakRecord : breakRecords) {
                if (breakRecord.getBreakStart() != null) {
                    boolean breakStartInRange = !breakRecord.getBreakStart().isBefore(startDateTime) && 
                            !breakRecord.getBreakStart().isAfter(endDateTime);
                    if (breakStartInRange) {
                        logs.add(new AttendanceLogDTO(
                            breakRecord.getBreakStart(),
                            user.getUsername(),
                            "休憩開始",
                            breakRecord.getBreakStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                            "自動記録"
                        ));
                    }
                }
                if (breakRecord.getBreakEnd() != null) {
                    boolean breakEndInRange = !breakRecord.getBreakEnd().isBefore(startDateTime) && 
                            !breakRecord.getBreakEnd().isAfter(endDateTime);
                    if (breakEndInRange) {
                        logs.add(new AttendanceLogDTO(
                            breakRecord.getBreakEnd(),
                            user.getUsername(),
                            "休憩終了",
                            breakRecord.getBreakEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                            "自動記録"
                        ));
                    }
                }
            }

            // 中抜けログ
            List<LeaveRecord> leaveRecords = leaveRecordRepository.findByAttendance_IdOrderByLeaveStartAsc(attendance.getId());
            for (LeaveRecord leaveRecord : leaveRecords) {
                if (leaveRecord.getLeaveStart() != null) {
                    boolean leaveStartInRange = !leaveRecord.getLeaveStart().isBefore(startDateTime) && 
                            !leaveRecord.getLeaveStart().isAfter(endDateTime);
                    if (leaveStartInRange) {
                        String leaveType = leaveRecord.getLeaveType() != null ? 
                                (leaveRecord.getLeaveType().equals("DEDUCTION") ? "控除" : 
                                 leaveRecord.getLeaveType().equals("PAID_LEAVE") ? "有給" : "未設定") : "未設定";
                        logs.add(new AttendanceLogDTO(
                            leaveRecord.getLeaveStart(),
                            user.getUsername(),
                            "中抜け開始",
                            leaveRecord.getLeaveStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                            "自動記録（扱い: " + leaveType + "）"
                        ));
                    }
                }
                if (leaveRecord.getLeaveEnd() != null) {
                    boolean leaveEndInRange = !leaveRecord.getLeaveEnd().isBefore(startDateTime) && 
                            !leaveRecord.getLeaveEnd().isAfter(endDateTime);
                    if (leaveEndInRange) {
                        String leaveType = leaveRecord.getLeaveType() != null ? 
                                (leaveRecord.getLeaveType().equals("DEDUCTION") ? "控除" : 
                                 leaveRecord.getLeaveType().equals("PAID_LEAVE") ? "有給" : "未設定") : "未設定";
                        logs.add(new AttendanceLogDTO(
                            leaveRecord.getLeaveEnd(),
                            user.getUsername(),
                            "中抜け終了",
                            leaveRecord.getLeaveEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                            "自動記録（扱い: " + leaveType + "）"
                        ));
                    }
                }
            }
        }

        // 修正依頼ログ
        List<FixRequest> fixRequests = fixRequestRepository.findAllByUser_CompanyId(companyId).stream()
                .filter(fr -> fr.getCreatedAt() != null &&
                        !fr.getCreatedAt().isBefore(startDateTime) &&
                        !fr.getCreatedAt().isAfter(endDateTime))
                .sorted((fr1, fr2) -> fr1.getCreatedAt().compareTo(fr2.getCreatedAt()))
                .collect(Collectors.toList());

        for (FixRequest fixRequest : fixRequests) {
            User user = users.stream()
                    .filter(u -> u.getId().equals(fixRequest.getUserId()))
                    .findFirst()
                    .orElse(null);
            if (user == null) continue;

            String value;
            if ("LEAVE_TYPE".equals(fixRequest.getRequestType())) {
                value = fixRequest.getNewLeaveType() != null ? 
                        ("DEDUCTION".equals(fixRequest.getNewLeaveType()) ? "控除" : "有給") : "未設定";
            } else if (fixRequest.getNewValue() != null) {
                value = fixRequest.getNewValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } else {
                value = "-";
            }

            logs.add(new AttendanceLogDTO(
                fixRequest.getCreatedAt(),
                user.getUsername(),
                "修正依頼",
                value,
                "依頼ID: " + fixRequest.getId() + ", 理由: " + fixRequest.getReason() + ", 種別: " + convertRequestTypeToJapanese(fixRequest.getRequestType())
            ));

            // 承認/却下ログ
            if ("APPROVED".equals(fixRequest.getStatus()) || "REJECTED".equals(fixRequest.getStatus())) {
                // 承認者のユーザー名を取得
                String approverName = "管理者";
                if (fixRequest.getApprovedBy() != null) {
                    approverName = fixRequest.getApprovedBy().getUsername();
                } else {
                    // 承認者が記録されていない場合は、ユーザーリストから探す
                    User approver = users.stream()
                            .filter(u -> u.getId().equals(fixRequest.getApprovedByUserId()))
                            .findFirst()
                            .orElse(null);
                    if (approver != null) {
                        approverName = approver.getUsername();
                    }
                }
                
                logs.add(new AttendanceLogDTO(
                    fixRequest.getCreatedAt(),
                    approverName,
                    "APPROVED".equals(fixRequest.getStatus()) ? "修正承認" : "修正却下",
                    "-",
                    "依頼ID: " + fixRequest.getId()
                ));
            }
        }

        // タイムスタンプでソート
        logs.sort((l1, l2) -> l1.getTimestamp().compareTo(l2.getTimestamp()));
        
        return logs;
    }

    /**
     * 打刻ログをXLSX形式で出力（証跡用、改ざん不可）
     * 列名: 日時, ユーザー名, 操作種別, 値, 備考
     */
    public byte[] exportAttendanceLog(Long companyId, LocalDate startDate, LocalDate endDate) throws IOException {
        return exportAttendanceLog(companyId, startDate, endDate, null, null);
    }

    /**
     * 打刻ログをXLSX形式で出力（フィルター対応版）
     * 列名: 日時, ユーザー名, 操作種別, 値, 備考
     */
    public byte[] exportAttendanceLog(Long companyId, LocalDate startDate, LocalDate endDate, String username, String operationType) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("打刻ログ");

        // スタイル
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dateTimeStyle = createDateTimeStyle(workbook);

        // ヘッダー行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"日時", "ユーザー名", "操作種別", "値", "備考"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 全ユーザーを取得
        List<User> users = userRepository.findAllByCompanyId(companyId);
        int rowNum = 1;

        // 出勤・退勤ログ
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        // 出勤または退勤のいずれかが期間内にある勤怠記録を取得
        List<Attendance> attendances = attendanceRepository.findAllByUser_CompanyId(companyId).stream()
                .filter(a -> {
                    // 出勤が期間内にあるか
                    boolean checkInInRange = a.getCheckIn() != null && 
                            !a.getCheckIn().isBefore(startDateTime) && 
                            !a.getCheckIn().isAfter(endDateTime);
                    // 退勤が期間内にあるか
                    boolean checkOutInRange = a.getCheckOut() != null && 
                            !a.getCheckOut().isBefore(startDateTime) && 
                            !a.getCheckOut().isAfter(endDateTime);
                    // 出勤または退勤のいずれかが期間内にあれば対象
                    return checkInInRange || checkOutInRange;
                })
                .sorted((a1, a2) -> {
                    // ソート用の時刻を決定（出勤時刻を優先、なければ退勤時刻）
                    LocalDateTime time1 = a1.getCheckIn() != null ? a1.getCheckIn() : 
                            (a1.getCheckOut() != null ? a1.getCheckOut() : LocalDateTime.MIN);
                    LocalDateTime time2 = a2.getCheckIn() != null ? a2.getCheckIn() : 
                            (a2.getCheckOut() != null ? a2.getCheckOut() : LocalDateTime.MIN);
                    return time1.compareTo(time2);
                })
                .collect(Collectors.toList());

        for (Attendance attendance : attendances) {
            User user = users.stream()
                    .filter(u -> u.getId().equals(attendance.getUserId()))
                    .findFirst()
                    .orElse(null);
            if (user == null) continue;

            // ユーザーフィルター
            if (username != null && !username.isEmpty() && !user.getUsername().equals(username)) {
                continue;
            }

            // 出勤ログ（出勤時刻が期間内にある場合のみ表示）
            if (attendance.getCheckIn() != null) {
                boolean checkInInRange = !attendance.getCheckIn().isBefore(startDateTime) && 
                        !attendance.getCheckIn().isAfter(endDateTime);
                if (checkInInRange) {
                    // 操作種別フィルター
                    if (operationType == null || operationType.isEmpty() || "出勤".equals(operationType)) {
                        Row row = sheet.createRow(rowNum++);
                        Cell dateCell = row.createCell(0);
                        dateCell.setCellValue(attendance.getCheckIn());
                        dateCell.setCellStyle(dateTimeStyle);

                        Cell userCell = row.createCell(1);
                        userCell.setCellValue(user.getUsername());

                        Cell typeCell = row.createCell(2);
                        typeCell.setCellValue("出勤");

                        Cell valueCell = row.createCell(3);
                        valueCell.setCellValue(attendance.getCheckIn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                        Cell noteCell = row.createCell(4);
                        noteCell.setCellValue("自動記録");
                    }
                }
            }

            // 退勤ログ（退勤時刻が期間内にある場合のみ表示）
            if (attendance.getCheckOut() != null) {
                boolean checkOutInRange = !attendance.getCheckOut().isBefore(startDateTime) && 
                        !attendance.getCheckOut().isAfter(endDateTime);
                if (checkOutInRange) {
                    // 操作種別フィルター
                    if (operationType == null || operationType.isEmpty() || "退勤".equals(operationType)) {
                        Row row = sheet.createRow(rowNum++);
                        Cell dateCell = row.createCell(0);
                        dateCell.setCellValue(attendance.getCheckOut());
                        dateCell.setCellStyle(dateTimeStyle);

                        Cell userCell = row.createCell(1);
                        userCell.setCellValue(user.getUsername());

                        Cell typeCell = row.createCell(2);
                        typeCell.setCellValue("退勤");

                        Cell valueCell = row.createCell(3);
                        valueCell.setCellValue(attendance.getCheckOut().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                        Cell noteCell = row.createCell(4);
                        noteCell.setCellValue("自動記録");
                    }
                }
            }

            // 休憩ログ（休憩開始・終了時刻が期間内にある場合のみ表示）
            List<BreakRecord> breakRecords = breakRecordRepository.findByAttendance_IdOrderByBreakStartAsc(attendance.getId());
            for (BreakRecord breakRecord : breakRecords) {
                // 休憩開始ログ
                if (breakRecord.getBreakStart() != null) {
                    boolean breakStartInRange = !breakRecord.getBreakStart().isBefore(startDateTime) && 
                            !breakRecord.getBreakStart().isAfter(endDateTime);
                    if (breakStartInRange) {
                        // 操作種別フィルター
                        if (operationType == null || operationType.isEmpty() || "休憩開始".equals(operationType)) {
                            Row row = sheet.createRow(rowNum++);
                            Cell dateCell = row.createCell(0);
                            dateCell.setCellValue(breakRecord.getBreakStart());
                            dateCell.setCellStyle(dateTimeStyle);

                            Cell userCell = row.createCell(1);
                            userCell.setCellValue(user.getUsername());

                            Cell typeCell = row.createCell(2);
                            typeCell.setCellValue("休憩開始");

                            Cell valueCell = row.createCell(3);
                            valueCell.setCellValue(breakRecord.getBreakStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                            Cell noteCell = row.createCell(4);
                            noteCell.setCellValue("自動記録");
                        }
                    }
                }

                // 休憩終了ログ
                if (breakRecord.getBreakEnd() != null) {
                    boolean breakEndInRange = !breakRecord.getBreakEnd().isBefore(startDateTime) && 
                            !breakRecord.getBreakEnd().isAfter(endDateTime);
                    if (breakEndInRange) {
                        // 操作種別フィルター
                        if (operationType == null || operationType.isEmpty() || "休憩終了".equals(operationType)) {
                            Row row = sheet.createRow(rowNum++);
                            Cell dateCell = row.createCell(0);
                            dateCell.setCellValue(breakRecord.getBreakEnd());
                            dateCell.setCellStyle(dateTimeStyle);

                            Cell userCell = row.createCell(1);
                            userCell.setCellValue(user.getUsername());

                            Cell typeCell = row.createCell(2);
                            typeCell.setCellValue("休憩終了");

                            Cell valueCell = row.createCell(3);
                            valueCell.setCellValue(breakRecord.getBreakEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                            Cell noteCell = row.createCell(4);
                            noteCell.setCellValue("自動記録");
                        }
                    }
                }
            }

            // 中抜けログ（中抜け開始・終了時刻が期間内にある場合のみ表示）
            List<LeaveRecord> leaveRecords = leaveRecordRepository.findByAttendance_IdOrderByLeaveStartAsc(attendance.getId());
            for (LeaveRecord leaveRecord : leaveRecords) {
                // 中抜け開始ログ
                if (leaveRecord.getLeaveStart() != null) {
                    boolean leaveStartInRange = !leaveRecord.getLeaveStart().isBefore(startDateTime) && 
                            !leaveRecord.getLeaveStart().isAfter(endDateTime);
                    if (leaveStartInRange) {
                        // 操作種別フィルター
                        if (operationType == null || operationType.isEmpty() || "中抜け開始".equals(operationType)) {
                            Row row = sheet.createRow(rowNum++);
                            Cell dateCell = row.createCell(0);
                            dateCell.setCellValue(leaveRecord.getLeaveStart());
                            dateCell.setCellStyle(dateTimeStyle);

                            Cell userCell = row.createCell(1);
                            userCell.setCellValue(user.getUsername());

                            Cell typeCell = row.createCell(2);
                            typeCell.setCellValue("中抜け開始");

                            Cell valueCell = row.createCell(3);
                            valueCell.setCellValue(leaveRecord.getLeaveStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                            Cell noteCell = row.createCell(4);
                            String leaveType = leaveRecord.getLeaveType() != null ? 
                                    (leaveRecord.getLeaveType().equals("DEDUCTION") ? "控除" : 
                                     leaveRecord.getLeaveType().equals("PAID_LEAVE") ? "有給" : "未設定") : "未設定";
                            noteCell.setCellValue("自動記録（扱い: " + leaveType + "）");
                        }
                    }
                }

                // 中抜け終了ログ
                if (leaveRecord.getLeaveEnd() != null) {
                    boolean leaveEndInRange = !leaveRecord.getLeaveEnd().isBefore(startDateTime) && 
                            !leaveRecord.getLeaveEnd().isAfter(endDateTime);
                    if (leaveEndInRange) {
                        // 操作種別フィルター
                        if (operationType == null || operationType.isEmpty() || "中抜け終了".equals(operationType)) {
                            Row row = sheet.createRow(rowNum++);
                            Cell dateCell = row.createCell(0);
                            dateCell.setCellValue(leaveRecord.getLeaveEnd());
                            dateCell.setCellStyle(dateTimeStyle);

                            Cell userCell = row.createCell(1);
                            userCell.setCellValue(user.getUsername());

                            Cell typeCell = row.createCell(2);
                            typeCell.setCellValue("中抜け終了");

                            Cell valueCell = row.createCell(3);
                            valueCell.setCellValue(leaveRecord.getLeaveEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                            Cell noteCell = row.createCell(4);
                            String leaveType = leaveRecord.getLeaveType() != null ? 
                                    (leaveRecord.getLeaveType().equals("DEDUCTION") ? "控除" : 
                                     leaveRecord.getLeaveType().equals("PAID_LEAVE") ? "有給" : "未設定") : "未設定";
                            noteCell.setCellValue("自動記録（扱い: " + leaveType + "）");
                        }
                    }
                }
            }
        }

        // 修正依頼ログ
        List<FixRequest> fixRequests = fixRequestRepository.findAllByUser_CompanyId(companyId).stream()
                .filter(fr -> fr.getCreatedAt() != null &&
                        !fr.getCreatedAt().isBefore(startDateTime) &&
                        !fr.getCreatedAt().isAfter(endDateTime))
                .sorted((fr1, fr2) -> fr1.getCreatedAt().compareTo(fr2.getCreatedAt()))
                .collect(Collectors.toList());

        for (FixRequest fixRequest : fixRequests) {
            User user = users.stream()
                    .filter(u -> u.getId().equals(fixRequest.getUserId()))
                    .findFirst()
                    .orElse(null);
            if (user == null) continue;

            // ユーザーフィルター
            if (username != null && !username.isEmpty() && !user.getUsername().equals(username)) {
                continue;
            }

            // 修正依頼作成ログ
            if (operationType == null || operationType.isEmpty() || "修正依頼".equals(operationType)) {
                Row row = sheet.createRow(rowNum++);
                Cell dateCell = row.createCell(0);
                dateCell.setCellValue(fixRequest.getCreatedAt());
                dateCell.setCellStyle(dateTimeStyle);

                Cell userCell = row.createCell(1);
                userCell.setCellValue(user.getUsername());

                Cell typeCell = row.createCell(2);
                typeCell.setCellValue("修正依頼");

                Cell valueCell = row.createCell(3);
                if ("LEAVE_TYPE".equals(fixRequest.getRequestType())) {
                    // 中抜けの扱い変更の場合
                    String leaveTypeLabel = fixRequest.getNewLeaveType() != null ? 
                            ("DEDUCTION".equals(fixRequest.getNewLeaveType()) ? "控除" : "有給") : "未設定";
                    valueCell.setCellValue(leaveTypeLabel);
                } else if (fixRequest.getNewValue() != null) {
                    // 時刻の修正依頼の場合
                    valueCell.setCellValue(fixRequest.getNewValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } else {
                    // newValueがnullの場合
                    valueCell.setCellValue("-");
                }

                Cell noteCell = row.createCell(4);
                noteCell.setCellValue("依頼ID: " + fixRequest.getId() + ", 理由: " + fixRequest.getReason() + ", 種別: " + convertRequestTypeToJapanese(fixRequest.getRequestType()));
            }

            // 承認/却下ログ（承認・却下された場合）
            if ("APPROVED".equals(fixRequest.getStatus()) || "REJECTED".equals(fixRequest.getStatus())) {
                String actionType = "APPROVED".equals(fixRequest.getStatus()) ? "修正承認" : "修正却下";
                if (operationType == null || operationType.isEmpty() || actionType.equals(operationType)) {
                    // 承認者のユーザー名を取得
                    String approverName = "管理者";
                    if (fixRequest.getApprovedBy() != null) {
                        approverName = fixRequest.getApprovedBy().getUsername();
                    } else {
                        // 承認者が記録されていない場合は、ユーザーリストから探す
                        User approver = users.stream()
                                .filter(u -> u.getId().equals(fixRequest.getApprovedByUserId()))
                                .findFirst()
                                .orElse(null);
                        if (approver != null) {
                            approverName = approver.getUsername();
                        }
                    }
                    
                    // ユーザーフィルター
                    if (username != null && !username.isEmpty() && !approverName.equals(username)) {
                        // 承認者がフィルター条件に一致しない場合はスキップ
                        continue;
                    }
                    
                    Row actionRow = sheet.createRow(rowNum++);
                    Cell actionDateCell = actionRow.createCell(0);
                    // 承認/却下時刻はcreatedAtを使用（実際の承認時刻を記録する場合は別途フィールドが必要）
                    actionDateCell.setCellValue(fixRequest.getCreatedAt());
                    actionDateCell.setCellStyle(dateTimeStyle);

                    Cell actionUserCell = actionRow.createCell(1);
                    actionUserCell.setCellValue(approverName);

                    Cell actionTypeCell = actionRow.createCell(2);
                    actionTypeCell.setCellValue(actionType);

                    Cell actionValueCell = actionRow.createCell(3);
                    actionValueCell.setCellValue("-");

                    Cell actionNoteCell = actionRow.createCell(4);
                    actionNoteCell.setCellValue("依頼ID: " + fixRequest.getId());
                }
            }
        }

        // 列幅を自動調整
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }

        // ワークブックをバイト配列に変換
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    /**
     * ヘッダー行のスタイルを作成
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 日時型のセルスタイルを作成（Excel日時型）
     */
    private CellStyle createDateTimeStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        // Excel日時型のフォーマット（yyyy/mm/dd hh:mm:ss）
        style.setDataFormat(format.getFormat("yyyy/mm/dd hh:mm:ss"));
        return style;
    }
    
    /**
     * 日付型のセルスタイルを作成（Excel日付型）
     */
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        // Excel日付型のフォーマット（yyyy/mm/dd）
        style.setDataFormat(format.getFormat("yyyy/mm/dd"));
        return style;
    }

    /**
     * 時間型のセルスタイルを作成
     */
    private CellStyle createTimeStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        return style;
    }

    /**
     * 数値型のセルスタイルを作成
     */
    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        return style;
    }

    /**
     * 修正依頼の種類を日本語に変換
     */
    private String convertRequestTypeToJapanese(String requestType) {
        if (requestType == null) {
            return "不明";
        }
        switch (requestType) {
            case "CHECK_IN":
                return "出勤時刻";
            case "CHECK_OUT":
                return "退勤時刻";
            case "BREAK_START":
                return "休憩開始時刻";
            case "BREAK_END":
                return "休憩終了時刻";
            case "LEAVE_START":
                return "中抜け開始時刻";
            case "LEAVE_END":
                return "中抜け終了時刻";
            default:
                return requestType;
        }
    }
}
