package com.example.todolist.Ui.maintaskfragement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.example.todolist.Data.entity.Category;
import com.example.todolist.Data.entity.RecurrenceRule;
import com.example.todolist.Data.entity.Task;
import com.example.todolist.R;
import com.example.todolist.Ui.viewmodel.TasksViewModel;
import com.example.todolist.utils.ReminderScheduler;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddTaskBottomSheet extends BottomSheetDialogFragment {

    private EditText editTaskTitle;
    private TextView textCategoryLabel;
    private ImageButton btnSetTime;
    private ImageButton btnSetReminder;
    private ImageButton btnSetRepeat;
    private ImageButton btnAddSubtask;
    private ImageButton btnSetTag;
    private ImageButton btnIdea;
    private FloatingActionButton btnSaveTask;

    private TasksViewModel viewModel;
    private List<Category> categories = new ArrayList<>();
    private Long selectedDueDate = null;
    private Long selectedReminderDate = null;
    private int selectedCategoryId = -1;  // Changed from long to int to match Task entity
    private String selectedCategoryName = "không có thẻ loại";
    private RecurrenceRule selectedRecurrenceRule;

    public static AddTaskBottomSheet newInstance() {
        return new AddTaskBottomSheet();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);

        // Initialize views
        initViews(view);

        // Setup listeners
        setupListeners();

        // Load categories
        loadCategories();

        // Auto focus on edit text
        editTaskTitle.requestFocus();
    }

    private void initViews(View view) {
        editTaskTitle = view.findViewById(R.id.editTaskTitle);
        textCategoryLabel = view.findViewById(R.id.textCategoryLabel);
        btnSetTime = view.findViewById(R.id.btnSetTime);
        btnSetReminder = view.findViewById(R.id.btnSetReminder);
        btnSetRepeat = view.findViewById(R.id.btnSetRepeat);
        btnAddSubtask = view.findViewById(R.id.btnAddSubtask);
        btnSetTag = view.findViewById(R.id.btnSetTag);
        btnIdea = view.findViewById(R.id.btnIdea);
        btnSaveTask = view.findViewById(R.id.btnSaveTask);
    }

    private void setupListeners() {
        textCategoryLabel.setOnClickListener(v -> showCategoryPicker());
        btnSetTime.setOnClickListener(v -> showDateTimePicker());
        btnSetReminder.setOnClickListener(v -> showReminderTimePicker());
        btnSetRepeat.setOnClickListener(v ->
        {
            RecurrenceBottomSheet bottomSheet = RecurrenceBottomSheet.newInstance(selectedRecurrenceRule);
            bottomSheet.setOnRecurrenceSelectedListener(rule -> {
                selectedRecurrenceRule = rule;
            });
            bottomSheet.show(getParentFragmentManager(), "RecurrenceBottomSheet");
        });
        btnAddSubtask.setOnClickListener(v -> showAddSubtaskDialog());
        btnSetTag.setOnClickListener(v -> showTagDialog());
        btnIdea.setOnClickListener(v -> toggleIdeaMode());
        btnSaveTask.setOnClickListener(v -> saveTask());
    }

    private void loadCategories() {
        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categoryList -> {
            if (categoryList != null && !categoryList.isEmpty()) {
                categories = categoryList;

                // Set default category from current filter
                viewModel.getSelectedCategoryId().observe(getViewLifecycleOwner(), selectedId -> {
                    if (selectedId != null && selectedId != -1) {
                        for (Category category : categories) {
                            if (category.getCategoryId() == selectedId) {
                                selectedCategoryId = category.getCategoryId();
                                selectedCategoryName = category.getName();
                                textCategoryLabel.setText(selectedCategoryName);
                                break;
                            }
                        }
                    }
                });
            }
        });
    }
    private void showDateTimePicker()
    {
        DateTimePickerBottomSheet picker = DateTimePickerBottomSheet.newInstance(
                selectedDueDate,
                selectedReminderDate,
                selectedRecurrenceRule
        );
        picker.setOnDateTimeSelectedListener((timestamp, reminderTimestamp, repeatRule) -> {
            if (timestamp != -1) {
                selectedDueDate = timestamp;
                String s = android.text.format.DateFormat.getDateFormat(requireContext()).format(timestamp);
                Toast.makeText(requireContext(), "Đã chọn: " + s, Toast.LENGTH_SHORT).show();
                btnSetTime.setColorFilter(getResources().getColor(R.color.primary, null));
            } else {
                selectedDueDate = null;
                Toast.makeText(requireContext(), "Bỏ chọn ngày", Toast.LENGTH_SHORT).show();
                btnSetTime.clearColorFilter();
            }

            // Update reminder timestamp
            if (reminderTimestamp != null) {
                selectedReminderDate = reminderTimestamp;
                btnSetReminder.setColorFilter(getResources().getColor(R.color.primary, null));
            } else {
                selectedReminderDate = null;
                btnSetReminder.clearColorFilter();
            }

            // Update recurrence rule
            if (repeatRule != null) {
                selectedRecurrenceRule = repeatRule;
                btnSetRepeat.setColorFilter(getResources().getColor(R.color.primary, null));
            } else {
                selectedRecurrenceRule = null;
                btnSetRepeat.clearColorFilter();
            }
        });
        picker.show(getParentFragmentManager(), "DateTimePicker");
    }
    private void showCategoryPicker() {
        if (categories.isEmpty()) {
            Toast.makeText(requireContext(), "Chưa có danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] categoryNames = new String[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            categoryNames[i] = categories.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Chọn danh mục");
        builder.setItems(categoryNames, (dialog, which) -> {
            selectedCategoryId = categories.get(which).getCategoryId();
            selectedCategoryName = categories.get(which).getName();
            textCategoryLabel.setText(selectedCategoryName);
        });
        builder.show();
    }

    private void showReminderTimePicker() {
        // Get base date from selectedDueDate or current date
        Calendar baseDateCal = Calendar.getInstance();
        if (selectedDueDate != null) {
            baseDateCal.setTimeInMillis(selectedDueDate);
        }

        // Get current hour/minute from existing reminder or now
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        if (selectedReminderDate != null) {
            Calendar reminderCal = Calendar.getInstance();
            reminderCal.setTimeInMillis(selectedReminderDate);
            hour = reminderCal.get(Calendar.HOUR_OF_DAY);
            minute = reminderCal.get(Calendar.MINUTE);
        }

        // Use MaterialTimePicker (like in Lich package)
        com.google.android.material.timepicker.MaterialTimePicker picker =
                new com.google.android.material.timepicker.MaterialTimePicker.Builder()
                .setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText("Đặt giờ nhắc nhở")
                .setInputMode(com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK)
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            int h = picker.getHour();
            int m = picker.getMinute();

            // Create reminder time based on base date
            Calendar reminderCal = (Calendar) baseDateCal.clone();
            reminderCal.set(Calendar.HOUR_OF_DAY, h);
            reminderCal.set(Calendar.MINUTE, m);
            reminderCal.set(Calendar.SECOND, 0);
            reminderCal.set(Calendar.MILLISECOND, 0);

            long reminderTime = reminderCal.getTimeInMillis();

            // Validate: Reminder must be in future
            if (reminderTime <= System.currentTimeMillis()) {
                Toast.makeText(requireContext(),
                        "Thời gian nhắc nhở phải ở tương lai!",
                        Toast.LENGTH_LONG).show();
                return;
            }

            // Validate: Reminder must be before due time (if due date has specific time)
            if (selectedDueDate != null) {
                Calendar dueCal = Calendar.getInstance();
                dueCal.setTimeInMillis(selectedDueDate);
                // Only check if due date has specific time (not end of day)
                if (dueCal.get(Calendar.HOUR_OF_DAY) != 23 || dueCal.get(Calendar.MINUTE) != 59) {
                    if (reminderTime >= selectedDueDate) {
                        Toast.makeText(requireContext(),
                                "Thời gian nhắc nhở phải trước giờ deadline!",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }

            // Save reminder time
            selectedReminderDate = reminderTime;
            btnSetReminder.setColorFilter(getResources().getColor(R.color.primary, null));

            String timeStr = String.format(java.util.Locale.getDefault(), "%02d:%02d", h, m);
            Toast.makeText(requireContext(),
                    "Đã đặt nhắc nhở lúc " + timeStr,
                    Toast.LENGTH_SHORT).show();
        });

        picker.show(getParentFragmentManager(), "ReminderPicker");
    }


    private void showAddSubtaskDialog() {
        Toast.makeText(requireContext(),
                "Tính năng công việc con đang phát triển",
                Toast.LENGTH_SHORT).show();
    }

    private void showTagDialog() {
        Toast.makeText(requireContext(),
                "Tính năng thẻ tag đang phát triển",
                Toast.LENGTH_SHORT).show();
    }

    private void toggleIdeaMode() {
        Toast.makeText(requireContext(),
                "Chế độ ý tưởng",
                Toast.LENGTH_SHORT).show();
    }

    private void saveTask() {
        String title = editTaskTitle.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Vui lòng nhập tên nhiệm vụ",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure we have a valid category before proceeding
        if (selectedCategoryId == -1) {
            if (!categories.isEmpty()) {
                selectedCategoryId = categories.get(0).getCategoryId();
                selectedCategoryName = categories.get(0).getName();
            } else {
                Toast.makeText(requireContext(),
                        "Đang tải danh mục, vui lòng thử lại",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Set default due date if not set
        if (selectedDueDate == null) {
            // Set default due date: today at 23:59 (end of day)
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            selectedDueDate = cal.getTimeInMillis();
        }

        // Create new task with proper Integer categoryId
        Task newTask = new Task(title, "", selectedCategoryId);

        // Explicitly set categoryId again to ensure it's saved
        newTask.setCategoryId(selectedCategoryId);

        // Debug log
        android.util.Log.d("AddTaskBottomSheet", "Saving task with categoryId: " + selectedCategoryId +
                " (categoryName: " + selectedCategoryName + ")");
        android.util.Log.d("AddTaskBottomSheet", "Task object categoryId: " + newTask.getCategoryId());

        // Set due date and reminder date to the task
        newTask.setDueDate(selectedDueDate);
        if (selectedReminderDate != null) {
            newTask.setReminderDate(selectedReminderDate);
        }

        // QUAN TRỌNG: Insert task TRƯỚC, sau đó mới save recurrence rule
        long taskId = viewModel.insertTask(newTask);

        if (taskId > 0) {
            // Set taskId cho newTask
            newTask.setTaskId((int) taskId);

            // Bây giờ mới save recurrence rule
            if (selectedRecurrenceRule != null && !selectedRecurrenceRule.getPattern().equals("NONE")) {
                selectedRecurrenceRule.setTaskId(taskId); // Set taskId vào rule
                viewModel.saveRecurrenceRule(newTask, selectedRecurrenceRule);
            }

            // Schedule reminder notification if user set a reminder time
            if (selectedReminderDate != null && selectedReminderDate > System.currentTimeMillis()) {
                ReminderScheduler.scheduleReminder(
                        requireContext(),
                        taskId,
                        title,
                        "", // description - can be empty for now
                        selectedReminderDate
                );
                Toast.makeText(requireContext(),
                        "Đã thêm nhiệm vụ và đặt nhắc nhở",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(),
                        "Đã thêm nhiệm vụ" +
                                (newTask.isRecurring() ? " (lặp lại)" : ""),
                        Toast.LENGTH_SHORT).show();
            }

            dismiss();
        } else {
            Toast.makeText(requireContext(),
                    "Lỗi khi thêm nhiệm vụ",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }


}
