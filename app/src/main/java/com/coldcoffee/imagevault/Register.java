package com.coldcoffee.imagevault;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Register extends AppCompatActivity {
    EditText username;

    TextView logintext;
    EditText Email;
    EditText password1;
    EditText password2;
    Button signupButton;
    CryptoUtils cryptoUtils;
    SharedPreferences sharedPreferences;
    String sharedPrefsFile = "com.coldcoffee.imagevault";
    String randomVerificationFile = "random";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        username = findViewById(R.id.username);
        Email = findViewById(R.id.Email);
        password1 = findViewById(R.id.Password1);
        password2 = findViewById(R.id.Password2);
        signupButton = findViewById(R.id.signupButton);
        logintext = findViewById(R.id.logintext);
        cryptoUtils = new CryptoUtils(getApplicationContext());
        sharedPreferences = getSharedPreferences(sharedPrefsFile, MODE_PRIVATE);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if(username.getText().toString().equals("") || username.getText().equals(null)
                        || Email.getText().toString().equals("") || Email.getText().equals(null)
                        || password1.getText().toString().equals("") ||  password1.getText().equals(null))
                    Toast.makeText(Register.this, "Please make sure the data entered is correct", Toast.LENGTH_LONG).show();
                else{
                    SharedPreferences.Editor spEditor = sharedPreferences.edit();
                    spEditor.putString("salt", Base64.getEncoder().encodeToString(CryptoUtils.generateSalt()));
                    spEditor.putString("iv", Base64.getEncoder().encodeToString(CryptoUtils.generateIv().getIV()));
                    spEditor.apply();
                    try {
                        خزن_سلسة_عشوائية();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    Toast.makeText(getApplicationContext(),"Registration successful habibi / habibati", Toast.LENGTH_LONG).show();
                    finish();
                }

            }
        });

        logintext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this,MainActivity.class));
            }
        });
    }

    /**
     * ما بعرفش ليش حولت عربي لكن يلا
     * بس بنعمل جينيريت لسلسلة عشوائية و بنخزنها للتعريف
     */
    private void خزن_سلسة_عشوائية() throws Exception {
        FileOutputStream fs = openFileOutput(randomVerificationFile, Context.MODE_PRIVATE);
        byte[] array = new byte[128]; // length is bounded by 7
        new Random().nextBytes(array);
        String randomString = new String(array, Charset.forName("UTF-8"));
        fs.write(randomString.getBytes());
        fs.close();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            cryptoUtils.cipher(cryptoUtils.getKeyFromPassword(password1.getText().toString(), Base64.getDecoder().decode(sharedPreferences.getString("salt","error"))),"random","random", Cipher.ENCRYPT_MODE,null);
        }
    }
}
