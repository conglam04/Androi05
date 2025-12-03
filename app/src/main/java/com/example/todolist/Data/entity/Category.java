package com.example.todolist.Data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {
    // ... (Giữ nguyên các biến)
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "category_id")
    private int categoryId;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    public Category() {}

    // --- THÊM @Ignore VÀO ĐÂY ---
    @Ignore
    public Category(@NonNull String name) {
        this.name = name;
    }

    // ... (Giữ nguyên getter/setter)
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    @NonNull public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    @Override
    public String toString() { return name; }
}