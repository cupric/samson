//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson.crypto;

import java.io.IOException;

import samson.Samson;

public class AES
{
    /**
     * Creates a new AES using a stored key. If the key is not present, creates a random one
     * and stores it for next time.
     */
    public static AES fromStore (String id)
        throws IOException
    {
        byte[] key = Samson.secureUtil().retrieveKey(id);
        AES aes = new AES(key);
        if (key == null) {
            Samson.secureUtil().storeKey(id, aes.secret());
        }
        return aes;
    }

    /**
     * Constructs an AES with the given key. If the given key is null, creates a new random key.
     */
    public AES (byte[] secret)
    {
        if (secret == null) {
            secret = Samson.secureUtil().createRandomKey(16);
        }
        _secret = secret;
    }

    /**
     * Encrypts a byte array.
     * @throws IOException if the passed value is not encrypted
     */
    public byte[] encrypt (byte[] value)
        throws IOException
    {
        return Samson.secureUtil().encryptAES(_secret, value);
    }

    /**
     * Decrypts an encrypted value to a byte array.
     * @throws IOException if the passed value is not decrypted
     */
    public byte[] decryptToBytes (byte[] value)
        throws IOException
    {
        return Samson.secureUtil().decryptAES(_secret, value);
    }

    /**
     * Decrypts an encrypted value to a string. This calls {@link #decryptToBytes(byte[])} and
     * decodes the result as utf 8.
     * @throws IOException if the passed value is not decrypted
     */
    public String decryptToString (byte[] value)
        throws IOException
    {
        return new String(decryptToBytes(value), "utf-8");
    }

    /**
     * Encrypts a string. Converts to utf 8 and calls {@link #encrypt(byte[])}.
     * @throws IOException if the passed value is not encrypted
     */
    public byte[] encrypt (String value)
        throws IOException
    {
        return encrypt(value.getBytes("utf-8"));
    }

    public byte[] secret ()
    {
        return _secret;
    }

    /** Secret to encrypt the data with, or null if no encryption should be used. */
    protected final byte[] _secret;
}
