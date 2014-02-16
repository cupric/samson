//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import java.util.StringTokenizer;

import static samson.Log.log;

/**
 * Abstractly defines the storage for a mapping of named values, delegating the actual storage
 * mechanism to a subclass.
 *
 * <p>The methods here retrieve values by name, converting them from an underlying string entry.
 * In all cases, a default value is passed in, which is used if the value is not present or
 * is incorrectly formatted (unparsable). In the latter case, a warning is also logged.</p>
 */
public abstract class ConfigBase
{
    /**
     * Manages one type of value stored in the config.
     */
    public static abstract class ValueType<T>
    {
        /** Returns the value for the given string. */
        public abstract T parse (String strValue);

        /** Returns the string for the given value. */
        public abstract String toString (T value);
    }

    /** Manages Integer config values. */
    public static final ValueType<Integer> INTEGER = new ValueType<Integer>() {
        @Override
        public Integer parse (String strValue) {
            return Integer.decode(strValue);
        }

        @Override
        public String toString (Integer val) {
            return Integer.toString(val);
        }
    };

    /** Manages Long config values. */
    public static final ValueType<Long> LONG = new ValueType<Long>() {
        @Override
        public Long parse (String strValue) {
            return Long.valueOf(strValue);
        }

        @Override
        public String toString (Long val) {
            return Long.toString(val);
        }
    };

    /** Manages Float config values. */
    public static final ValueType<Float> FLOAT = new ValueType<Float>() {
        @Override
        public Float parse (String strValue) {
            return Float.valueOf(strValue);
        }

        @Override
        public String toString (Float val) {
            return Float.toString(val);
        }
    };

    /** Manages Boolean config values. */
    public static final ValueType<Boolean> BOOLEAN = new ValueType<Boolean>() {
        @Override
        public Boolean parse (String strValue) {
            return !strValue.equalsIgnoreCase("false");
        }

        @Override
        public String toString (Boolean val) {
            return Boolean.toString(val);
        }
    };

    /** Manages String config values. */
    public static final ValueType<String> STRING = new ValueType<String>() {
        @Override
        public String parse (String strValue) {
            return strValue;
        }

        @Override
        public String toString (String val) {
            return val;
        }
    };

    /** Manages int[] config values. When parsing, if any value in the array cannot be parsed
     * as an int, the exception is propagated. */
    public static final ValueType<int[]> INT_ARRAY = new ValueType<int[]>() {
        @Override
        public int[] parse (String strValue) {
            StringTokenizer tok = new StringTokenizer(strValue, ",");
            int[] result = new int[tok.countTokens()];
            for (int ii = 0; tok.hasMoreTokens(); ii++) {
                result[ii] = Integer.parseInt(tok.nextToken().trim());
            }
            return result;
        }

        @Override
        public String toString (int[] vals) {
            StringBuilder str = new StringBuilder();
            for (int val : vals) {
                if (str.length() > 0) {
                    str.append(",");
                }
                str.append(val);
            }
            return str.toString();
        }
    };

    /** Manages long[] config values. When parsing, if any value in the array cannot be parsed
     * as a long, the exception is propagated. */
    public static final ValueType<long[]> LONG_ARRAY = new ValueType<long[]>() {
        @Override
        public long[] parse (String strValue) {
            StringTokenizer tok = new StringTokenizer(strValue, ",");
            long[] result = new long[tok.countTokens()];
            for (int ii = 0; tok.hasMoreTokens(); ii++) {
                result[ii] = Long.parseLong(tok.nextToken().trim());
            }
            return result;
        }

        @Override
        public String toString (long[] vals) {
            StringBuilder str = new StringBuilder();
            for (long val : vals) {
                if (str.length() > 0) {
                    str.append(",");
                }
                str.append(val);
            }
            return str.toString();
        }
    };

    /** Manages float[] config values. When parsing, if any value in the array cannot be parsed
     * as a float, the exception is propagated. */
    public static final ValueType<float[]> FLOAT_ARRAY = new ValueType<float[]>() {
        @Override
        public float[] parse (String strValue) {
            StringTokenizer tok = new StringTokenizer(strValue, ",");
            float[] result = new float[tok.countTokens()];
            for (int ii = 0; tok.hasMoreTokens(); ii++) {
                result[ii] = Long.parseLong(tok.nextToken().trim());
            }
            return result;
        }

        @Override
        public String toString (float[] vals) {
            StringBuilder str = new StringBuilder();
            for (float val : vals) {
                if (str.length() > 0) {
                    str.append(",");
                }
                str.append(val);
            }
            return str.toString();
        }
    };

    /** Manages String[] config values. Note that, when converting an array to a string, does not
     * handle escaping of commas in elements in the array. Instead, fails fast by throwing an
     * illegal argument exception. */
    public static final ValueType<String[]> STRING_ARRAY = new ValueType<String[]>() {
        @Override
        public String[] parse (String strValue) {
            StringTokenizer tok = new StringTokenizer(strValue, ",");
            String[] result = new String[tok.countTokens()];
            for (int ii = 0; tok.hasMoreTokens(); ii++) {
                result[ii] = tok.nextToken().trim();
            }
            return result;
        }

        @Override
        public String toString (String[] vals) {
            StringBuilder str = new StringBuilder();
            for (String val : vals) {
                if (val.indexOf(',') >= 0) {
                    throw new IllegalArgumentException("String has a comma");
                }
                if (str.length() > 0) {
                    str.append(",");
                }
                str.append(val);
            }
            return str.toString();
        }
    };

    /**
     * Returns the value for the given key, or null if not present.
     */
    public abstract String getEntry (String key);

    public <T> T getValue (ValueType<T> vtype, String name, T defval)
    {
        String val = getEntry(name);
        if (val == null) {
            return defval;
        }
        try {
            return vtype.parse(val);
        } catch (Exception ex) {
            log.warning("Malformed property", "type", vtype, "value", val);
        }
        return defval;
    }
}
