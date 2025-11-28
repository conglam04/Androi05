package com.example.todolist.Data.Repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.todolist.Data.AppDatabase;
import com.example.todolist.Data.dao.CategoryDao;
import com.example.todolist.Data.dao.RecurrenceRuleDao;
import com.example.todolist.Data.dao.TaskDao;
import com.example.todolist.Data.entity.Category;
import com.example.todolist.Data.entity.RecurrenceRule;
import com.example.todolist.Data.entity.Task;
import com.example.todolist.Data.entity.TaskWithCategory;
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
    private final CategoryDao categoryDao; // Cần thêm CategoryDao
    private final RecurrenceRuleDao recurrenceRuleDao;
    private final ExecutorService executorService;

    public TaskRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.taskDao = db.taskDao();
        this.categoryDao = db.categoryDao();
        this.recurrenceRuleDao = db.recurrenceRuleDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // --- CRUD TASK ---
    public long insert(Task task) {
        return taskDao.insert(task);
    }

    public void update(Task task) {
        executorService.execute(() -> taskDao.update(task));
    }

    public void delete(Task task) {
        executorService.execute(() -> taskDao.delete(task));
    }

    // --- QUERY ---
    // Trả về TaskWithCategory để lấy được tên danh mục
    public List<TaskWithCategory> getAllTasks() {
        return taskDao.getAllTasks();
    }

    public List<TaskWithCategory> getTasksByDateRange(long start, long end) {
        return taskDao.getTasksByDate(start, end);
    }

    // --- HELPERS CHO CATEGORY ---

    /**
     * Hàm này sẽ tìm ID của category dựa theo tên.
     * Nếu chưa có tên đó trong DB, nó sẽ tự tạo mới và trả về ID mới.
     */
    public int getCategoryIdByName(String categoryName) {
        Category existing = categoryDao.getCategoryByName(categoryName);
        if (existing != null) {
            return existing.getCategoryId();
        } else {
            // Nếu chưa có thì tạo mới
            Category newCat = new Category(categoryName);
            long newId = categoryDao.insertCategory(newCat);
            return (int) newId;
        }
    }

    // --- THỐNG KÊ ---
    public int getCompletedCount() {
        return taskDao.getCompletedCount();
    }

    public int getNotCompletedCount() {
        return taskDao.getNotCompletedCount();
    }

    public LinkedHashMap<String, Integer> getCompletedTaskCountByDays(int days) {
        List<TaskDao.CompletedTaskByDate> list = taskDao.getCompletedTaskCountByDays(days);
        LinkedHashMap<String, Integer> data = new LinkedHashMap<>();
        if (list != null) {
            for (TaskDao.CompletedTaskByDate item : list) {
                data.put(item.date, item.total);
            }
        }
        return data;
    }
    public LiveData<List<TaskWithCategory>> getTasks(Long categoryId, Integer chipId) {
        long[] range = getDateRange(chipId == null ? R.id.chipToday : chipId);
        long startDate = range[0];
        long endDate = range[1];

        boolean isDefaultCategory = (categoryId == null || categoryId == -1);

        return isDefaultCategory
                ? taskDao.getTasksByDateRange(startDate, endDate)
                : taskDao.getTasksByCategoryIdAndDateRange(startDate, endDate, categoryId);
    }

    private long[] getDateRange(int chipId) {
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance();

        if (chipId == R.id.chipToday) {
            end.setTimeInMillis(start.getTimeInMillis());
            end.add(Calendar.DAY_OF_YEAR, 1);
        } else if (chipId == R.id.chipThisWeek) {
            start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            end.setTimeInMillis(start.getTimeInMillis());
            end.add(Calendar.WEEK_OF_YEAR, 1);
        } else { // R.id.chipThisMonth
            start.set(Calendar.DAY_OF_MONTH, 1);
            end.setTimeInMillis(start.getTimeInMillis());
            end.add(Calendar.MONTH, 1);
        }

        return new long[]{start.getTimeInMillis(), end.getTimeInMillis()};
    }

    public long insertTask(Task task) {
        Callable<Long> insertCallable = () -> taskDao.insertTask(task);
        Future<Long> future = AppDatabase.databaseWriteExecutor.submit(insertCallable);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void updateTask(Task task) {
        AppDatabase.databaseWriteExecutor.execute(() -> taskDao.updateTask(task));
    }

    public void deleteTask(Task task) {
        AppDatabase.databaseWriteExecutor.execute(() -> taskDao.deleteTask(task));
    }

    public void toggleTaskCompletion(long taskId, int isCompleted) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            taskDao.updateTaskCompletion(taskId, isCompleted, System.currentTimeMillis());
        });
    }

    public void toggleTaskFlag(long taskId, boolean isFlagged) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                taskDao.updateTaskFlag(taskId, isFlagged, System.currentTimeMillis())
        );
    }

    public void toggleTaskStar(long taskId, int isStared) {
        AppDatabase.databaseWriteExecutor.execute(() ->
                taskDao.updateTaskStar(taskId, isStared, System.currentTimeMillis())
        );
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
        } catch (ExecutionException | InterruptedException e) {
            Log.e("TaskRepository", "Error getting all recurring tasks", e);
            return null;
        }
    }

    public RecurrenceRule getRecurrenceRuleByTaskIdSync(long taskId) {
        try {
            return executorService.submit(() -> recurrenceRuleDao.getRuleByTaskId(taskId)).get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e("TaskRepository", "Error getting recurrence rule by task id", e);
            return null;
        }
    }

    public boolean doesInstanceExist(long parentTaskId, long dueDate) {
        try {
            return executorService.submit(() -> taskDao.getTaskInstanceByParentAndDate(parentTaskId, dueDate) != null).get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e("TaskRepository", "Error checking if instance exists", e);
            return false;
        }
    }

    public boolean isInstanceCompleted(long parentTaskId, long dueDate) {
        try {
            return executorService.submit(() -> {
                Task task = taskDao.getTaskInstanceByParentAndDate(parentTaskId, dueDate);
                return task != null && task.getIsCompleted() == 1;
            }).get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e("TaskRepository", "Error checking if instance is completed", e);
            return false;
        }
    }
}