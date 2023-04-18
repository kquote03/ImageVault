package com.coldcoffee.imagevault;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * CryptoUtils master class. Everything you need to handle crypto except poorly written and coded
 * by yours truly 3_3
 * Signed SSBsb3ZlIHlvdSwgWWhnaHUh
 */
public class CryptoUtils extends AppCompatActivity {
    //Transferring the Context from MainActivity
    //(or any other calling activity lmao, who gives a f!ck)
    Context context;

    /**
     * Contructor, the class cannot be static due to Android f!ckery
     *
     * @param context Pass the context from the calling object (usually this)
     */
    public CryptoUtils(Context context) {
        this.context = context;
    }


    public SecretKey keyGen(String username) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyGenerator keyGenerator = KeyGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(username,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build();
        keyGenerator.init(keyGenParameterSpec);
        return keyGenerator.generateKey();
    }

    public SecretKey retrieveKey(String username) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableEntryException {
        KeyStore keystore = KeyStore.getInstance("AndroidKeyStore");
        keystore.load(null);
        KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keystore.getEntry(username, null);
        return secretKeyEntry.getSecretKey();
    }

    public EncryptedPair encryptHash(byte[] hash, SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return new EncryptedPair(cipher.doFinal(hash), new IvParameterSpec(cipher.getIV()));
    }

    public EncryptedPair cryptStream(FileInputStream stream, SecretKey key, int cryptoMode) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {
        //Initialize the cipher
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = generateIv();
        cipher.init(cryptoMode, key, iv);
        //Initialize the input stream from an internal storage file
        int buffer;
        int count = 0;
        byte[] inputBytes = new byte[getFilestreamLen(stream)];
        int bytesRead;
        //Copies the entire file to the inputBytes byte array (used as a temporary location
        //to store the data between inputStream and outputStream).
        while ((buffer = stream.read()) != -1) {
            inputBytes[count++] = (byte) buffer;
        }
        inputBytes = cipher.doFinal(inputBytes);
        stream.close();
        return new EncryptedPair(inputBytes, iv);

    }

    /**
     * Generates the Initialization Vector
     *
     * @return returns an IvParemeterSpec object (the IV)
     */
    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    //Simple method to get the length from a file stream
    //Probably inefficient. Too Bad.
    private int getFilestreamLen(FileInputStream i) throws IOException {
        int count = 0;
        while (i.read() != -1) {
            count++;
        }
        i.reset();
        return count;
    }

}

















