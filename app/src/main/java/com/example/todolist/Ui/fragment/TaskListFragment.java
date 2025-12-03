package com.example.todolist.Ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.Data.Repository.TaskRepository;
import com.example.todolist.Data.entity.Task;
import com.example.todolist.Data.entity.TaskWithCategory;
import com.example.todolist.R;
import com.example.todolist.Ui.adapter.TaskAdapter;
import com.example.todolist.ViewModel.TaskViewModel;

import java.util.ArrayList;

public class TaskListFragment extends Fragment {

    private TaskAdapter adapter;
    private TaskViewModel taskViewModel;
    private TaskRepository taskRepository; // Dùng để update/delete trực tiếp

    public TaskListFragment() {
        super(R.layout.fragment_task_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Khởi tạo Repository (để xử lý các hành động ghi: Update/Delete)
        taskRepository = new TaskRepository(requireContext());

        // 2. Khởi tạo ViewModel (để xử lý việc đọc dữ liệu: Load list)
        // Sử dụng requireActivity() để ViewModel tồn tại theo vòng đời Activity (giữ dữ liệu khi xoay màn hình)
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        // 3. Setup RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 4. Setup Adapter
        // Truyền vào list rỗng ban đầu và lắng nghe sự kiện
        adapter = new TaskAdapter(new ArrayList<TaskWithCategory>(), new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskStatusChanged(Task task) {
                // Cập nhật trạng thái vào Database
                taskRepository.update(task);

                // Refresh lại list sau 1 khoảng thời gian ngắn để cập nhật thống kê/sắp xếp
                reloadListWithDelay();
            }

            @Override
            public void onTaskClick(Task task) {
                // Xử lý khi click vào item (ví dụ: hiện dialog sửa)
                Toast.makeText(getContext(), "Đã chọn: " + task.getTitle(), Toast.LENGTH_SHORT).show();
                // Bạn có thể gọi AddTaskBottomSheet.newInstance(...) ở đây để sửa task
            }

            @Override
            public void onTaskLongClick(Task task) {
                // Xử lý xóa task khi nhấn giữ
                showDeleteConfirmation(task);
            }
        });

        recyclerView.setAdapter(adapter);

        // 5. Quan sát LiveData từ ViewModel
        // Khi DB thay đổi hoặc loadTasks() được gọi, hàm này sẽ chạy và cập nhật UI
        taskViewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                adapter.updateTasksWithCategory(tasks);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // 6. Load lại dữ liệu mỗi khi màn hình hiện lên (quan trọng)
        if (taskViewModel != null) {
            taskViewModel.loadTasks();
        }
    }

    // Helper: Hiện hộp thoại xác nhận xóa
    private void showDeleteConfirmation(Task task) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xóa công việc")
                .setMessage("Bạn có chắc muốn xóa \"" + task.getTitle() + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    taskRepository.delete(task);
                    Toast.makeText(getContext(), "Đã xóa", Toast.LENGTH_SHORT).show();
                    reloadListWithDelay();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Helper: Load lại list có delay (để tránh Race Condition giữa luồng Ghi và Đọc)
    private void reloadListWithDelay() {
        if (getView() != null) {
            getView().postDelayed(() -> {
                if (taskViewModel != null) taskViewModel.loadTasks();
            }, 200); // Delay 200ms
        }
    }
}
