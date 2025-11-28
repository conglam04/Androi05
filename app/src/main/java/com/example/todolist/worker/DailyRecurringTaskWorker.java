package com.example.todolist.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


import com.example.todolist.Data.Repository.TaskRepository;
import com.example.todolist.Data.entity.RecurrenceRule;
import com.example.todolist.Data.entity.Task;

import java.util.Calendar;
import java.util.List;

public class DailyRecurringTaskWorker extends Worker {

    private static final String TAG = "DailyRecurringTaskWorker";
    private final TaskRepository taskRepository;

    public DailyRecurringTaskWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        taskRepository = new TaskRepository((android.app.Application) context.getApplicationContext());
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Bắt đầu kiểm tra các công việc lặp lại hàng ngày...");

        try {
            List<Task> recurringTasks = taskRepository.getAllRecurringTasksSync();
            if (recurringTasks == null || recurringTasks.isEmpty()) {
                Log.d(TAG, "Không tìm thấy công việc lặp lại nào.");
                return Result.success();
            }

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            for (Task templateTask : recurringTasks) {
                RecurrenceRule rule = taskRepository.getRecurrenceRuleByTaskIdSync(templateTask.getTaskId());
                if (rule == null || "NONE".equals(rule.getPattern())) {
                    continue; // Bỏ qua nếu không có quy tắc hợp lệ
                }

                long nextOccurrenceDate = calculateNextOccurrence(templateTask, rule);

                // Chỉ tạo task nếu ngày lặp lại tiếp theo là hôm nay
                if (isSameDay(nextOccurrenceDate, today.getTimeInMillis())) {
                    boolean instanceExists = taskRepository.doesInstanceExist(templateTask.getTaskId(), nextOccurrenceDate);
                    boolean isCompleted = taskRepository.isInstanceCompleted(templateTask.getTaskId(), nextOccurrenceDate);

                    if (!instanceExists && !isCompleted) {
                        Log.d(TAG, "Tạo công việc mới cho: " + templateTask.getTitle() + " vào ngày: " + new java.util.Date(nextOccurrenceDate));
                        createNewInstanceFromTemplate(templateTask, nextOccurrenceDate);
                    } else {
                        Log.d(TAG, "Công việc cho '" + templateTask.getTitle() + "' hôm nay đã tồn tại hoặc đã hoàn thành.");
                    }
                }
            }

            Log.d(TAG, "Kiểm tra công việc lặp lại hàng ngày đã hoàn tất.");
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong quá trình tạo công việc lặp lại", e);
            return Result.failure();
        }
    }

    private void createNewInstanceFromTemplate(Task template, long instanceDueDate) {
        Task instance = new Task(template.getTitle(), template.getDescription(), template.getCategoryId());
        instance.setDueDate(instanceDueDate);
        instance.setParentTaskId((long) template.getTaskId()); // Liên kết với task gốc
        instance.setRecurring(false); // Bản thân task con không lặp lại
        instance.setIsStarred(template.getIsStarred());
        instance.setFlagged(template.isFlagged());

        taskRepository.insertTask(instance);
    }

    private long calculateNextOccurrence(Task templateTask, RecurrenceRule rule) {
        Calendar nextDate = Calendar.getInstance();
        if (templateTask.getDueDate() == null) {
            return -1; // Task lặp lại phải có ngày bắt đầu
        }
        nextDate.setTimeInMillis(templateTask.getDueDate());

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0); today.set(Calendar.MINUTE, 0); today.set(Calendar.SECOND, 0); today.set(Calendar.MILLISECOND, 0);

        // Tiến ngày cho đến khi nó bằng hoặc sau ngày hôm nay
        while (nextDate.before(today)) {
            boolean isCompleted = taskRepository.isInstanceCompleted(templateTask.getTaskId(), nextDate.getTimeInMillis());
            // Chỉ tăng ngày nếu công việc đã hoàn thành hoặc đã qua
            if (isCompleted || nextDate.before(today)) {
                 switch (rule.getPattern()) {
                    case "DAILY":
                        nextDate.add(Calendar.DAY_OF_YEAR, rule.getInterval());
                        break;
                    case "WEEKLY":
                        nextDate.add(Calendar.WEEK_OF_YEAR, rule.getInterval());
                        break;
                    case "MONTHLY":
                        nextDate.add(Calendar.MONTH, rule.getInterval());
                        break;
                    case "YEARLY":
                        nextDate.add(Calendar.YEAR, rule.getInterval());
                        break;
                    default:
                        return -1; // Kiểu không xác định
                }
            } else {
                // Nếu chưa hoàn thành và chưa qua, đây chính là ngày cần làm
                break;
            }
        }

        nextDate.set(Calendar.HOUR_OF_DAY, 0); nextDate.set(Calendar.MINUTE, 0); nextDate.set(Calendar.SECOND, 0); nextDate.set(Calendar.MILLISECOND, 0);
        return nextDate.getTimeInMillis();
    }

    private boolean isSameDay(long time1, long time2) {
        if (time1 <= 0 || time2 <= 0) return false;
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(time1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(time2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
