package com.example.todolist.Ui.maintaskfragement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.todolist.Data.entity.Task;
import com.example.todolist.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Bottom sheet để edit/cancel recurring task khi long click
 */
public class EditRecurrenceBottomSheet extends BottomSheetDialogFragment {

    private Task task;
    private OnActionSelectedListener listener;

    public interface OnActionSelectedListener {
        void onEditRecurrence(Task task);
        void onCancelRecurrence(Task task);
    }

    public static EditRecurrenceBottomSheet newInstance(Task task) {
        EditRecurrenceBottomSheet sheet = new EditRecurrenceBottomSheet();
        Bundle args = new Bundle();
        // Pass task data through bundle if needed
        sheet.setArguments(args);
        sheet.task = task;
        return sheet;
    }

    public void setOnActionSelectedListener(OnActionSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_edit_recurrence, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textTitle = view.findViewById(R.id.textTitle);
        TextView textInfo = view.findViewById(R.id.textInfo);
        Button btnEditRecurrence = view.findViewById(R.id.btnEditRecurrence);
        Button btnCancelRecurrence = view.findViewById(R.id.btnCancelRecurrence);
        Button btnClose = view.findViewById(R.id.btnClose);

        if (task != null) {
            textTitle.setText(task.getTitle());

            if (task.isRecurring()) {
                textInfo.setText("Task này đang lặp lại");
            } else {
                textInfo.setText("Task này không lặp lại");
                btnEditRecurrence.setText("Thêm lặp lại");
                btnCancelRecurrence.setVisibility(View.GONE);
            }
        }

        btnEditRecurrence.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditRecurrence(task);
            }
            dismiss();
        });

        btnCancelRecurrence.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelRecurrence(task);
            }
            dismiss();
        });

        btnClose.setOnClickListener(v -> dismiss());
    }
}

