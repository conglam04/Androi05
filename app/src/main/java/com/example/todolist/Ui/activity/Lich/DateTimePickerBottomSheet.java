package com.example.todolist.Ui.activity.Lich;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.todolist.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateTimePickerBottomSheet extends BottomSheetDialogFragment {

    // Interface gửi dữ liệu về Activity
    public interface OnDateTimeSelectedListener {
        void onDateTimeSelected(@Nullable Long dateMillis, @Nullable Long timeMillis, @Nullable Long reminderMillis, @Nullable String repeatRule);
    }

    private OnDateTimeSelectedListener listener;

    private CalendarView calendarView;
    private TextView valueTime, valueReminder, valueRepeat;
    private ImageButton btnClose, btnOk;

    private long selectedDateMillis = -1;
    private Long selectedTimeMillis = null;
    private Long selectedReminderMillis = null;
    private String selectedRepeat = "Không";

    // Format hiển thị
    private final DateFormat displayTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    // Format để parse dữ liệu truyền vào
    private static final DateFormat PARSE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final DateFormat PARSE_TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public static DateTimePickerBottomSheet newInstance(
            @Nullable String initialDate,
            @Nullable String initialTime,
            @Nullable String initialReminder,
            @Nullable String initialRepeat
    ) {
        DateTimePickerBottomSheet fragment = new DateTimePickerBottomSheet();
        Bundle args = new Bundle();
        args.putString("INITIAL_DATE", initialDate);
        args.putString("INITIAL_TIME", initialTime);
        args.putString("INITIAL_REMINDER", initialReminder);
        args.putString("INITIAL_REPEAT", initialRepeat);
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
        // Ánh xạ View
        calendarView = view.findViewById(R.id.calendarView);
        valueTime = view.findViewById(R.id.value_time);
        valueReminder = view.findViewById(R.id.value_reminder);
        valueRepeat = view.findViewById(R.id.value_repeat);
        btnClose = view.findViewById(R.id.btnClose);
        btnOk = view.findViewById(R.id.btnOk);

        Bundle args = getArguments();
        String initialDate = null;
        String initialTime = null;
        String initialReminder = null;
        String initialRepeat = null;

        if (args != null) {
            initialDate = args.getString("INITIAL_DATE");
            initialTime = args.getString("INITIAL_TIME");
            initialReminder = args.getString("INITIAL_REMINDER");
            initialRepeat = args.getString("INITIAL_REPEAT");
        }

        // --- XỬ LÝ DỮ LIỆU BAN ĐẦU ---
        try {
            // 1. Ngày
            Calendar dateCal = Calendar.getInstance();
            resetTime(dateCal);

            if (initialDate != null && !initialDate.isEmpty()) {
                try {
                    dateCal.setTime(PARSE_DATE_FORMAT.parse(initialDate));
                } catch (Exception e) {
                    // Fallback về hôm nay
                }
            }
            resetTime(dateCal);
            selectedDateMillis = dateCal.getTimeInMillis();
            calendarView.setDate(selectedDateMillis, true, true);

            // 2. Giờ
            if (initialTime != null && !initialTime.isEmpty()) {
                // Kiểm tra nếu là 00:00 thì coi như chưa chọn (null)
                if (!initialTime.equals("00:00")) {
                    Calendar timeCal = Calendar.getInstance();
                    timeCal.setTime(PARSE_TIME_FORMAT.parse(initialTime));
                    selectedTimeMillis = timeCal.getTimeInMillis();
                } else {
                    selectedTimeMillis = null;
                }
            }

            // 3. Reminder
            if (initialReminder != null && !initialReminder.isEmpty()) {
                Calendar reminderCal = Calendar.getInstance();
                reminderCal.setTime(PARSE_TIME_FORMAT.parse(initialReminder));
                selectedReminderMillis = reminderCal.getTimeInMillis();
            }

            // 4. Repeat
            selectedRepeat = (initialRepeat != null && !initialRepeat.isEmpty()) ? initialRepeat : "Không";

        } catch (ParseException e) {
            Log.e("DateTimePicker", "Lỗi parse ngày/giờ ban đầu", e);
            selectedRepeat = "Không";
        }

        // --- SỰ KIỆN LỊCH ---
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar c = Calendar.getInstance();
            c.set(year, month, dayOfMonth);
            resetTime(c);
            selectedDateMillis = c.getTimeInMillis();
        });

        setupChipListeners(view);

        view.findViewById(R.id.row_time).setOnClickListener(v -> showTimePicker(true));
        view.findViewById(R.id.row_reminder).setOnClickListener(v -> showTimePicker(false));
        view.findViewById(R.id.row_repeat).setOnClickListener(v -> showRepeatDialog());

        btnClose.setOnClickListener(v -> dismiss());

        btnOk.setOnClickListener(v -> {
            if (listener != null) {
                Long finalTime = selectedTimeMillis;

                // Kiểm tra lại lần cuối: Nếu giờ là 00:00 thì trả về NULL (Không)
                if (finalTime != null) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(finalTime);
                    if (c.get(Calendar.HOUR_OF_DAY) == 0 && c.get(Calendar.MINUTE) == 0) {
                        finalTime = null;
                    }
                }

                if (selectedDateMillis == -1) {
                    listener.onDateTimeSelected(null, null, selectedReminderMillis, selectedRepeat);
                } else {
                    listener.onDateTimeSelected(selectedDateMillis, finalTime, selectedReminderMillis, selectedRepeat);
                }
            }
            dismiss();
        });

        updateUI();
    }

