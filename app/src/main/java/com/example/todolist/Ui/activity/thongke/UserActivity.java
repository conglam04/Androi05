package com.example.todolist.Ui.activity.thongke;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todolist.Data.Repository.UserRepository;
import com.example.todolist.Data.entity.User;
import com.example.todolist.R;
import com.example.todolist.Ui.activity.Login.LoginActivity;
import com.example.todolist.Ui.activity.MainActivity;
import com.example.todolist.utils.SecurityUtils; // Import tiện ích bảo mật

public class UserActivity extends AppCompatActivity {

    private TextView tvMainDisplayName, tvEmail, tvPhone, tvUsername;
    private ImageView btnBack;
    private View btnLogout;
    private View btnEditProfile;      // Nút sửa thông tin chung
    private View btnChangePassword;   // Nút đổi mật khẩu (bạn cần có view này trong XML)

    private UserRepository userRepository;
    private String currentUsernameSession;
    private User currentUser;

    private static final String PREF_NAME = "USER_DATA";
    private static final String KEY_USERNAME = "USERNAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userlayout);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.user), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userRepository = new UserRepository(getApplication());

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        currentUsernameSession = prefs.getString(KEY_USERNAME, null);

        if (currentUsernameSession == null) {
            goToLoginScreen();
            return;
        }

        initViews();
        loadUserData();
        handleEvents();
    }

    private void initViews() {
        tvMainDisplayName = findViewById(R.id.tvMainDisplayName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvUsername = findViewById(R.id.tvUsername);

        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnLogout);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
    }

    private void loadUserData() {
        new Thread(() -> {
            currentUser = userRepository.findUserByUsername(currentUsernameSession);
            runOnUiThread(() -> {
                if (currentUser != null) {
                    updateUI(currentUser);
                }
            });
        }).start();
    }

    private void updateUI(User user) {
        tvMainDisplayName.setText(user.getUsername());
        tvEmail.setText(user.getEmail());
        tvUsername.setText(user.getUsername());

        String phone = user.getSdt();
        if (phone != null && phone.length() >= 10) {
            phone = phone.substring(0, 4) + " " + phone.substring(4, 7) + " " + phone.substring(7);
        }
        tvPhone.setText(phone != null ? phone : "Chưa cập nhật");
    }

    private void handleEvents() {
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit().clear().apply();
                goToLoginScreen();
            });
        }

        // 1. Sự kiện Chỉnh sửa thông tin chung (Username, Email, SDT)
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                if (currentUser != null) showEditInfoDialog();
            });
        }

        // 2. Sự kiện Đổi mật khẩu
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> {
                if (currentUser != null) showChangePasswordDialog();
            });
        }
    }

    // --- DIALOG 1: CHỈNH SỬA THÔNG TIN CHUNG ---
    private void showEditInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_info, null);
        builder.setView(dialogView);

        EditText etUser = dialogView.findViewById(R.id.etEditUsername);
        EditText etEmail = dialogView.findViewById(R.id.etEditEmail);
        EditText etPhone = dialogView.findViewById(R.id.etEditPhone);

        // Fill dữ liệu cũ
        etUser.setText(currentUser.getUsername());
        etEmail.setText(currentUser.getEmail());
        etPhone.setText(currentUser.getSdt());

        builder.setPositiveButton("Lưu", (dialog, which) -> {}); // Override sau
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newUser = etUser.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();
            String newPhone = etPhone.getText().toString().trim();

            if (TextUtils.isEmpty(newUser) || TextUtils.isEmpty(newEmail) || TextUtils.isEmpty(newPhone)) {
                Toast.makeText(this, "Vui lòng không để trống thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật DB
            new Thread(() -> {
                currentUser.setUsername(newUser);
                currentUser.setEmail(newEmail);
                currentUser.setSdt(newPhone);
                userRepository.updateUser(currentUser);

                // Nếu đổi Username thì phải cập nhật lại Session
                getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                        .edit()
                        .putString(KEY_USERNAME, newUser)
                        .apply();
                currentUsernameSession = newUser;

                runOnUiThread(() -> {
                    updateUI(currentUser);
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            }).start();
        });
    }

    // --- DIALOG 2: ĐỔI MẬT KHẨU ---
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        EditText etOldPass = dialogView.findViewById(R.id.etOldPass);
        EditText etNewPass = dialogView.findViewById(R.id.etNewPass);
        EditText etConfirmPass = dialogView.findViewById(R.id.etConfirmNewPass);

        builder.setPositiveButton("Đổi mật khẩu", (dialog, which) -> {});
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String oldPass = etOldPass.getText().toString().trim();
            String newPass = etNewPass.getText().toString().trim();
            String confirmPass = etConfirmPass.getText().toString().trim();

            // 1. Validate trống
            if (TextUtils.isEmpty(oldPass) || TextUtils.isEmpty(newPass) || TextUtils.isEmpty(confirmPass)) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Validate độ dài mật khẩu mới
            if (newPass.length() < 6) {
                Toast.makeText(this, "Mật khẩu mới phải từ 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3. Kiểm tra mật khẩu cũ (Phải Hash trước khi so sánh)
            String hashedOldPass = SecurityUtils.hashPassword(oldPass);
            if (!currentUser.getPassword().equals(hashedOldPass)) {
                etOldPass.setError("Mật khẩu cũ không đúng!");
                return;
            }

            // 4. Kiểm tra khớp mật khẩu mới
            if (!newPass.equals(confirmPass)) {
                etConfirmPass.setError("Mật khẩu xác nhận không khớp!");
                return;
            }

            // 5. Mọi thứ OK -> Lưu vào DB
            new Thread(() -> {
                String hashedNewPass = SecurityUtils.hashPassword(newPass);
                currentUser.setPassword(hashedNewPass);
                userRepository.updateUser(currentUser);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            }).start();
        });
    }

    private void goToLoginScreen() {
        Intent intent = new Intent(UserActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}