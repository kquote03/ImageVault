package com.coldcoffee.imagevault;


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

import java.util.Base64;

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
                    Toast.makeText(Register.this, "Failed to sign up", Toast.LENGTH_LONG).show();
                else{
                    SharedPreferences.Editor spEditor = sharedPreferences.edit();
                    spEditor.putString("salt", Base64.getEncoder().encodeToString(CryptoUtils.generateSalt()));
                    spEditor.putString("iv", Base64.getEncoder().encodeToString(CryptoUtils.generateIv().getIV()));
                    spEditor.apply();
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
}
