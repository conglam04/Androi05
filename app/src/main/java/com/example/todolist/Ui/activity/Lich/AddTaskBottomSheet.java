package com.example.todolist.Ui.activity.Lich;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.todolist.R;
import com.example.todolist.Data.Repository.TaskRepository;
import com.example.todolist.Data.entity.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;
import java.util.Locale;

public class AddTaskBottomSheet extends BottomSheetDialogFragment {

    public interface OnTaskAddedListener {
        void onTaskAdded();
    }

    private OnTaskAddedListener taskAddedListener;

    private EditText editTaskTitle;
    private FloatingActionButton btnSaveTask;
    private ImageButton btnSetTime;
    private ImageButton btnSetReminder;
    private TextView textCategoryLabel;

    private TaskRepository taskRepository;

    private Long selectedDueDate = null;
    private Long selectedTime = null;
    private Long selectedReminder = null;
    private String selectedRepeat = "Không";
    private String selectedCategoryName = "Khác";

    private String fallbackDate;
    private boolean dateTimePickerWasUsed = false;

    public static AddTaskBottomSheet newInstance(String selectedDate) {
        AddTaskBottomSheet fragment = new AddTaskBottomSheet();
        Bundle args = new Bundle();
        args.putString("SELECTED_DATE", selectedDate);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnTaskAddedListener(OnTaskAddedListener listener) {
        this.taskAddedListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fallbackDate = getArguments().getString("SELECTED_DATE");
        }
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
        taskRepository = new TaskRepository(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTaskTitle = view.findViewById(R.id.editTaskTitle);
        btnSaveTask = view.findViewById(R.id.btnSaveTask);
        btnSetTime = view.findViewById(R.id.btnSetTime);
        btnSetReminder = view.findViewById(R.id.btnSetReminder);
        textCategoryLabel = view.findViewById(R.id.textCategoryLabel);

        if(textCategoryLabel != null) textCategoryLabel.setText(selectedCategoryName);

        if (btnSetReminder != null) {
            btnSetReminder.setVisibility(View.VISIBLE);
            btnSetReminder.setOnClickListener(v -> showReminderPicker());
        }

        if (textCategoryLabel != null) {
            textCategoryLabel.setOnClickListener(v -> showCategoryPicker());
        }

        btnSetTime.setOnClickListener(v -> showDateTimePicker());
        btnSaveTask.setOnClickListener(v -> saveTask());

        checkNotificationPermission();
        editTaskTitle.requestFocus();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void showReminderPicker() {
        Calendar baseDateCal = Calendar.getInstance();

        if (selectedDueDate != null) {
            baseDateCal.setTimeInMillis(selectedDueDate);
        } else if (fallbackDate != null) {
            try {
                long fallbackMillis = Long.parseLong(fallbackDate);
                baseDateCal.setTimeInMillis(fallbackMillis);
            } catch (NumberFormatException e) {
            }
        }

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
                .setMinute(Calendar.getInstance().get(Calendar.MINUTE))
                .setTitleText("Đặt giờ nhắc nhở")
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            int h = picker.getHour();
            int m = picker.getMinute();

            Calendar reminderCal = (Calendar) baseDateCal.clone();
            reminderCal.set(Calendar.HOUR_OF_DAY, h);
            reminderCal.set(Calendar.MINUTE, m);
            reminderCal.set(Calendar.SECOND, 0);
            reminderCal.set(Calendar.MILLISECOND, 0);

            this.selectedReminder = reminderCal.getTimeInMillis();

            if (btnSetReminder != null) {
                btnSetReminder.setColorFilter(requireContext().getColor(R.color.primary));
            }

            String debugDate = String.format(Locale.getDefault(), "%02d:%02d %d/%d",
                    h, m, reminderCal.get(Calendar.DAY_OF_MONTH), reminderCal.get(Calendar.MONTH) + 1);
            Toast.makeText(requireContext(), "Nhắc nhở: " + debugDate, Toast.LENGTH_SHORT).show();
        });
        picker.show(getParentFragmentManager(), "ReminderPicker");
    }

    private void showCategoryPicker() {
        CategoryBottomSheet picker = new CategoryBottomSheet();
        picker.setCategorySelectedListener(category -> {
            this.selectedCategoryName = category;
            if (textCategoryLabel != null) {
                textCategoryLabel.setText(category);
            }
        });
        picker.show(getParentFragmentManager(), "CategoryPicker");
    }

