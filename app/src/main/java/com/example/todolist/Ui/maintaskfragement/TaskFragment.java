package com.example.todolist.Ui.maintaskfragement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.Data.entity.TaskWithCategory;
import com.example.todolist.R;
import com.example.todolist.Ui.adapter.CategoryTabAdapter;
import com.example.todolist.Ui.adapter.TaskAdapterMain;
import com.example.todolist.Ui.viewmodel.TasksViewModel;
import com.example.todolist.Ui.widget.SwipeManager;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class TaskFragment extends Fragment {
    private TasksViewModel viewModel;
    private View root;
    private RecyclerView categoryRecyclerView;
    private TextView pastLabel, todayLabel, futureLabel;
    private RecyclerView todayTaskRecyclerView, passTaskRecyclerView, futureTaskRecyclerView;
    private CategoryTabAdapter categoryAdapter;
    private TaskAdapterMain pastAdapter, todayAdapter, futureAdapter;
    private FloatingActionButton fabAddTask;
    private ChipGroup chipGroupDateFilter;

    public TaskFragment() {
        super(R.layout.fragment_task);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(requireActivity());
        viewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        root = view;

        initViews();

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupCategoryTabs();
        setupDateFilterChips();
        setupTasksList();
        setupOutsideTouchListener();

        fabAddTask.setOnClickListener(v -> {
            closeAllOpenedItems();
            AddTaskBottomSheet bottomSheet = AddTaskBottomSheet.newInstance();
            bottomSheet.show(getParentFragmentManager(), "AddTaskBottomSheet");
        });

        observeData();
    }

    private void initViews() {
        categoryRecyclerView = root.findViewById(R.id.recyclerViewCategories);
        todayTaskRecyclerView = root.findViewById(R.id.recyclerViewTodayTasks);
        passTaskRecyclerView = root.findViewById(R.id.recyclerViewPassTasks);
        futureTaskRecyclerView = root.findViewById(R.id.recyclerViewFutureTasks);
        fabAddTask = root.findViewById(R.id.fabAddTask);
        pastLabel = root.findViewById(R.id.pastLabel);
        todayLabel = root.findViewById(R.id.todayLabel);
        futureLabel = root.findViewById(R.id.futureLabel);
        chipGroupDateFilter = root.findViewById(R.id.chipGroupDateFilter);
    }

    private void setupDateFilterChips() {
        chipGroupDateFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                onDateFilterChanged(checkedIds.get(0));
            }
        });

        if (chipGroupDateFilter.getCheckedChipId() != View.NO_ID) {
            onDateFilterChanged(chipGroupDateFilter.getCheckedChipId());
        }
    }

    private void onDateFilterChanged(int chipId) {
        viewModel.setDateFilter(chipId);
    }

    private void setupTasksList() {
        todayTaskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        passTaskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        futureTaskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        TaskAdapterMain.TaskClickListener taskClickListener = new TaskAdapterMain.TaskClickListener() {

            @Override
            public void onTaskFocusClick(TaskWithCategory task) {
                android.content.Intent intent = new android.content.Intent(requireContext(),
                    FocusTimerActivity.class);
                intent.putExtra("taskName", task.task.getTitle());
                intent.putExtra("taskId", task.task.getTaskId());
                startActivity(intent);
            }

            @Override
            public void onTaskCalendarClick(TaskWithCategory task) {
                Toast.makeText(requireContext(), "Calendar: " + task.task.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTaskStarClick(TaskWithCategory task) {
                viewModel.toggleTaskStar(task.task);
            }

            @Override
            public void onTaskCheckChange(TaskWithCategory task) {
                viewModel.toggleTaskCompletion(task.task);
            }

            @Override
            public void onTaskFlagClick(TaskWithCategory task) {
                viewModel.toggleTaskFlag(task.task);
            }

            @Override
            public void onTaskDelete(TaskWithCategory task) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa công việc này?")
                        .setPositiveButton("Xóa", (dialog, which) -> viewModel.deleteTask(task.task))
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public void onTaskLongClick(TaskWithCategory task) {
                Toast.makeText(requireContext(), "Chức năng chỉnh sửa lặp lại đang được phát triển", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTaskRepeatClick(TaskWithCategory task) {
                Toast.makeText(requireContext(), "Chức năng chỉnh sửa lặp lại đang được phát triển", Toast.LENGTH_SHORT).show();
            }
        };

        pastAdapter = new TaskAdapterMain(new ArrayList<>(), taskClickListener);
        todayAdapter = new TaskAdapterMain(new ArrayList<>(), taskClickListener);
        futureAdapter = new TaskAdapterMain(new ArrayList<>(), taskClickListener);

        todayTaskRecyclerView.setAdapter(todayAdapter);
        passTaskRecyclerView.setAdapter(pastAdapter);
        futureTaskRecyclerView.setAdapter(futureAdapter);

        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    closeAllOpenedItems();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (Math.abs(dy) > 0) {
                    closeAllOpenedItems();
                }
            }
        };

        todayTaskRecyclerView.addOnScrollListener(scrollListener);
        passTaskRecyclerView.addOnScrollListener(scrollListener);
        futureTaskRecyclerView.addOnScrollListener(scrollListener);

        setupRecyclerViewTouchListener(todayTaskRecyclerView, todayAdapter);
        setupRecyclerViewTouchListener(passTaskRecyclerView, pastAdapter);
        setupRecyclerViewTouchListener(futureTaskRecyclerView, futureAdapter);
    }

    private void setupRecyclerViewTouchListener(RecyclerView recyclerView, TaskAdapterMain currentAdapter) {
        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    if (pastAdapter != null && pastAdapter != currentAdapter) {
                        pastAdapter.closeOpenedItem();
                    }
                    if (todayAdapter != null && todayAdapter != currentAdapter) {
                        todayAdapter.closeOpenedItem();
                    }
                    if (futureAdapter != null && futureAdapter != currentAdapter) {
                        futureAdapter.closeOpenedItem();
                    }
                }
                return false;
            }
        });
    }

    private void setupOutsideTouchListener() {
        root.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                SwipeManager.getInstance().closeAllOpenedItems();
            }
            return false;
        });
    }

    private void closeAllOpenedItems() {
        SwipeManager.getInstance().closeAllOpenedItems();
    }

    private void observeData() {
        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                categoryAdapter.updateCategories(categories);
            }
        });

        viewModel.getPastTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (pastAdapter != null) {
                pastAdapter.updateTasks(tasks);
            }
            pastLabel.setVisibility(tasks == null || tasks.isEmpty() ? View.GONE : View.VISIBLE);
            passTaskRecyclerView.setVisibility(tasks == null || tasks.isEmpty() ? View.GONE : View.VISIBLE);
        });

        viewModel.getTodayTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (todayAdapter != null) {
                todayAdapter.updateTasks(tasks);
            }
            todayLabel.setVisibility(tasks == null || tasks.isEmpty() ? View.GONE : View.VISIBLE);
            todayTaskRecyclerView.setVisibility(tasks == null || tasks.isEmpty() ? View.GONE : View.VISIBLE);
        });

        viewModel.getFutureTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (futureAdapter != null) {
                futureAdapter.updateTasks(tasks);
            }
            futureLabel.setVisibility(tasks == null || tasks.isEmpty() ? View.GONE : View.VISIBLE);
            futureTaskRecyclerView.setVisibility(tasks == null || tasks.isEmpty() ? View.GONE : View.VISIBLE);
        });
    }

    private void setupCategoryTabs() {
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryTabAdapter(new ArrayList<>(), category -> {
            closeAllOpenedItems();
            categoryAdapter.setSelectedCategoryId(category.getCategoryId());
            viewModel.selectCategory(category.getCategoryId());
        });
        categoryRecyclerView.setAdapter(categoryAdapter);

        categoryAdapter.setSelectedCategoryId(-1);
    }
}
