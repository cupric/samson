//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson.util;

import com.google.common.base.Preconditions;

/**
 * Defines a locale for the purposes of rendering text.
 */
public class Locale
{
    public static Locale getDefault () {
        // TODO: wire this up to device settings
        return ENGLISH;
    }

    public static Locale ENGLISH = new Locale("en");
    public static Locale GERMAN = new Locale("de");
    public static Locale SPANISH = new Locale("es");
    // TODO: more instances as needed

    /** The two-letter, lowercase, language code. */
    public final String langCode;

    /** The country code. */
    public final String countryCode;

    /**
     * Creates a new Locale instance from a string like en_US, es_MX, or de_DE. Defaults to English.
     */
    public static Locale createLocale (String localeCode)
    {
        Locale locale = Locale.ENGLISH;
        if (localeCode != null) {
            String language = localeCode;
            String countryCode = null;
            int underscore = localeCode.indexOf('_');
            if (underscore != -1) {
                language = localeCode.substring(0, underscore);
                countryCode = localeCode.substring(underscore + 1);
                // note: this is trimming off any variant info, which we aren't using atm.
                underscore = countryCode.indexOf('_');
                if (underscore != -1) {
                    countryCode = countryCode.substring(0, underscore);
                }
            }
            locale = new Locale(language, countryCode);
        }
        return locale;
    }

    public Locale (String langCode)
    {
        this(langCode, null);
    }

    public Locale (String langCode, String countryCode)
    {
        this.langCode = Preconditions.checkNotNull(langCode);
        this.countryCode = countryCode;
    }

    @Override
    public boolean equals (Object other)
    {
        return other instanceof Locale &&
            ((Locale)other).langCode.equals(langCode);
    }

    @Override
    public int hashCode ()
    {
        return langCode.hashCode();
    }

    @Override
    public String toString ()
    {
        return getLocaleCode();
    }

    public String getLanguage ()
    {
        return langCode;
    }

    public boolean hasCountry ()
    {
        return countryCode != null;
    }

    public String getCountry ()
    {
        return countryCode;
    }

    public String getLocaleCode ()
    {
        String localeCode = getLanguage();
        if (hasCountry()) {
            localeCode += "_" + getCountry();
        }
        return localeCode;
    }
}
