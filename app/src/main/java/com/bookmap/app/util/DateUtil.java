package com.bookmap.app.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    private static final SimpleDateFormat DB_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private static final SimpleDateFormat DB_FORMAT_SHORT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DB_FORMAT_DATE_ONLY = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat UI_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", new Locale("pt", "BR"));
    private static final SimpleDateFormat UI_FORMAT_DATE_ONLY = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));

    public static String formatToBrazilian(String dbDate) {
        if (dbDate == null || dbDate.trim().isEmpty()) return "";
        try {
            Date date;
            if (dbDate.length() == 10) {
                date = DB_FORMAT_DATE_ONLY.parse(dbDate);
                if (date != null) return UI_FORMAT_DATE_ONLY.format(date);
            } else if (dbDate.length() == 16) {
                date = DB_FORMAT_SHORT.parse(dbDate);
            } else {
                date = DB_FORMAT.parse(dbDate);
            }
            if (date != null) {
                return UI_FORMAT.format(date);
            }
        } catch (ParseException e) {
            
        }
        return dbDate;
    }
}
