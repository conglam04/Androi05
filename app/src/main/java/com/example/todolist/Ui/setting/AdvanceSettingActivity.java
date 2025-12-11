package com.example.todolist.Ui.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.todolist.R;
import com.example.todolist.Ui.theme.ThemeActivity;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Locale;

public class AdvanceSettingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SettingPrefs";
    private static final String DARK_MODE_KEY = "dark_mode";
    private static final String LANGUAGE_KEY = "language";
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_advance_setting);

        toolbar = findViewById(R.id.toolbar_advance_setting);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.advanced_settings);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        applyTheme();

        SwitchMaterial darkModeSwitch = findViewById(R.id.dark_mode_switch);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean(DARK_MODE_KEY, false);
        darkModeSwitch.setChecked(isDarkMode);

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(DARK_MODE_KEY, isChecked);
            editor.apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        RadioGroup languageRadioGroup = findViewById(R.id.language_radio_group);
        String currentLanguage = sharedPreferences.getString(LANGUAGE_KEY, "vi");
        if (currentLanguage.equals("en")) {
            languageRadioGroup.check(R.id.radio_english);
        } else {
            languageRadioGroup.check(R.id.radio_vietnamese);
        }

        languageRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String lang;
            if (checkedId == R.id.radio_english) {
                lang = "en";
            } else {
                lang = "vi";
            }
            setLocale(lang);
            recreate();
        });
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(LANGUAGE_KEY, lang);
        editor.apply();
    }

    private void loadLocale() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String language = prefs.getString(LANGUAGE_KEY, "vi");
        setLocale(language);
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
