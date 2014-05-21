//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import java.util.Comparator;

import playn.java.JavaPlatform;
import samson.crypto.SecureUtil;
import samson.text.DateTimeFormat;
import samson.text.NumberFormat;
import samson.util.Locale;

public class JavaSamson implements Samson.Platform
{
    public static JavaSamson register (JavaPlatform platform) {
        JavaSamson samson = new JavaSamson();
        Samson.register(samson);
        return samson;
    }

    @Override
    public boolean exists (String path) {
        return getClass().getClassLoader().getResource("assets/" + path) != null;
    }

    @Override
    public void setLocale (Locale locale) {
        _formats.setLocale(_locale = locale);
    }

    @Override
    public Locale getDefaultLocale () {
        return Locale.createLocale(java.util.Locale.getDefault().toString());
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

    @Override
    public SecureUtil secureUtil () {
        return _secureUtil;
    }

    private JavaFormats getFormats () {
        if (_locale == null) {
            setLocale(getDefaultLocale());
        }
        return _formats;
    }

    private Locale _locale;
    private JavaFormats _formats = new JavaFormats();
    private JavaSecureUtil _secureUtil = new JavaSecureUtil();
}
