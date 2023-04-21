package com.coldcoffee.imagevault;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
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
    static final int IV_LENGTH = 16;

    /**
     * Contructor, the class cannot be static due to Android f!ckery
     * @param context Pass the context from the calling object (usually this)
     */
    public CryptoUtils(Context context){
        this.context = context;
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
    public OpenSecrets encryptFile(String passphrase, String file){
        IvParameterSpec iv = generateIv();
        byte[] salt = generateSalt();
        try {
            cipher(getKeyFromPassword(passphrase, salt), file, file, Cipher.ENCRYPT_MODE);
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
    public void decryptFile(String passphrase, String file, OpenSecrets openSecrets){
        byte[] salt = openSecrets.getSalt();
        try {
            cipher(getKeyFromPassword(passphrase, salt), file, file, Cipher.DECRYPT_MODE);
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
    public void cipher(SecretKey key,
                        String inputFile, String outputFile, int cryptoMode) throws Exception {
        String algorithm = "AES/CBC/PKCS5Padding";
        try {
            //Initialize the cipher
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            FileInputStream inputStream = context.openFileInput(inputFile);

            int buffer;
            int readCount = 0;
            byte[] inputBytes = new byte[getFilestreamLen(inputFile)+IV_LENGTH];

            IvParameterSpec iv;
            //If encrypting, generate the IV and make the file begin with it.
            if(cryptoMode == Cipher.ENCRYPT_MODE) {
                iv = generateIv();
                for(byte b : iv.getIV()){
                    inputBytes[readCount++] = b;
                }
            }
            else{
                //Else read the IV from the beginning of the file
                byte[] tempIv = new byte[IV_LENGTH];
                while((buffer = inputStream.read()) != -1 && readCount <= 15){
                    tempIv[readCount] = (byte) buffer;
                }
                iv = new IvParameterSpec(tempIv);
            }


            cipher.init(cryptoMode, key, iv);
            //Initialize the input stream from an internal storage file
            int bytesRead;
            //Copies the entire file to the inputBytes byte array (used as a temporary location
            //to store the data between inputStream and outputStream).
            while ((buffer = inputStream.read()) != -1) {
                inputBytes[readCount++] = (byte) buffer;
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
            throw new Exception("Failed to "+(cryptoMode==Cipher.ENCRYPT_MODE?"encrypt":"decrypt")+" message "+ e.getMessage());
        }
    }

    public byte[] cryptStream(FileInputStream stream, SecretKey key, int cryptoMode) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {
        //Initialize the cipher
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        int buffer;
        int readCount = 0;
        byte[] inputBytes = new byte[getFilestreamLen(stream)+IV_LENGTH];

        IvParameterSpec iv;
        //If encrypting, generate the IV and make the file begin with it.
        if(cryptoMode == Cipher.ENCRYPT_MODE) {
            iv = generateIv();
            for(byte b : iv.getIV()){
                inputBytes[readCount++] = b;
            }
        }
        else{
            //Else read the IV from the beginning of the file
            byte[] tempIv = new byte[IV_LENGTH];
            while((buffer = stream.read()) != -1 && readCount <= 15){
                tempIv[readCount] = (byte) buffer;
            }
            iv = new IvParameterSpec(tempIv);
        }


        cipher.init(cryptoMode, key, iv);
        //Initialize the input stream from an internal storage file
        int bytesRead;
        //Copies the entire file to the inputBytes byte array (used as a temporary location
        //to store the data between inputStream and outputStream).
        while ((buffer = stream.read()) != -1) {
            inputBytes[readCount++] = (byte) buffer;
        }
        inputBytes = cipher.doFinal(inputBytes);
        stream.close();
        return inputBytes;

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
        int count = 0;
        while (i.read() != -1) {
            count++;
        }
        i.reset();
        return count;
    }

    public Bitmap getBitmapFromEncryptedImage(String filename, SecretKey key) throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        CryptoUtils cryptoUtils = new CryptoUtils(context);
        byte[] decryptedData = cryptoUtils.cryptStream(context.openFileInput(filename), key, Cipher.DECRYPT_MODE);
        return BitmapFactory.decodeByteArray(decryptedData, 0, decryptedData.length);
    }

}