//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import ikvm.lang.CIL;

import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;
import java.util.Locale;

import samson.text.DateTimeFormat;
import samson.text.NumberFormat;
import cli.System.DateTime;
import cli.System.Globalization.NumberStyles;
import cli.System.Int32;
import cli.System.TimeSpan;
import cli.System.Globalization.CultureInfo;
import static samson.Log.log;

public class IOSFormats
    implements DateTimeFormat, NumberFormat, Comparator<String>
{
    private CultureInfo cinfo = CultureInfo.get_CurrentCulture();
    private CultureInfo usinfo;
    private String weekly, order;

    // January 1, 1970, 00:00:00 GMT
    public static final long EPOCH = new DateTime(1970, 1, 1, 0, 0, 0).get_Ticks();

    private DateTime convert (Date date)
    {
        // convert to the time zone of the device
        TimeZone zone = TimeZone.getDefault();
        long ms = date.getTime();
        ms += zone.getOffset(ms);
        // convert to ticks relative to the epoch
        return new DateTime(EPOCH + ms * TimeSpan.TicksPerMillisecond);
    }

    public IOSFormats ()
    {
        try {
            usinfo = CultureInfo.GetCultureInfo("en-US");
        } catch (Throwable t) {
            throw new RuntimeException("US English locale not available", t);
        }
    }

    public void setLocale (Locale locale)
    {
        try {
            cinfo = CultureInfo.GetCultureInfo(locale.toString().replace("_", "-"));
            if (cinfo.get_IsNeutralCulture() && locale.getLanguage().equals("en")) {
                // TODO: tablet: choose between GB & US
                log.warning("en language is neutral, falling back to US english");
                cinfo = usinfo;
            }

        } catch (Throwable e) {
            cinfo = CultureInfo.get_CurrentCulture();
            log.warning("Specified unknown locale. Falling back to default",
                "locale", locale, "default", cinfo);
        }

        if (locale.getLanguage().equals("ja")) {
            weekly = "ddd HH:mm";
            order = "MM/dd H:mm";

        } else if (locale.getLanguage().equals("de")) {
            weekly = "ddd H:mm";
            order = "MMM dd HH:mm";

        } else {
            weekly = "ddd h:mm tt";
            order = "MMM dd h:mm tt";
        }
    }

    @Override
    public String integer (int n)
    {
        return CIL.box_int(n).ToString("N0");
    }

    @Override
    public int parseInteger (String nstr)
    {
        return Int32.Parse(nstr,
            NumberStyles.wrap(NumberStyles.Integer | NumberStyles.AllowThousands));
    }

    @Override
    public String decimal (Number n, int places)
    {
        return CIL.box_double(n.doubleValue()).ToString("N" + places);
    }

    @Override
    public String general (Number n)
    {
        return CIL.box_double(n.doubleValue()).ToString("N");
    }

    @Override
    public String percent (Number n)
    {
        return CIL.box_double(n.doubleValue()).ToString("P0");
    }

    @Override
    public String dollars (Number n)
    {
        return CIL.box_double(n.doubleValue()).ToString("C2", usinfo);
    }

    @Override
    public String defaultDateAndTime (Date date)
    {
        // TODO: tablet: does this have the weekday in it? do we need to remove it?
        return convert(date).ToString("f", cinfo);
    }

    @Override
    public String weekly (Date date)
    {
        return convert(date).ToString(weekly, cinfo);
    }

    @Override
    public String order (Date date)
    {
        return convert(date).ToString(order, cinfo);
    }

    @Override
    public String monthAndYear (Date date)
    {
        return convert(date).ToString("Y", cinfo);
    }

    @Override
    public String dayOfWeek (int day)
    {
        return cinfo.get_DateTimeFormat().get_DayNames()[day - 1];
    }

    @Override
    public String fullDate (Date date)
    {
        return convert(date).ToString("D", cinfo);
    }

    @Override
    public String dateWithDayOfWeek (Date date)
    {
        return convert(date).ToString("ddd, MMM d", cinfo);
    }

    @Override
    public Date midnight (Date date)
    {
        DateTime dt = convert(date);
        dt = dt.Subtract(dt.get_TimeOfDay());
        return new Date((dt.get_Ticks() - EPOCH) / TimeSpan.TicksPerMillisecond);
    }

    @Override
    public int compare (String a, String b)
    {
        return cinfo.get_CompareInfo().Compare(a, b);
    }
}
