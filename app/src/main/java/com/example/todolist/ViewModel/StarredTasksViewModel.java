package com.example.todolist.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.todolist.Data.Repository.TaskRepository;
import com.example.todolist.Data.entity.Task;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StarredTasksViewModel extends AndroidViewModel {

    private final TaskRepository repository;
    private final ExecutorService executorService;
    private final MutableLiveData<List<Task>> starredTasks = new MutableLiveData<>();

    public StarredTasksViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        executorService = Executors.newSingleThreadExecutor();
        loadStarredTasks();
    }

    public LiveData<List<Task>> getStarredTasks() {
        return starredTasks;
    }

    private void loadStarredTasks() {
        executorService.execute(() -> {
            List<Task> tasks = repository.getStarredTasks();
            starredTasks.postValue(tasks);
        });
    }

    public void updateTask(Task task) {
        repository.update(task);
    }
}
