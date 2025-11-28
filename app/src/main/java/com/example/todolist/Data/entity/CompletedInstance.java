package com.example.todolist.Data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Lưu các instance đã complete của recurring task
 * Dùng để track history và prevent duplicate
 */
@Entity(tableName = "completed_instances")
public class CompletedInstance {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "templateTaskId")
    private long templateTaskId; // ID của task template

    @ColumnInfo(name = "completedDate")
    private long completedDate; // Timestamp khi complete

    @ColumnInfo(name = "dueDate")
    private long dueDate; // Due date của instance này

    @ColumnInfo(name = "completedAt")
    private long completedAt; // Timestamp thực tế complete

    // Constructors
    public CompletedInstance() {}

    public CompletedInstance(long templateTaskId, long completedDate, long dueDate) {
        this.templateTaskId = templateTaskId;
        this.completedDate = completedDate;
        this.dueDate = dueDate;
        this.completedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTemplateTaskId() {
        return templateTaskId;
    }

    public void setTemplateTaskId(long templateTaskId) {
        this.templateTaskId = templateTaskId;
    }

    public long getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(long completedDate) {
        this.completedDate = completedDate;
    }

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }
}

