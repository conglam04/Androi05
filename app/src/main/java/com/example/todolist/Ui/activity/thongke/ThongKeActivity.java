package com.example.todolist.Ui.activity.thongke;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.todolist.Data.entity.TaskWithCategory;
import com.example.todolist.R;
import com.example.todolist.Ui.activity.BaseActivity;
import com.example.todolist.Ui.activity.Login.HelloActivity;
import com.example.todolist.utils.ChartUtils;
import com.example.todolist.ViewModel.TaskViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;

import java.util.LinkedHashMap;
import java.util.List;

public class ThongKeActivity extends BaseActivity {

    private LineChart lineChart;
    private PieChart pieChart;
    // Đã sửa tên biến để khớp với XML (tvIncomplete thay vì tvPending)
    private TextView tvCompleted, tvIncomplete, tvGreeting;
    private Button btnLoginRegister;
    private Spinner spFilterTime;
    private TaskViewModel taskViewModel;
    private ImageView imgAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.thongke);

        // 1. Ánh xạ View (Mapping)
        mappingViews();

        // 2. Setup Bottom Navigation
        setupBottomNav(R.id.navigation_profile);

        // 3. Xử lý giao diện User (Login/Logout)
        updateUserUI();

        // 4. Căn chỉnh giao diện (Edge to edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0); // Để 0 ở bottom để BottomNav tự xử lý
            return insets;
        });

        // 5. Khởi tạo ViewModel
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // --- LOGIC BIỂU ĐỒ ---

        // A. Biểu đồ tròn (PieChart) - Dùng LiveData getAllTasks để tự động cập nhật
        taskViewModel.loadTasks(); // Gọi load dữ liệu
        taskViewModel.getAllTasks().observe(this, this::updatePieChartStatistics);

        // B. Biểu đồ đường (LineChart) - Quan sát dữ liệu thống kê
        taskViewModel.getCompletedTaskStats().observe(this, data -> {
            if (data != null && !data.isEmpty()) {
                ChartUtils.setupLineChart(lineChart, new LinkedHashMap<>(data));
            } else {
                lineChart.clear();
                lineChart.setNoDataText("Chưa có dữ liệu thống kê");
            }
        });

        // Load mặc định 7 ngày cho biểu đồ đường
        taskViewModel.loadCompletedTaskStats(7);

        // C. Sự kiện bộ lọc thời gian (Spinner)
        setupSpinnerListener();
    }

    private void mappingViews() {
        pieChart = findViewById(R.id.pieChart);
        lineChart = findViewById(R.id.lineChart);

        // Sửa lỗi: Dùng đúng ID trong XML
        tvCompleted = findViewById(R.id.tvCompleted);
        tvIncomplete = findViewById(R.id.tvIncomplete);

        spFilterTime = findViewById(R.id.spFilterTime);
        tvGreeting = findViewById(R.id.tvGreeting);
        btnLoginRegister = findViewById(R.id.btnLoginRegister);
        imgAvatar = findViewById(R.id.imgAvatar);

        // Đã bỏ các biến tvTotalTasks, tvPercentage, btnBack vì XML không có
    }

    // Hàm cập nhật số liệu và PieChart
    private void updatePieChartStatistics(List<TaskWithCategory> tasks) {
        if (tasks == null) return;

        int completed = 0;
        int incomplete = 0;

        for (TaskWithCategory item : tasks) {
            if (item.task.getIsCompleted() == 1) {
                completed++;
            } else {
                incomplete++;
            }
        }

        // Cập nhật Text
        tvCompleted.setText(String.valueOf(completed));
        tvIncomplete.setText(String.valueOf(incomplete));

        // Vẽ biểu đồ tròn
        ChartUtils.setupPieChart(pieChart, completed, incomplete);
    }

    private void setupSpinnerListener() {
        spFilterTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                // Lấy số ngày từ chuỗi (ví dụ: "7 ngày" -> 7)
                try {
                    int days = Integer.parseInt(selected.replaceAll("\\D", ""));
                    taskViewModel.loadCompletedTaskStats(days);
                } catch (NumberFormatException e) {
                    taskViewModel.loadCompletedTaskStats(7); // Mặc định
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void updateUserUI() {
        String username = getSharedPreferences("USER_DATA", MODE_PRIVATE)
                .getString("USERNAME", "");

        if (username != null && !username.isEmpty()) {
            // Đã đăng nhập
            tvGreeting.setText("Xin chào, " + username);
            btnLoginRegister.setText("Đăng xuất");
            btnLoginRegister.setTextColor(getColor(android.R.color.holo_red_dark));

            btnLoginRegister.setOnClickListener(v -> {
                getSharedPreferences("USER_DATA", MODE_PRIVATE).edit().clear().apply();
                updateUserUI(); // Refresh lại UI
            });
        } else {
            // Chưa đăng nhập
            tvGreeting.setText("Bạn đã giữ theo kế hoạch của mình...");
            btnLoginRegister.setText("Đăng nhập / Đăng ký");
            btnLoginRegister.setTextColor(getColor(R.color.primary)); // Hoặc màu xanh mặc định

            btnLoginRegister.setOnClickListener(v -> {
                startActivity(new Intent(this, HelloActivity.class));
            });
        }
    }
}