package com.example.todolist.Ui.activity.thongke;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.todolist.Data.entity.TaskWithCategory;
import com.example.todolist.R;
import com.example.todolist.Ui.activity.BaseActivity;
import com.example.todolist.Ui.activity.Login.HelloActivity;
import com.example.todolist.ViewModel.TaskViewModel;
import com.example.todolist.utils.ChartUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;

import java.util.LinkedHashMap;
import java.util.List;

public class ThongKeActivity extends BaseActivity {

    private LineChart lineChart;
    private PieChart pieChart;
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

        // 1. Ánh xạ View
        mappingViews();

        // 2. Setup Bottom Navigation
        setupBottomNav(R.id.navigation_profile);

        // 3. Khởi tạo ViewModel
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // 4. Quan sát dữ liệu từ ViewModel
        observeViewModel();

        // 5. Setup Spinner bộ lọc thời gian
        setupSpinnerListener();

        // 6. Căn chỉnh giao diện (Edge to edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cập nhật UI User mỗi khi quay lại màn hình
        updateUserUI();
        // Tải lại dữ liệu (để đảm bảo đúng User hoặc Guest)
        reloadData();
    }

    private void mappingViews() {
        pieChart = findViewById(R.id.pieChart);
        lineChart = findViewById(R.id.lineChart);
        tvCompleted = findViewById(R.id.tvCompleted);
        tvIncomplete = findViewById(R.id.tvIncomplete);
        spFilterTime = findViewById(R.id.spFilterTime);
        tvGreeting = findViewById(R.id.tvGreeting);
        btnLoginRegister = findViewById(R.id.btnLoginRegister);
        imgAvatar = findViewById(R.id.imgAvatar);
    }

    private void observeViewModel() {
        // A. Quan sát danh sách Task để vẽ PieChart và cập nhật số liệu
        taskViewModel.getAllTasks().observe(this, this::updatePieChartStatistics);

        // B. Quan sát dữ liệu thống kê để vẽ LineChart
        taskViewModel.getCompletedTaskStats().observe(this, data -> {
            if (data != null && !data.isEmpty()) {
                ChartUtils.setupLineChart(lineChart, new LinkedHashMap<>(data));
            } else {
                lineChart.clear();
                lineChart.setNoDataText("Chưa có dữ liệu thống kê");
                lineChart.invalidate();
            }
        });
    }

    private void reloadData() {
        if (taskViewModel == null) return;

        // 1. Load tất cả tasks (cho PieChart)
        taskViewModel.loadTasks();

        // 2. Load thống kê (cho LineChart)
        int days = 7;
        if (spFilterTime != null && spFilterTime.getSelectedItem() != null) {
            String selected = spFilterTime.getSelectedItem().toString();
            try {
                days = Integer.parseInt(selected.replaceAll("\\D", ""));
            } catch (Exception e) {
                days = 7;
            }
        }
        taskViewModel.loadCompletedTaskStats(days);
    }

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

        tvCompleted.setText(String.valueOf(completed));
        tvIncomplete.setText(String.valueOf(incomplete));
        ChartUtils.setupPieChart(pieChart, completed, incomplete);
    }

    private void setupSpinnerListener() {
        spFilterTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reloadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void updateUserUI() {
        // Lấy thông tin từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("USER_DATA", MODE_PRIVATE);
        String username = prefs.getString("USERNAME", "");
        int userId = prefs.getInt("USER_ID", -1);

        // Kiểm tra xem đã đăng nhập chưa
        boolean isLoggedIn = (userId != -1 && username != null && !username.isEmpty());

        if (isLoggedIn) {
            // --- TRƯỜNG HỢP 1: ĐÃ ĐĂNG NHẬP ---
            tvGreeting.setText("Xin chào, " + username);

            // Ẩn nút Đăng nhập/Đăng ký đi (vì đã đăng nhập rồi)
            if (btnLoginRegister != null) {
                btnLoginRegister.setVisibility(View.GONE);
            }

            // Click Avatar -> Sang trang Hồ sơ (UserActivity)
            imgAvatar.setOnClickListener(v -> {
                Intent intent = new Intent(ThongKeActivity.this, UserActivity.class);
                startActivity(intent);
            });

        } else {
            // --- TRƯỜNG HỢP 2: KHÁCH (CHƯA ĐĂNG NHẬP) ---
            tvGreeting.setText("Bạn đang xem ở chế độ Khách");

            // Hiển thị nút Đăng nhập/Đăng ký
            if (btnLoginRegister != null) {
                btnLoginRegister.setVisibility(View.VISIBLE); // Quan trọng: Phải hiện lên
                btnLoginRegister.setText("Đăng nhập / Đăng ký");

                // Click nút -> Sang màn hình Đăng nhập
                btnLoginRegister.setOnClickListener(v -> {
                    startActivity(new Intent(this, HelloActivity.class));
                });
            }

            // Click Avatar -> Cũng sang màn hình Đăng nhập (nhắc nhở)
            imgAvatar.setOnClickListener(v -> {
                Toast.makeText(this, "Vui lòng đăng nhập để xem hồ sơ", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HelloActivity.class));
            });
        }
    }
    }

