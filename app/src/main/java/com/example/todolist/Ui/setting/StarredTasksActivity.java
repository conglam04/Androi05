package com.example.todolist.Ui.setting;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.Data.entity.Task;
import com.example.todolist.R;
import com.example.todolist.Ui.adapter.TaskAdapter;
import com.example.todolist.ViewModel.StarredTasksViewModel;

import java.util.ArrayList;

public class StarredTasksActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private StarredTasksViewModel viewModel;
    private TaskAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starred_tasks);

        Toolbar toolbar = findViewById(R.id.toolbar_starred);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Nhiệm vụ gắn sao");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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
