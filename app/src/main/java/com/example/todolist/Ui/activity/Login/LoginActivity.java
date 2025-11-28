package com.example.todolist.Ui.activity.Login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todolist.Data.entity.User;
import com.example.todolist.R;
import com.example.todolist.Repository.UserRepository;
import com.example.todolist.Ui.activity.MainActivity;
import com.example.todolist.utils.SecurityUtils;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvLinkToSignup, tvForgotPassword;
    private String serverOtp = null;

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        userRepository = new UserRepository(getApplication());

        setupWindowInsets();
        initViews();
        setupEventListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username_login);
        etPassword = findViewById(R.id.et_password_login);
        btnLogin = findViewById(R.id.btn_login);
        tvLinkToSignup = findViewById(R.id.tv_link_to_signup);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupEventListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
        tvLinkToSignup.setOnClickListener(v -> navigateToSignup());
        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Tên đăng nhập và Mật khẩu.", Toast.LENGTH_SHORT).show();
            return;
        }

        User userFromDb = userRepository.findUserByUsername(username);

        if (userFromDb == null) {
            Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không đúng.", Toast.LENGTH_SHORT).show();
            return;
        }

        String hashedPassword = SecurityUtils.hashPassword(password);
        if (userFromDb.getPassword().equals(hashedPassword)) {
            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            getSharedPreferences("USER_DATA", MODE_PRIVATE)
                    .edit()
                    .putString("USERNAME", username)
                    .apply();
            navigateToMain(userFromDb.getUsername());
        } else {
            Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không đúng.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleForgotPassword() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.verify_sdt, null);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();

        EditText etConfirm_sdt = dialogView.findViewById(R.id.et_confirm_sdt);
        Button btnConfirm_sdt = dialogView.findViewById(R.id.btn_confirm_sdt);

        btnConfirm_sdt.setOnClickListener(v -> {
            String phoneNumber = etConfirm_sdt.getText().toString().trim();

            if (phoneNumber.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số điện thoại.", Toast.LENGTH_SHORT).show();
                return;
            }
            User userFromDb = findUserByPhoneNumber(phoneNumber);
            dialog.dismiss();
            showOTPDialog(userFromDb.getUsername(),phoneNumber);
        });
        dialog.show();
    }

    private void navigateToMain(String username) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
        finish();
    }

    private void navigateToSignup() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }
    private String generateRandomOTP() {
        int randomPin = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(randomPin);
    }
    private void showOTPDialog(String username ,String phoneNumber) {
        // 1. Tạo Dialog từ Layout XML
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_verify_otp, null);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();

        // 2. Ánh xạ các view trong Dialog
        TextView tvPhoneDisplay = dialogView.findViewById(R.id.tv_phone_display);
        EditText etOtpInput = dialogView.findViewById(R.id.et_otp_input);
        Button btnSendOtp = dialogView.findViewById(R.id.btn_send_otp);
        Button btnConfirmOtp = dialogView.findViewById(R.id.btn_confirm_otp);

        tvPhoneDisplay.setText("Xác thực SĐT: " + phoneNumber);

        // 3. Xử lý sự kiện nút "Lấy mã xác thực"
        btnSendOtp.setOnClickListener(v -> {
            // Tạo mã ngẫu nhiên
            serverOtp = generateRandomOTP();

            // --- GIẢ LẬP GỬI TIN NHẮN ---
            // Trong thực tế, bạn sẽ gọi API gửi SMS ở đây.
            // Ở đây mình dùng Toast để hiện mã lên màn hình cho bạn test.
            Toast.makeText(this, "Mã OTP của bạn là: " + serverOtp, Toast.LENGTH_LONG).show();

            // (Tùy chọn) Đổi text nút để báo đã gửi
            btnSendOtp.setText("Gửi lại mã");
        });

        // 4. Xử lý sự kiện nút "Xác nhận"
        btnConfirmOtp.setOnClickListener(v -> {
            String userEnteredOtp = etOtpInput.getText().toString().trim();

            if (serverOtp == null) {
                Toast.makeText(this, "Vui lòng nhấn Gửi mã trước!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userEnteredOtp.isEmpty()) {
                etOtpInput.setError("Vui lòng nhập mã!");
                return;
            }

            // SO SÁNH MÃ
            if (userEnteredOtp.equals(serverOtp)) {
                Toast.makeText(this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                setNewPassword(username,phoneNumber);

            } else {
                Toast.makeText(this, "Mã OTP không đúng, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
    private void setNewPassword(String username, String phoneNumber) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.set_pass, null);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();

        EditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        EditText etConfirmNewPass= dialogView.findViewById(R.id.et_confirm_new_password);
        Button btnConfirmNewPass = dialogView.findViewById(R.id.btn_confirm_new_password);


        btnConfirmNewPass.setOnClickListener(v -> {
            String password = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmNewPass.getText().toString().trim();
            if (password.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự.", Toast.LENGTH_SHORT).show();
                return ;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp.", Toast.LENGTH_SHORT).show();
                return ;
            }
            updateUser(username, password);
            Toast.makeText(this, "Đổi nật khẩu thành công!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();

        });
        dialog.show();

    }
    private void updateUser(String username, String password){
        String hashedPassword = SecurityUtils.hashPassword(password);
        if (hashedPassword == null) {
            Toast.makeText(this, "Lỗi hệ thống, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            return;
        }
        User userFromDb = userRepository.findUserByUsername(username);
        userFromDb.setPassword(hashedPassword);
        userRepository.updateUser(userFromDb);
    }
    private User findUserByPhoneNumber(String phoneNumber) {
        User userFromDb = userRepository.findUserByPhoneNumber(phoneNumber);
        if (userFromDb == null) {
            Toast.makeText(this, "Số điện thoại chưa được đăng kí.", Toast.LENGTH_SHORT).show();
            return null;
        }
        return userFromDb;
    }
}
