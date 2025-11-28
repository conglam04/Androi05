package com.example.todolist.Ui.maintaskfragement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.todolist.Data.entity.RecurrenceRule;
import com.example.todolist.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class RecurrenceBottomSheet extends BottomSheetDialogFragment {

    private RadioGroup radioGroupRecurrence;
    private RadioButton radioNone, radioDaily, radioWeekly, radioMonthly, radioYearly, radioCustom;
    private LinearLayout layoutCustomOptions;
    private CheckBox checkSunday, checkMonday, checkTuesday, checkWednesday,
            checkThursday, checkFriday, checkSaturday;

    // RepeatEndType views
    private RadioGroup radioGroupRepeatEnd;
    private RadioButton radioNever, radioUntilDate, radioCount;
    private LinearLayout layoutUntilDate, layoutCount;
    private Button btnSelectEndDate;
    private android.widget.NumberPicker numberPickerCount;

    private Button btnSave;
    private OnRecurrenceSelectedListener listener;

    // State
    private Long selectedEndDate = null;
    private int selectedOccurrenceCount = 3;

    public interface OnRecurrenceSelectedListener {
        void onRecurrenceSelected(RecurrenceRule rule);
    }

    public static RecurrenceBottomSheet newInstance(RecurrenceRule currentRule) {
        RecurrenceBottomSheet fragment = new RecurrenceBottomSheet();
        Bundle args = new Bundle();
        if (currentRule != null) {
            args.putLong("ruleId", currentRule.getRuleId());
            args.putString("pattern", currentRule.getPattern());
            args.putInt("interval", currentRule.getInterval());
            args.putString("daysOfWeek", currentRule.getDaysOfWeek());

            // RepeatEndType data
            args.putString("repeatEndType", currentRule.getRepeatEndType());
            if (currentRule.getEndDate() != null) {
                args.putLong("endDate", currentRule.getEndDate());
            }
            if (currentRule.getOccurrenceCount() != null) {
                args.putInt("occurrenceCount", currentRule.getOccurrenceCount());
            }
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnRecurrenceSelectedListener(OnRecurrenceSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_recurrence, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadCurrentRule();
        setupListeners();
    }

    private void initViews(View view) {
        radioGroupRecurrence = view.findViewById(R.id.radioGroupRecurrence);
        radioNone = view.findViewById(R.id.radioNone);
        radioDaily = view.findViewById(R.id.radioDaily);
        radioWeekly = view.findViewById(R.id.radioWeekly);
        radioMonthly = view.findViewById(R.id.radioMonthly);
        radioYearly = view.findViewById(R.id.radioYearly);
        radioCustom = view.findViewById(R.id.radioCustom);
        layoutCustomOptions = view.findViewById(R.id.layoutCustomOptions);

        checkSunday = view.findViewById(R.id.checkSunday);
        checkMonday = view.findViewById(R.id.checkMonday);
        checkTuesday = view.findViewById(R.id.checkTuesday);
        checkWednesday = view.findViewById(R.id.checkWednesday);
        checkThursday = view.findViewById(R.id.checkThursday);
        checkFriday = view.findViewById(R.id.checkFriday);
        checkSaturday = view.findViewById(R.id.checkSaturday);

        // RepeatEndType views
        radioGroupRepeatEnd = view.findViewById(R.id.radioGroupRepeatEnd);
        radioNever = view.findViewById(R.id.radioNever);
        radioUntilDate = view.findViewById(R.id.radioUntilDate);
        radioCount = view.findViewById(R.id.radioCount);
        layoutUntilDate = view.findViewById(R.id.layoutUntilDate);
        layoutCount = view.findViewById(R.id.layoutCount);
        btnSelectEndDate = view.findViewById(R.id.btnSelectEndDate);
        numberPickerCount = view.findViewById(R.id.numberPickerCount);

        // Configure NumberPicker
        numberPickerCount.setMinValue(1);
        numberPickerCount.setMaxValue(99);
        numberPickerCount.setValue(3);
        numberPickerCount.setWrapSelectorWheel(false);

        btnSave = view.findViewById(R.id.btnSave);
    }

    private void loadCurrentRule() {
        if (getArguments() != null) {
            String pattern = getArguments().getString("pattern", "NONE");
            String daysOfWeek = getArguments().getString("daysOfWeek");

            switch (pattern) {
                case "NONE":
                    radioNone.setChecked(true);
                    break;
                case "DAILY":
                    radioDaily.setChecked(true);
                    break;
                case "WEEKLY":
                    radioWeekly.setChecked(true);
                    break;
                case "MONTHLY":
                    radioMonthly.setChecked(true);
                    break;
                case "YEARLY":
                    radioYearly.setChecked(true);
                    break;
                case "CUSTOM":
                    radioCustom.setChecked(true);
                    layoutCustomOptions.setVisibility(View.VISIBLE);
                    loadSelectedDays(daysOfWeek);
                    break;
            }

            // Load repeatEndType
            String repeatEndType = getArguments().getString("repeatEndType", "NEVER");
            switch (repeatEndType) {
                case "NEVER":
                    radioNever.setChecked(true);
                    break;
                case "UNTIL_DATE":
                    radioUntilDate.setChecked(true);
                    layoutUntilDate.setVisibility(View.VISIBLE);
                    if (getArguments().containsKey("endDate")) {
                        selectedEndDate = getArguments().getLong("endDate");
                        // Format and show date on button
                        java.util.Calendar cal = java.util.Calendar.getInstance();
                        cal.setTimeInMillis(selectedEndDate);
                        String dateStr = cal.get(java.util.Calendar.DAY_OF_MONTH) + "/" +
                            (cal.get(java.util.Calendar.MONTH) + 1) + "/" +
                            cal.get(java.util.Calendar.YEAR);
                        btnSelectEndDate.setText(dateStr);
                    }
                    break;
                case "COUNT":
                    radioCount.setChecked(true);
                    layoutCount.setVisibility(View.VISIBLE);
                    if (getArguments().containsKey("occurrenceCount")) {
                        int count = getArguments().getInt("occurrenceCount", 3);
                        selectedOccurrenceCount = count;
                        numberPickerCount.setValue(count);
                    }
                    break;
            }
        } else {
            radioNone.setChecked(true);
            radioNever.setChecked(true);
        }
    }

    private void loadSelectedDays(String daysOfWeekJson) {
        if (daysOfWeekJson == null) return;

        try {
            JSONArray array = new JSONArray(daysOfWeekJson);
            for (int i = 0; i < array.length(); i++) {
                int day = array.getInt(i);
                switch (day) {
                    case 0: checkSunday.setChecked(true); break;
                    case 1: checkMonday.setChecked(true); break;
                    case 2: checkTuesday.setChecked(true); break;
                    case 3: checkWednesday.setChecked(true); break;
                    case 4: checkThursday.setChecked(true); break;
                    case 5: checkFriday.setChecked(true); break;
                    case 6: checkSaturday.setChecked(true); break;
                }
            }
        } catch (Exception e) {
            android.util.Log.e("RecurrenceBottomSheet", "Error loading selected days", e);
        }
    }

    private void setupListeners() {
        // Show/hide custom options
        radioGroupRecurrence.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioCustom) {
                layoutCustomOptions.setVisibility(View.VISIBLE);
            } else {
                layoutCustomOptions.setVisibility(View.GONE);
            }
        });

        setupRepeatEndListeners();

        btnSave.setOnClickListener(v -> {
            RecurrenceRule rule = buildRecurrenceRule();
            if (rule == null) {
                return; // Validation failed
            }
            if (listener != null) {
                listener.onRecurrenceSelected(rule);
            }
            dismiss();
        });
    }

    private void setupRepeatEndListeners() {
        // Show/hide repeatEnd layouts
        radioGroupRepeatEnd.setOnCheckedChangeListener((group, checkedId) -> {
            layoutUntilDate.setVisibility(View.GONE);
            layoutCount.setVisibility(View.GONE);

            if (checkedId == R.id.radioUntilDate) {
                layoutUntilDate.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.radioCount) {
                layoutCount.setVisibility(View.VISIBLE);
            }
        });

        // DatePicker for endDate
        btnSelectEndDate.setOnClickListener(v -> showDatePicker());

        // NumberPicker listener
        numberPickerCount.setOnValueChangedListener((picker, oldVal, newVal) -> {
            selectedOccurrenceCount = newVal;
        });
    }

    private void showDatePicker() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                java.util.Calendar selected = java.util.Calendar.getInstance();
                selected.set(year, month, dayOfMonth, 23, 59, 59);
                selectedEndDate = selected.getTimeInMillis();

                // Update button text
                String dateStr = dayOfMonth + "/" + (month + 1) + "/" + year;
                btnSelectEndDate.setText(dateStr);
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private RecurrenceRule buildRecurrenceRule() {
        RecurrenceRule rule = new RecurrenceRule();

        int selectedId = radioGroupRecurrence.getCheckedRadioButtonId();

        if (selectedId == R.id.radioNone) {
            rule.setPattern("NONE");
        } else if (selectedId == R.id.radioDaily) {
            rule.setPattern("DAILY");
            rule.setInterval(1);
        } else if (selectedId == R.id.radioWeekly) {
            rule.setPattern("WEEKLY");
            rule.setInterval(1);
        } else if (selectedId == R.id.radioMonthly) {
            rule.setPattern("MONTHLY");
            rule.setInterval(1);
        } else if (selectedId == R.id.radioYearly) {
            rule.setPattern("YEARLY");
            rule.setInterval(1);
        } else if (selectedId == R.id.radioCustom) {
            rule.setPattern("CUSTOM");
            rule.setInterval(1);
            String selectedDays = getSelectedDays();

            // Validation: Phải chọn ít nhất 1 ngày
            if (selectedDays.equals("[]")) {
                android.widget.Toast.makeText(getContext(),
                    "Vui lòng chọn ít nhất 1 ngày trong tuần",
                    android.widget.Toast.LENGTH_SHORT).show();
                return null;
            }

            rule.setDaysOfWeek(selectedDays);
        }

        // Set repeatEndType
        int repeatEndId = radioGroupRepeatEnd.getCheckedRadioButtonId();
        if (repeatEndId == R.id.radioNever) {
            rule.setRepeatEndType("NEVER");
            rule.setEndDate(null);
            rule.setOccurrenceCount(null);
        } else if (repeatEndId == R.id.radioUntilDate) {
            rule.setRepeatEndType("UNTIL_DATE");
            if (selectedEndDate == null) {
                android.widget.Toast.makeText(getContext(),
                    "Vui lòng chọn ngày kết thúc",
                    android.widget.Toast.LENGTH_SHORT).show();
                return null;
            }
            rule.setEndDate(selectedEndDate);
            rule.setOccurrenceCount(null);
        } else if (repeatEndId == R.id.radioCount) {
            rule.setRepeatEndType("COUNT");
            rule.setOccurrenceCount(selectedOccurrenceCount);
            rule.setEndDate(null);
        }

        return rule;
    }

    private String getSelectedDays() {
        List<Integer> selectedDays = new ArrayList<>();

        if (checkSunday.isChecked()) selectedDays.add(0);
        if (checkMonday.isChecked()) selectedDays.add(1);
        if (checkTuesday.isChecked()) selectedDays.add(2);
        if (checkWednesday.isChecked()) selectedDays.add(3);
        if (checkThursday.isChecked()) selectedDays.add(4);
        if (checkFriday.isChecked()) selectedDays.add(5);
        if (checkSaturday.isChecked()) selectedDays.add(6);

        JSONArray array = new JSONArray(selectedDays);
        return array.toString();
    }
}