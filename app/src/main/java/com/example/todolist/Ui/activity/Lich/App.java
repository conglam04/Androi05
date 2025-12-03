package com.example.todolist.Ui.activity.Lich;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

// Lớp này chạy một lần duy nhất khi ứng dụng khởi động
public class App extends Application {

    public static final String CHANNEL_ID = "REMINDER_CHANNEL_1";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Chỉ tạo kênh trên API 26 (Android 8.0) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            // Channel 1: For Lich/AlarmReceiver
            CharSequence name = "Kênh Nhắc nhở Nhiệm vụ";
            String description = "Kênh để hiển thị thông báo nhắc nhở nhiệm vụ";
            int importance = NotificationManager.IMPORTANCE_HIGH; // Ưu tiên cao

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.enableLights(true);
            notificationManager.createNotificationChannel(channel);

            // Channel 2: For task reminders (NotificationHelper)
            NotificationChannel taskReminderChannel = new NotificationChannel(
                    "task_reminder_channel",
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            taskReminderChannel.setDescription("Notifications for task reminders");
            taskReminderChannel.enableVibration(true);
            taskReminderChannel.enableLights(true);
            notificationManager.createNotificationChannel(taskReminderChannel);
        }
    }
}