//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import java.text.Collator;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import samson.text.DateTimeFormat;

import com.google.common.collect.Lists;

/**
 * Java implementation of formats.
 * TODO: tablet: verify this works on android
 */
public class JvmFormats
    implements DateTimeFormat, samson.text.NumberFormat, Comparator<String>
{
    private DateFormat weekly, order, dfault, monthAndYear, dayOfWeek, full, dayOfWeekWithDate;
    private NumberFormat integer, general, percent, dollars;
    private List<NumberFormat> decimals = Lists.newArrayList();
    private Collator collator = Collator.getInstance();
    private char timeSeparator;

    public void setLocale (Locale locale) {
        java.util.Locale jlocale = new java.util.Locale(locale.getLanguage());
        collator = Collator.getInstance(jlocale);
        dfault = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, jlocale);
        integer = NumberFormat.getIntegerInstance();
        general = NumberFormat.getInstance();
        percent = NumberFormat.getPercentInstance();
        dayOfWeek = new SimpleDateFormat("EEEE", jlocale);
        full = DateFormat.getDateInstance(DateFormat.LONG, jlocale);
        dayOfWeekWithDate = new SimpleDateFormat("EEE, MMM dd", jlocale);
        dollars = NumberFormat.getCurrencyInstance(java.util.Locale.US); // always US

        if (locale.getLanguage().equals("ja")) {
            weekly = new SimpleDateFormat("EEE HH:mm", jlocale);
            order = new SimpleDateFormat("MM/dd H:mm", jlocale);

        } else if (locale.getLanguage().equals("de")) {
            weekly = new SimpleDateFormat("EEE H:mm", jlocale);
            order = new SimpleDateFormat("MMM dd HH:mm", jlocale);

        } else {
            weekly = new SimpleDateFormat("EEE h:mm a", jlocale);
            order = new SimpleDateFormat("MMM dd h:mm a", jlocale);
        }

        // hack out the time separator since java doesn't provide it directly
        String sampleTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(0));
        Matcher matcher = Pattern.compile(".*[0-9](.)[0-9].*").matcher(sampleTime);
        if (matcher.matches()) {
            timeSeparator = matcher.group(1).charAt(0);
        } else {
            Log.log.warning("No time separator found, falling back to :", "sample", sampleTime,
                "locale", locale);
            timeSeparator = ':';
        }

        try {
            SimpleDateFormat formatter =
                (SimpleDateFormat) java.text.DateFormat.getDateInstance(DateFormat.FULL, jlocale);
            String pattern = formatter.toPattern().toLowerCase(jlocale);
            if (pattern.indexOf('m') < pattern.indexOf('y')) {
                monthAndYear = new SimpleDateFormat("MMMM yyyy", jlocale);
            } else {
                monthAndYear = new SimpleDateFormat("yyyy MMMM", jlocale);
            }
        } catch (ClassCastException cce) {
            // Shit, it was too exotic a locale. Oh well. We'll fall back to a default
            monthAndYear = new SimpleDateFormat("MMMM yyyy", jlocale);
        }
    }

    @Override
    public String weekly (Date date) {
        return weekly.format(date);
    }

    @Override
    public char timeSeparator () {
        return timeSeparator;
    }

    @Override
    public String order (Date date) {
        return order.format(date);
    }

    @Override
    public String defaultDateAndTime (Date date) {
        return dfault.format(date);
    }

    @Override
    public String monthAndYear (Date date) {
        return monthAndYear.format(date);
    }

    @Override
    public String dayOfWeek (int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, day);
        return dayOfWeek.format(cal.getTime());
    }

    @Override
    public String integer (int value) {
        return integer.format(value);
    }

    @Override
    public int parseInteger (String nstr) {
        try {
            return integer.parse(nstr).intValue();
        } catch (ParseException ex) {
            NumberFormatException nfex = new NumberFormatException();
            nfex.initCause(ex);
            throw nfex;
        }
    }

    @Override
    public String general (Number value) {
        return general.format(value);
    }

    @Override
    public String percent (Number value) {
        return percent.format(value);
    }

    @Override
    public String decimal (Number n, int places) {
        while (decimals.size() <= places) {
            decimals.add(null);
        }
        NumberFormat nfmt = decimals.get(places);
        if (nfmt == null) {
            StringBuilder fmt = new StringBuilder("#.");
            for (int ii = 0; ii < places; ++ii) {
                fmt.append("#");
            }
            decimals.set(places, nfmt = new DecimalFormat(fmt.toString()));
        }
        return nfmt.format(n);
    }

    @Override
    public String fullDate (Date date) {
        return full.format(date);
    }

    @Override
    public String dateWithDayOfWeek (Date date) {
        return dayOfWeekWithDate.format(date);
    }

    @Override
    public int compare (String a, String b)
    {
        return collator.compare(a,  b);
    }

    @Override
    public String dollars (Number n)
    {
        return dollars.format(n);
    }

    @Override
    public LocalTime toLocal (Date date)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        LocalTime time = new LocalTime();
        time.year = (short)cal.get(Calendar.YEAR);
        time.month = (byte)cal.get(Calendar.MONTH);
        time.day = (byte)cal.get(Calendar.DAY_OF_MONTH);
        time.hour = (byte)cal.get(Calendar.HOUR);
        time.minute = (byte)cal.get(Calendar.MINUTE);
        time.second = (byte)cal.get(Calendar.SECOND);
        return time;
    }
}
