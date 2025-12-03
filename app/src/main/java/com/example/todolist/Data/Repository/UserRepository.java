package com.example.todolist.Data.Repository; // SỬA LẠI PACKAGE CHO ĐÚNG

import android.app.Application;

import com.example.todolist.Data.AppDatabase;
import com.example.todolist.Data.dao.UserDao;
import com.example.todolist.Data.entity.User;

public class UserRepository {

    private final UserDao userDao;

    public UserRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        this.userDao = database.userDao();
    }

    public User findUserByUsername(String username) {
        return userDao.findUserByUsername(username);
    }

    // Trả về ID (long) để biết user vừa tạo là ai
    public long insertUser(User user) {
        return userDao.insertUser(user);
    }

    public User findUserByPhoneNumber(String phone) {
        return userDao.findUserByPhoneNumber(phone);
    }

    public void updateUser(User user) {
        userDao.updateUser(user);
    }
}