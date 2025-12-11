package com.example.todolist.Ui.setting;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolist.R;
import com.example.todolist.Ui.theme.ThemeActivity;

import java.util.ArrayList;
import java.util.List;

public class FaqActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        toolbar = findViewById(R.id.toolbar_faq);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.faq_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        applyTheme();

        RecyclerView recyclerView = findViewById(R.id.faq_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<FaqItem> faqList = new ArrayList<>();
        faqList.add(new FaqItem(getString(R.string.faq_q1), getString(R.string.faq_a1)));
        faqList.add(new FaqItem(getString(R.string.faq_q2), getString(R.string.faq_a2)));
        faqList.add(new FaqItem(getString(R.string.faq_q3), getString(R.string.faq_a3)));
        faqList.add(new FaqItem(getString(R.string.faq_q4), getString(R.string.faq_a4)));
        faqList.add(new FaqItem(getString(R.string.faq_q5), getString(R.string.faq_a5)));
        faqList.add(new FaqItem(getString(R.string.faq_q6), getString(R.string.faq_a6)));
        faqList.add(new FaqItem(getString(R.string.faq_q7), getString(R.string.faq_a7)));
        faqList.add(new FaqItem(getString(R.string.faq_q8), getString(R.string.faq_a8)));

        FaqAdapter adapter = new FaqAdapter(faqList);
        recyclerView.setAdapter(adapter);
    }

    private void applyTheme() {
        SharedPreferences prefs = getSharedPreferences(ThemeActivity.PREFS_NAME, MODE_PRIVATE);
        int defaultColor = ContextCompat.getColor(this, R.color.blue);
        int themeColor = prefs.getInt(ThemeActivity.KEY_THEME_COLOR, defaultColor);

        toolbar.setBackgroundColor(themeColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(themeColor);
        }

        if (isColorDark(themeColor)) {
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
