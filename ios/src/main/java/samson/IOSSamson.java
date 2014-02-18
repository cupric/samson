//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import java.util.Comparator;

import playn.ios.IOSPlatform;
import cli.MonoTouch.Foundation.NSLocale;
import cli.System.IO.File;
import cli.System.IO.Path;
import samson.Samson.Platform;
import samson.text.DateTimeFormat;
import samson.text.NumberFormat;
import samson.util.Locale;

public class IOSSamson implements Platform
{
    public static IOSSamson register (IOSPlatform platform) {
        IOSSamson samson = new IOSSamson();
        Samson.register(samson);
        return samson;
    }

    @Override
    public void setLocale (Locale locale) {
        _formats.setLocale(locale);
    }

    @Override
    public Locale getDefaultLocale () {
        return Locale.createLocale(NSLocale.get_CurrentLocale().get_LocaleIdentifier());
    }

    @Override
    public NumberFormat numberFormat () {
        return _formats;
    }

    @Override
    public DateTimeFormat dateTimeFormat () {
        return _formats;
    }

    @Override
    public Comparator<String> stringComparator () {
        return _formats;
    }

    public boolean exists (String path) {
        return File.Exists(Path.Combine("assets/", path));
    }

    private IOSFormats _formats = new IOSFormats();
}
