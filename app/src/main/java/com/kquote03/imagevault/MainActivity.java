package com.kquote03.imagevault;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import com.google.common.io.ByteStreams;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class MainActivity extends AppCompatActivity {
    SharedPreferences sharedprefs;
    String sharedPrefFilename = "com.kquote03.imagevault";

    String internalFile = "7a7a";
    String externalFileName = "File3";
    OutputStream oStream;
    FileOutputStream fStream;
    File externalFilePath;
    File externalFile;
    CryptoUtils bill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        externalFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        externalFile  = new File(externalFilePath, externalFileName);
    }

    public void saveToInternal(View view) {
        EditText textVar = findViewById(R.id.message);
        String message = textVar.getText().toString();

        try {
            oStream = openFileOutput(internalFile, Context.MODE_PRIVATE);
            oStream.write(message.getBytes());
            oStream.close();
            Toast.makeText(getApplicationContext(), "The file"+internalFile+"is created"+getFilesDir().getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void saveToExternal(View view) {
        EditText textVar = findViewById(R.id.message);
        String message = textVar.getText().toString();
        if(isExternalStorageWritable()){
            try {
                fStream = new FileOutputStream(externalFile);
                fStream.write(message.getBytes());
                fStream.close();
                Toast.makeText(getApplicationContext(),"File "+externalFileName+" created at "+getFilesDir().getAbsolutePath(),Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "External Storage Unavailable",Toast.LENGTH_LONG).show();
        }
    }

    public void deleteAll(View view) {
        externalFile.delete();
        getApplicationContext().deleteFile(internalFile);
    }

    public void encrypt(View view) throws Exception {
        IvParameterSpec iv = generateIv();

        sharedprefs = getSharedPreferences(sharedPrefFilename, MODE_PRIVATE);
        SharedPreferences.Editor sharedprefsEditor = sharedprefs.edit();
        sharedprefsEditor.putString("iv", Base64.encodeToString(iv.getIV(),Base64.DEFAULT));
        sharedprefsEditor.apply();
        encryptFile(getKeyFromPassword("SSBsb3ZlIHlvdSwgWWhnaHUh","PDM="),internalFile, internalFile, iv);
    }
    public void decrypt(View view) throws Exception {
        sharedprefs = getSharedPreferences(sharedPrefFilename, MODE_PRIVATE);
        byte[] rawIv = Base64.decode(sharedprefs.getString("iv","ERROR"),Base64.DEFAULT);
        IvParameterSpec iv = new IvParameterSpec(rawIv);
        decryptFile(getKeyFromPassword("SSBsb3ZlIHlvdSwgWWhnaHUh","PDM="),internalFile, internalFile, iv);
    }

    public void readText(View view) {
        TextView myTextBox = findViewById(R.id.outputText);
        String message;
        try {
            InputStream iStream = openFileInput(internalFile);
            message = new BufferedReader(new InputStreamReader(iStream)).readLine();
            iStream.close();
            myTextBox.setText(message);
        }
        catch(Exception e){
            Toast.makeText(this, e.getMessage(),Toast.LENGTH_LONG);
        }
    }

    public SecretKey getKeyFromPassword(String password, String salt) throws Exception {
        try {
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey key = new SecretKeySpec(f.generateSecret(spec).getEncoded(), "AES");
            return key;
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e){
            throw new Exception("Failed to generate secret key "+ e.getMessage());
        }
    }

    //Generates the IV
    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public void encryptFile(SecretKey key,
                        String inputFile, String outputFile, IvParameterSpec iv) throws Exception {
        String algorithm = "AES/CBC/PKCS5Padding";
        try {

            //Encrypt the actual file stream
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            InputStream inputStream = openFileInput(inputFile);
            oStream = openFileOutput(internalFile, MODE_PRIVATE);
            byte[] buffer = new byte[2];
            int bytesRead;
            byte[] bytes = ByteStreams.toByteArray(inputStream);
            //while ((bytesRead = new BufferedReader(new InputStreamReader(inputStream)).read()) != -1 || true) {
                /*byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    oStream.write(output);
                }*/
                //Log.d("bytes ", new String(String.valueOf(bytesRead)));
            //}
            byte[] outputBytes = cipher.doFinal();
            if (outputBytes != null) {
                Log.d("haha", Arrays.toString(outputBytes));
                oStream.write(outputBytes);
            }
            inputStream.close();
            oStream.close();
        }
        catch (IOException | NoSuchPaddingException |
               NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException |
               BadPaddingException | IllegalBlockSizeException e){
            throw new Exception("Failed to encrypt message "+ e.getMessage());
        }
    }

    public void decryptFile(SecretKey key,
                        String inputFile, String outputFile, IvParameterSpec iv) {
        String algorithm = "AES/CBC/PKCS5Padding";
        try {
            //Decrypt the actual file stream
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            FileInputStream inputStream = openFileInput(inputFile);
            oStream = openFileOutput(outputFile, MODE_PRIVATE);
            byte[] buffer = new byte[64];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                Log.d("current buff: ",Arrays.toString(output));
                if (output != null) {
                    oStream.write(output);
                }
            }
            byte[] outputBytes = cipher.doFinal();
            Log.d("After buff: ", new String(outputBytes));
            if (outputBytes != null) {
                oStream.write(outputBytes);
            }
            inputStream.close();
            oStream.close();
        }
        catch (IOException | NoSuchPaddingException |
               NoSuchAlgorithmException| InvalidAlgorithmParameterException| InvalidKeyException|
               BadPaddingException| IllegalBlockSizeException e){
            throw new IllegalArgumentException("Failed to decrypt message "+ e.getMessage());
        }
    }


}