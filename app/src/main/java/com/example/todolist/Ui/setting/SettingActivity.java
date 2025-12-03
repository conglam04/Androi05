package com.example.todolist.Ui.setting;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todolist.R;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // --- TOÀN BỘ MÃ TOOLBAR ĐÃ BỊ VÔ HIỆU HÓA ĐỂ GỠ LỖI ---
        /*
        Toolbar toolbar = findViewById(R.id.toolbar_setting);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Cài đặt");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        */

        // --- Gán sự kiện cho NÚT GỠ LỖI ---
        Button themeButtonDebug = findViewById(R.id.theme_button_debug);
        themeButtonDebug.setOnClickListener(v -> {
            Toast.makeText(SettingActivity.this, "NÚT GỠ LỖI ĐÃ HOẠT ĐỘNG!", Toast.LENGTH_LONG).show();
        });


        // --- Sự kiện cho các mục khác (giữ nguyên) ---
        LinearLayout advanceSettingLayout = findViewById(R.id.advance_setting_layout);
        LinearLayout faqLayout = findViewById(R.id.faq_layout);
        LinearLayout feedbackLayout = findViewById(R.id.feedback_layout);
        LinearLayout starredTasksLayout = findViewById(R.id.starred_tasks_layout);

        advanceSettingLayout.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, AdvanceSettingActivity.class);
            startActivity(intent);
        });

        faqLayout.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, FaqActivity.class);
            startActivity(intent);
        });

        feedbackLayout.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, FeedbackActivity.class);
            startActivity(intent);
        });

        starredTasksLayout.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, StarredTasksActivity.class);
            startActivity(intent);
        });
    }

    // Tạm thời không cần onSupportNavigateUp vì không có Toolbar
    /*
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    */
}
