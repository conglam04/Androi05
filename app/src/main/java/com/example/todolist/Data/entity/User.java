package com.example.todolist.Data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    private int userId;

    @NonNull
    @ColumnInfo(name = "username")
    private String username;

    @NonNull
    @ColumnInfo(name = "password")
    private String password;

    @ColumnInfo(name = "sdt")
    private String sdt;

    @ColumnInfo(name = "email")
    private String email;

    /**
     * ✅ SỬA 1: Đổi kiểu dữ liệu của createdAt thành long.
     * Lưu dưới dạng timestamp (số mili giây từ 1/1/1970) để dễ dàng so sánh và định dạng.
     */
    @ColumnInfo(name = "created_at")
    private long createdAt;

    // 🔹 Constructor mặc định (Room yêu cầu)
    public User() {
    }

    /**
     * 🔹 Constructor tiện ích để tạo user mới.
     * ✅ SỬA 2: Thêm @Ignore để báo cho Room bỏ qua hàm khởi tạo này.
     */
    @Ignore
    public User(@NonNull String username, @NonNull String password, String sdt, String email) {
        this.username = username;
        this.password = password;
        this.sdt = sdt;
        this.email = email;
        // Gán thời gian hiện tại khi tạo mới
        this.createdAt = System.currentTimeMillis();
    }

    // 🔹 Getter & Setter (đã cập nhật cho createdAt)

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
        // ✅ SỬA 3: Xóa dòng "this.createdAt = System.currentTimeMillis();" khỏi đây.
        // Ngày tạo tài khoản không nên bị thay đổi khi cập nhật username.
        this.createdAt = System.currentTimeMillis();
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    public void setPassword(@NonNull String password) {
        this.password = password;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return username + " (" + email + ")";
    }
}
