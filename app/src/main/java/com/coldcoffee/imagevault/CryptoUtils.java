package com.coldcoffee.imagevault;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * CryptoUtils master class. Everything you need to handle crypto except poorly written an coded
 * by yours truly 3_3
 * Signed SSBsb3ZlIHlvdSwgWWhnaHUh
 */
public class CryptoUtils extends AppCompatActivity {
    //Transferring the Context from MainActivity
    //(or any other calling activity lmao, who gives a f!ck)
    Context context;
    String sharedPrefsFile = "com.coldcoffee.imagevault";
    SharedPreferences sharedPreferences;

    static final int IV_LENGTH = 16;

    /**
     * Contructor, the class cannot be static due to Android f!ckery
     * @param context Pass the context from the calling object (usually this)
     */
    public CryptoUtils(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences(sharedPrefsFile, MODE_PRIVATE);
    }

    //Derives 256 bit key from a password
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


    /**
     * Generates the Initialization Vector
     * @return returns an IvParemeterSpec object (the IV)
     */
    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    /**
     * Generates a 20-byte salt in a byte array
     * @return 20-byte array of random stuff used for salt (yummy)
     */
    public static byte[] generateSalt() {
        Random r = new SecureRandom();
        byte[] salt = new byte[20];
        r.nextBytes(salt);
        return salt;
    }

    /**
     * Encrypt wrapper method
     * @param passphrase The password
     * @param file The file name
     * @return throws an OpenSecrets object so the user is forced to deal with it.
     * @see OpenSecrets
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public OpenSecrets encryptFile(String passphrase, String file){
        IvParameterSpec iv = generateIv();
        byte[] salt = generateSalt();
        try {
            cipher(getKeyFromPassword(passphrase, salt), file, file, Cipher.ENCRYPT_MODE, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new OpenSecrets(iv, salt);
    }

    /**
     * Decrypt wrapper function
     * @param passphrase The password (duh)
     * @param file The filename
     * @param openSecrets Takes an OpenSecrets object to reuse the IV and salt so chaos does not ensue
     * @see OpenSecrets
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void decryptFile(String passphrase, String file, OpenSecrets openSecrets){
        byte[] salt = openSecrets.getSalt();
        try {
            cipher(getKeyFromPassword(passphrase, salt), file, file, Cipher.DECRYPT_MODE, openSecrets.iv);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A cipher function that encrypts or decrypts a given filename
     * in internal storage.
     * @param key SecretKey, generated from the passphrase and salt
     * @param inputFile input file name
     * @param outputFile output file name
     * @param cryptoMode Cipher.ENCRYPT_MODE or DECRYPT_MODE (1 or 2)
     * @throws Exception
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void cipher(SecretKey key,
                       String inputFile, String outputFile, int cryptoMode, @Nullable IvParameterSpec iv) throws Exception {
        try{
            EncryptedPair data = cryptStream(context.openFileInput(inputFile), key, cryptoMode, iv);
            FileOutputStream outputStream = context.openFileOutput(outputFile, context.MODE_PRIVATE);
            outputStream.write(data.data);
            outputStream.close();
            SharedPreferences.Editor spEditor = sharedPreferences.edit();
            spEditor.putString(outputFile, Base64.getEncoder().encodeToString(data.iv.getIV()));
            spEditor.apply();
        }
        catch (IOException | NoSuchPaddingException |
               NoSuchAlgorithmException| InvalidAlgorithmParameterException| InvalidKeyException|
               BadPaddingException| IllegalBlockSizeException e){
            throw new Exception("Failed to "+(cryptoMode==Cipher.ENCRYPT_MODE?"encrypt":"decrypt")+" message "+ e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void cipher(SecretKey key,
                       Uri inputFile, String outputFile, int cryptoMode, @Nullable IvParameterSpec iv) throws Exception {
        try{
            ContentResolver cr = context.getContentResolver();
            EncryptedPair data = cryptStream( (FileInputStream) cr.openInputStream(inputFile), key, cryptoMode, iv);
            FileOutputStream outputStream = context.openFileOutput(outputFile, context.MODE_PRIVATE);
            outputStream.write(data.data);
            outputStream.close();
            SharedPreferences.Editor spEditor = sharedPreferences.edit();
            spEditor.putString(outputFile, Base64.getEncoder().encodeToString(data.iv.getIV()));
            spEditor.apply();

        }
        catch (IOException | NoSuchPaddingException |
               NoSuchAlgorithmException| InvalidAlgorithmParameterException| InvalidKeyException|
               BadPaddingException| IllegalBlockSizeException e){
            throw new Exception("Failed to "+(cryptoMode==Cipher.ENCRYPT_MODE?"encrypt":"decrypt")+" message "+ e.getMessage());
        }
    }

    public EncryptedPair cryptStream(FileInputStream stream, SecretKey key, int cryptoMode, @Nullable IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {
        //Initialize the cipher
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        if(cryptoMode == Cipher.ENCRYPT_MODE)
            iv = generateIv();
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

    //Simple method to get the length from a file stream
    //Probably inefficient. Too Bad.
    private int getFilestreamLen(String file) throws IOException {
        FileInputStream i = context.openFileInput(file);
        int count = 0;
        while(i.read() != -1){
            count++;
        }
        return count;
    }
    private int getFilestreamLen(FileInputStream i) throws IOException {
        return (int) i.getChannel().size();
    }
    private int getFilestreamLen(Uri i) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getContentResolver().openAssetFileDescriptor(i , "r");
        int fileSize = (int) fileDescriptor.getLength(); //hopefully kheir
        return fileSize;
    }

    public Bitmap getBitmapFromEncryptedImage(String filename, SecretKey key, IvParameterSpec iv) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        CryptoUtils cryptoUtils = new CryptoUtils(context);
        EncryptedPair decryptedData = cryptoUtils.cryptStream(context.openFileInput(filename), key, Cipher.DECRYPT_MODE, iv);
        return BitmapFactory.decodeByteArray(decryptedData.data, 0, decryptedData.data.length);
    }

}