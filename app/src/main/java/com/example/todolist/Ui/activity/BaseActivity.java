package com.example.todolist.Ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todolist.R;
import com.example.todolist.Ui.activity.Lich.LichActivity;
import com.example.todolist.Ui.activity.thongke.ThongKeActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Lưu ý: Các Activity con sẽ tự gọi setContentView()
        // => KHÔNG setContentView ở đây

    }

    /**
     * Gọi phương thức này sau khi setContentView() ở từng Activity con,
     * nếu layout đó có chứa BottomNav.
     */
    protected void setupBottomNav(int checkedItemId) {
        bottomNav = findViewById(R.id.bottomNav);

        if (bottomNav == null) return; // Activity này không dùng BottomNav

        bottomNav.setSelectedItemId(checkedItemId);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.navigation_tasks) {
                if (!(this instanceof MainActivity)) {
                    startActivity(new Intent(this, MainActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
                return true;

            } else if (id == R.id.navigation_calendar) {
                if (!(this instanceof LichActivity)) {
                    startActivity(new Intent(this, LichActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
                return true;

            } else if (id == R.id.navigation_profile) {
                if (!(this instanceof ThongKeActivity)) {
                    startActivity(new Intent(this, ThongKeActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
                return true;
            }
            return false;
        });
    }
}
