//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson.text;

import java.util.Date;

/**
 * Date and time formatting methods. All method that consume a {@link Date} object assume the
 * data is in UTC and format the string using the local time zone.
 */
public interface DateTimeFormat
{
    /**
     * Represents local time in the Gregorian calendar.
     */
    public static class LocalTime
    {
        /** Year, e.g. 2014. */
        public short year;

        /** Month, 1 = January. */
        public byte month;

        /** Day of month, from 1. */
        public byte day;

        /** Hour, 0-23. */
        public byte hour;

        /** Minute, 0-59. */
        public byte minute;

        /** Second, 0-59. */
        public byte second;
    }

    /**
     * Formats the given date as a full date and a short time of day,
     * e.g. August 25th, 2012 5:30 AM.
     */
    String defaultDateAndTime (Date date);

    /**
     * Formats the given date as a week time, e.g. Wed 5:23 PM.
     */
    String weekly (Date date);

    /**
     * Formats the given date as an order time, e.g. Nov 17, 6:45 AM.
     * TODO: this seems kind of odd, change to "brief"
     */
    String order (Date date);

    /**
     * Formats the given date as a month and year, e.g. April 2009.
     */
    String monthAndYear (Date date);

    /**
     * Returns the local full name of the day of the week for the given day. Days are
     * consecutively numbered from Sunday = 1 to Saturday = 7 (like in java).
     */
    String dayOfWeek (int day);

    /**
     * Returns the given date as a month, day and year, e.g. April 12, 2011
     */
    String fullDate (Date date);

    /**
     * Returns the given date as a day name abbreviation, month abbreviation and day,
     * e.g. Mon, Jan 28.
     */
    String dateWithDayOfWeek (Date date);

    /**
     * Returns the character used as a time separator in the current locale. This is useful for
     * manually formatting time spans.
     */
    char timeSeparator ();

    /**
     * Converts the given date to local time, using the system's local time zone.
     */
    LocalTime toLocal (Date date);
}
