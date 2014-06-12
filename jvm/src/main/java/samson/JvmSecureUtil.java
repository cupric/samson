//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import samson.crypto.SecureUtil;

import static samson.Log.log;

public class JvmSecureUtil extends SecureUtil
{
    @Override public byte[] encryptAES (byte[] key, byte[] contents)
        throws IOException
    {
        return doAES(Cipher.ENCRYPT_MODE, key, contents);
    }

    @Override public byte[] decryptAES (byte[] key, byte[] contents)
        throws IOException
    {
        return doAES(Cipher.DECRYPT_MODE, key, contents);
    }

    @Override public byte[] encryptRSA (String publicKey, byte[] secret, byte[] salt)
    {
        byte[] encrypt = new byte[secret.length + salt.length];
        for (int ii = 0; ii < secret.length; ii++) {
            encrypt[ii] = secret[ii];
        }
        for (int ii = 0; ii < salt.length; ii++) {
            encrypt[secret.length + ii] = salt[ii];
        }
        try {
            return getRSAEncryptCipher(publicKey).doFinal(encrypt);
        } catch (GeneralSecurityException gse) {
            log.warning("Failed to encrypt bytes", gse);
        }
        return encrypt;
    }

    @Override public byte[] createRandomKey (int length)
    {
        byte[] secret = new byte[length];
        _rand.nextBytes(secret);
        return secret;
    }

    /**
     * Creates our AES cipher.
     */
    public static Cipher getAESCipher(int mode, byte[] key)
    {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec aesKey = new SecretKeySpec(key, "AES");
            cipher.init(mode, aesKey, IVPS);
            return cipher;
        } catch (GeneralSecurityException gse) {
            log.warning("Failed to create cipher", gse);
        }
        return null;
    }

    protected PublicKey stringToRSAPublicKey (String str)
    {
        try {
            BigInteger mod = new BigInteger(str.substring(0, str.indexOf(SPLIT)), 16);
            BigInteger exp = new BigInteger(str.substring(str.indexOf(SPLIT) + 1), 16);
            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(mod, exp);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(keySpec);
        } catch (NumberFormatException nfe) {
            log.warning("Failed to read key from string.", "str", str, nfe);
        } catch (GeneralSecurityException gse) {
            log.warning("Failed to read key from string.", "str", str, gse);
        }
        return null;
    }

    /**
     * Creates our RSA cipher.
     */
    protected Cipher getRSAEncryptCipher (String publicKey)
    {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, stringToRSAPublicKey(publicKey));
            return cipher;
        } catch (GeneralSecurityException gse) {
            log.warning("Failed to create cipher", gse);
        }
        return null;
    }

    protected byte[] doAES (int mode, byte[] key, byte[] contents)
        throws IOException
    {
        Cipher cipher = getAESCipher(mode, key);
        if (cipher == null) {
            throw new IOException("Failed to create AES cipher");
        }

        try {
            return cipher.doFinal(contents);
        } catch (GeneralSecurityException gse) {
            IOException ioe = new IOException("Failed to AES encrypt");
            ioe.initCause(gse);
            throw ioe;
        }
    }

    protected static final IvParameterSpec IVPS = new IvParameterSpec(IV);

    protected SecureRandom _rand = new SecureRandom();
}
