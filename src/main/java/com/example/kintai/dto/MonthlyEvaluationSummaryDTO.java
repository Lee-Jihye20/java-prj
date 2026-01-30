package com.example.kintai.dto;

import java.time.YearMonth;
import java.util.List;

public class MonthlyEvaluationSummaryDTO {
    private Long employeeId;
    private String employeeName;
    private YearMonth yearMonth;
    private List<WeeklyEvaluationDTO> weeklyEvaluations;
    private double averageRating; // 数値化した平均（S=5, A=4, B=3, C=2, D=1）
    private String averageRatingLabel; // S, A, B, C, D形式の平均
    private int evaluationCount; // その月の評価数

    public MonthlyEvaluationSummaryDTO() {
    }

    public MonthlyEvaluationSummaryDTO(Long employeeId, String employeeName, YearMonth yearMonth) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.yearMonth = yearMonth;
        this.averageRating = 0.0;
        this.evaluationCount = 0;
    }

    /**
     * 評価を数値に変換（S=5, A=4, B=3, C=2, D=1）
     */
    public static double ratingToNumeric(String rating) {
        if (rating == null) return 0.0;
        switch (rating.toUpperCase()) {
            case "S": return 5.0;
            case "A": return 4.0;
            case "B": return 3.0;
            case "C": return 2.0;
            case "D": return 1.0;
            default: return 0.0;
        }
    }

    /**
     * 数値を評価ラベルに変換
     */
    public static String numericToRating(double numeric) {
        if (numeric >= 4.5) return "S";
        if (numeric >= 3.5) return "A";
        if (numeric >= 2.5) return "B";
        if (numeric >= 1.5) return "C";
        if (numeric >= 0.5) return "D";
        return "-";
    }

    /**
     * 平均評価を計算
     */
    public void calculateAverage() {
        if (weeklyEvaluations == null || weeklyEvaluations.isEmpty()) {
            this.averageRating = 0.0;
            this.averageRatingLabel = "-";
            this.evaluationCount = 0;
            return;
        }

        double sum = 0.0;
        int count = 0;
        for (WeeklyEvaluationDTO eval : weeklyEvaluations) {
            if (eval.getRating() != null && !eval.getRating().isEmpty()) {
                sum += ratingToNumeric(eval.getRating());
                count++;
            }
        }

        this.evaluationCount = count;
        if (count > 0) {
            this.averageRating = sum / count;
            this.averageRatingLabel = numericToRating(this.averageRating);
        } else {
            this.averageRating = 0.0;
            this.averageRatingLabel = "-";
        }
    }

    // Getters and Setters
    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(YearMonth yearMonth) {
        this.yearMonth = yearMonth;
    }

    public List<WeeklyEvaluationDTO> getWeeklyEvaluations() {
        return weeklyEvaluations;
    }

    public void setWeeklyEvaluations(List<WeeklyEvaluationDTO> weeklyEvaluations) {
        this.weeklyEvaluations = weeklyEvaluations;
        calculateAverage();
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public String getAverageRatingLabel() {
        return averageRatingLabel;
    }

    public void setAverageRatingLabel(String averageRatingLabel) {
        this.averageRatingLabel = averageRatingLabel;
    }

    public int getEvaluationCount() {
        return evaluationCount;
    }

    public void setEvaluationCount(int evaluationCount) {
        this.evaluationCount = evaluationCount;
    }
}
