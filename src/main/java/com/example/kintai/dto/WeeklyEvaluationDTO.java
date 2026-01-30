package com.example.kintai.dto;

import java.time.LocalDate;

public class WeeklyEvaluationDTO {
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private String rating;
    private String comment;
    private String evaluatorName;

    public WeeklyEvaluationDTO() {
    }

    public WeeklyEvaluationDTO(LocalDate weekStartDate, LocalDate weekEndDate, String rating, String comment, String evaluatorName) {
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        this.rating = rating;
        this.comment = comment;
        this.evaluatorName = evaluatorName;
    }

    // Getters and Setters
    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    public LocalDate getWeekEndDate() {
        return weekEndDate;
    }

    public void setWeekEndDate(LocalDate weekEndDate) {
        this.weekEndDate = weekEndDate;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getEvaluatorName() {
        return evaluatorName;
    }

    public void setEvaluatorName(String evaluatorName) {
        this.evaluatorName = evaluatorName;
    }
}
