package com.example.todolist.Ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.todolist.R;
import com.example.todolist.Ui.activity.Lich.LichActivity;
import com.example.todolist.Ui.activity.thongke.ThongKeActivity;
import com.example.todolist.Ui.maintaskfragement.TaskFragment;
import com.example.todolist.utils.NotificationHelper;
import com.example.todolist.utils.RecurringTaskScheduler;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends BaseActivity{

    private BottomNavigationView bottomNavigationView;
    // Permission launcher for notifications
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission granted - notifications will work
                } else {
                    // Permission denied - show message to user
                    android.widget.Toast.makeText(this,
                            "Không thể gửi thông báo nhắc nhở nếu không có quyền",
                            android.widget.Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        // Handle navigation icon click (menu)
        toolbar.setNavigationOnClickListener(v -> {
            android.widget.Toast.makeText(this, "Menu clicked", android.widget.Toast.LENGTH_SHORT).show();

        });

        // Create notification channel for reminders
        NotificationHelper.createNotificationChannel(this);

        // Request notification permission for Android 13+
        requestNotificationPermission();

        // Lên lịch cho công việc chạy nền để tạo các task lặp lại hàng ngày
        RecurringTaskScheduler.scheduleDailyTaskCreation(this);

        // Setup bottom navigation
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        setupBottomNavigation();

        // Load default fragment (Tasks)
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new TaskFragment())
                    .commit();
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_tasks) {
                selectedFragment = new TaskFragment();
            } else if (itemId == R.id.navigation_calendar) {
                Intent intent = new Intent(this, LichActivity.class);
                startActivity(intent);
            } else if (itemId == R.id.navigation_profile) {
                Intent intent = new Intent(this, ThongKeActivity.class);
                startActivity(intent);
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tasks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_search) {
            android.widget.Toast.makeText(this, "Search clicked", android.widget.Toast.LENGTH_SHORT).show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}