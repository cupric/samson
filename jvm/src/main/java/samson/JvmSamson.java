//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import java.util.Comparator;
import java.util.Locale;

import samson.text.DateTimeFormat;
import samson.text.NumberFormat;

public abstract class JvmSamson implements Samson.Platform
{
    @Override
    public boolean exists (String path) {
        // TODO: check this works on android
        return getClass().getClassLoader().getResource("assets/" + path) != null;
    }

    @Override
    public void setFormattingLocale (Locale locale) {
        _formats.setLocale(_locale = locale);
    }

    @Override
    public Locale getDeviceLocale () {
        return Locale.getDefault();
    }

    @Override
    public String getPreferredLanguage () {
        return Locale.getDefault().getLanguage();
    }

    @Override
    public NumberFormat numberFormat () {
        return getFormats();
    }

    @Override
    public DateTimeFormat dateTimeFormat () {
        return getFormats();
    }

    @Override
    public Comparator<String> stringComparator () {
        return getFormats();
    }

    private JvmFormats getFormats () {
        if (_locale == null) {
            setFormattingLocale(getDeviceLocale());
        }
        return _formats;
    }

    private Locale _locale;
    private JvmFormats _formats = new JvmFormats();
}
