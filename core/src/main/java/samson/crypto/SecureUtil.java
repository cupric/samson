//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson.crypto;

import java.io.IOException;

/**
 * A port of Narya's SecureUtil that can be implemented in terms of a specific platform.
 */
public abstract class SecureUtil
{
    /** The version of our security protocol (for backwards compatibility with older clients). */
    public static final int VERSION = 1;

    public abstract byte[] encryptAES (byte[] key, byte[] contents)
        throws IOException;

    public abstract byte[] decryptAES (byte[] key, byte[] contents)
        throws IOException;

    public abstract byte[] encryptRSA (String publicKey, byte[] secret, byte[] salt);

    /**
     * Creates a random key.
     */
    public abstract byte[] createRandomKey (int length);

    /**
     * XORs a byte array against a key.
     */
    public static byte[] xorBytes (byte[] data, byte[] key)
    {
        byte[] xored = new byte[data.length];
        for (int ii = 0; ii < data.length; ii++) {
            xored[ii] = (byte)(data[ii] ^ key[ii % key.length]);
        }
        return xored;
    }

    /** Our split character. */
    protected static final char SPLIT = '#';

    /** Our initialization vector. */
    protected static final byte[] IV = new byte[] {
        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
    };
}
