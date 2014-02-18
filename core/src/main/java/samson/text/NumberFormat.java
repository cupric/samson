//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson.text;

/**
 * Number formatting methods, for taking into account locales.
 */
public interface NumberFormat
{
    /**
     * Gets a locale specific string for an integer.
     */
    String integer (int n);

    /**
     * Parses the integer if possible, or throws NumberFormatException.
     */
    int parseInteger (String nstr);

    /**
     * Gets a string for the given decimal in the current locale.
     */
    String general (Number n);

    /**
     * Multiply by 100 and show as a 2 digit percentage.
     */
    String percent (Number n);

    /**
     * Gets a string for the given decimal rounded (and forced) to the given number of places,
     * in the current locale for the.
     */
    String decimal (Number n, int places);

    /**
     * Formats the given number as a US dollar amount.
     */
    String dollars (Number n);
}
