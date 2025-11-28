package com.example.todolist.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.todolist.receiver.NotificationReceiver;


public class ReminderScheduler {

    private static final String TAG = "ReminderScheduler";

    /**
     * Schedule a reminder notification for a task
     *
     * @param context Application context
     * @param taskId Unique task ID
     * @param taskTitle Title of the task
     * @param taskDescription Description of the task
     * @param reminderTimeMillis Time in milliseconds when to show notification
     */
    public static void scheduleReminder(Context context, long taskId, String taskTitle,
                                       String taskDescription, long reminderTimeMillis) {

        // Don't schedule if time is in the past
        if (reminderTimeMillis <= System.currentTimeMillis()) {
            Log.w(TAG, "Reminder time is in the past, not scheduling");
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager is null");
            return;
        }

        // Create intent for notification
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.EXTRA_TASK_ID, taskId);
        intent.putExtra(NotificationReceiver.EXTRA_TASK_TITLE, taskTitle);
        intent.putExtra(NotificationReceiver.EXTRA_TASK_DESCRIPTION,
                taskDescription != null ? taskDescription : "");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) taskId, // Use taskId as request code to make it unique
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Schedule alarm
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ - check if can schedule exact alarms
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            reminderTimeMillis,
                            pendingIntent
                    );
                    Log.d(TAG, "Exact alarm scheduled for task: " + taskTitle +
                            " at " + new java.util.Date(reminderTimeMillis));
                } else {
                    // Fallback to inexact alarm
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            reminderTimeMillis,
                            pendingIntent
                    );
                    Log.d(TAG, "Inexact alarm scheduled for task: " + taskTitle);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android 6.0+
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTimeMillis,
                        pendingIntent
                );
                Log.d(TAG, "Alarm scheduled for task: " + taskTitle);
            } else {
                // Android 5.x and below
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        reminderTimeMillis,
                        pendingIntent
                );
                Log.d(TAG, "Alarm scheduled for task: " + taskTitle);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException when scheduling alarm", e);
        }
    }

    /**
     * Cancel a scheduled reminder
     *
     * @param context Application context
     * @param taskId ID of the task to cancel reminder for
     */
    public static void cancelReminder(Context context, long taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

        Log.d(TAG, "Reminder cancelled for task ID: " + taskId);
    }

    /**
     * Reschedule reminder for a task (e.g., when task is updated)
     */
    public static void rescheduleReminder(Context context, long taskId, String taskTitle,
                                         String taskDescription, long newReminderTimeMillis) {
        // Cancel existing reminder
        cancelReminder(context, taskId);

        // Schedule new reminder
        if (newReminderTimeMillis > System.currentTimeMillis()) {
            scheduleReminder(context, taskId, taskTitle, taskDescription, newReminderTimeMillis);
        }
    }
}

