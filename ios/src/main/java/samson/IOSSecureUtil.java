//
// SamsoN - utilities for playn clients and servers
// Copyright (c) 2014, Cupric - All rights reserved.
// http://github.com/cupric/samson/blob/master/LICENSE

package samson;

import java.io.IOException;

import cli.MonoTouch.Foundation.NSData;
import cli.MonoTouch.Security.SecKeyChain;
import cli.MonoTouch.Security.SecKind;
import cli.MonoTouch.Security.SecRecord;
import cli.MonoTouch.Security.SecStatusCode;
import cli.System.Convert;
import cli.System.IO.MemoryStream;
import cli.System.Runtime.InteropServices.Marshal;
import cli.System.Security.Cryptography.CryptoStream;
import cli.System.Security.Cryptography.CryptoStreamMode;
import cli.System.Security.Cryptography.ICryptoTransform;
import cli.System.Security.Cryptography.RNGCryptoServiceProvider;
import cli.System.Security.Cryptography.RSACryptoServiceProvider;
import cli.System.Security.Cryptography.RSAParameters;
import cli.System.Security.Cryptography.RijndaelManaged;
import samson.crypto.SecureUtil;
import samson.util.Encode;

import tripleplay.util.Logger;

public class IOSSecureUtil extends SecureUtil
{
    @Override public byte[] encryptAES (byte[] key, byte[] contents)
            throws IOException
    {
        try {
            return doAES(true, key, contents);
        } catch (Throwable t) {
            throw new IOException(t);
        }
    }

    @Override public byte[] decryptAES (byte[] key, byte[] contents)
            throws IOException
    {
        try {
            return doAES(false, key, contents);
        } catch (Throwable t) {
            throw new IOException(t);
        }
    }

    @Override public byte[] encryptRSA (String publicKey, byte[] secret, byte[] salt)
    {
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
        params.Modulus = Encode.unhex(modString);
        params.Exponent = Encode.unhex(expString);

        RSACryptoServiceProvider rsa = new RSACryptoServiceProvider();
        rsa.ImportParameters(params);
        byte[] encrypt = new byte[secret.length + salt.length];
        System.arraycopy(secret, 0, encrypt, 0, secret.length);
        System.arraycopy(salt, 0, encrypt, secret.length, salt.length);
        return rsa.Encrypt(encrypt, false); // false for PKCS#1 to match Java
    }

    @Override public byte[] createRandomKey (int length)
    {
        byte[] secret = new byte[length];
        _rand.GetBytes(secret);
        return secret;
    }

    @Override public byte[] retrieveKey (String id)
    {
        // basic record to query for the key
        SecRecord query = new SecRecord(SecKind.wrap(SecKind.GenericPassword));
        query.set_Generic(NSData.FromString(id));
        query.set_Account(id);
        query.set_Service(id);

        SecStatusCode[] status = {null};
        SecRecord match = SecKeyChain.QueryAsRecord(query, status);
        if (status[0].Value == SecStatusCode.Success) {
            Log.log.debug("Retrieved keychain item: " + id);

            // copy to bytes
            NSData value = match.get_ValueData();
            byte[] bytes = new byte[Convert.ToInt32(value.get_Length())];
            Marshal.Copy(value.get_Bytes(), bytes, 0, bytes.length);
            return bytes;
        } else {
            Log.log.error("Failed to retrieve keychain item: " + id, "status", status[0].ToString());
            return null;
        }
    }

    @Override public void storeKey (String id, byte[] value) throws IOException
    {
        SecStatusCode status;

        // insert a complete record
        SecRecord rec = new SecRecord(SecKind.wrap(SecKind.GenericPassword));
        rec.set_Generic(NSData.FromString(id));
        rec.set_Account(id);
        rec.set_Service(id);
        rec.set_ValueData(NSData.FromArray(value));
        rec.set_Comment("Please don't hack us =(");

        // remove an existing record, if applicable
        status = SecKeyChain.Remove(rec);
        if (status.Value == SecStatusCode.Success) {
            Log.log.debug("Removed keychain item: " + id);
        } else if (status.Value == SecStatusCode.ItemNotFound) {
            // noop
        } else {
            String error = Logger.format("Failed to remove keychain item: " + id, "status", status.ToString());
            Log.log.error(error);
            throw new IOException(error);
        }

        // insert the new record
        status = SecKeyChain.Add(rec);
        if (status.Value == SecStatusCode.Success) {
            Log.log.debug("Stored keychain item: " + id);
        } else {
            String error = Logger.format("Failed to store keychain item: " + id, "status", status.ToString());
            Log.log.error(error);
            throw new IOException(error);
        }
    }

    protected byte[] doAES (boolean encrypting, byte[] key, byte[] contents)
    {
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

    protected RNGCryptoServiceProvider _rand = new RNGCryptoServiceProvider();
}
