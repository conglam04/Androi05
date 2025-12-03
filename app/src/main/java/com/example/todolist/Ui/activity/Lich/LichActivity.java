package com.example.todolist.Ui.activity.Lich;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.Data.Repository.TaskRepository;
import com.example.todolist.Data.entity.Task;
import com.example.todolist.R;
import com.example.todolist.Ui.activity.BaseActivity;
import com.example.todolist.Ui.activity.Lich.AddTaskBottomSheet;
import com.example.todolist.Ui.activity.Lich.AlarmScheduler;
import com.example.todolist.Ui.activity.Lich.DateTimePickerBottomSheet;
import com.example.todolist.Ui.adapter.TaskAdapter;
import com.example.todolist.Ui.widget.EventDecorator;
import com.example.todolist.ViewModel.LichViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LichActivity extends BaseActivity implements
        AddTaskBottomSheet.OnTaskAddedListener,
        TaskAdapter.OnTaskClickListener {

    private MaterialCalendarView calendarView;
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private LichViewModel viewModel;
    private long selectedDateMillis;
    private TaskRepository taskRepository;

    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat debugDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    private Task taskToEdit = null;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lich);
        setupBottomNav(R.id.navigation_calendar);

        taskRepository = new TaskRepository(getApplication());

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (!isGranted)
                Toast.makeText(this, "Bạn sẽ không nhận được thông báo!", Toast.LENGTH_SHORT).show();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);
                if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        requestNotificationPermission();

        viewModel = new ViewModelProvider(this).get(LichViewModel.class);
        viewModel.getTasksLiveData().observe(this, tasks -> {
            adapter.updateTasksWithCategory(tasks);
            updateCalendarDots();
        });

        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recyclerViewTasks);
        FloatingActionButton btnAdd = findViewById(R.id.btnAdd);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        Calendar instance = Calendar.getInstance();
        calendarView.setSelectedDate(CalendarDay.from(
                instance.get(Calendar.YEAR),
                instance.get(Calendar.MONTH) + 1,
                instance.get(Calendar.DAY_OF_MONTH)
        ));
        selectedDateMillis = instance.getTimeInMillis();

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(date.getYear(), date.getMonth() - 1, date.getDay());
            selectedDateMillis = cal.getTimeInMillis();
            viewModel.loadTasksOnDate(selectedDateMillis);
        });

        btnAdd.setOnClickListener(v -> {
            AddTaskBottomSheet bottomSheet = AddTaskBottomSheet.newInstance(String.valueOf(selectedDateMillis));
            bottomSheet.setOnTaskAddedListener(this);
            bottomSheet.show(getSupportFragmentManager(), "AddTaskBottomSheet");
        });

        viewModel.loadTasksOnDate(selectedDateMillis);
        updateCalendarDots();
    }

    private void updateCalendarDots() {
        new Thread(() -> {
            try {
                List<Long> millisList = taskRepository.getAllTaskDates();
                List<CalendarDay> dates = new ArrayList<>();
                if (millisList != null) {
                    for (Long millis : millisList) {
                        if (millis != null) {
                            Calendar c = Calendar.getInstance();
                            c.setTimeInMillis(millis);
                            dates.add(CalendarDay.from(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)));
                        }
                    }
                }
                runOnUiThread(() -> {
                    if (calendarView != null) {
                        calendarView.removeDecorators();
                        calendarView.addDecorator(new EventDecorator(getColor(R.color.primary), dates));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @Override
    public void onTaskAdded() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            viewModel.refresh(selectedDateMillis);
            updateCalendarDots();
            Toast.makeText(this, "Đã thêm nhiệm vụ", Toast.LENGTH_SHORT).show();
        }, 300);
    }

    @Override
    public void onTaskStatusChanged(Task task) {
        viewModel.updateTask(task);

        if (task.getIsCompleted() == 1) {
            AlarmScheduler.cancelReminder(this, task);
            Toast.makeText(this, "Đã xong! Đã tắt nhắc nhở", Toast.LENGTH_SHORT).show();
        } else {
            if (task.getReminderTime() != null && task.getReminderTime() > System.currentTimeMillis()) {
                AlarmScheduler.scheduleReminder(this, task);
                Toast.makeText(this, "Đã kích hoạt lại nhắc nhở", Toast.LENGTH_SHORT).show();
            }
        }

        new Handler(Looper.getMainLooper()).postDelayed(this::updateCalendarDots, 200);
    }

    @Override
    public void onTaskClick(Task task) {
        this.taskToEdit = task;
        String displayDate = "";
        String displayTime = "";
        String displayReminder = null;

        if (task.getDueDate() != null && task.getDueDate() > 0) {
            Date d = new Date(task.getDueDate());
            displayDate = displayDateFormat.format(d);
            displayTime = displayTimeFormat.format(d);
        }

        if (task.getReminderTime() != null && task.getReminderTime() > 0) {
            Date r = new Date(task.getReminderTime());
            displayReminder = displayTimeFormat.format(r);
        }

        DateTimePickerBottomSheet picker = DateTimePickerBottomSheet.newInstance(
                displayDate, displayTime, displayReminder, task.getRepeatRule()
        );

        picker.setOnDateTimeSelectedListener((dateMillis, timeMillis, reminderMillis, repeatRule) -> {
            try {
                if (taskToEdit == null) return;

                long finalDueDate = 0;
                if (dateMillis != null) {
                    Calendar finalCal = Calendar.getInstance();
                    finalCal.setTimeInMillis(dateMillis);
                    if (timeMillis != null) {
                        Calendar timeCal = Calendar.getInstance();
                        timeCal.setTimeInMillis(timeMillis);
                        finalCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
                        finalCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
                        finalCal.set(Calendar.SECOND, 0);
                    } else {
                        finalCal.set(Calendar.HOUR_OF_DAY, 0);
                        finalCal.set(Calendar.MINUTE, 0);
                    }
                    finalDueDate = finalCal.getTimeInMillis();
                    taskToEdit.setDueDate(finalDueDate);
                } else {
                    taskToEdit.setDueDate(null);
                }

                if (reminderMillis != null) {
                    Calendar timePickerCal = Calendar.getInstance();
                    timePickerCal.setTimeInMillis(reminderMillis);
                    int hour = timePickerCal.get(Calendar.HOUR_OF_DAY);
                    int minute = timePickerCal.get(Calendar.MINUTE);

                    Calendar finalReminderCal = Calendar.getInstance();
                    if (taskToEdit.getDueDate() != null) {
                        Calendar dueCal = Calendar.getInstance();
                        dueCal.setTimeInMillis(taskToEdit.getDueDate());
                        finalReminderCal.set(Calendar.YEAR, dueCal.get(Calendar.YEAR));
                        finalReminderCal.set(Calendar.MONTH, dueCal.get(Calendar.MONTH));
                        finalReminderCal.set(Calendar.DAY_OF_MONTH, dueCal.get(Calendar.DAY_OF_MONTH));
                    }
                    finalReminderCal.set(Calendar.HOUR_OF_DAY, hour);
                    finalReminderCal.set(Calendar.MINUTE, minute);
                    finalReminderCal.set(Calendar.SECOND, 0);
                    finalReminderCal.set(Calendar.MILLISECOND, 0);

                    taskToEdit.setReminderTime(finalReminderCal.getTimeInMillis());
                } else {
                    taskToEdit.setReminderTime(null);
                }

                taskToEdit.setRepeatRule(repeatRule);
                viewModel.updateTask(taskToEdit);

                if (taskToEdit.getReminderTime() != null) {
                    if (taskToEdit.getIsCompleted() == 1) {
                        AlarmScheduler.cancelReminder(this, taskToEdit);
                        Toast.makeText(this, "Task đã xong, không đặt báo thức", Toast.LENGTH_SHORT).show();
                    } else if (taskToEdit.getReminderTime() > System.currentTimeMillis()) {
                        try {
                            AlarmScheduler.scheduleReminder(this, taskToEdit);
                            Toast.makeText(this, "Đã đặt nhắc nhở", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(this, "Thời gian nhắc nhở đã qua", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    AlarmScheduler.cancelReminder(this, taskToEdit);
                }

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        viewModel.refresh(selectedDateMillis);
                        updateCalendarDots();
                    }
                }, 300);

                Toast.makeText(this, "Đã cập nhật nhiệm vụ!", Toast.LENGTH_SHORT).show();
                taskToEdit = null;

            } catch (Exception e) {
                Log.e("LichActivity", "Critical Error: " + e.getMessage());
                e.printStackTrace();
                Toast.makeText(this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
            }
        });

        picker.show(getSupportFragmentManager(), "DateTimePicker_Edit");
    }

    @Override
    public void onTaskLongClick(Task task) {
        boolean isCompleted = (task.getIsCompleted() == 1);
        String statusOption = isCompleted ? "Đánh dấu chưa xong" : "Đánh dấu đã xong";
        String[] options = {statusOption, "Xóa nhiệm vụ"};

        new AlertDialog.Builder(this)
                .setTitle(task.getTitle())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        task.setIsCompleted(isCompleted ? 0 : 1);
                        viewModel.updateTask(task);

                        if (task.getIsCompleted() == 1) {
                            AlarmScheduler.cancelReminder(this, task);
                            Toast.makeText(this, "Đã xong! Đã tắt nhắc nhở", Toast.LENGTH_SHORT).show();
                        } else {
                            if (task.getReminderTime() != null && task.getReminderTime() > System.currentTimeMillis()) {
                                AlarmScheduler.scheduleReminder(this, task);
                                Toast.makeText(this, "Đã bật lại nhắc nhở", Toast.LENGTH_SHORT).show();
                            }
                        }

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            viewModel.refresh(selectedDateMillis);
                            updateCalendarDots();
                        }, 200);
                    } else if (which == 1) {
                        new AlertDialog.Builder(this)
                                .setTitle("Xác nhận xóa")
                                .setMessage("Bạn có chắc muốn xóa nhiệm vụ này không?")
                                .setPositiveButton("Xóa", (d, w) -> {
                                    viewModel.deleteTask(task, selectedDateMillis);
                                    AlarmScheduler.cancelReminder(this, task);
                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                        viewModel.refresh(selectedDateMillis);
                                        updateCalendarDots();
                                        Toast.makeText(this, "Đã xóa nhiệm vụ", Toast.LENGTH_SHORT).show();
                                    }, 200);
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                    }
                })
                .show();
    }
}
