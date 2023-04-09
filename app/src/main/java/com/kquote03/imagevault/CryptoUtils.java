package com.kquote03.imagevault;

import android.content.Context;
import android.os.Debug;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


public class CryptoUtils extends AppCompatActivity {

    Context context;

    public CryptoUtils(Context context){
        this.context = context;
    }
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    public void encrypt(String key, String inputFile, String outputFile)
            throws IllegalArgumentException {
        doCrypto(Cipher.ENCRYPT_MODE, key, inputFile, outputFile);
    }

    public void decrypt(String key, String inputFile, String outputFile)
            throws IllegalArgumentException {
        doCrypto(Cipher.DECRYPT_MODE, key, inputFile, outputFile);
    }

    private void doCrypto(int cipherMode, String key, String inputFile,
                                 String outputFile) throws IllegalArgumentException {
        try {
            Key secretKey = new SecretKeySpec(key.getBytes("UTF-8"), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(cipherMode, secretKey);
            FileInputStream inputStream = context.openFileInput(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            int blockSize = cipher.getBlockSize();
            int paddingSize = blockSize - (inputBytes.length % blockSize);
            byte[] paddedInputBytes = Arrays.copyOf(inputBytes, inputBytes.length + paddingSize);


            byte[] outputBytes = cipher.doFinal(inputBytes);

            FileOutputStream outputStream = context.openFileOutput(outputFile,Context.MODE_PRIVATE);
            outputStream.write(outputBytes);

            inputStream.close();
            outputStream.close();

        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                 | InvalidKeyException | BadPaddingException
                 | IllegalBlockSizeException | IOException ex) {
            throw new IllegalArgumentException("Error encrypting/decrypting file", ex);
        }
    }
}