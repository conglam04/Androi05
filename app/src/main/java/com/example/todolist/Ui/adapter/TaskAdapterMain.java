package com.example.todolist.Ui.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.example.todolist.Data.entity.TaskWithCategory;
import com.example.todolist.R;
import com.example.todolist.Ui.widget.SwipeManager;
import com.example.todolist.Ui.widget.SwipeRevealLayout;
import com.example.todolist.helper.DateConverter;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapterMain extends RecyclerView.Adapter<TaskAdapterMain.TaskViewHolder> {
    private List<TaskWithCategory> tasks;
    private final TaskClickListener listener;

    public interface TaskClickListener {
        void onTaskFocusClick(TaskWithCategory task);
        void onTaskCalendarClick(TaskWithCategory task);
        void onTaskStarClick(TaskWithCategory task);
        void onTaskCheckChange(TaskWithCategory task);
        void onTaskFlagClick(TaskWithCategory task);
        void onTaskDelete(TaskWithCategory task);
        void onTaskLongClick(TaskWithCategory task);
        void onTaskRepeatClick(TaskWithCategory task);
    }

    public TaskAdapterMain(List<TaskWithCategory> tasks, TaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskWithCategory task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<TaskWithCategory> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    public List<TaskWithCategory> getTasks() {
        return tasks != null ? tasks : new ArrayList<>();
    }

    public void closeOpenedItem() {
        SwipeManager.getInstance().closeAllOpenedItems();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        private final SwipeRevealLayout swipeLayout;
        private final CheckBox checkboxTask;
        private final TextView textTitle;
        private final TextView textDate;
        private final ImageButton btnFlag;
        private final ImageButton btnRepeat;
        private final ImageButton btnCalendar;
        private final ImageButton btnFocus;
        private final ImageButton btnStar;
        private final ImageButton btnDelete;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            swipeLayout = itemView.findViewById(R.id.swipeLayout);
            btnCalendar = itemView.findViewById(R.id.btnCalendar);
            btnFocus = itemView.findViewById(R.id.btnFocus);
            btnStar = itemView.findViewById(R.id.btnStar);
            checkboxTask = itemView.findViewById(R.id.checkboxTask);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDate = itemView.findViewById(R.id.textCategory);
            btnFlag = itemView.findViewById(R.id.btnFlag);
            btnRepeat = itemView.findViewById(R.id.btnRepeat);
            btnDelete = itemView.findViewById(R.id.btnDelete);

        }

        public void bind(TaskWithCategory taskWithCategory) {
            swipeLayout.reset();
            textTitle.setText(taskWithCategory.task.getTitle());
            textDate.setText(DateConverter.formatDate(taskWithCategory.task.getDueDate()));
            checkboxTask.setChecked(taskWithCategory.task.getIsCompleted()==1);

            ImageViewCompat.setImageTintList(
                    btnFlag,
                    taskWithCategory.task.isFlagged()
                            ? ColorStateList.valueOf(Color.RED)
                            : ColorStateList.valueOf(Color.GRAY)

            );

            ImageViewCompat.setImageTintList(
                    btnStar,
                    taskWithCategory.task.getIsStarred()==1
                            ? ColorStateList.valueOf(Color.YELLOW)
                            : ColorStateList.valueOf(Color.GRAY)

            );

            // Show/hide repeat button
            if (taskWithCategory.task.isRecurring()) {
                btnRepeat.setVisibility(View.VISIBLE);
                ImageViewCompat.setImageTintList(
                    btnRepeat,
                    ColorStateList.valueOf(Color.BLUE)
                );
            } else {
                btnRepeat.setVisibility(View.GONE);
            }

            // Long click để edit recurring
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onTaskLongClick(taskWithCategory);
                }
                return true;
            });

            checkboxTask.setOnClickListener(v -> {
                // Animate checkbox
                v.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .start();
                    })
                    .start();

                if (listener != null) {
                    listener.onTaskCheckChange(taskWithCategory);
                }
            });

            btnFlag.setOnClickListener(v -> {
                // Visual feedback
                v.setPressed(true);
                v.postDelayed(() -> v.setPressed(false), 100);

                if (listener != null) {
                    listener.onTaskFlagClick(taskWithCategory);
                }
            });

            btnRepeat.setOnClickListener(v -> {
                // Visual feedback
                v.setPressed(true);
                v.postDelayed(() -> v.setPressed(false), 100);

                if (listener != null) {
                    listener.onTaskRepeatClick(taskWithCategory);
                }
            });

            // Background buttons
            btnCalendar.setOnClickListener(v -> {
                // Visual feedback
                v.setPressed(true);
                v.postDelayed(() -> v.setPressed(false), 100);

                if (listener != null) {
                    listener.onTaskCalendarClick(taskWithCategory);
                }
                swipeLayout.animateClose();
            });

            btnStar.setOnClickListener(v -> {
                // Visual feedback
                v.setPressed(true);
                v.postDelayed(() -> v.setPressed(false), 100);

                if (listener != null) {
                    listener.onTaskStarClick(taskWithCategory);
                }
                swipeLayout.animateClose();
            });

            btnFocus.setOnClickListener(v -> {
                // Visual feedback
                v.setPressed(true);
                v.postDelayed(() -> v.setPressed(false), 100);

                if (listener != null) {
                    listener.onTaskFocusClick(taskWithCategory);
                }
                swipeLayout.animateClose();
            });

            btnDelete.setOnClickListener(v -> {
                // Visual feedback with red tint
                v.setPressed(true);
                v.postDelayed(() -> v.setPressed(false), 100);

                if (listener != null) {
                    listener.onTaskDelete(taskWithCategory);
                }
                swipeLayout.animateClose();
            });
        }
    }
}