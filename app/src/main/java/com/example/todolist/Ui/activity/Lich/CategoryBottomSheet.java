package com.example.todolist.Ui.activity.Lich;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.Data.Repository.CategoryRepository;
import com.example.todolist.R;
import com.example.todolist.Ui.adapter.CategoryPickerAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class CategoryBottomSheet extends BottomSheetDialogFragment {

    private CategorySelectedListener listener;
    private CategoryRepository categoryRepository;
    private RecyclerView recyclerView;
    private CategoryPickerAdapter adapter;

    public interface CategorySelectedListener {
        void onCategorySelected(String category);
    }

    public void setCategorySelectedListener(CategorySelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo Repository
        categoryRepository = new CategoryRepository(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_category_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerCategoryPicker);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Khởi tạo adapter với list rỗng ban đầu
        adapter = new CategoryPickerAdapter(new ArrayList<>(), category -> {
            if (listener != null) {
                // Trả về tên category khi user click chọn
                listener.onCategorySelected(category.getName());
            }
            dismiss();
        });
        recyclerView.setAdapter(adapter);

        // Quan sát dữ liệu từ DB (LiveData) để tự động cập nhật list
        categoryRepository.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                adapter.setCategories(categories);
            }
        });
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }
}