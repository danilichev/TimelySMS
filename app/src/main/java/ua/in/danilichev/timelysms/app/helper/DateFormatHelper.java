package ua.in.danilichev.timelysms.app.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateFormatHelper {

    public static String timePattern = "HH:mm";
    public static String datePattern = "yyyy-MM-dd";

    public static String getTime(Calendar calendar) {
        SimpleDateFormat timeFormat = new SimpleDateFormat(timePattern);
        return timeFormat.format(new Date(calendar.getTimeInMillis()));
    }

    public static String getDate(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
        return dateFormat.format(new Date(calendar.getTimeInMillis()));
    }

    public static Calendar getCalendar(String date, String time) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern + " " + timePattern);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateFormat.parse(date + " " + time));
            return calendar;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
