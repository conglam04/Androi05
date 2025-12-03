package com.example.todolist.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.todolist.Data.Repository.TaskRepository;
import com.example.todolist.Data.entity.Task;
import com.example.todolist.Data.entity.TaskWithCategory;

import java.util.Calendar;
import java.util.List;

public class LichViewModel extends AndroidViewModel {

    private final TaskRepository repository;

    // --- SỬA ĐỔI 1: Đổi kiểu dữ liệu từ List<Task> sang List<TaskWithCategory> ---
    private final MutableLiveData<List<TaskWithCategory>> tasksLiveData = new MutableLiveData<>();

    public LichViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
    }

    // --- SỬA ĐỔI 2: Cập nhật Getter ---
    public MutableLiveData<List<TaskWithCategory>> getTasksLiveData() {
        return tasksLiveData;
    }

    // Hàm load dữ liệu theo ngày
    public void loadTasksOnDate(long dateMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateMillis);

        // Đầu ngày
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();

        // Cuối ngày
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        long endOfDay = cal.getTimeInMillis();

        new Thread(() -> {
            // --- SỬA ĐỔI 3: Repository giờ trả về List<TaskWithCategory> ---
            List<TaskWithCategory> tasks = repository.getTasksByDateRange(startOfDay, endOfDay);
            tasksLiveData.postValue(tasks);
        }).start();
    }

    public void updateTask(Task task) {
        repository.update(task);
    }

    public void deleteTask(Task task, long currentSelectedDate) {
        repository.delete(task);
        // Chờ một chút để DB kịp xóa rồi load lại
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        loadTasksOnDate(currentSelectedDate);
    }

    public void refresh(long currentSelectedDate) {
        loadTasksOnDate(currentSelectedDate);
    }
}