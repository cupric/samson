//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson.text;

import java.util.Date;

/**
 * Date and time formatting methods.
 */
public interface DateTimeFormat
{
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
     * Returns a date representing midnight of the given date in the local time zone. This is
     * platform-specific due to the way time zones are accessed.
     */
    Date midnight (Date date);
}