    private void showDateTimePicker() {
        String strDate = null;
        String strTime = null;
        String strReminder = null;

        java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        java.text.DateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm", Locale.getDefault());

        long displayDateMillis = -1;

        if (selectedDueDate != null) {
            displayDateMillis = selectedDueDate;
        } else if (fallbackDate != null) {
            try {
                displayDateMillis = Long.parseLong(fallbackDate);
            } catch (NumberFormatException e) {
            }
        }

        if (displayDateMillis != -1) {
            strDate = dateFormat.format(new java.util.Date(displayDateMillis));
        }

        if (selectedTime != null) {
            strTime = timeFormat.format(new java.util.Date(selectedTime));
        }
        if (selectedReminder != null) {
            strReminder = timeFormat.format(new java.util.Date(selectedReminder));
        }

        DateTimePickerBottomSheet picker = DateTimePickerBottomSheet.newInstance(strDate, strTime, strReminder, selectedRepeat);

        picker.setOnDateTimeSelectedListener((dateMillis, timeMillis, reminderMillis, repeatRule) -> {
            this.dateTimePickerWasUsed = true;

            if (dateMillis != null) {
                selectedDueDate = dateMillis;
                String s = android.text.format.DateFormat.getDateFormat(requireContext())
                        .format(new java.util.Date(dateMillis));
                Toast.makeText(requireContext(), "Hẹn: " + s, Toast.LENGTH_SHORT).show();
                btnSetTime.setColorFilter(requireContext().getColor(R.color.primary));
            } else {
                selectedDueDate = null;
                btnSetTime.clearColorFilter();
            }

            selectedTime = timeMillis;

            if (reminderMillis != null) {
                selectedReminder = reminderMillis;
                if (btnSetReminder != null) btnSetReminder.setColorFilter(requireContext().getColor(R.color.primary));
            } else {
                selectedReminder = null;
            }

            if (repeatRule != null) {
                selectedRepeat = repeatRule;
            }
        });
        picker.show(getParentFragmentManager(), "DateTimePicker");
    }

    private void saveTask() {
        String title = editTaskTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Nhập tên nhiệm vụ", Toast.LENGTH_SHORT).show();
            return;
        }

        Long finalDueDate = null;

        // 1. Tính toán ngày hạn
        Calendar cal = Calendar.getInstance();
        if (dateTimePickerWasUsed && selectedDueDate != null) {
            cal.setTimeInMillis(selectedDueDate);
        } else {
            try {
                if (fallbackDate != null) {
                    cal.setTimeInMillis(Long.parseLong(fallbackDate));
                }
            } catch (NumberFormatException e) {
                // now
            }
        }

        // Reset giờ về 00:00 mặc định
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // 2. Ghép giờ nếu người dùng đã chọn Giờ (selectedTime != null)
        if (dateTimePickerWasUsed && selectedTime != null) {
            Calendar timeCal = Calendar.getInstance();
            timeCal.setTimeInMillis(selectedTime);
            cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        }

        finalDueDate = cal.getTimeInMillis();

        Task newTask = new Task(title, "", finalDueDate);

        // 3. Xử lý nhắc nhở (Ghép vào ngày của Task)
        Long finalReminderTime = null;
        if (selectedReminder != null) {
            Calendar reminderTimeCal = Calendar.getInstance();
            reminderTimeCal.setTimeInMillis(selectedReminder);
            int remHour = reminderTimeCal.get(Calendar.HOUR_OF_DAY);
            int remMinute = reminderTimeCal.get(Calendar.MINUTE);

            Calendar finalReminderCal = Calendar.getInstance();
            finalReminderCal.setTimeInMillis(finalDueDate); // Lấy ngày/tháng/năm của Task

            finalReminderCal.set(Calendar.HOUR_OF_DAY, remHour);
            finalReminderCal.set(Calendar.MINUTE, remMinute);
            finalReminderCal.set(Calendar.SECOND, 0);
            finalReminderCal.set(Calendar.MILLISECOND, 0);

            finalReminderTime = finalReminderCal.getTimeInMillis();
            newTask.setReminderTime(finalReminderTime);
        } else {
            newTask.setReminderTime(null);
        }

        // --- 4. KIỂM TRA HỢP LỆ (VALIDATION) ---
        // Nếu có đặt Giờ cho Task (selectedTime != null) VÀ có đặt Nhắc nhở
        if (dateTimePickerWasUsed && selectedTime != null && finalReminderTime != null) {
            if (finalReminderTime > finalDueDate) {
                // Nếu giờ nhắc nhở > giờ hạn
                Toast.makeText(requireContext(), "Giờ nhắc nhở phải trước hoặc bằng giờ nhiệm vụ!", Toast.LENGTH_SHORT).show();
                return; // Dừng lại, không lưu
            }
        }
        // ---------------------------------------

        newTask.setRepeatRule(selectedRepeat);

        // 5. Lưu vào Database
        new Thread(() -> {
            try {
                int catId = taskRepository.getCategoryIdByName(selectedCategoryName);
                newTask.setCategoryId(catId);

                long id = taskRepository.insert(newTask);
                newTask.setTaskId((int) id);

                if (selectedReminder != null) {
                    requireActivity().runOnUiThread(() ->
                            AlarmScheduler.scheduleReminder(requireContext(), newTask)
                    );
                }

                requireActivity().runOnUiThread(() -> {
                    if (taskAddedListener != null) {
                        taskAddedListener.onTaskAdded();
                    }
                    dismiss();
                });
            } catch (Exception e) {
                Log.e("AddTask", "Lỗi lưu task", e);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }
}