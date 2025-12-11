package com.example.todolist.Ui.theme;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.example.todolist.R;

public class ThemeActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "ThemePrefs";
    public static final String KEY_THEME_COLOR = "theme_color";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_theme);

        Toolbar toolbar = findViewById(R.id.toolbar_theme);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.theme_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        GridLayout colorGrid = findViewById(R.id.color_grid);
        for (int i = 0; i < colorGrid.getChildCount(); i++) {
            View child = colorGrid.getChildAt(i);
            if (child instanceof CardView) {
                final CardView colorCard = (CardView) child;
                colorCard.setOnClickListener(v -> {
                    int newColor = colorCard.getCardBackgroundColor().getDefaultColor();
                    toolbar.setBackgroundColor(newColor);

                    // Save the selected color
                    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
                    editor.putInt(KEY_THEME_COLOR, newColor);
                    editor.apply();

                    // Check contrast and change title/icon color
                    if (isColorDark(newColor)) {
                        toolbar.setTitleTextColor(Color.WHITE);
                        if (toolbar.getNavigationIcon() != null) {
                            toolbar.getNavigationIcon().setTint(Color.WHITE);
                        }
                    } else {
                        toolbar.setTitleTextColor(Color.BLACK);
                        if (toolbar.getNavigationIcon() != null) {
                            toolbar.getNavigationIcon().setTint(Color.BLACK);
                        }
                    }
                });
            }
        }
    }

    private boolean isColorDark(@ColorInt int color) {
        double luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return luminance < 0.5;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
