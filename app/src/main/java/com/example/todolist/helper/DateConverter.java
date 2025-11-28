package com.example.todolist.helper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateConverter {

    public static String formatDate(Long dueDate) {
        if (dueDate == null) {
            return null;
        }
        // Convert Long timestamp to LocalDate
        LocalDate date = Instant.ofEpochMilli(dueDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // Format to dd/MM/yyyy
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }
}