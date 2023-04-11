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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class CryptoUtils extends AppCompatActivity {
    //Transferring the Context from MainActivity
    //(or any other calling activity lmao)
    Context context;
    public CryptoUtils(Context context){
        this.context = context;
    }

    //Derives 256 bit key from a password
    //Also salts it
    public SecretKey getKeyFromPassword(String password, String salt) throws Exception {
        try {
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey key = new SecretKeySpec(f.generateSecret(spec).getEncoded(), "AES");
            return key;
        }
        catch ( NoSuchAlgorithmException | InvalidKeySpecException e){
            throw new Exception("Failed to generate secret key "+ e.getMessage());
        }
    }

    //Generates the IV  
    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public void encryptFile(String algorithm, SecretKey key,
                            String inputFile, String outputFile, IvParameterSpec iv) throws Exception {
        try {

            //Encrypt the actual file stream
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            FileInputStream inputStream = context.openFileInput(inputFile);
            FileOutputStream outputStream = context.openFileOutput(outputFile, context.MODE_PRIVATE);
            byte[] buffer = new byte[64];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    outputStream.write(output);
                }
            }
            byte[] outputBytes = cipher.doFinal();
            if (outputBytes != null) {
                outputStream.write(outputBytes);
            }
            inputStream.close();
            outputStream.close();
        }
        catch (IOException | NoSuchPaddingException |
                NoSuchAlgorithmException| InvalidAlgorithmParameterException| InvalidKeyException|
                BadPaddingException| IllegalBlockSizeException e){
            throw new Exception("Failed to encrypt message "+ e.getMessage());
        }
    }


}