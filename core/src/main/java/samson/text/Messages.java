//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson.text;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import samson.Samson;

/**
 * Utility functions for translation string handling.
 */
public class Messages
{
    /** Used to mark fully qualified message keys. */
    public static final String QUAL_PREFIX = "%";

    /** Used to separate the bundle qualifier from the message key in a fully qualified message
     * key. */
    public static final String QUAL_SEP = ":";

    /**
     * Call this to "taint" any string that has been entered by an entity outside the application
     * so that the translation code knows not to attempt to translate this string when doing
     * recursive translations.
     */
    public static String taint (Object text) {
        return TAINT_CHAR + text;
    }

    /**
     * Returns whether or not the provided string is tainted. See {@link #taint}. Null strings
     * are considered untainted.
     */
    public static boolean isTainted (String text) {
        return text != null && text.startsWith(TAINT_CHAR);
    }

    /**
     * Removes the tainting character added to a string by {@link #taint}. If the provided string
     * is not tainted, this silently returns the originally provided string.
     */
    public static String untaint (String text) {
        return isTainted(text) ? text.substring(TAINT_CHAR.length()) : text;
    }

    /**
     * Composes a message key with an array of arguments. The message can subsequently be
     * decomposed and translated without prior knowledge of how many arguments were provided.
     */
    public static String compose (String key, Object... args) {
        StringBuilder buf = new StringBuilder();
        buf.append(key);
        buf.append('|');
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                buf.append('|');
            }
            // escape the string while adding to the buffer
            String arg = (args[i] == null) ? "" : String.valueOf(args[i]);
            int alength = arg.length();
            for (int p = 0; p < alength; p++) {
                char ch = arg.charAt(p);
                if (ch == '|') {
                    buf.append("\\!");
                } else if (ch == '\\') {
                    buf.append("\\\\");
                } else {
                    buf.append(ch);
                }
            }
        }
        return buf.toString();
    }

    /**
     * Compose a message with String args. This is just a convenience so callers do not have to
     * cast their String[] to an Object[].
     */
    public static String compose (String key, String... args) {
        return compose(key, (Object[]) args);
    }

    /**
     * Unescapes characters that are escaped in a call to compose.
     */
    public static String unescape (String value) {
        int bsidx = value.indexOf('\\');
        if (bsidx == -1) {
            return value;
        }

        StringBuilder buf = new StringBuilder();
        int vlength = value.length();
        for (int ii = 0; ii < vlength; ii++) {
            char ch = value.charAt(ii);
            if (ch != '\\' || ii == vlength-1) {
                buf.append(ch);
            } else {
                // look at the next character
                ch = value.charAt(++ii);
                buf.append((ch == '!') ? '|' : ch);
            }
        }

        return buf.toString();
    }

    /**
     * A convenience method for calling {@link #compose(String,Object[])} with an array of
     * arguments that will be automatically tainted (see {@link #taint}).
     */
    public static String tcompose (String key, Object... args) {
        int acount = args.length;
        String[] targs = new String[acount];
        for (int ii = 0; ii < acount; ii++) {
            targs[ii] = taint(args[ii]);
        }
        return compose(key, (Object[]) targs);
    }

    /**
     * A convenience method for calling {@link #compose(String,String[])} with an array of argument
     * that will be automatically tainted.
     */
    public static String tcompose (String key, String... args) {
        for (int ii = 0, nn = args.length; ii < nn; ii++) {
            args[ii] = taint(args[ii]);
        }
        return compose(key, args);
    }

    /**
     * Decomposes a compound key into its constituent parts. Arguments that were tainted during
     * composition will remain tainted.
     */
    public static String[] decompose (String compoundKey) {
        String[] args = compoundKey.split("\\|");
        for (int ii = 0; ii < args.length; ii++) {
            args[ii] = unescape(args[ii]);
        }
        return args;
    }

    /**
     * Returns a fully qualified message key which, when translated by some other bundle, will know
     * to resolve and utilize the supplied bundle to translate this particular key.
     */
    public static String qualify (String bundle, String key) {
        // sanity check
        if (bundle.indexOf(QUAL_PREFIX) != -1 ||
            bundle.indexOf(QUAL_SEP) != -1) {
            String errmsg = "Message bundle may not contain '" + QUAL_PREFIX +
                "' or '" + QUAL_SEP + "' [bundle=" + bundle +
                ", key=" + key + "]";
            throw new IllegalArgumentException(errmsg);
        }
        return QUAL_PREFIX + bundle + QUAL_SEP + key;
    }

    /**
     * Returns the bundle name from a fully qualified message key.
     *
     * @see #qualify
     */
    public static String getBundle (String qualifiedKey) {
        if (!qualifiedKey.startsWith(QUAL_PREFIX)) {
            throw new IllegalArgumentException(
                qualifiedKey + " is not a fully qualified message key.");
        }

        int qsidx = qualifiedKey.indexOf(QUAL_SEP);
        if (qsidx == -1) {
            throw new IllegalArgumentException(
                qualifiedKey + " is not a valid fully qualified key.");
        }

        return qualifiedKey.substring(QUAL_PREFIX.length(), qsidx);
    }

    /**
     * Returns the unqualified portion of the key from a fully qualified message key.
     *
     * @see #qualify
     */
    public static String getUnqualifiedKey (String qualifiedKey) {
        if (!qualifiedKey.startsWith(QUAL_PREFIX)) {
            throw new IllegalArgumentException(
                qualifiedKey + " is not a fully qualified message key.");
        }

        int qsidx = qualifiedKey.indexOf(QUAL_SEP);
        if (qsidx == -1) {
            throw new IllegalArgumentException(
                qualifiedKey + " is not a valid fully qualified key.");
        }

        return qualifiedKey.substring(qsidx+1);
    }

    /**
     * Turn an array of strings into a composed string representing them. Optionally uses an "and"
     * list.
     */
    public static String composeAsList (String[] list, boolean and) {
        return composeAsList(Lists.newArrayList(list), and);
    }

    /**
     * Turn a list of strings into a composed string representing them. Optionally uses an "and"
     * list.
     */
    public static String composeAsList (List<String> list, boolean and) {
        int size = list.size();

        if (size == 1) {
            return list.get(0);
        }

        if (size <= 9) {
            return Messages.compose((and ? "m.andlist" : "m.list") + list.size(),
                list.toArray());
        }

        ArrayList<String> subLists = Lists.newArrayList();
        for (int ii = 0; ii < 8; ii ++) {
            subLists.add(composeAsList(list.subList(ii * size/9, (ii+1) * size/9), false));
        }
        subLists.add(composeAsList(list.subList(8 * size/9, size), and));

        return composeAsList(subLists, false);
    }

    /**
     *  Formats the given pattern, replacing <code>{0}</code>, <code>{1}</code>, etc, if present,
     *  with the result of calling {@link Object#toString()} on its corresponding argument.
     *  <p><b>NOTE</b>: This is a subset of java {@code MessageFormat}'s features:<ul>
     *  <li>Format types and subformat patterns are not supported. Number and time formats may be
     *  substituted by composing results from {@link Samson#numberFormat()} and {@link
     *  Samson#dateTimeFormat()}.</li>
     *  <li>Single quotes are not treated specially. Therefore a literal open brace must be passed
     *  in as a pattern argument.</li></ul>
     *  These differences are designed to make it as easy as possible for translators to supply
     *  correct data.</p>
     *  @throws IllegalArgumentException if there are any errors such as not enough arguments,
     *  a format that is not a simple integer, or a missing close brace
     */
    public static String format (String pattern, Object... args) {
        StringBuilder result = null;
        for (int off = 0, len = pattern.length(); off < len; ++off) {
            char ch = pattern.charAt(off);
            int close = off;
            // format elements, e.g. {0}
            if (ch == '{') {
                close = pattern.indexOf('}', off + 1);
                if (close == -1) {
                    error("Close brace is missing", pattern, off);
                    return pattern;
                }
                // to save garbage collection, lazily create the result buffer
                if (result == null) {
                    // initialize with the contents thus far
                    result = new StringBuilder();
                    result.append(pattern, 0, off);
                }
                subformat(pattern, off + 1, close, args, result);

            } else {
                if (result != null) {
                    result.append(ch);
                }
            }

            off = close;
        }

        return result == null ? pattern : result.toString();
    }

    private static void subformat (String pattern, int start, int end, Object[] args,
            StringBuilder result) {
        int off = start;
        while (off < end && pattern.charAt(off) != ',') {
            off++;
        }
        try {
            int index = Integer.parseInt(pattern.substring(start, off));
            if (index < 0 || index >= args.length) {
                error("Not enough arguments (got " + args.length + ")", pattern ,start);
            }
            result.append(args[index]);
        } catch (NumberFormatException e) {
            error("Expected a numeric index in format element", pattern, start);
        }
    }

    private static void error (String str, String pattern, int offset) {
        throw new IllegalArgumentException(
            str + " in pattern \"" + pattern + "\", offset " + offset);
    }

    /** Text prefixed by this character will be considered tainted when doing recursive
     * translations and won't be translated. */
    protected static final String TAINT_CHAR = "~";
}
