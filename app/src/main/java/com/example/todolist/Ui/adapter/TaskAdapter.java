package com.example.todolist.Ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.Data.entity.Task;
import com.example.todolist.Data.entity.TaskWithCategory;
import com.example.todolist.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    // Thay đổi List<Task> thành List<TaskWithCategory>
    private List<TaskWithCategory> taskList;
    private final OnTaskClickListener listener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public interface OnTaskClickListener {
        void onTaskStatusChanged(Task task);
        void onTaskClick(Task task);
        void onTaskLongClick(Task task);
    }

    public TaskAdapter(List<TaskWithCategory> taskList, OnTaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    public void updateTasks(List<TaskWithCategory> newTasks) {
        this.taskList = newTasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task1, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskWithCategory item = taskList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView tvTitle, tvTime, tvCategory;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTime = itemView.findViewById(R.id.tvTaskTime);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }

        public void bind(TaskWithCategory item, OnTaskClickListener listener) {
            Task task = item.task;

            tvTitle.setText(task.getTitle());
            checkBox.setChecked(task.getIsCompleted() == 1);

            // --- LẤY TÊN DANH MỤC TỪ BẢNG CATEGORY ---
            if (item.category != null) {
                tvCategory.setText(item.category.getName());
                tvCategory.setVisibility(View.VISIBLE);
            } else {
                tvCategory.setVisibility(View.GONE);
            }

            // Hiển thị thời gian (Logic cũ giữ nguyên)
            if (task.getDueDate() != null) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(task.getDueDate());
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                if (hour == 0 && minute == 0) {
                    tvTime.setText("Không");
                    tvTime.setTextColor(itemView.getContext().getColor(android.R.color.darker_gray));
                } else {
                    tvTime.setText(timeFormat.format(new Date(task.getDueDate())));
                    tvTime.setTextColor(itemView.getContext().getColor(R.color.black));
                }
                tvTime.setVisibility(View.VISIBLE);
            } else {
                tvTime.setText("Không");
                tvTime.setVisibility(View.VISIBLE);
            }

            checkBox.setOnClickListener(v -> {
                task.setIsCompleted(checkBox.isChecked() ? 1 : 0);
                listener.onTaskStatusChanged(task);
            });

            itemView.setOnClickListener(v -> listener.onTaskClick(task));
            itemView.setOnLongClickListener(v -> {
                listener.onTaskLongClick(task);
                return true;
            });
        }
    }
}