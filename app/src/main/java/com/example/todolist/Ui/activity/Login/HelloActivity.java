package com.example.todolist.Ui.activity.Login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todolist.R;
import com.example.todolist.Ui.activity.MainActivity;
import com.example.todolist.Ui.activity.thongke.ThongKeActivity;

public class HelloActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hello_layout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.hello), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;


        });

        Button btnLogin = findViewById(R.id.btn_welcome_login);
        Button btnSignup = findViewById(R.id.btn_welcome_signup);
        ImageButton btnback = findViewById(R.id.btn_back);


        btnLogin.setOnClickListener(v -> {

            Intent intent = new Intent(HelloActivity.this, LoginActivity.class);
            startActivity(intent);
        });


        btnSignup.setOnClickListener(v -> {

            Intent intent = new Intent(HelloActivity.this, SignupActivity.class);
            startActivity(intent);
        });
        btnback.setOnClickListener(v -> {
            Intent intent = new Intent(HelloActivity.this, ThongKeActivity.class);
            startActivity(intent);
        });
    }
}