//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import java.io.IOException;

import samson.crypto.SecureUtil;
import cli.System.IO.MemoryStream;
import cli.System.Security.Cryptography.CryptoStream;
import cli.System.Security.Cryptography.CryptoStreamMode;
import cli.System.Security.Cryptography.ICryptoTransform;
import cli.System.Security.Cryptography.RNGCryptoServiceProvider;
import cli.System.Security.Cryptography.RSACryptoServiceProvider;
import cli.System.Security.Cryptography.RSAParameters;
import cli.System.Security.Cryptography.RijndaelManaged;

public class IOSSecureUtil extends SecureUtil
{
    @Override public byte[] encryptAES (byte[] key, byte[] contents) throws IOException {
        return doAES(true, key, contents);
    }

    @Override public byte[] decryptAES (byte[] key, byte[] contents) throws IOException {
        return doAES(false, key, contents);
    }

    @Override public byte[] encryptRSA (String publicKey, byte[] secret, byte[] salt) {
        int idx = publicKey.indexOf(SPLIT);
        RSAParameters params = new RSAParameters();
        String modString = publicKey.substring(0, idx);
        if (modString.length() % 2 != 0) {
            modString = "0" + modString;
        }
        String expString = publicKey.substring(idx + 1);
        if (expString.length() % 2 != 0) {
            expString = "0" + expString;
        }
        params.Modulus = unhexlate(modString);
        params.Exponent = unhexlate(expString);

        RSACryptoServiceProvider rsa = new RSACryptoServiceProvider();
        rsa.ImportParameters(params);
        byte[] encrypt = new byte[secret.length + salt.length];
        System.arraycopy(secret, 0, encrypt, 0, secret.length);
        System.arraycopy(salt, 0, encrypt, secret.length, salt.length);
        return rsa.Encrypt(encrypt, false); // false for PKCS#1 to match Java
    }

    @Override public byte[] createRandomKey (int length) {
        byte[] secret = new byte[length];
        _rand.GetBytes(secret);
        return secret;
    }

    protected byte[] doAES (boolean encrypting, byte[] key, byte[] contents) {
        MemoryStream memory = new MemoryStream();
        ICryptoTransform transform = encrypting ?
            new RijndaelManaged().CreateEncryptor(key, IV) :
            new RijndaelManaged().CreateDecryptor(key, IV);
        CryptoStream crypto = new CryptoStream(memory, transform,
            CryptoStreamMode.wrap(CryptoStreamMode.Write));
        crypto.Write(contents, 0, contents.length);
        crypto.FlushFinalBlock();
        memory.set_Position(0);
        byte[] transformed = new byte[(int)memory.get_Length()];
        memory.Read(transformed, 0, transformed.length);
        crypto.Close();
        memory.Close();
        return transformed;
    }

    protected static byte[] unhexlate (String hex) {
        if (hex == null || (hex.length() % 2 != 0)) return null;
        // convert to lowercase so things work
        hex = hex.toLowerCase();
        byte[] data = new byte[hex.length()/2];
        for (int ii = 0; ii < hex.length(); ii+=2) {
            int value = (byte)(XLATE.indexOf(hex.charAt(ii)) << 4);
            value  += XLATE.indexOf(hex.charAt(ii+1));
            // values over 127 are wrapped around, restoring negative bytes
            data[ii/2] = (byte)value;
        }
        return data;
    }

    protected RNGCryptoServiceProvider _rand = new RNGCryptoServiceProvider();
    protected static final String XLATE = "0123456789abcdef";
}
