package com.example.todolist.Data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.todolist.Data.entity.Category;
import com.example.todolist.Data.entity.User;

@Entity(
        tableName = "tasks",
        foreignKeys = {
                @ForeignKey(entity = Category.class, parentColumns = "category_id", childColumns = "category_id", onDelete = ForeignKey.SET_NULL),
                @ForeignKey(entity = User.class, parentColumns = "user_id", childColumns = "user_id", onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index(value = "category_id"), @Index(value = "user_id")}
)
public class Task {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "task_id")
    private int taskId;
//
    private boolean isFlagged;
    private Long reminderDate;
//lăp lại
    @ColumnInfo(name = "isRecurring")
    private boolean isRecurring = false;

    @ColumnInfo(name = "recurrenceRuleId")
    private Long recurrenceRuleId;

    @ColumnInfo(name = "parentTaskId")
    private Long parentTaskId;
    @Ignore
    public Task(String title, String description, Integer categoryId) {
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
    }

    public boolean isFlagged() {
        return isFlagged;
    }

    public void setFlagged(boolean flagged) {
        isFlagged = flagged;
    }

    public Long getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(Long reminderDate) {
        this.reminderDate = reminderDate;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public Long getRecurrenceRuleId() {
        return recurrenceRuleId;
    }

    public void setRecurrenceRuleId(Long recurrenceRuleId) {
        this.recurrenceRuleId = recurrenceRuleId;
    }

    public Long getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(Long parentTaskId) {
        this.parentTaskId = parentTaskId;
    }
    // Đã xóa trường String category

    @NonNull
    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "due_date")
    private Long dueDate;

    @ColumnInfo(name = "reminder_time")
    private Long reminderTime;

    @ColumnInfo(name = "repeat_rule")
    private String repeatRule;

    @ColumnInfo(name = "is_completed", defaultValue = "0")
    private int isCompleted;

    @ColumnInfo(name = "is_starred", defaultValue = "0")
    private int isStarred;

    @ColumnInfo(name = "created_at")
    private Long createdAt = System.currentTimeMillis();

    @ColumnInfo(name = "updated_at")
    private Long updatedAt;

    @ColumnInfo(name = "user_id") private Integer userId;

    // Đây là trường quan trọng để liên kết
    @ColumnInfo(name = "category_id") private Integer categoryId;

    public Task() {}

    @Ignore
    public Task(@NonNull String title, String description, Long dueDate) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.isCompleted = 0;
        this.reminderTime = null;
        this.repeatRule = "Không";

        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
        // categoryId sẽ được set sau
    }

    // --- Getter & Setter ---
    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    @NonNull public String getTitle() { return title; }
    public void setTitle(@NonNull String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getDueDate() { return dueDate; }
    public void setDueDate(Long dueDate) { this.dueDate = dueDate; }

    public Long getReminderTime() { return reminderTime; }
    public void setReminderTime(Long reminderTime) { this.reminderTime = reminderTime; }

    public String getRepeatRule() { return repeatRule; }
    public void setRepeatRule(String repeatRule) { this.repeatRule = repeatRule; }

    public int getIsCompleted() { return isCompleted; }
    public void setIsCompleted(int isCompleted) { this.isCompleted = isCompleted; }

    public int getIsStarred() { return isStarred; }
    public void setIsStarred(int isStarred) { this.isStarred = isStarred; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public Long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
}