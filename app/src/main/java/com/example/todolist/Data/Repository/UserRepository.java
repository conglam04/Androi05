package com.example.todolist.Repository;

import android.app.Application;

import com.example.todolist.Data.AppDatabase;
import com.example.todolist.Data.dao.UserDao;
import com.example.todolist.Data.entity.User;

/**
 * Repository quản lý tất cả các nguồn dữ liệu liên quan đến User.
 * Nó trừu tượng hóa các hoạt động dữ liệu khỏi phần còn lại của ứng dụng.
 * Hiện tại, nó chỉ lấy dữ liệu từ Room Database.
 */
public class UserRepository {

    private final UserDao userDao;

    // Hàm khởi tạo, nhận vào Application context để lấy instance của database
    public UserRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        this.userDao = database.userDao();
    }

    /**
     * Tìm một người dùng trong database bằng username.
     * @param username Tên người dùng cần tìm.
     * @return Đối tượng User nếu tìm thấy, ngược lại trả về null.
     */
    public User findUserByUsername(String username) {
        // Room tự động xử lý việc chạy query này trên luồng nền
        // nếu sử dụng LiveData hoặc Coroutines.
        // Vì chúng ta đang dùng allowMainThreadQueries(), nó sẽ chạy ngay lập tức.
        return userDao.findUserByUsername(username);
    }

    /**
     * Chèn một người dùng mới vào database.
     * @param user Đối tượng người dùng cần chèn.
     */
    public void insertUser(User user) {
        // Trong một ứng dụng thực tế, chúng ta sẽ chạy tác vụ này trên luồng nền.
        // Ví dụ: AppDatabase.databaseWriteExecutor.execute(() -> { userDao.insertUser(user); });
        userDao.insertUser(user);
    }
    public User findUserByPhoneNumber(String phone) {
        return userDao.findUserByPhoneNumber(phone);
    }
    public void updateUser(User user) {
        userDao.updateUser(user);
    }
}
