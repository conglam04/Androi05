package com.example.todolist.Ui.activity.Login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todolist.Data.entity.User;
import com.example.todolist.R;
// --- SỬA IMPORT NÀY ---
import com.example.todolist.Data.Repository.UserRepository;
import com.example.todolist.Ui.activity.MainActivity;
import com.example.todolist.utils.SecurityUtils;

public class SignupActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etConfirmPassword, etSdt, etEmail;
    private Button btnSignup;
    private TextView tvLinkToLogin;
    private String serverOtp = null;

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_layout);

        userRepository = new UserRepository(getApplication());

        setupWindowInsets();
        initViews();
        setupEventListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.et_username_signup);
        etSdt = findViewById(R.id.et_phone_signup);
        etEmail = findViewById(R.id.et_email_signup);
        etPassword = findViewById(R.id.et_password_signup);
        etConfirmPassword = findViewById(R.id.et_confirm_password_signup);
        btnSignup = findViewById(R.id.btn_signup);
        tvLinkToLogin = findViewById(R.id.tv_link_to_login);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupEventListeners() {
        btnSignup.setOnClickListener(v -> handleSignup());
        tvLinkToLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void handleSignup() {
        String username = etUsername.getText().toString().trim();
        String sdt = etSdt.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!isInputValid(username, password, confirmPassword, sdt, email)) {
            return;
        }

        if (isUsernameExists(username)) {
            Toast.makeText(this, "Tên đăng nhập này đã tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }

        showOTPDialog(username, sdt, email, password);
    }

    private boolean isInputValid(String username, String password, String confirmPassword, String sdt, String email) {
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || sdt.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Email không hợp lệ hoặc bị bỏ trống!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isValidPhoneNumber(sdt)) {
            Toast.makeText(this, "Số điện thoại phải 10 chữ số và bắt đầu bằng 0.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isUsernameExists(String username) {
        User existingUser = userRepository.findUserByUsername(username);
        return existingUser != null;
    }

    private void createUserAndSave(String username, String password, String sdt, String email) {
        String hashedPassword = SecurityUtils.hashPassword(password);
        if (hashedPassword == null) {
            Toast.makeText(this, "Lỗi hệ thống, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(hashedPassword);
        newUser.setSdt(sdt);
        newUser.setEmail(email);

        long userId = userRepository.insertUser(newUser);

        if (userId > 0) {
            getSharedPreferences("USER_DATA", MODE_PRIVATE)
                    .edit()
                    .putString("USERNAME", username)
                    .putInt("USER_ID", (int) userId)
                    .apply();
        }
    }

    private void navigateToMain(String username) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("USERNAME", username);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        String phoneRegex = "^0[0-9]{9}$";
        return phone.matches(phoneRegex);
    }

    public boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private String generateRandomOTP() {
        int randomPin = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(randomPin);
    }

    private void showOTPDialog(String username, String phoneNumber, String email, String password) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_verify_otp, null);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();

        TextView tvPhoneDisplay = dialogView.findViewById(R.id.tv_phone_display);
        EditText etOtpInput = dialogView.findViewById(R.id.et_otp_input);
        Button btnSendOtp = dialogView.findViewById(R.id.btn_send_otp);
        Button btnConfirmOtp = dialogView.findViewById(R.id.btn_confirm_otp);

        tvPhoneDisplay.setText("Xác thực SĐT: " + phoneNumber);

        btnSendOtp.setOnClickListener(v -> {
            serverOtp = generateRandomOTP();
            Toast.makeText(this, "Mã OTP của bạn là: " + serverOtp, Toast.LENGTH_LONG).show();
            btnSendOtp.setText("Gửi lại mã");
        });

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

            if (userEnteredOtp.equals(serverOtp)) {
                Toast.makeText(this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                createUserAndSave(username, password, phoneNumber, email);

                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_LONG).show();
                navigateToMain(username);
            } else {
                Toast.makeText(this, "Mã OTP không đúng, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}