package com.theporouscity.flagging.util;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.security.auth.x500.X500Principal;

/**
 * Created by bergstroml on 1/31/17.
 */

public class KeystoreHelper {

    public static final String TAG = "KeystoreHelper";
    public static final String ourKey = "xyzzy_and_etc";

    public static void storeEncryptedString(String key, String value, Context context) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            int nBefore = keyStore.size();

            // Create the keys if necessary
            if (!keyStore.containsAlias(ourKey)) {

                Calendar notBefore = Calendar.getInstance();
                Calendar notAfter = Calendar.getInstance();
                notAfter.add(Calendar.YEAR, 1);
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                        .setAlias(ourKey)
                        .setKeyType("RSA")
                        .setKeySize(2048)
                        .setSubject(new X500Principal("CN=test"))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(notBefore.getTime())
                        .setEndDate(notAfter.getTime())
                        .build();
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                generator.initialize(spec);

                KeyPair keyPair = generator.generateKeyPair();
            }
            int nAfter = keyStore.size();
            Log.v(TAG, "Before = " + nBefore + " After = " + nAfter);

            // Retrieve the keys
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)keyStore.getEntry(ourKey, null);
            PrivateKey privateKey = privateKeyEntry.getPrivateKey();
            PublicKey publicKey = privateKeyEntry.getCertificate().getPublicKey();

            Log.v(TAG, "private key = " + privateKey.toString());
            Log.v(TAG, "public key = " + publicKey.toString());

            // Encrypt the text
            String encryptedDataFilePath = context.getFilesDir().getAbsolutePath() + File.separator + key;

            Log.v(TAG, "plainText = " + value);
            Log.v(TAG, "encryptedDataFilePath = " + encryptedDataFilePath);

            Cipher inCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            inCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            CipherOutputStream cipherOutputStream =
                    new CipherOutputStream(
                            new FileOutputStream(encryptedDataFilePath), inCipher);
            cipherOutputStream.write(value.getBytes("UTF-8"));
            cipherOutputStream.close();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public static String retriveEncryptedString(String key, Context context) {

        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            // Retrieve the keys
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(ourKey, null);
            PrivateKey privateKey = privateKeyEntry.getPrivateKey();
            PublicKey publicKey = privateKeyEntry.getCertificate().getPublicKey();

            Cipher outCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            outCipher.init(Cipher.DECRYPT_MODE, privateKey);

            String encryptedDataFilePath = context.getFilesDir().getAbsolutePath() + File.separator + key;

            CipherInputStream cipherInputStream =
                    new CipherInputStream(new FileInputStream(encryptedDataFilePath),
                            outCipher);
            byte [] roundTrippedBytes = new byte[1000]; // TODO: dynamically resize as we get more data

            int index = 0;
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                roundTrippedBytes[index] = (byte)nextByte;
                index++;
            }
            String roundTrippedString = new String(roundTrippedBytes, 0, index, "UTF-8");
            Log.v(TAG, "round tripped string = " + roundTrippedString);

            return roundTrippedString;

        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return null;
    }
}
