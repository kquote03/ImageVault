package com.coldcoffee.imagevault;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.crypto.spec.IvParameterSpec;


public class MainActivity extends AppCompatActivity {
    SharedPreferences sharedprefs;
    String sharedPrefFilename = "com.coldcoffee.imagevault";

    String internalFile = "7a7a7a7a";
    String externalFileName = "File3";
    String tempPasswd = "SSBsb3ZlIHlvdSwgWWhnaHUh";
    OutputStream oStream;
    FileOutputStream fStream;
    File externalFilePath;
    File externalFile;
    CryptoUtils bill = new CryptoUtils(this);
    AppDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppDB db = Room.databaseBuilder(getApplicationContext(),
                AppDB.class, "user").build();

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

    public void deleteAll(View view) {
        externalFile.delete();
        getApplicationContext().deleteFile(internalFile);
    }

    public void encrypt(View view) throws Exception {
        sharedprefs = getSharedPreferences(sharedPrefFilename, MODE_PRIVATE);
        SharedPreferences.Editor sharedprefsEditor = sharedprefs.edit();

        OpenSecrets enc = bill.encrypt(tempPasswd, internalFile);

        sharedprefsEditor.putString("iv", Base64.encodeToString(enc.getIv().getIV(),Base64.DEFAULT));
        sharedprefsEditor.putString("salt", Base64.encodeToString(enc.getSalt(),Base64.DEFAULT));
        sharedprefsEditor.apply();
    }
    public void decrypt(View view) throws Exception {
        sharedprefs = getSharedPreferences(sharedPrefFilename, MODE_PRIVATE);
        byte[] salt = Base64.decode(sharedprefs.getString("salt","ERROR"),Base64.DEFAULT);
        byte[] rawIv = Base64.decode(sharedprefs.getString("iv","ERROR"),Base64.DEFAULT);
        IvParameterSpec iv = new IvParameterSpec(rawIv);
        OpenSecrets sec = new OpenSecrets(iv, salt);

        bill.decrypt(tempPasswd,internalFile, sec);
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
}