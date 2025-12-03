package com.example.todolist.Data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recurrence_rules")
public class RecurrenceRule {
    @PrimaryKey(autoGenerate = true)
    private long ruleId;

    private long taskId;

    @NonNull
    private String pattern = "NONE"; // DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM

    private int interval = 1; // Lặp lại mỗi X ngày/tuần/tháng

    private String daysOfWeek; // JSON array: [0,1,2] (0=CN, 1=T2,...)

    private int dayOfMonth; // Ngày trong tháng (1-31)

    private Long endDate; // Ngày kết thúc lặp lại
    private Integer occurrenceCount; // Số lần lặp lại (repeat end type = count)

    private int completedCount = 0; // Đếm số lần đã complete

    @NonNull
    private String repeatStrategy = "SCHEDULE"; // SCHEDULE (theo lịch) hoặc COMPLETION (theo ngày complete)

    private String timeOfDay; // HH:mm - giờ cố định mỗi ngày

    @NonNull
    private String repeatEndType = "NEVER"; // NEVER, UNTIL_DATE, COUNT

    private boolean isActive = true; // Recurring có đang active không

    // Getters and Setters
    public long getRuleId() {
        return ruleId;
    }

    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    @NonNull
    public String getPattern() {
        return pattern;
    }

    public void setPattern(@NonNull String pattern) {
        this.pattern = pattern;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(String daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public Integer getOccurrenceCount() {
        return occurrenceCount;
    }

    public void setOccurrenceCount(Integer occurrenceCount) {
        this.occurrenceCount = occurrenceCount;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }

    @NonNull
    public String getRepeatStrategy() {
        return repeatStrategy;
    }

    public void setRepeatStrategy(@NonNull String repeatStrategy) {
        this.repeatStrategy = repeatStrategy;
    }

    public String getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(String timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    @NonNull
    public String getRepeatEndType() {
        return repeatEndType;
    }

    public void setRepeatEndType(@NonNull String repeatEndType) {
        this.repeatEndType = repeatEndType;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}