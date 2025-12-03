package com.example.todolist.Data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.todolist.Data.dao.CategoryDao;
import com.example.todolist.Data.dao.RecurrenceRuleDao;
import com.example.todolist.Data.dao.TaskDao;
import com.example.todolist.Data.dao.UserDao;
import com.example.todolist.Data.entity.Category;
import com.example.todolist.Data.entity.RecurrenceRule;
import com.example.todolist.Data.entity.Task;
import com.example.todolist.Data.entity.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Task.class, Category.class, User.class, RecurrenceRule.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract TaskDao taskDao();
    public abstract CategoryDao categoryDao();
    public abstract UserDao userDao();
    public abstract RecurrenceRuleDao recurrenceRuleDao();
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "todo_database")
                    .fallbackToDestructiveMigration() // Cho phép xóa DB cũ xây lại mới
                    .addCallback(roomCallback)
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

    // Callback để thêm các Category mặc định khi tạo DB
    private static final Callback roomCallback = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Executors.newSingleThreadExecutor().execute(() -> {
                CategoryDao dao = INSTANCE.categoryDao();
                // Thêm các danh mục mặc định khớp với CategoryBottomSheet
                dao.insertCategory(new Category("Công việc"));
                dao.insertCategory(new Category("Cá nhân"));
                dao.insertCategory(new Category("Học tập"));
                dao.insertCategory(new Category("Sức khỏe"));
                dao.insertCategory(new Category("Khác"));
            });
        }
    };
}