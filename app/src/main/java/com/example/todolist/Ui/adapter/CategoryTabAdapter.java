package com.example.todolist.Ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.Data.entity.Category;
import com.example.todolist.R;

import java.util.ArrayList;
import java.util.List;

public class CategoryTabAdapter extends RecyclerView.Adapter<CategoryTabAdapter.CategoryViewHolder> {
    private List<Category> categories;
    private long selectedCategoryId = -1; // Default: Tất cả
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryTabAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_tab, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        if (position == 0) {
            // Item "Tất cả"
            holder.bind(null, selectedCategoryId == -1);
        } else {
            Category category = categories.get(position - 1);
            holder.bind(category, category.getCategoryId() == selectedCategoryId);
        }
    }

    @Override
    public int getItemCount() {
        return categories.size() + 1; // +1 cho item "Tất cả"
    }

    public void updateCategories(List<Category> newCategories) {
        this.categories = newCategories != null ? newCategories : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setSelectedCategoryId(long categoryId) {
        this.selectedCategoryId = categoryId;
        notifyDataSetChanged();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView textCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardCategory);
            textCategoryName = itemView.findViewById(R.id.textCategoryName);
        }

        public void bind(Category category, boolean isSelected) {
            // Nếu category == null thì đây là item "Tất cả"
            String categoryName = category != null ? category.getName() : "Tất cả";
            textCategoryName.setText(categoryName);

            // Thay đổi màu nền khi được chọn
            if (isSelected) {
                cardView.setCardBackgroundColor(Color.parseColor("#2196F3")); // Xanh khi chọn
                textCategoryName.setTextColor(Color.WHITE);
            } else {
                cardView.setCardBackgroundColor(Color.parseColor("#F5F5F5")); // Xám nhạt khi không chọn
                textCategoryName.setTextColor(Color.parseColor("#424242"));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    if (category != null) {
                        // Category thường
                        listener.onCategoryClick(category);
                    } else {
                        // Item "Tất cả" - tạo category giả với ID = -1
                        Category allCategory = new Category();
                        allCategory.setCategoryId(-1);
                        allCategory.setName("Tất cả");
                        listener.onCategoryClick(allCategory);
                    }
                }
            });
        }
    }
}