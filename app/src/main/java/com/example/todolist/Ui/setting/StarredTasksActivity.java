package com.example.todolist.Ui.setting;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.Data.entity.Task;
import com.example.todolist.R;
import com.example.todolist.Ui.adapter.TaskAdapter;
import com.example.todolist.Ui.theme.ThemeActivity;
import com.example.todolist.ViewModel.StarredTasksViewModel;

import java.util.ArrayList;

public class StarredTasksActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private StarredTasksViewModel viewModel;
    private TaskAdapter adapter;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starred_tasks);

        toolbar = findViewById(R.id.toolbar_starred);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.nav_starred_tasks);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        applyTheme();

        RecyclerView recyclerView = findViewById(R.id.starred_tasks_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // --- SỬA LỖI: Sử dụng hàm khởi tạo 3 tham số ---
        // Truyền "this" làm listener và "true" cho isGridView nếu muốn
        adapter = new TaskAdapter(new ArrayList<>(), this, false); // false để dùng list view
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(StarredTasksViewModel.class);
        viewModel.getStarredTasks().observe(this, tasks -> {
            if(tasks != null) {
                adapter.updateTasks(tasks);
            }
        });
    }

    private void applyTheme() {
        SharedPreferences prefs = getSharedPreferences(ThemeActivity.PREFS_NAME, MODE_PRIVATE);
        int defaultColor = ContextCompat.getColor(this, R.color.blue);
        int themeColor = prefs.getInt(ThemeActivity.KEY_THEME_COLOR, defaultColor);

        toolbar.setBackgroundColor(themeColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(themeColor);
        }

        if (isColorDark(themeColor)) {
            toolbar.setTitleTextColor(Color.WHITE);
            if (toolbar.getNavigationIcon() != null) {
                toolbar.getNavigationIcon().setTint(Color.WHITE);
            }
        } else {
            toolbar.setTitleTextColor(Color.BLACK);
            if (toolbar.getNavigationIcon() != null) {
                toolbar.getNavigationIcon().setTint(Color.BLACK);
            }
        }
    }

    private boolean isColorDark(@ColorInt int color) {
        double luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return luminance < 0.5;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // --- SỬA LỖI: Implement các phương thức của OnTaskClickListener ---
    @Override
    public void onTaskClick(Task task) {
        // Có thể để trống nếu không cần xử lý click ở màn hình này
    }

    @Override
    public void onTaskLongClick(Task task) {
        // Có thể để trống nếu không cần xử lý long click ở màn hình này
    }

    @Override
    public void onTaskStatusChanged(Task task) {
        viewModel.updateTask(task); // Cập nhật trạng thái hoàn thành của task
    }
}
