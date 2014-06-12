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
import samson.crypto.SecureUtil;
import samson.notify.IOSNotifier;
import samson.notify.Notifier;
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
        _formats.setLocale(_locale = locale);
    }

    @Override
    public Locale getDefaultLocale () {
        return Locale.createLocale(NSLocale.get_CurrentLocale().get_LocaleIdentifier());
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

    public boolean exists (String path) {
        return File.Exists(Path.Combine("assets/", path));
    }

    @Override
    public SecureUtil secureUtil () {
        return _secureUtil;
    }

    @Override
    public Notifier notifier () {
        return _notifier;
    }

    private IOSFormats getFormats () {
        if (_locale == null) {
            setLocale(getDefaultLocale());
        }
        return _formats;
    }

    private Locale _locale;
    private IOSFormats _formats = new IOSFormats();
    private IOSSecureUtil _secureUtil = new IOSSecureUtil();
    private IOSNotifier _notifier = new IOSNotifier();
}
