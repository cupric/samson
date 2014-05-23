//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson.util;

import java.io.ByteArrayOutputStream;

public class Encode
{
    public static String hex (byte[] bytes, String separator)
    {
        if (bytes == null) {
            return "";
        }

        int count = bytes.length;
        StringBuilder hex = new StringBuilder();

        for (int i = 0; i < count; i++) {
            int val = bytes[i];
            if (val < 0) {
                val += 256;
            }
            if (i > 0) {
                hex.append(separator);
            }
            hex.append(XLATE.charAt(val/16));
            hex.append(XLATE.charAt(val%16));
        }

        return hex.toString();
    }

    public static String hex (byte[] bytes)
    {
        return hex(bytes, "");
    }

    public static byte[] unhex (String hex)
    {
        int ll = hex.length();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(ll / 2);
        for (int ii = 0; ii < ll; ii += 2) {
            bytes.write(digitVal(hex.charAt(ii)) << 4 | digitVal(hex.charAt(ii + 1)));
        }
        return bytes.toByteArray();
    }

    private static int digitVal (char hex)
    {
        if (hex >= '0' && hex <= '9') {
            return hex - '0';
        } else if (hex >= 'a' && hex <= 'f') {
            return 10 + hex - 'a';
        } else if (hex >= 'A' && hex <= 'F') {
            return 10 + hex - 'A';
        } else {
            throw new IllegalArgumentException("Hex char out of range: " + hex);
        }
    }

    /** Used by {@link #hexlate} and {@link #unhexlate}. */
    private static final String XLATE = "0123456789abcdef";
}
