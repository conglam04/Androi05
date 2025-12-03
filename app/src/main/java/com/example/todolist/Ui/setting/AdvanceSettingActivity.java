package com.example.todolist.Ui.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.example.todolist.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class AdvanceSettingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SettingPrefs";
    private static final String DARK_MODE_KEY = "dark_mode";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advance_setting);

        Toolbar toolbar = findViewById(R.id.toolbar_advance_setting);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Cài đặt nâng cao");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
