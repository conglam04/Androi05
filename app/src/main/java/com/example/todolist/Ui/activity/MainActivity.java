package com.example.todolist.Ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.todolist.R;
import com.example.todolist.Ui.activity.Lich.LichActivity;
import com.example.todolist.Ui.activity.thongke.ThongKeActivity;
import com.example.todolist.Ui.maintaskfragement.TaskFragment;
import com.example.todolist.Ui.setting.AdvanceSettingActivity;
import com.example.todolist.Ui.setting.FaqActivity;
import com.example.todolist.Ui.setting.FeedbackActivity;
import com.example.todolist.Ui.setting.StarredTasksActivity;
import com.example.todolist.Ui.theme.ThemeActivity;
import com.example.todolist.utils.NotificationHelper;
import com.example.todolist.utils.RecurringTaskScheduler;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private NavigationView navigationView;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
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

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new TaskFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_theme);
        }

        NotificationHelper.createNotificationChannel(this);
        requestNotificationPermission();
        RecurringTaskScheduler.scheduleDailyTaskCreation(this);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        setupBottomNavigation();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new TaskFragment())
                    .commit();
        }
        applyTheme();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyTheme();
    }

    private void applyTheme() {
        SharedPreferences prefs = getSharedPreferences(ThemeActivity.PREFS_NAME, MODE_PRIVATE);
        int defaultColor = ContextCompat.getColor(this, R.color.blue);
        int themeColor = prefs.getInt(ThemeActivity.KEY_THEME_COLOR, defaultColor);

        toolbar.setBackgroundColor(themeColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(themeColor);
        }

        View headerView = navigationView.getHeaderView(0);
        headerView.setBackgroundColor(themeColor);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_tasks) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new TaskFragment()).commit();
                return true;
            } else if (itemId == R.id.navigation_calendar) {
                startActivity(new Intent(this, LichActivity.class));
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(this, ThongKeActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_theme) {
            startActivity(new Intent(this, ThemeActivity.class));
        } else if (itemId == R.id.nav_faq) {
            startActivity(new Intent(this, FaqActivity.class));
        } else if (itemId == R.id.nav_feedback) {
            startActivity(new Intent(this, FeedbackActivity.class));
        } else if (itemId == R.id.nav_advance_setting) {
            startActivity(new Intent(this, AdvanceSettingActivity.class));
        } else if (itemId == R.id.nav_starred_tasks) {
            startActivity(new Intent(this, StarredTasksActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
