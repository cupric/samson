//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import java.util.Comparator;
import java.util.Locale;

import samson.crypto.SecureUtil;
import samson.text.DateTimeFormat;
import samson.text.NumberFormat;

/**
 * Main entry point for samson cross-platform features. All platform-specific methods are defined
 * in the {@link Platform} interface. The implemntation for the platform must be constructed and
 * registered on application startup using {@link #register(Platform)}. Currently IOS and Java are
 * supported, see the relevant sibling maven submodules. Static method equivalents may also be
 * used, which simply delegate to the instance methods of {@link #platform()}.
 * TODO: support more platforms
 */
public class Samson
{
    /**
     * Defines cross platform methods.
     */
    public interface Platform {
        /**
         * Sets the locale to use when formatting dates, times and numbers.
         */
        void setLocale (Locale locale);

        /**
         * Gets the default locale for this environment.
         */
        Locale getDefaultLocale ();

        /**
         * Gets implementation of number formatting for the platform.
         */
        NumberFormat numberFormat ();

        /**
         * Gets implementation of date and time formatting for the platform.
         */
        DateTimeFormat dateTimeFormat ();

        /**
         * Gets implementation of lexical string comparison for the platform.
         */
        Comparator<String> stringComparator ();

        /**
         * Tests if the asset exists at the given path in the application resources.
         */
        boolean exists (String assetPath);

        /**
         * Gets the secure utilities for the platform.
         */
        SecureUtil secureUtil ();

        /**
         * Gets the notifications for the platform.
         */
        Notifications notifications ();
    }

    /**
     * Registers a new samson implementation, must be called exactly once on application startup.
     */
    public static void register (Platform instance) {
        _instance = instance;
    }

    public static void setLocale (Locale locale) {
        _instance.setLocale(locale);
    }

    public static Locale getDefaultLocale () {
        return _instance.getDefaultLocale();
    }

    public static NumberFormat numberFormat () {
        return _instance.numberFormat();
    }

    public static DateTimeFormat dateTimeFormat () {
        return _instance.dateTimeFormat();
    }

    public static Comparator<String> stringComparator () {
        return _instance.stringComparator();
    }

    public static boolean exists (String assetPath) {
        return _instance.exists(assetPath);
    }

    public static SecureUtil secureUtil () {
        return _instance.secureUtil();
    }

    public static Notifications notifications () {
        return _instance.notifications();
    }

    public static Platform platform () {
        return _instance;
    }

    private static Platform _instance;
}
