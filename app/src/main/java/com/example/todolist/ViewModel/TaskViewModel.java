package com.example.todolist.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.todolist.Data.Repository.TaskRepository;
import com.example.todolist.Data.entity.TaskWithCategory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository repository;
    private final ExecutorService executorService;

    // Sửa List<Task> thành List<TaskWithCategory>
    private final MutableLiveData<List<TaskWithCategory>> tasksLiveData = new MutableLiveData<>();
    private final MutableLiveData<LinkedHashMap<String, Integer>> statsLiveData = new MutableLiveData<>();

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    // CRUD - Chỉ dùng để load danh sách
    public LiveData<List<TaskWithCategory>> getAllTasks() {
        return tasksLiveData;
    }

    // Gọi hàm này để refresh dữ liệu từ DB lên UI
    public void loadTasks() {
        executorService.execute(() -> {
            List<TaskWithCategory> list = repository.getAllTasks();
            tasksLiveData.postValue(list);
        });
    }

    public int getCompletedCount() { return repository.getCompletedCount(); }

    public int getNotCompletedCount() { return repository.getNotCompletedCount(); }

    public void loadCompletedTaskStats(int days) {
        executorService.execute(() -> {
            LinkedHashMap<String, Integer> data = repository.getCompletedTaskCountByDays(days);
            statsLiveData.postValue(data);
        });
    }

    public LiveData<LinkedHashMap<String, Integer>> getCompletedTaskStats() {
        return statsLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}