// ... Các phần import và biến khai báo giữ nguyên

    // 1. Sửa lại hàm này (tìm đến dòng setupChipListeners cũ và thay thế)
    private void setupChipListeners(View view) {
        // Phải ép kiểu về TextView để setTextColor được
        TextView chipNone = view.findViewById(R.id.chip_none);
        TextView chipToday = view.findViewById(R.id.chip_today);
        TextView chipTomorrow = view.findViewById(R.id.chip_tomorrow);
        TextView chipSunday = view.findViewById(R.id.chip_sunday);
        TextView chip3Days = view.findViewById(R.id.chip_3days);

        chipToday.setOnClickListener(v -> {
            setDateFromNow(0);
            updateChipSelection(chipToday, chipNone, chipTomorrow, chipSunday, chip3Days);
        });

        chipTomorrow.setOnClickListener(v -> {
            setDateFromNow(1);
            updateChipSelection(chipTomorrow, chipToday, chipNone, chipSunday, chip3Days);
        });

        chip3Days.setOnClickListener(v -> {
            setDateFromNow(3);
            updateChipSelection(chip3Days, chipToday, chipTomorrow, chipNone, chipSunday);
        });

        chipSunday.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            resetTime(c);
            while (c.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                c.add(Calendar.DAY_OF_YEAR, 1);
            }
            selectedDateMillis = c.getTimeInMillis();
            calendarView.setDate(selectedDateMillis, true, true);

            updateChipSelection(chipSunday, chipToday, chipTomorrow, chipNone, chip3Days);
        });

        chipNone.setOnClickListener(v -> {
            selectedDateMillis = -1;
            selectedTimeMillis = null;
            Toast.makeText(requireContext(), "Đã bỏ chọn ngày", Toast.LENGTH_SHORT).show();
            updateUI();

            updateChipSelection(chipNone, chipToday, chipTomorrow, chipSunday, chip3Days);
        });
    }

    // 2. Thêm hàm mới này vào dưới cùng class (hoặc ngay sau hàm setupChipListeners)
    private void updateChipSelection(TextView selectedChip, TextView... otherChips) {
        // Set màu xanh cho chip được chọn
        selectedChip.setBackgroundResource(R.drawable.bg_chip_selected); // Màu xanh (file bạn đã có)
        selectedChip.setTextColor(getResources().getColor(R.color.white, null));

        // Set màu xám cho các chip còn lại
        for (TextView chip : otherChips) {
            chip.setBackgroundResource(R.drawable.bg_chip_gray); // Màu xám (file bạn đã có)
            chip.setTextColor(getResources().getColor(R.color.text_primary, null)); // Màu chữ đen
        }
    }

    // ... Các hàm khác giữ nguyên

    private void setDateFromNow(int daysToAdd) {
        Calendar c = Calendar.getInstance();
        resetTime(c);
        c.add(Calendar.DAY_OF_YEAR, daysToAdd);
        selectedDateMillis = c.getTimeInMillis();
        calendarView.setDate(selectedDateMillis, true, true);
    }

    private void resetTime(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    private void updateUI() {
        // --- SỬA LOGIC HIỂN THỊ TẠI ĐÂY ---
        if (selectedTimeMillis != null) {
            // Kiểm tra xem có phải 00:00 không
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(selectedTimeMillis);
            if (c.get(Calendar.HOUR_OF_DAY) == 0 && c.get(Calendar.MINUTE) == 0) {
                valueTime.setText("Không"); // Nếu là 00:00 -> Hiện "Không"
            } else {
                valueTime.setText(displayTimeFormat.format(selectedTimeMillis));
            }
        } else {
            valueTime.setText("Không");
        }

        if (selectedReminderMillis != null) {
            valueReminder.setText(displayTimeFormat.format(selectedReminderMillis));
        } else {
            valueReminder.setText("Không");
        }
        valueRepeat.setText(selectedRepeat != null ? selectedRepeat : "Không");
    }

    private void showTimePicker(boolean isDueTime) {
        Calendar c = Calendar.getInstance();
        Long targetMillis = isDueTime ? selectedTimeMillis : selectedReminderMillis;
        if (targetMillis != null) {
            c.setTimeInMillis(targetMillis);
        }

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog tpd = new TimePickerDialog(requireContext(), (view, hourOfDay, minute1) -> {
            Calendar sel = Calendar.getInstance();
            sel.set(Calendar.HOUR_OF_DAY, hourOfDay);
            sel.set(Calendar.MINUTE, minute1);

            // Nếu chọn 00:00 -> coi như set về 00:00 (updateUI sẽ hiển thị là "Không")
            // Nhưng vẫn giữ giá trị để logic biết là đã chọn
            if (isDueTime) {
                selectedTimeMillis = sel.getTimeInMillis();
            } else {
                selectedReminderMillis = sel.getTimeInMillis();
            }
            updateUI();
        }, hour, minute, true);

        tpd.setButton(TimePickerDialog.BUTTON_NEUTRAL, "Xóa", (dialog, which) -> {
            if (isDueTime) {
                selectedTimeMillis = null;
            } else {
                selectedReminderMillis = null;
            }
            updateUI();
        });

        tpd.setButton(TimePickerDialog.BUTTON_NEGATIVE, "Hủy", (dialog, which) -> dialog.dismiss());

        tpd.show();
    }

    private void showRepeatDialog() {
        final String[] options = new String[]{"Không", "Hàng ngày", "Hàng tuần", "Hàng tháng", "Hàng năm"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Lặp lại");
        builder.setItems(options, (dialog, which) -> {
            selectedRepeat = options[which];
            valueRepeat.setText(selectedRepeat);
        });
        builder.show();
    }
}