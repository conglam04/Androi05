package com.example.todolist.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.todolist.utils.NotificationHelper;


public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";

    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_TASK_TITLE = "task_title";
    public static final String EXTRA_TASK_DESCRIPTION = "task_description";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received notification broadcast");

        if (intent == null) {
            return;
        }

        long taskId = intent.getLongExtra(EXTRA_TASK_ID, -1);
        String taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE);
        String taskDescription = intent.getStringExtra(EXTRA_TASK_DESCRIPTION);

        if (taskId == -1 || taskTitle == null) {
            Log.e(TAG, "Invalid task data in broadcast");
            return;
        }

        // Show notification
        NotificationHelper.showTaskReminder(context, taskId, taskTitle, taskDescription);

        Log.d(TAG, "Notification shown for task: " + taskTitle);
    }
}

