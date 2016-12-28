package com.theporouscity.flagging.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by bergstroml on 6/14/16.
 */

public class ILXDateOutputFormatter {

    public static String formatAbsoluteDateShort(Date date) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        SimpleDateFormat sdf = null;

        if ((date.getYear() + 1900) == year) {
            sdf = new SimpleDateFormat("h:mm d MMM");
        } else {
            sdf = new SimpleDateFormat("h:mm d MMM yy");
        }

        TimeZone tz = TimeZone.getDefault();
        sdf.setTimeZone(tz);
        return sdf.format(date);
    }

    public static String formatAbsoluteDateLong(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm d MMM yy");
        TimeZone tz = TimeZone.getDefault();
        sdf.setTimeZone(tz);
        return sdf.format(date);
    }

    public static String formatRelativeDateShort(Date date, boolean timeUntil) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        Long d;

        if (timeUntil) {
            d = date.getTime() - cal.getTime().getTime();
        } else {
            d = cal.getTime().getTime() - date.getTime();
        }

        if (d < 1000 * 60) {
            return "0 min";
        } else if (d < 1000 * 60 * 2) {
            return "1 min";
        } else if (d < 1000 * 60 * 60) {
            return Long.toString(d / (1000 * 60)) + " mins";
        } else if (d < 1000 * 60 * 60 * 2) {
            return "1 hour";
        } else if (d < 1000 * 60 * 60 * 24) {
            return Long.toString(d / (1000 * 60 * 60)) + " hours";
        } else if (d < 1000 * 60 * 60 * 24 * 2) {
            return "1 day";
        } else if (d < 1000 * 60 * 60 * 24 * 7) {
            return Long.toString(d / (1000 * 60 * 60 * 24)) + " days";
        } else if (d < 1000 * 60 * 60 * 24 * 14) {
            return "1 week";
        } else if (d < 2592000000L) { // 1000 * 60 * 60 * 24 * 30
            return Long.toString(d / (1000 * 60 * 60 * 24 * 7)) + " weeks";
        } else if (d < 5184000000L) { // 1000 * 60 * 60 * 24 * 30 * 2
            return "1 month";
        } else if (d < 31536000000L) { // 1000 * 60 * 60 * 24 * 365
            return Long.toString(d / 2592000000L) + " months"; // 1000 * 60 * 60 * 24 * 30
        } else if (d < 63072000000L) { // 1000 * 60 * 60 * 24 * 365 * 2
            return "1 year";
        } else {
            return Long.toString(d / 31536000000L) + " years"; // 1000 * 60 * 60 * 24 * 365
        }
    }
}
