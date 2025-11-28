package com.example.todolist.Data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.todolist.Data.entity.Category;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertCategory(Category category);

    @Update
    void updateCategory(Category category);

    @Delete
    void deleteCategory(Category category);

    @Query("SELECT * FROM categories ORDER BY category_id DESC")
    List<Category> getAllCategories();

    @Query("SELECT * FROM categories WHERE category_id = :id LIMIT 1")
    Category getCategoryById(int id);

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    Category getCategoryByName(String name);
    //

    @Query("SELECT * FROM categories ORDER BY name ASC")
    LiveData<List<Category>> getAllLiveDataCategories();

    @Query("SELECT * FROM categories WHERE category_id = :categoryId")
    LiveData<Category> getCategoryById(long categoryId);
    @Query("SELECT COUNT(*) FROM tasks WHERE category_id = :categoryId")
    int getTaskCountForCategory(long categoryId);
    @Query("SELECT COUNT(*) FROM categories")
    int getCategoryCount();

}