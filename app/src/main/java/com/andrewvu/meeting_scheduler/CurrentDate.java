package com.andrewvu.meeting_scheduler;

import java.util.Calendar;

/**
 * @author Andrew Vu (6044937)
 * <p>
 * Static class to return int value for current day of month, month (0-11), year, or String of date.
 * Created because many methods of Date are deprecated, and current date can be used in multiple
 * activities in this Android Application.
 */
public abstract class CurrentDate {
    private static Calendar c = Calendar.getInstance();

    /**
     * getDayOfMonth
     *
     * @return int day of month
     */
    public static int getDayOfMonth() {
        return c.get(Calendar.DAY_OF_MONTH);
    }


    /**
     * getMonth
     *
     * @return return int month (from 0 to 11)
     */
    public static int getMonth() {
        return c.get(Calendar.MONTH);
    }

    /**
     * getYear
     *
     * @return int year
     */
    public static int getYear() {
        return c.get(Calendar.YEAR);
    }

    /**
     * getDate
     *
     * @return String date in form DD/MM/YYYY
     */
    public static String getDate() {
        return (getDayOfMonth() + "/" + (getMonth() + 1) + "/" + getYear());
    }
}
