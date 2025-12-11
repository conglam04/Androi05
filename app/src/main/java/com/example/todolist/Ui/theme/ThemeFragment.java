package com.example.todolist.Ui.theme;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.todolist.R;

public class ThemeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_theme, container, false);

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        CardView redCard = view.findViewWithTag("red");

        if (redCard != null) {
            redCard.setOnClickListener(v -> {
                int color = ContextCompat.getColor(requireContext(), R.color.red);
                toolbar.setBackgroundColor(color);
            });
        }

        return view;
    }
}