/*    //Derives 256 bit key from a password
    //Also salts it
    public SecretKey getKeyFromPassword(String password, byte[] salt) throws Exception {
        try {
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKey key = new SecretKeySpec(f.generateSecret(spec).getEncoded(), "AES");
            return key;
        }
        catch ( NoSuchAlgorithmException | InvalidKeySpecException e){
            throw new Exception("Failed to generate secret key "+ e.getMessage());
        }
    }

    *//**
     * Stores a key in the Android Keystore where each key
     * corresponds to a username.
     * @param username The username taken from the UI
     * @param key The key to store of type SecretKey
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     *//*
    public void storeSecretKey(String username, SecretKey key, String password) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        byte[] keyBytes = key.getEncoded();
        SecretKey materialKey = new SecretKeySpec(keyBytes, "AES");
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        KeyProtection.Builder builder = new KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT);
        builder.setUserAuthenticationRequired(false);
        builder.setUserAuthenticationValidityDurationSeconds(30);
        builder.setBlockModes(KeyProperties.BLOCK_MODE_CBC);
        builder.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);
        builder.setRandomizedEncryptionRequired(false);
        KeyProtection protection = builder.build();
        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(materialKey);
        keyStore.setEntry(username, secretKeyEntry, protection);
        Enumeration<String> aliasess = keyStore.aliases();
        Log.d(" ","");
    }

    *//*public void generateKey(String username) throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException {
        String AndroidKeyStore = "AndroidKeyStore";
        String AES_MODE = "AES/GCM/NoPadding";
        KeyStore keyStore = KeyStore.getInstance(AndroidKeyStore);
        keyStore.load(null);

        if (!keyStore.containsAlias(username)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, AndroidKeyStore);
            keyGenerator.init(
                    new KeyGenParameterSpec.Builder(username,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .setRandomizedEncryptionRequired(false)
                            .build());
            keyGenerator.generateKey();
        }
    }
*//*
    public SecretKey getSecretKey(String username, String passphrase, OpenSecrets openSecrets) throws Exception {
        SecretKey generatedKey = getKeyFromPassword(passphrase, openSecrets.getSalt());
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(username,null);
        if(keyStore.containsAlias(username)) Log.d("DEBUG","CONTAINS ALIAS");
        else Log.d("DEBUG","DOES NOT");
        if (generatedKey.equals(secretKeyEntry.getSecretKey()))
            return secretKeyEntry.getSecretKey();
        else throw new IllegalArgumentException(""+generatedKey.getEncoded()+" kek "+secretKeyEntry.getSecretKey().getEncoded());
    }
        public SecretKey getSecretKey(String username) throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
          return (SecretKey) keyStore.getKey(username, null);
        }


    *//**
     * Generates a 20-byte salt in a byte array
     * @return 20-byte array of random stuff used for salt (yummy)
     *//*
    public static byte[] generateSalt() {
        Random r = new SecureRandom();
        byte[] salt = new byte[20];
        r.nextBytes(salt);
        return salt;
    }

    *//**
     * Encrypt wrapper method
     * @param passphrase The password
     * @param file The file name
     * @return throws an OpenSecrets object so the user is forced to deal with it.
     * @see OpenSecrets
     *//*
    public OpenSecrets encrypt(String passphrase, String file){
        IvParameterSpec iv = generateIv();
        byte[] salt = generateSalt();
        try {
            cipher(getKeyFromPassword(passphrase, salt), file, file, iv, Cipher.ENCRYPT_MODE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new OpenSecrets(iv, salt);
    }

    *//**
     * Decrypt wrapper function
     * @param passphrase The password (duh)
     * @param file The filename
     * @param openSecrets Takes an OpenSecrets object to reuse the IV and salt so chaos does not ensue
     * @see OpenSecrets
     *//*
    public void decrypt(String passphrase, String file, OpenSecrets openSecrets){
        IvParameterSpec iv = openSecrets.getIv();
        byte[] salt = openSecrets.getSalt();
        try {
            cipher(getKeyFromPassword(passphrase, salt), file, file, iv, Cipher.DECRYPT_MODE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    *//**
     * A cipher function that encrypts or decrypts a given filename
     * in internal storage.
     * @param key SecretKey, generated from the passphrase and salt
     * @param inputFile input file name
     * @param outputFile output file name
     * @param iv initializtion vector (IvParameterSpec)
     * @param cipherMode Cipher.ENCRYPT_MODE or DECRYPT_MODE (1 or 2)
     * @throws Exception
     *//*
    public void cipher(SecretKey key,
                        String inputFile, String outputFile, IvParameterSpec iv, int cipherMode) throws Exception {
        String algorithm = "AES/CBC/PKCS5Padding";
        try {
            //Initialize the cipher
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(cipherMode, key, iv);
            //Initialize the input stream from an internal storage file
            FileInputStream inputStream = context.openFileInput(inputFile);
            int buffer;
            int count = 0;
            byte[] inputBytes = new byte[getFilestreamLen(inputFile)];
            int bytesRead;
            //Copies the entire file to the inputBytes byte array (used as a temporary location
            //to store the data between inputStream and outputStream).
            while ((buffer = inputStream.read()) != -1) {
                inputBytes[count++] = (byte)buffer;
            }
            inputBytes = cipher.doFinal(inputBytes);
            inputStream.close();

            //Writes everything to the file.
            FileOutputStream outputStream = context.openFileOutput(outputFile, context.MODE_PRIVATE);
            outputStream.write(inputBytes);
            outputStream.close();

        }
        catch (IOException | NoSuchPaddingException |
               NoSuchAlgorithmException| InvalidAlgorithmParameterException| InvalidKeyException|
               BadPaddingException| IllegalBlockSizeException e){
            throw new Exception("Failed to "+(cipherMode==Cipher.ENCRYPT_MODE?"encrypt":"decrypt")+" message "+ e.getMessage());
        }
    }

    //Just a method I used to print the file for debugging this insanity.
    //Keeping it because I'm too sentimental to remove it :sniffle:
    private void printFile(String file, int length) throws IOException {
        FileInputStream f = context.openFileInput(file);
        int buffer;
        int count = 0;
        byte[] a = new byte[length];
        while((buffer = f.read()) != -1)
            a[count++] = (byte)buffer;
        f.close();
    }
   **/


