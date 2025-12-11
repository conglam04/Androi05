package com.example.todolist.Ui.setting;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.todolist.R;
import com.example.todolist.Ui.theme.ThemeActivity;

public class FeedbackActivity extends AppCompatActivity {

    private EditText recipientEditText;
    private EditText senderEditText;
    private EditText subjectEditText;
    private EditText messageEditText;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        toolbar = findViewById(R.id.toolbar_feedback);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Gửi phản hồi");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        applyTheme();

        recipientEditText = findViewById(R.id.recipient_edit_text);
        senderEditText = findViewById(R.id.sender_edit_text);
        subjectEditText = findViewById(R.id.subject_edit_text);
        messageEditText = findViewById(R.id.message_edit_text);
        Button sendButton = findViewById(R.id.send_feedback_button);

        // Đặt người nhận mặc định
        recipientEditText.setText("todolistAdmin@gmail.com");

        // Lấy email người dùng từ SharedPreferences và đặt vào ô người gửi
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", ""); // Giả sử email được lưu với key là "user_email"
        senderEditText.setText(userEmail);

        sendButton.setOnClickListener(v -> sendFeedback());
    }

    private void sendFeedback() {
        String recipient = recipientEditText.getText().toString();
        String sender = senderEditText.getText().toString();
        String subject = subjectEditText.getText().toString();
        String message = messageEditText.getText().toString();

        if (subject.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề và nội dung phản hồi", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // Chỉ các ứng dụng email mới xử lý được
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, "From: " + sender + "\n\n" + message);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Không tìm thấy ứng dụng email", Toast.LENGTH_SHORT).show();
        }
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
}
