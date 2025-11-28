package com.example.todolist.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.todolist.R;
import com.example.todolist.Ui.activity.MainActivity;
import com.example.todolist.Ui.maintaskfragement.FocusTimerActivity;

import java.util.Locale;

public class FocusTimerService extends Service {

    public static final String ACTION_START = "com.example.todolistdemo.ACTION_START";
    public static final String ACTION_STOP = "com.example.todolistdemo.ACTION_STOP";
    public static final String ACTION_TIMER_TICK = "com.example.todolistdemo.ACTION_TIMER_TICK";
    public static final String ACTION_TIMER_FINISH = "com.example.todolistdemo.ACTION_TIMER_FINISH";
    public static final String ACTION_TIMER_STOPPED = "com.example.todolistdemo.ACTION_TIMER_STOPPED";

    public static final String EXTRA_DURATION_MS = "extra_duration_ms";
    public static final String EXTRA_TASK_NAME = "extra_task_name";
    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_REMAINING = "extra_remaining";
    public static final String EXTRA_TOTAL = "extra_total";

    private static final String CHANNEL_ID = "focus_timer_channel";
    private static final int NOTIFICATION_ID = 9001;

    private CountDownTimer countDownTimer;
    private long totalDurationMs;
    private long remainingMs;
    private String taskName = "Tập trung";
    private long taskId = -1;
    private boolean isRunning = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        if (ACTION_START.equals(action)) {
            if (!isRunning) { // Only start a new timer if one isn't already running
                totalDurationMs = intent.getLongExtra(EXTRA_DURATION_MS, 25 * 60 * 1000);
                remainingMs = totalDurationMs;
                taskName = intent.getStringExtra(EXTRA_TASK_NAME);
                taskId = intent.getLongExtra(EXTRA_TASK_ID, -1);

                if (taskName == null || taskName.isEmpty()) {
                    taskName = "Tập trung";
                }
                startForegroundTimer();
            }
        } else if (ACTION_STOP.equals(action)) {
            stopTimer();
        }

        return START_NOT_STICKY;
    }

    private void startForegroundTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isRunning = true;

        startForeground(NOTIFICATION_ID, buildNotification(remainingMs));

        countDownTimer = new CountDownTimer(remainingMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMs = millisUntilFinished;
                broadcastTick();
                updateNotification();
            }

            @Override
            public void onFinish() {
                remainingMs = 0;
                isRunning = false;
                broadcastFinish();
                showCompletionNotification();
                stopForeground(true);
                stopSelf();
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        isRunning = false;
        broadcastStopped();
        stopForeground(true);
        stopSelf();
    }

    private void broadcastTick() {
        Intent tickIntent = new Intent(ACTION_TIMER_TICK);
        tickIntent.putExtra(EXTRA_REMAINING, remainingMs);
        tickIntent.putExtra(EXTRA_TOTAL, totalDurationMs);
        LocalBroadcastManager.getInstance(this).sendBroadcast(tickIntent);
    }

    private void broadcastFinish() {
        Intent finishIntent = new Intent(ACTION_TIMER_FINISH);
        LocalBroadcastManager.getInstance(this).sendBroadcast(finishIntent);
    }

    private void broadcastStopped() {
        Intent stoppedIntent = new Intent(ACTION_TIMER_STOPPED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(stoppedIntent);
    }

    private void updateNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, buildNotification(remainingMs));
        }
    }

    private Notification buildNotification(long remainingMs) {
        Intent notificationIntent = new Intent(this, FocusTimerActivity.class);
        notificationIntent.putExtra("taskName", taskName);
        notificationIntent.putExtra("taskId", taskId);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long minutes = (remainingMs / 1000) / 60;
        long seconds = (remainingMs / 1000) % 60;
        String timeText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(taskName)
                .setContentText(timeText)
                .setSmallIcon(R.drawable.ic_timer)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setOnlyAlertOnce(true)
                .build();
    }

    private void showCompletionNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Hoàn thành!")
                .setContentText(taskName + " - Thời gian tập trung đã hết")
                .setSmallIcon(R.drawable.ic_check)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID + 1, notification);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Focus Timer",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Hiển thị bộ đếm thời gian tập trung");
            channel.setShowBadge(false);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
