package com.example.todolist.Ui.maintaskfragement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.todolist.R;
import com.example.todolist.service.FocusTimerService;

import java.util.Locale;

public class FocusTimerActivity extends AppCompatActivity {

    private TextView tvTaskName;
    private TextView tvTimer;
    private Button btnStartStop;
    private Button btnFinish;
    private ImageButton btnBack;
    private ImageButton btnMenu;
    private ProgressBar circleProgress;

    private long totalDurationMs = 25 * 60 * 1000; // Default 25 minutes
    private long remainingMs = 25 * 60 * 1000;
    private String taskName = "";
    private long taskId = -1;
    private boolean isRunning = false;

    private final BroadcastReceiver timerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (FocusTimerService.ACTION_TIMER_TICK.equals(action)) {
                remainingMs = intent.getLongExtra(FocusTimerService.EXTRA_REMAINING, 0);
                totalDurationMs = intent.getLongExtra(FocusTimerService.EXTRA_TOTAL, totalDurationMs);
                isRunning = true;
                updateUI();
            } else if (FocusTimerService.ACTION_TIMER_FINISH.equals(action)) {
                remainingMs = 0;
                isRunning = false;
                updateUI();
                // Optionally, you can add a sound or vibration here
            } else if (FocusTimerService.ACTION_TIMER_STOPPED.equals(action)) {
                isRunning = false;
                updateUI();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_timer);

        initViews();
        loadTaskData();
        setupListeners();
        updateUI();

        // Register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(FocusTimerService.ACTION_TIMER_TICK);
        filter.addAction(FocusTimerService.ACTION_TIMER_FINISH);
        filter.addAction(FocusTimerService.ACTION_TIMER_STOPPED);
        LocalBroadcastManager.getInstance(this).registerReceiver(timerReceiver, filter);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void initViews() {
        tvTaskName = findViewById(R.id.tvTaskName);
        tvTimer = findViewById(R.id.tvTimer);
        btnStartStop = findViewById(R.id.btnStartStop);
        btnFinish = findViewById(R.id.btnFinish);
        btnBack = findViewById(R.id.btnBack);
        btnMenu = findViewById(R.id.btnMenu);
        circleProgress = findViewById(R.id.circleProgress);
    }

    private void loadTaskData() {
        Intent intent = getIntent();
        if (intent != null) {
            taskName = intent.getStringExtra("taskName");
            taskId = intent.getLongExtra("taskId", -1);

            if (taskName == null || taskName.isEmpty()) {
                taskName = "Tập trung";
            }
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnMenu.setOnClickListener(v -> showOptionsMenu());
        tvTimer.setOnClickListener(v -> {
            if (!isRunning) {
                showEditTimeDialog();
            }
        });
        btnStartStop.setOnClickListener(v -> {
            if (!isRunning) {
                startTimer();
            } else {
                pauseTimer();
            }
        });
        btnFinish.setOnClickListener(v -> {
            stopTimer();
            finish();
        });
    }

    private void startTimer() {
        Intent serviceIntent = new Intent(this, FocusTimerService.class);
        serviceIntent.setAction(FocusTimerService.ACTION_START);
        serviceIntent.putExtra(FocusTimerService.EXTRA_DURATION_MS, remainingMs);
        serviceIntent.putExtra(FocusTimerService.EXTRA_TASK_NAME, taskName);
        serviceIntent.putExtra(FocusTimerService.EXTRA_TASK_ID, taskId);
        startService(serviceIntent);
    }

    private void pauseTimer() {
        Intent serviceIntent = new Intent(this, FocusTimerService.class);
        serviceIntent.setAction(FocusTimerService.ACTION_STOP);
        startService(serviceIntent);
    }

    private void stopTimer() {
        Intent serviceIntent = new Intent(this, FocusTimerService.class);
        serviceIntent.setAction(FocusTimerService.ACTION_STOP);
        startService(serviceIntent);
    }

    private void updateUI() {
        tvTaskName.setText(taskName);

        long minutes = (remainingMs / 1000) / 60;
        long seconds = (remainingMs / 1000) % 60;
        tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));

        if (isRunning) {
            btnStartStop.setText(R.string.pause);
            btnFinish.setVisibility(View.VISIBLE);
        } else {
            btnStartStop.setText(R.string.start);
            btnFinish.setVisibility(View.GONE);
        }

        updateProgressBar();
    }

    private void updateProgressBar() {
        if (totalDurationMs > 0) {
            int progress = (int) (((double) remainingMs / totalDurationMs) * 100);
            circleProgress.setProgress(progress);
        } else {
            circleProgress.setProgress(100);
        }
    }

    private void showEditTimeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đặt thời gian (phút)");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(remainingMs / 60000));
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            try {
                long minutes = Long.parseLong(input.getText().toString());
                if (minutes > 0 && minutes <= 180) { // Max 3 hours
                    totalDurationMs = minutes * 60 * 1000;
                    remainingMs = totalDurationMs;
                    updateUI();
                }
            } catch (NumberFormatException e) {
                // Invalid input, ignore
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showOptionsMenu() {
        String[] options = {"Đặt lại thời gian", "Kết thúc"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(taskName);
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showEditTimeDialog();
            } else if (which == 1) {
                stopTimer();
                finish();
            }
        });
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timerReceiver);
    }
}
