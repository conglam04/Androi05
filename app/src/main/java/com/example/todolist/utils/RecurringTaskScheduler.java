package com.example.todolist.utils;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.todolist.worker.DailyRecurringTaskWorker;

import java.util.concurrent.TimeUnit;

public class RecurringTaskScheduler {

    private static final String DAILY_RECURRING_TASK_WORK_TAG = "daily_recurring_task_work";

    public static void scheduleDailyTaskCreation(Context context) {
        // Đảm bảo context là application context để tránh memory leak
        Context appContext = context.getApplicationContext();

        // Tạo Constraints cho công việc
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresCharging(false)
                .build();

        // Tạo một PeriodicWorkRequest để chạy mỗi 24 giờ
        PeriodicWorkRequest dailyWorkRequest = new PeriodicWorkRequest.Builder(
                DailyRecurringTaskWorker.class, 24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();

        // Lên lịch cho công việc, sử dụng KEEP để không tạo lại nếu đã tồn tại
        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
                DAILY_RECURRING_TASK_WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
        );
    }
}
