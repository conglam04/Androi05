package com.example.todolist.helper;


import com.example.todolist.Data.entity.RecurrenceRule;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RecurrenceHelper {

    /**
     * Tính toán ngày kế tiếp dựa trên recurrence rule
     */
    public static Long calculateNextOccurrence(Long currentDate, RecurrenceRule rule) {
        if (currentDate == null || rule == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentDate);

        switch (rule.getPattern()) {
            case "DAILY":
                calendar.add(Calendar.DAY_OF_YEAR, rule.getInterval());
                break;

            case "WEEKLY":
                calendar.add(Calendar.WEEK_OF_YEAR, rule.getInterval());
                break;

            case "MONTHLY":
                calendar.add(Calendar.MONTH, rule.getInterval());
                break;

            case "YEARLY":
                calendar.add(Calendar.YEAR, rule.getInterval());
                break;

            case "CUSTOM":
                return calculateCustomNextOccurrence(currentDate, rule);

            default:
                return null;
        }

        // Kiểm tra end date
        if (rule.getEndDate() != null && calendar.getTimeInMillis() > rule.getEndDate()) {
            return null;
        }

        return calendar.getTimeInMillis();
    }

    /**
     * Tính toán cho custom pattern (theo ngày trong tuần)
     */
    private static Long calculateCustomNextOccurrence(Long currentDate, RecurrenceRule rule) {
        if (rule.getDaysOfWeek() == null || rule.getDaysOfWeek().isEmpty()) {
            return null;
        }

        try {
            JSONArray daysArray = new JSONArray(rule.getDaysOfWeek());

            // Validation: Kiểm tra có ít nhất 1 ngày được chọn
            if (daysArray.length() == 0) {
                return null;
            }

            List<Integer> selectedDays = new ArrayList<>();
            for (int i = 0; i < daysArray.length(); i++) {
                selectedDays.add(daysArray.getInt(i));
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(currentDate);

            // Tìm ngày tiếp theo trong danh sách các ngày đã chọn
            for (int i = 0; i < 7; i++) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                int checkDay = calendar.get(Calendar.DAY_OF_WEEK);

                if (selectedDays.contains(checkDay - 1)) { // -1 vì Calendar.SUNDAY = 1
                    return calendar.getTimeInMillis();
                }
            }

        } catch (Exception e) {
            // Sử dụng logging thay vì printStackTrace
            android.util.Log.e("RecurrenceHelper", "Error calculating custom occurrence", e);
        }

        return null;
    }

    /**
     * Check xem có nên tiếp tục generate instances không dựa trên repeatEndType
     */
    public static boolean shouldContinueRecurrence(RecurrenceRule rule, long currentDate, int generatedCount) {
        String endType = rule.getRepeatEndType();

        if ("NEVER".equals(endType)) {
            return true;
        } else if ("UNTIL_DATE".equals(endType)) {
            if (rule.getEndDate() != null && currentDate > rule.getEndDate()) {
                return false;
            }
        } else if ("COUNT".equals(endType)) {
            if (rule.getOccurrenceCount() != null && generatedCount >= rule.getOccurrenceCount()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Normalize date về đầu ngày (00:00:00) để so sánh
     */
    public static long normalizeDateToDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Check xem recurring task có còn active không
     */
    public static boolean isRecurringActive(RecurrenceRule rule) {
        if (rule == null || !rule.isActive()) {
            return false;
        }

        if ("COUNT".equals(rule.getRepeatEndType())) {
            if (rule.getOccurrenceCount() != null &&
                rule.getCompletedCount() >= rule.getOccurrenceCount()) {
                return false;
            }
        } else if ("UNTIL_DATE".equals(rule.getRepeatEndType())) {
            if (rule.getEndDate() != null &&
                System.currentTimeMillis() > rule.getEndDate()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Format recurrence pattern thành text dễ đọc
     */
    public static String formatRecurrenceText(RecurrenceRule rule) {
        if (rule == null) {
            return "Không lặp lại";
        }

        switch (rule.getPattern()) {
            case "DAILY":
                if (rule.getInterval() == 1) {
                    return "Hàng ngày";
                } else {
                    return "Mỗi " + rule.getInterval() + " ngày";
                }

            case "WEEKLY":
                if (rule.getInterval() == 1) {
                    return "Hàng tuần";
                } else {
                    return "Mỗi " + rule.getInterval() + " tuần";
                }

            case "MONTHLY":
                if (rule.getInterval() == 1) {
                    return "Hàng tháng";
                } else {
                    return "Mỗi " + rule.getInterval() + " tháng";
                }

            case "YEARLY":
                return "Hàng năm";

            case "CUSTOM":
                return formatCustomRecurrence(rule);

            default:
                return "Không lặp lại";
        }
    }

    private static String formatCustomRecurrence(RecurrenceRule rule) {
        if (rule.getDaysOfWeek() == null) {
            return "Tùy chỉnh";
        }

        try {
            JSONArray daysArray = new JSONArray(rule.getDaysOfWeek());
            String[] dayNames = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
            StringBuilder builder = new StringBuilder("Mỗi ");

            for (int i = 0; i < daysArray.length(); i++) {
                int day = daysArray.getInt(i);
                builder.append(dayNames[day]);
                if (i < daysArray.length() - 1) {
                    builder.append(", ");
                }
            }

            return builder.toString();
        } catch (Exception e) {
            return "Tùy chỉnh";
        }
    }
}
