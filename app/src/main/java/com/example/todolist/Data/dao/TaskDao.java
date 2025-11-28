package com.example.todolist.Data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.todolist.Data.entity.Task;
import com.example.todolist.Data.entity.TaskWithCategory;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    long insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    // Dùng @Transaction vì ta đang query từ 2 bảng (Task và Category)
    @Transaction
    @Query("SELECT * FROM tasks ORDER BY created_at DESC")
    List<TaskWithCategory> getAllTasks();

    @Transaction
    @Query("SELECT * FROM tasks WHERE due_date >= :startOfDay AND due_date <= :endOfDay")
    List<TaskWithCategory> getTasksByDate(long startOfDay, long endOfDay);

    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 1")
    int getCompletedCount();

    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 0")
    int getNotCompletedCount();

    class CompletedTaskByDate {
        public String date;
        public int total;
    }

    @Query("SELECT date(due_date / 1000, 'unixepoch', 'localtime') AS date, COUNT(*) AS total " +
            "FROM tasks " +
            "WHERE is_completed = 1 " +
            "AND date(due_date / 1000, 'unixepoch', 'localtime') >= date('now', '-' || :days || ' days', 'localtime') " +
            "GROUP BY date " +
            "ORDER BY date ASC")
    List<CompletedTaskByDate> getCompletedTaskCountByDays(int days);
    //
    @Query("SELECT * FROM tasks WHERE due_date >= :startOfDay AND due_date < :endOfDay ORDER BY due_date ASC")
    LiveData<List<TaskWithCategory>> getTasksByDateRange(long startOfDay, long endOfDay);

    @Query("SELECT * FROM tasks WHERE due_date >= :startOfDay AND due_date < :endOfDay AND category_id = :categoryId ORDER BY due_date ASC")
    LiveData<List<TaskWithCategory>> getTasksByCategoryIdAndDateRange(long startOfDay, long endOfDay, long categoryId);

    @Query("SELECT * FROM tasks WHERE task_id = :taskId")
    LiveData<Task> getTaskById(long taskId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    @Query("UPDATE tasks SET is_completed = :isCompleted, updated_at = :updatedAt WHERE task_id = :taskId")
    void updateTaskCompletion(long taskId, int isCompleted, long updatedAt);

    @Query("UPDATE tasks SET isFlagged = :isFlagged, updated_at = :updatedAt WHERE task_id = :taskId")
    void updateTaskFlag(long taskId, boolean isFlagged, long updatedAt);

    @Query("UPDATE tasks SET is_starred = :isStared, updated_at = :updatedAt WHERE task_id = :taskId")
    void updateTaskStar(long taskId, int isStared, long updatedAt);

    @Query("UPDATE tasks SET category_id = :newCategoryId WHERE category_id = :oldCategoryId")
    void moveTasksToCategory(long oldCategoryId, long newCategoryId);

    @Query("SELECT * FROM tasks WHERE isRecurring = 1")
    List<Task> getAllRecurringTasksSync();

    @Query("SELECT * FROM tasks WHERE parentTaskId = :parentTaskId AND due_date = :dueDate LIMIT 1")
    Task getTaskInstanceByParentAndDate(long parentTaskId, long dueDate);

}