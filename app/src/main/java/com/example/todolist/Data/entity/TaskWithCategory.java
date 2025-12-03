package com.example.todolist.Data.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.todolist.Data.entity.Category;
import com.example.todolist.Data.entity.Task;

public class TaskWithCategory {
    @Embedded
    public com.example.todolist.Data.entity.Task task;

    @Relation(
            parentColumn = "category_id",
            entityColumn = "category_id"
    )
    public com.example.todolist.Data.entity.Category category;

    // Add a constructor that takes a Task and a Category
    public TaskWithCategory(Task task, Category category) {
        this.task = task;
        this.category = category;
    }

    // Add a no-argument constructor for Room
    public TaskWithCategory() {}
}
