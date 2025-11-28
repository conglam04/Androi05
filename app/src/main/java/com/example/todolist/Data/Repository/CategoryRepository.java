package com.example.todolist.Data.Repository;


import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.todolist.Data.AppDatabase;
import com.example.todolist.Data.dao.CategoryDao;
import com.example.todolist.Data.dao.TaskDao;
import com.example.todolist.Data.entity.Category;

import java.util.List;

public class CategoryRepository {
    private final CategoryDao categoryDao;
    private final TaskDao taskDao;
    private final LiveData<List<Category>> allCategories;


    public CategoryRepository(Context context) {

        AppDatabase db = AppDatabase.getInstance(context);
        categoryDao = db.categoryDao();
        taskDao = db.taskDao();
        allCategories = categoryDao.getAllLiveDataCategories();
    }

    public long insert(Category category) {
        return categoryDao.insertCategory(category);
    }

    public void update(Category category) {
        categoryDao.updateCategory(category);
    }

    public void delete(Category category) {
        categoryDao.deleteCategory(category);
    }

    public List<Category> getAll() {
        return categoryDao.getAllCategories();
    }
    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }
}

