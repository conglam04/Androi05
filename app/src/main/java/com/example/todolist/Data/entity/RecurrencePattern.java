package com.example.todolist.Data.entity;

public enum RecurrencePattern {
    NONE("Không lặp lại"),
    DAILY("Hàng ngày"),
    WEEKLY("Hàng tuần"),
    MONTHLY("Hàng tháng"),
    YEARLY("Hàng năm"),
    CUSTOM("Tùy chỉnh");

    private final String displayName;

    RecurrencePattern(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}