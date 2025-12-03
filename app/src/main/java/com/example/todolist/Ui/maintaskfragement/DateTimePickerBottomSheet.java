package com.example.todolist.Ui.maintaskfragement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.todolist.Data.entity.RecurrenceRule;
import com.example.todolist.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateTimePickerBottomSheet extends BottomSheetDialogFragment {

    public interface OnDateTimeSelectedListener {
        void onDateTimeSelected(long timestamp, Long reminderTimestamp, RecurrenceRule repeatRule);
    }

    private OnDateTimeSelectedListener listener;

    private CalendarView calendarView;
    private TextView valueTime, valueReminder, valueRepeat;
    private ImageButton btnClose, btnOk;
    private long selectedDateMillis = -1;
    private Long selectedTimeMillis = null;
    private Long selectedReminderMillis = null;
    private RecurrenceRule selectedRecurrenceRule;

    private final DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public DateTimePickerBottomSheet() {
        // Required empty public constructor
    }

    public static DateTimePickerBottomSheet newInstance(Long initialDueDate, Long initialReminderDate, RecurrenceRule recurrenceRule) {
        DateTimePickerBottomSheet fragment = new DateTimePickerBottomSheet();
        Bundle args = new Bundle();
        if (initialDueDate != null) {
            args.putLong("dueDate", initialDueDate);
        }
        if (initialReminderDate != null) {
            args.putLong("reminderDate", initialReminderDate);
        }
        if (recurrenceRule != null) {
            args.putSerializable("recurrenceRule", recurrenceRule);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnDateTimeSelectedListener(OnDateTimeSelectedListener l) {
        this.listener = l;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_date_time_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        calendarView = view.findViewById(R.id.calendarView);
        valueTime = view.findViewById(R.id.value_time);
        valueReminder = view.findViewById(R.id.value_reminder);
        valueRepeat = view.findViewById(R.id.value_repeat);
        btnClose = view.findViewById(R.id.btnClose);
        btnOk = view.findViewById(R.id.btnOk);
//        btnIdea = view.findViewById(R.id.findViewById);

        // Initialize from arguments if provided
        if (getArguments() != null) {
            if (getArguments().containsKey("dueDate")) {
                selectedDateMillis = getArguments().getLong("dueDate");
                Calendar dueCal = Calendar.getInstance();
                dueCal.setTimeInMillis(selectedDateMillis);
                selectedTimeMillis = selectedDateMillis;
                calendarView.setDate(selectedDateMillis, false, false);
            } else {
                selectedDateMillis = calendarView.getDate();
            }

            if (getArguments().containsKey("reminderDate")) {
                selectedReminderMillis = getArguments().getLong("reminderDate");
            }

            if (getArguments().containsKey("recurrenceRule")) {
                selectedRecurrenceRule = (RecurrenceRule) getArguments().getSerializable("recurrenceRule");
            }
        } else {
            // init selectedDate = today
            selectedDateMillis = calendarView.getDate();
        }

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar c = Calendar.getInstance();
            c.set(year, month, dayOfMonth, 0, 0, 0);
            c.set(Calendar.MILLISECOND, 0);
            selectedDateMillis = c.getTimeInMillis();

            // Check if selected date matches any quick select chip and highlight it
            checkAndHighlightMatchingChip(view);
        });

        // chips quick actions (today, tomorrow, 3 days...) - using IDs from layout
        View chipToday = view.findViewById(R.id.chip_today);
        View chipTomorrow = view.findViewById(R.id.chip_tomorrow);
        View chip3Days = view.findViewById(R.id.chip_3days);
        View chipNone = view.findViewById(R.id.chip_none);
        View chipSunday = view.findViewById(R.id.chip_sunday);

        chipToday.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            selectedDateMillis = c.getTimeInMillis();
            calendarView.setDate(selectedDateMillis, true, true);
            updateChipSelection(chipToday, chipTomorrow, chip3Days, chipNone, chipSunday);
        });

        chipTomorrow.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, 1);
            selectedDateMillis = c.getTimeInMillis();
            calendarView.setDate(selectedDateMillis, true, true);
            updateChipSelection(chipTomorrow, chipToday, chip3Days, chipNone, chipSunday);
        });

        chip3Days.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, 3);
            selectedDateMillis = c.getTimeInMillis();
            calendarView.setDate(selectedDateMillis, true, true);
            updateChipSelection(chip3Days, chipToday, chipTomorrow, chipNone, chipSunday);
        });

        chipNone.setOnClickListener(v -> {
            selectedDateMillis = -1;
            // you may visually clear selection; CalendarView default cannot clear selection easily
            Toast.makeText(requireContext(), "Bỏ chọn ngày", Toast.LENGTH_SHORT).show();
            updateChipSelection(chipNone, chipToday, chipTomorrow, chip3Days, chipSunday);
        });

        chipSunday.setOnClickListener(v -> {
            // set to next Sunday
            Calendar c = Calendar.getInstance();
            while (c.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                c.add(Calendar.DAY_OF_YEAR, 1);
            }
            selectedDateMillis = c.getTimeInMillis();
            calendarView.setDate(selectedDateMillis, true, true);
            updateChipSelection(chipSunday, chipToday, chipTomorrow, chip3Days, chipNone);
        });

        // time row click -> time picker
        View rowTime = view.findViewById(R.id.row_time);
        rowTime.setOnClickListener(v -> showTimePicker(true));

        View rowReminder = view.findViewById(R.id.row_reminder);
        rowReminder.setOnClickListener(v -> showTimePicker(false));

        View rowRepeat = view.findViewById(R.id.row_repeat);
        rowRepeat.setOnClickListener(v -> showRepeatDialog());

        // close / ok
        btnClose.setOnClickListener(v -> dismiss());
        btnOk.setOnClickListener(v -> {
            long resultTimestamp = -1;
            Long resultReminderTimestamp = null;

            if (selectedDateMillis != -1) {
                if (selectedTimeMillis != null) {
                    // merge date + time
                    Calendar dateCal = Calendar.getInstance();
                    dateCal.setTimeInMillis(selectedDateMillis);

                    Calendar timeCal = Calendar.getInstance();
                    timeCal.setTimeInMillis(selectedTimeMillis);

                    dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
                    dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
                    dateCal.set(Calendar.SECOND, 0);
                    resultTimestamp = dateCal.getTimeInMillis();
                } else {
                    // date only -> default time to end of day (23:59:59)
                    Calendar dateCal = Calendar.getInstance();
                    dateCal.setTimeInMillis(selectedDateMillis);
                    dateCal.set(Calendar.HOUR_OF_DAY, 23);
                    dateCal.set(Calendar.MINUTE, 59);
                    dateCal.set(Calendar.SECOND, 59);
                    dateCal.set(Calendar.MILLISECOND, 999);
                    resultTimestamp = dateCal.getTimeInMillis();
                }
            }

            if (selectedReminderMillis != null && selectedDateMillis != -1) {
                Calendar reminderCal = Calendar.getInstance();
                reminderCal.setTimeInMillis(selectedDateMillis);

                Calendar reminderTimeCal = Calendar.getInstance();
                reminderTimeCal.setTimeInMillis(selectedReminderMillis);

                reminderCal.set(Calendar.HOUR_OF_DAY, reminderTimeCal.get(Calendar.HOUR_OF_DAY));
                reminderCal.set(Calendar.MINUTE, reminderTimeCal.get(Calendar.MINUTE));
                reminderCal.set(Calendar.SECOND, 0);
                resultReminderTimestamp = reminderCal.getTimeInMillis();

                // Validate: Reminder time must be before due time (if same day)
                if (resultTimestamp != -1 && resultReminderTimestamp >= resultTimestamp) {
                    Toast.makeText(requireContext(),
                        "Thời gian nhắc nhở phải trước giờ deadline!",
                        Toast.LENGTH_SHORT).show();
                    return; // Don't close dialog
                }

                // Validate: Reminder time must be in future
                if (resultReminderTimestamp <= System.currentTimeMillis()) {
                    Toast.makeText(requireContext(),
                        "Thời gian nhắc nhở phải ở tương lai!",
                        Toast.LENGTH_SHORT).show();
                    return; // Don't close dialog
                }
            }

            if (listener != null) {
                listener.onDateTimeSelected(resultTimestamp, resultReminderTimestamp, selectedRecurrenceRule);
            }
            dismiss();
        });

        // initial UI values
        updateUI();
    }

    private void updateUI() {
        if (selectedTimeMillis != null) {
            valueTime.setText(timeFormat.format(selectedTimeMillis));
        } else {
            valueTime.setText("Không");
        }

        if (selectedReminderMillis != null) {
            valueReminder.setText(timeFormat.format(selectedReminderMillis));
        } else {
            valueReminder.setText("Không");
        }

        valueRepeat.setText(selectedRecurrenceRule != null ? selectedRecurrenceRule.getPattern() : "Không");
    }

    private void showTimePicker(boolean isDueTime) {
        // Get current time or existing selected time
        Calendar currentCal = Calendar.getInstance();
        int hour = currentCal.get(Calendar.HOUR_OF_DAY);
        int minute = currentCal.get(Calendar.MINUTE);

        // If editing existing time, use that value
        if (isDueTime && selectedTimeMillis != null) {
            currentCal.setTimeInMillis(selectedTimeMillis);
            hour = currentCal.get(Calendar.HOUR_OF_DAY);
            minute = currentCal.get(Calendar.MINUTE);
        } else if (!isDueTime && selectedReminderMillis != null) {
            currentCal.setTimeInMillis(selectedReminderMillis);
            hour = currentCal.get(Calendar.HOUR_OF_DAY);
            minute = currentCal.get(Calendar.MINUTE);
        }

        // Use MaterialTimePicker (like in Lich package)
        String title = isDueTime ? "Đặt giờ deadline" : "Đặt giờ nhắc nhở";

        com.google.android.material.timepicker.MaterialTimePicker picker =
                new com.google.android.material.timepicker.MaterialTimePicker.Builder()
                .setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText(title)
                .setInputMode(com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK)
                .build();

        picker.addOnPositiveButtonClickListener(v -> {
            int h = picker.getHour();
            int m = picker.getMinute();

            // Create time calendar
            Calendar sel = Calendar.getInstance();
            sel.set(Calendar.HOUR_OF_DAY, h);
            sel.set(Calendar.MINUTE, m);
            sel.set(Calendar.SECOND, 0);
            sel.set(Calendar.MILLISECOND, 0);

            if (isDueTime) {
                selectedTimeMillis = sel.getTimeInMillis();
                valueTime.setText(timeFormat.format(selectedTimeMillis));
            } else {
                selectedReminderMillis = sel.getTimeInMillis();
                valueReminder.setText(timeFormat.format(selectedReminderMillis));
            }
        });

        picker.show(getParentFragmentManager(), isDueTime ? "TimePickerDue" : "TimePickerReminder");
    }

    private void showRepeatDialog() {
        RecurrenceBottomSheet bottomSheet = RecurrenceBottomSheet.newInstance(selectedRecurrenceRule);
        bottomSheet.setOnRecurrenceSelectedListener(rule -> {
            selectedRecurrenceRule = rule;
            updateUI();
        });
        bottomSheet.show(getParentFragmentManager(), "RecurrenceBottomSheet");
    }

    private void updateChipSelection(View selectedChip, View... otherChips) {
        // Set selected chip to blue background and white text
        selectedChip.setBackgroundResource(R.drawable.bg_chip_selected);
        if (selectedChip instanceof TextView) {
            ((TextView) selectedChip).setTextColor(getResources().getColor(R.color.white, null));
        }

        // Set other chips to gray background and primary text color
        for (View chip : otherChips) {
            chip.setBackgroundResource(R.drawable.bg_chip_gray);
            if (chip instanceof TextView) {
                ((TextView) chip).setTextColor(getResources().getColor(R.color.text_primary, null));
            }
        }
    }

    private void checkAndHighlightMatchingChip(View parentView) {
        if (selectedDateMillis == -1) {
            return;
        }

        View chipToday = parentView.findViewById(R.id.chip_today);
        View chipTomorrow = parentView.findViewById(R.id.chip_tomorrow);
        View chip3Days = parentView.findViewById(R.id.chip_3days);
        View chipSunday = parentView.findViewById(R.id.chip_sunday);
        View chipNone = parentView.findViewById(R.id.chip_none);

        // Get selected date (reset time to compare only dates)
        Calendar selectedCal = Calendar.getInstance();
        selectedCal.setTimeInMillis(selectedDateMillis);
        selectedCal.set(Calendar.HOUR_OF_DAY, 0);
        selectedCal.set(Calendar.MINUTE, 0);
        selectedCal.set(Calendar.SECOND, 0);
        selectedCal.set(Calendar.MILLISECOND, 0);

        // Check today
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        if (isSameDay(selectedCal, today)) {
            updateChipSelection(chipToday, chipNone, chipTomorrow, chip3Days, chipSunday);
            return;
        }

        // Check tomorrow
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        if (isSameDay(selectedCal, tomorrow)) {
            updateChipSelection(chipTomorrow, chipNone, chipToday, chip3Days, chipSunday);
            return;
        }

        // Check 3 days later
        Calendar threeDays = (Calendar) today.clone();
        threeDays.add(Calendar.DAY_OF_YEAR, 3);

        if (isSameDay(selectedCal, threeDays)) {
            updateChipSelection(chip3Days, chipNone, chipToday, chipTomorrow, chipSunday);
            return;
        }

        // Check next Sunday
        Calendar nextSunday = (Calendar) today.clone();
        while (nextSunday.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            nextSunday.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (isSameDay(selectedCal, nextSunday)) {
            updateChipSelection(chipSunday, chipNone, chipToday, chipTomorrow, chip3Days);
            return;
        }

        // If no match, don't highlight any chip (keep current state or highlight none)
        // Reset all chips to gray
        updateChipSelection(chipNone, chipToday, chipTomorrow, chip3Days, chipSunday);
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}