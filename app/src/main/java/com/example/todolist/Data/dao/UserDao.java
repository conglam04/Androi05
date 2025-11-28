package com.example.todolist.Data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.todolist.Data.entity.User;

import java.util.List;

/**
 * Data Access Object (DAO) cho bảng 'users'.
 * Đã được tối ưu hóa: loại bỏ các phương thức trùng lặp và thiếu an toàn.
 */
@Dao
public interface UserDao {

    // 🔹 ----- CRUD Operations (Create - Read - Update - Delete) -----

    /**
     * Chèn một người dùng mới vào database.
     * @return ID của dòng mới được chèn.
     */
    @Insert
    long insertUser(User user);

    /**
     * Cập nhật thông tin của một người dùng đã có.
     */
    @Update
    void updateUser(User user);

    /**
     * Xóa một người dùng khỏi database.
     */
    @Delete
    void deleteUser(User user);


    // 🔹 ----- Query Operations (Truy vấn) -----

    /**
     * Lấy một người dùng dựa trên ID.
     * @param id ID của người dùng cần tìm.
     * @return Đối tượng User hoặc null nếu không tìm thấy.
     */
    @Query("SELECT * FROM users WHERE user_id = :id LIMIT 1")
    User findUserById(int id);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findUserByUsername(String username);


    @Query("SELECT * FROM users ORDER BY user_id DESC")
    List<User> getAllUsers();
    @Query("SELECT * FROM users WHERE sdt = :phoneNumber LIMIT 1")
    User findUserByPhoneNumber(String phoneNumber);



}
