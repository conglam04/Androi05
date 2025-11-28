package com.example.todolist.Ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.todolist.Data.Repository.CategoryRepository;
import com.example.todolist.Data.Repository.TaskRepository;
import com.example.todolist.Data.entity.Category;
import com.example.todolist.Data.entity.RecurrenceRule;
import com.example.todolist.Data.entity.Task;
import com.example.todolist.Data.entity.TaskWithCategory;
import com.example.todolist.R;

import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class TasksViewModel extends AndroidViewModel {
    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final LiveData<List<Category>> allCategories;

    private final MutableLiveData<Long> selectedCategoryId = new MutableLiveData<>(-1L);
    private final MutableLiveData<Integer> dateFilterChipId = new MutableLiveData<>(R.id.chipToday);

    private final LiveData<List<TaskWithCategory>> filteredTasks;
    private final LiveData<List<TaskWithCategory>> pastTasks;
    private final LiveData<List<TaskWithCategory>> todayTasks;
    private final LiveData<List<TaskWithCategory>> futureTasks;

    public TasksViewModel(@NonNull Application application) {
        super(application);
        taskRepository = new TaskRepository(application);
        categoryRepository = new CategoryRepository(application);
        allCategories = categoryRepository.getAllCategories();

        MediatorLiveData<Pair<Long, Integer>> filters = new MediatorLiveData<>();
        filters.addSource(selectedCategoryId, categoryId -> filters.setValue(new Pair<>(categoryId, dateFilterChipId.getValue())));
        filters.addSource(dateFilterChipId, chipId -> filters.setValue(new Pair<>(selectedCategoryId.getValue(), chipId)));

        LiveData<Pair<Long, Integer>> distinctFilters = Transformations.distinctUntilChanged(filters);

        filteredTasks = Transformations.switchMap(distinctFilters, filter ->
                taskRepository.getTasks(filter.first, filter.second));

        pastTasks = Transformations.map(filteredTasks, this::filterPastTasks);
        todayTasks = Transformations.map(filteredTasks, this::filterTodayTasks);
        futureTasks = Transformations.map(filteredTasks, this::filterFutureTasks);
    }

    private List<TaskWithCategory> filterPastTasks(List<TaskWithCategory> tasks) {
        Calendar today = getStartOfToday();
        long todayMillis = today.getTimeInMillis();
        return tasks.stream()
                .filter(task -> task.task.getDueDate() < todayMillis)
                .collect(Collectors.toList());
    }

    private List<TaskWithCategory> filterTodayTasks(List<TaskWithCategory> tasks) {
        Calendar today = getStartOfToday();
        long startOfDay = today.getTimeInMillis();
        today.add(Calendar.DAY_OF_YEAR, 1);
        long endOfDay = today.getTimeInMillis();

        return tasks.stream()
                .filter(task -> task.task.getDueDate() >= startOfDay && task.task.getDueDate() < endOfDay)
                .collect(Collectors.toList());
    }

    private List<TaskWithCategory> filterFutureTasks(List<TaskWithCategory> tasks) {
        Calendar today = getStartOfToday();
        today.add(Calendar.DAY_OF_YEAR, 1);
        long startOfTomorrow = today.getTimeInMillis();

        return tasks.stream()
                .filter(task -> task.task.getDueDate() >= startOfTomorrow)
                .collect(Collectors.toList());
    }

    private Calendar getStartOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public LiveData<List<TaskWithCategory>> getPastTasks() {
        return pastTasks;
    }

    public LiveData<List<TaskWithCategory>> getTodayTasks() {
        return todayTasks;
    }

    public LiveData<List<TaskWithCategory>> getFutureTasks() {
        return futureTasks;
    }

    public void selectCategory(long categoryId) {
        selectedCategoryId.setValue(categoryId);
    }

    public void setDateFilter(int chipId) {
        dateFilterChipId.setValue(chipId);
    }

    public LiveData<Long> getSelectedCategoryId() {
        return selectedCategoryId;
    }

    public long insertTask(Task task) {
        return taskRepository.insertTask(task);
    }

    public void updateTask(Task task) {
        taskRepository.updateTask(task);
    }

    public void deleteTask(Task task) {
        taskRepository.deleteTask(task);
    }

    public void toggleTaskCompletion(Task task) {
        taskRepository.toggleTaskCompletion(task.getTaskId(), task.getIsCompleted()==1 ? 0 : 1);
    }

    public void toggleTaskFlag(Task task) {
        taskRepository.toggleTaskFlag(task.getTaskId(), !task.isFlagged());
    }

    public void toggleTaskStar(Task task) {
        taskRepository.toggleTaskStar(task.getTaskId(), task.getIsStarred()==1 ? 0 : 1);
    }

    public void saveRecurrenceRule(Task task, RecurrenceRule rule) {
        taskRepository.saveRecurrenceRule(task, rule);
    }

    public LiveData<List<Category>> getAllCategories() {
        return allCategories;
    }
}
