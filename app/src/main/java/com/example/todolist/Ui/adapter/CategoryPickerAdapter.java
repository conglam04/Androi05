package com.example.todolist.Ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.Data.entity.Category;
import com.example.todolist.R;

import java.util.ArrayList;
import java.util.List;

public class CategoryPickerAdapter extends RecyclerView.Adapter<CategoryPickerAdapter.ViewHolder> {

    private List<Category> categories = new ArrayList<>();
    private final OnItemClickListener listener;

    // Interface để bắt sự kiện click ra bên ngoài
    public interface OnItemClickListener {
        void onItemClick(Category category);
    }

    public CategoryPickerAdapter(List<Category> categories, OnItemClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    // Hàm cập nhật dữ liệu mới từ LiveData
    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item_category_simple.xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_simple, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);

        // Gán tên danh mục
        if (category != null) {
            holder.tvName.setText(category.getName());

            // Bắt sự kiện click vào item
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(category);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}