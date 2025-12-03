package com.example.todolist.Ui.activity.Lich;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.todolist.Data.entity.Task;
import com.example.todolist.Ui.activity.Lich.AlarmReceiver;

import java.util.Calendar;

public class AlarmScheduler {

    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleReminder(Context context, Task task) {
        if (task.getReminderTime() == null || task.getReminderTime() <= System.currentTimeMillis()) {
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("TASK_TITLE", task.getTitle());
        intent.putExtra("TASK_ID", task.getTaskId());

        // Dùng taskId làm requestCode để mỗi task có một alarm riêng
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.getTaskId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            // Cài đặt báo thức chính xác
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.getReminderTime(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, task.getReminderTime(), pendingIntent);
            }

            // Thông báo Toast
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(task.getReminderTime());
            String time = String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
            // Toast.makeText(context, "Đã đặt báo thức lúc " + time, Toast.LENGTH_SHORT).show();
        }
    }

    public static void cancelReminder(Context context, Task task) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.getTaskId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}