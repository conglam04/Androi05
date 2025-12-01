package com.example.todolist.Data.Repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.todolist.Data.AppDatabase;
import com.example.todolist.Data.dao.CategoryDao;
import com.example.todolist.Data.dao.RecurrenceRuleDao;
import com.example.todolist.Data.dao.TaskDao;
import com.example.todolist.Data.dao.UserDao;
import com.example.todolist.Data.entity.Category;
import com.example.todolist.Data.entity.RecurrenceRule;
import com.example.todolist.Data.entity.Task;
import com.example.todolist.Data.entity.TaskWithCategory;
import com.example.todolist.Data.entity.User;
import com.example.todolist.R;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TaskRepository {

    private final TaskDao taskDao;
    private final CategoryDao categoryDao;
    private final RecurrenceRuleDao recurrenceRuleDao;
    private final UserDao userDao;
    private final ExecutorService executorService;
    private final Context context;

    public TaskRepository(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(this.context);
        this.taskDao = db.taskDao();
        this.categoryDao = db.categoryDao();
        this.recurrenceRuleDao = db.recurrenceRuleDao();
        this.userDao = db.userDao();
        // Dùng 4 luồng để xử lý song song, tránh tắc nghẽn (Lag)
        this.executorService = Executors.newFixedThreadPool(4);
    }

    // --- HÀM LẤY ID TỪ BỘ NHỚ (NHANH) ---
    private int getCachedUserId() {
        return context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
                .getInt("USER_ID", -1);
    }

    // --- HÀM TỰ SỬA LỖI USER ID (CHẠY NGẦM) ---
    private int getOrFixUserId() {
        int userId = getCachedUserId();

        // Nếu ID hợp lệ, trả về ngay
        if (userId != -1) return userId;

        // Nếu ID lỗi (-1), tự động tìm user đầu tiên trong DB
        try {
            List<User> users = userDao.getAllUsers();
            if (users != null && !users.isEmpty()) {
                userId = users.get(0).getUserId();
            } else {
                // Nếu DB rỗng, tạo user admin cứu hộ
                User defaultUser = new User("admin", "123456", "0000000000", "admin@gmail.com");
                userId = (int) userDao.insertUser(defaultUser);
            }

            // Lưu lại ID vừa tìm được
            if (userId > 0) {
                context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
                        .edit().putInt("USER_ID", userId).apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userId;
    }

    // --- CRUD TASK (ĐÃ NÂNG CẤP) ---

    public long insert(Task task) {
        task.setUserId(getOrFixUserId()); // Luôn đảm bảo có ID trước khi thêm
        return taskDao.insert(task);
    }

    public long insertTask(Task task) {
        Callable<Long> insertCallable = () -> {
            task.setUserId(getOrFixUserId()); // Tự sửa lỗi ID trong luồng nền
            return taskDao.insertTask(task);
        };
        Future<Long> future = executorService.submit(insertCallable);
        try {
            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void update(Task task) {
        executorService.execute(() -> {
            try {
                // Kiểm tra và sửa User ID nếu task này bị lỗi
                if (task.getUserId() == null || task.getUserId() <= 0) {
                    task.setUserId(getOrFixUserId());
                }
                taskDao.update(task);
            } catch (Exception e) {
                Log.e("TaskRepo", "Lỗi update: " + e.getMessage());
            }
        });
    }

    public void updateTask(Task task) {
        executorService.execute(() -> {
            try {
                if (task.getUserId() == null || task.getUserId() <= 0) {
                    task.setUserId(getOrFixUserId());
                }
                taskDao.updateTask(task);
            } catch (Exception e) {
                Log.e("TaskRepo", "Lỗi updateTask: " + e.getMessage());
            }
        });
    }

    public void delete(Task task) {
        executorService.execute(() -> taskDao.delete(task));
    }

    public void deleteTask(Task task) {
        executorService.execute(() -> taskDao.deleteTask(task));
    }

    // --- CÁC HÀM QUERY (LẤY DỮ LIỆU) ---

    public List<TaskWithCategory> getAllTasks() {
        return taskDao.getAllTasks(getCachedUserId());
    }

    public List<TaskWithCategory> getTasksByDateRange(long start, long end) {
        return taskDao.getTasksByDate(getCachedUserId(), start, end);
    }

    public List<Long> getAllTaskDates() {
        try {
            return executorService.submit(() -> taskDao.getAllTaskDates(getCachedUserId())).get();
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<List<TaskWithCategory>> getTasks(Long categoryId, Integer chipId) {
        long[] range = getDateRange(chipId == null ? R.id.chipToday : chipId);
        boolean isDefaultCategory = (categoryId == null || categoryId == -1);
        int userId = getCachedUserId();

        return isDefaultCategory
                ? taskDao.getTasksByDateRange(userId, range[0], range[1])
                : taskDao.getTasksByCategoryIdAndDateRange(userId, range[0], range[1], categoryId);
    }

    public int getCompletedCount() {
        return taskDao.getCompletedCount(getCachedUserId());
    }

    public int getNotCompletedCount() {
        return taskDao.getNotCompletedCount(getCachedUserId());
    }

    public LinkedHashMap<String, Integer> getCompletedTaskCountByDays(int days) {
        List<TaskDao.CompletedTaskByDate> list = taskDao.getCompletedTaskCountByDays(getCachedUserId(), days);
        LinkedHashMap<String, Integer> data = new LinkedHashMap<>();
        if (list != null) {
            for (TaskDao.CompletedTaskByDate item : list) {
                data.put(item.date, item.total);
            }
        }
        return data;
    }

    // --- HELPER METHODS ---

    public int getCategoryIdByName(String categoryName) {
        Category existing = categoryDao.getCategoryByName(categoryName);
        if (existing != null) {
            return existing.getCategoryId();
        } else {
            Category newCat = new Category(categoryName);
            return (int) categoryDao.insertCategory(newCat);
        }
    }

    private long[] getDateRange(int chipId) {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0); start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0); start.set(Calendar.MILLISECOND, 0);
        Calendar end = Calendar.getInstance();

        if (chipId == R.id.chipToday) {
            end.setTimeInMillis(start.getTimeInMillis());
            end.add(Calendar.DAY_OF_YEAR, 1);
        } else if (chipId == R.id.chipThisWeek) {
            start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            end.setTimeInMillis(start.getTimeInMillis());
            end.add(Calendar.WEEK_OF_YEAR, 1);
        } else { // Month
            start.set(Calendar.DAY_OF_MONTH, 1);
            end.setTimeInMillis(start.getTimeInMillis());
            end.add(Calendar.MONTH, 1);
        }
        return new long[]{start.getTimeInMillis(), end.getTimeInMillis()};
    }

    public void toggleTaskCompletion(long taskId, int isCompleted) {
        executorService.execute(() -> taskDao.updateTaskCompletion(taskId, isCompleted, System.currentTimeMillis()));
    }

    public void toggleTaskFlag(long taskId, boolean isFlagged) {
        executorService.execute(() -> taskDao.updateTaskFlag(taskId, isFlagged, System.currentTimeMillis()));
    }

    public void toggleTaskStar(long taskId, int isStared) {
        executorService.execute(() -> taskDao.updateTaskStar(taskId, isStared, System.currentTimeMillis()));
    }

    public void saveRecurrenceRule(Task task, RecurrenceRule rule) {
        executorService.execute(() -> {
            long ruleId = recurrenceRuleDao.insert(rule);
            task.setRecurrenceRuleId(ruleId);
            task.setRecurring(true);
            taskDao.updateTask(task);
        });
    }

    public List<Task> getAllRecurringTasksSync() {
        try {
            return executorService.submit(taskDao::getAllRecurringTasksSync).get();
        } catch (Exception e) { return null; }
    }

    public RecurrenceRule getRecurrenceRuleByTaskIdSync(long taskId) {
        try {
            return executorService.submit(() -> recurrenceRuleDao.getRuleByTaskId(taskId)).get();
        } catch (Exception e) { return null; }
    }

    public boolean doesInstanceExist(long parentTaskId, long dueDate) {
        try {
            return executorService.submit(() -> taskDao.getTaskInstanceByParentAndDate(parentTaskId, dueDate) != null).get();
        } catch (Exception e) { return false; }
    }

    public boolean isInstanceCompleted(long parentTaskId, long dueDate) {
        try {
            return executorService.submit(() -> {
                Task task = taskDao.getTaskInstanceByParentAndDate(parentTaskId, dueDate);
                return task != null && task.getIsCompleted() == 1;
            }).get();
        } catch (Exception e) { return false; }
    }
}