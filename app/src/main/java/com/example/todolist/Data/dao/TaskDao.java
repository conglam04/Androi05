package com.example.todolist.Data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.todolist.Data.entity.Task;
import com.example.todolist.Data.entity.TaskWithCategory;

import java.util.List;

@Dao
public interface TaskDao {
    // --- INSERT / UPDATE / DELETE (Dựa trên task object) ---
    @Insert
    long insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    // --- QUERY CÓ LỌC THEO USER_ID ---

    // Đếm số lượng task hoàn thành của user
    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 1 AND user_id = :userId")
    int getCompletedCount(int userId);

    // Đếm số lượng task chưa hoàn thành của user
    @Query("SELECT COUNT(*) FROM tasks WHERE is_completed = 0 AND user_id = :userId")
    int getNotCompletedCount(int userId);

    // Lấy ngày có task của user để vẽ dấu chấm
    @Query("SELECT due_date FROM tasks WHERE user_id = :userId")
    List<Long> getAllTaskDates(int userId);

    // Lấy tất cả task của user
    @Transaction
    @Query("SELECT * FROM tasks WHERE user_id = :userId ORDER BY created_at DESC")
    List<TaskWithCategory> getAllTasks(int userId);

    // Lấy task theo ngày (cũ) - dùng cho các hàm logic cũ nếu cần
    @Transaction
    @Query("SELECT * FROM tasks WHERE user_id = :userId AND due_date >= :startOfDay AND due_date <= :endOfDay")
    List<TaskWithCategory> getTasksByDate(int userId, long startOfDay, long endOfDay);

    // LiveData cho TaskFragment (Lọc ngày + User)
    @Transaction
    @Query("SELECT * FROM tasks WHERE user_id = :userId AND due_date >= :startOfDay AND due_date < :endOfDay ORDER BY due_date ASC")
    LiveData<List<TaskWithCategory>> getTasksByDateRange(int userId, long startOfDay, long endOfDay);

    // LiveData cho TaskFragment (Lọc ngày + Category + User)
    @Transaction
    @Query("SELECT * FROM tasks WHERE user_id = :userId AND due_date >= :startOfDay AND due_date < :endOfDay AND category_id = :categoryId ORDER BY due_date ASC")
    LiveData<List<TaskWithCategory>> getTasksByCategoryIdAndDateRange(int userId, long startOfDay, long endOfDay, long categoryId);

    // Thống kê biểu đồ (Lọc theo user)
    @Query("SELECT date(due_date / 1000, 'unixepoch', 'localtime') AS date, COUNT(*) AS total " +
            "FROM tasks " +
            "WHERE is_completed = 1 " +
            "AND user_id = :userId " +
            "AND date(due_date / 1000, 'unixepoch', 'localtime') >= date('now', '-' || :days || ' days', 'localtime') " +
            "GROUP BY date " +
            "ORDER BY date ASC")
    List<CompletedTaskByDate> getCompletedTaskCountByDays(int userId, int days);

    @Transaction
    @Query("SELECT * FROM tasks WHERE user_id IS NULL ORDER BY created_at DESC")
    List<TaskWithCategory> getTasksForGuest();

    // Thống kê biểu đồ cho khách
    @Query("SELECT date(due_date / 1000, 'unixepoch', 'localtime') AS date, COUNT(*) AS total " +
            "FROM tasks " +
            "WHERE is_completed = 1 " +
            "AND user_id IS NULL " + // Lọc task không có chủ sở hữu
            "AND date(due_date / 1000, 'unixepoch', 'localtime') >= date('now', '-' || :days || ' days', 'localtime') " +
            "GROUP BY date " +
            "ORDER BY date ASC")
    List<CompletedTaskByDate> getCompletedTaskCountByDaysForGuest(int days);

    // Helper class cho thống kê
    class CompletedTaskByDate {
        public String date;
        public int total;
    }

    // Các hàm lấy 1 task cụ thể thì không nhất thiết cần userId vì taskId là duy nhất,
    // nhưng thêm vào để bảo mật càng tốt (ở đây mình giữ nguyên cho đơn giản)
    @Query("SELECT * FROM tasks WHERE task_id = :taskId")
    LiveData<Task> getTaskById(long taskId);

    @Query("UPDATE tasks SET is_completed = :isCompleted, updated_at = :updatedAt WHERE task_id = :taskId")
    void updateTaskCompletion(long taskId, int isCompleted, long updatedAt);

    @Query("UPDATE tasks SET isFlagged = :isFlagged, updated_at = :updatedAt WHERE task_id = :taskId")
    void updateTaskFlag(long taskId, boolean isFlagged, long updatedAt);

    @Query("UPDATE tasks SET is_starred = :isStared, updated_at = :updatedAt WHERE task_id = :taskId")
    void updateTaskStar(long taskId, int isStared, long updatedAt);

    // Các hàm cho Recurring Task
    @Query("SELECT * FROM tasks WHERE isRecurring = 1")
    List<Task> getAllRecurringTasksSync();

    @Query("SELECT * FROM tasks WHERE parentTaskId = :parentTaskId AND due_date = :dueDate LIMIT 1")
    Task getTaskInstanceByParentAndDate(long parentTaskId, long dueDate);

    @Query("SELECT * FROM tasks WHERE is_starred = 1 AND user_id = :userId")
    List<Task> getStarredTasks(int userId);
}
