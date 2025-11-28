package com.example.todolist.Ui.activity.Lich;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.Data.entity.Task;
import com.example.todolist.R;
import com.example.todolist.Ui.activity.BaseActivity;
import com.example.todolist.Ui.adapter.TaskAdapter;
import com.example.todolist.ViewModel.LichViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LichActivity extends BaseActivity implements
        AddTaskBottomSheet.OnTaskAddedListener,
        TaskAdapter.OnTaskClickListener {

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private LichViewModel viewModel;
    private long selectedDateMillis;

    // Format hiển thị
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat displayTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private Task taskToEdit = null;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lich);
        setupBottomNav(R.id.navigation_calendar);

        // Xin quyền thông báo (Android 13+)
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (!isGranted) Toast.makeText(this, "Bạn sẽ không nhận được thông báo!", Toast.LENGTH_SHORT).show();
        });
        requestNotificationPermission();

        // Init ViewModel
        viewModel = new ViewModelProvider(this).get(LichViewModel.class);

        // Observe dữ liệu
        viewModel.getTasksLiveData().observe(this, tasks -> {
            adapter.updateTasks(tasks);
        });

        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recyclerViewTasks);
        FloatingActionButton btnAdd = findViewById(R.id.btnAdd);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Mặc định chọn hôm nay
        Calendar today = Calendar.getInstance();
        selectedDateMillis = today.getTimeInMillis();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            selectedDateMillis = cal.getTimeInMillis();
            viewModel.loadTasksOnDate(selectedDateMillis);
        });

        btnAdd.setOnClickListener(v -> {
            AddTaskBottomSheet bottomSheet = AddTaskBottomSheet.newInstance(String.valueOf(selectedDateMillis));
            bottomSheet.setOnTaskAddedListener(this);
            bottomSheet.show(getSupportFragmentManager(), "AddTaskBottomSheet");
        });

        // Load lần đầu
        viewModel.loadTasksOnDate(selectedDateMillis);
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
        // Delay refresh để chờ DB lưu xong
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            viewModel.refresh(selectedDateMillis);
            Toast.makeText(this, "Đã thêm nhiệm vụ", Toast.LENGTH_SHORT).show();
        }, 300);
    }

    @Override
    public void onTaskStatusChanged(Task task) {
        viewModel.updateTask(task);
    }

    @Override
    public void onTaskClick(Task task) {
        this.taskToEdit = task;
        String displayDate = "";
        String displayTime = "";
        String displayReminder = null; // Biến String để chứa giờ nhắc

        // 1. Format Ngày Hạn (Due Date)
        if (task.getDueDate() != null && task.getDueDate() > 0) {
            Date d = new Date(task.getDueDate());
            displayDate = displayDateFormat.format(d);
            displayTime = displayTimeFormat.format(d);
        }

        // 2. SỬA LỖI Ở ĐÂY: Chuyển Long -> String cho ReminderTime
        if (task.getReminderTime() != null && task.getReminderTime() > 0) {
            Date r = new Date(task.getReminderTime());
            displayReminder = displayTimeFormat.format(r); // Ví dụ: "14:30"
        }

        // Mở Dialog chỉnh sửa với các tham số String
        DateTimePickerBottomSheet picker = DateTimePickerBottomSheet.newInstance(
                displayDate,
                displayTime,
                displayReminder, // Truyền chuỗi String đã format
                task.getRepeatRule() // Truyền quy tắc lặp lại
        );

        picker.setOnDateTimeSelectedListener((dateMillis, timeMillis, reminderMillis, repeatRule) -> {
            if (taskToEdit == null) return;

            // Cập nhật Due Date
            if (dateMillis != null) {
                Calendar finalCal = Calendar.getInstance();
                finalCal.setTimeInMillis(dateMillis);
                if (timeMillis != null) {
                    Calendar timeCal = Calendar.getInstance();
                    timeCal.setTimeInMillis(timeMillis);
                    finalCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
                    finalCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
                    finalCal.set(Calendar.SECOND, 0);
                    finalCal.set(Calendar.MILLISECOND, 0);
                }
                taskToEdit.setDueDate(finalCal.getTimeInMillis());
            } else {
                taskToEdit.setDueDate(null);
            }

            // Cập nhật Reminder và Repeat
            taskToEdit.setReminderTime(reminderMillis);
            taskToEdit.setRepeatRule(repeatRule);

            // Cập nhật UI ngay lập tức
            adapter.notifyDataSetChanged();

            // Lưu xuống DB
            viewModel.updateTask(taskToEdit);

            // Cài đặt hoặc hủy báo thức
            if (reminderMillis != null) {
                AlarmScheduler.scheduleReminder(this, taskToEdit);
            } else {
                AlarmScheduler.cancelReminder(this, taskToEdit);
            }

            // Refresh lại danh sách sau một chút
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                viewModel.refresh(selectedDateMillis);
            }, 300);

            taskToEdit = null;
            Toast.makeText(this, "Đã cập nhật!", Toast.LENGTH_SHORT).show();
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
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            viewModel.refresh(selectedDateMillis);
                        }, 200);
                    } else if (which == 1) {
                        // Hủy báo thức trước khi xóa
                        AlarmScheduler.cancelReminder(this, task);
                        viewModel.deleteTask(task, selectedDateMillis);
                        Toast.makeText(this, "Đã xóa nhiệm vụ", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}