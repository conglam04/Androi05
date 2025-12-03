package com.example.todolist.Ui.activity.Lich;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todolist.Data.Repository.TaskRepository;
import com.example.todolist.Data.entity.Task;
import com.example.todolist.R;

import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity {

    private TaskRepository taskRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Khởi tạo Repository
        taskRepository = new TaskRepository(this);

        EditText edtTitle = findViewById(R.id.edtTaskTitle);
        TimePicker timePicker = findViewById(R.id.timePicker);
        Button btnSave = findViewById(R.id.btnSave);

        // Thiết lập TimePicker hiển thị chế độ 24h cho đẹp
        timePicker.setIs24HourView(true);

        // Lấy timestamp ngày (dưới dạng String) được truyền từ LichActivity
        String dateStr = getIntent().getStringExtra("date");

        btnSave.setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên công việc", Toast.LENGTH_SHORT).show();
                return;
            }

            // --- XỬ LÝ THỜI GIAN (Logic mới) ---

            // 1. Parse ngày từ Intent
            long dateMillis;
            try {
                if (dateStr != null) {
                    dateMillis = Long.parseLong(dateStr);
                } else {
                    dateMillis = System.currentTimeMillis();
                }
            } catch (NumberFormatException e) {
                dateMillis = System.currentTimeMillis();
            }

            // 2. Lấy giờ và phút từ TimePicker
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            // 3. Gộp Ngày + Giờ vào chung biến Calendar
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(dateMillis); // Set ngày
            calendar.set(Calendar.HOUR_OF_DAY, hour); // Set giờ
            calendar.set(Calendar.MINUTE, minute);    // Set phút
            calendar.set(Calendar.SECOND, 0);

            long finalDueDate = calendar.getTimeInMillis();

            // --- LƯU VÀO DATABASE ---

            // Tạo Task mới theo Constructor: (Title, Description, DueDate)
            Task task = new Task(title, "", finalDueDate);

            // Dùng Repository để insert (an toàn hơn gọi trực tiếp DAO)
            taskRepository.insert(task);

            finish(); // Đóng màn hình
        });
    }
}