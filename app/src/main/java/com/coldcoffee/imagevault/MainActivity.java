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

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class MainActivity extends AppCompatActivity {

    EditText username;
    EditText password;
    Button loginButton;
    TextView signup;
    SecretKey secretKey;
    CryptoUtils cryptoUtils;
    SharedPreferences sharedPreferences;
    String sharedPrefsFile = "com.coldcoffee.imagevault";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signup = findViewById(R.id.signupText);
        cryptoUtils = new CryptoUtils(getApplicationContext());
        sharedPreferences = getSharedPreferences(sharedPrefsFile, MODE_PRIVATE);

        //TODO check user entry.
        loginButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if(username.getText().toString().equals("") || username.getText().equals(null)
                || password.getText().toString().equals("") || password.getText().equals(null)){
                    Toast.makeText(getApplicationContext(), "Please verify that the details are correct", Toast.LENGTH_LONG).show();
                }
                try {
                    //I know we're not using rust-style unwrapping but it looked cool so i used it 3_3
                    if(sharedPreferences.contains("salt")) {
                        byte[] salt = Base64
                                .getDecoder()
                                .decode(sharedPreferences
                                        .getString("salt", ""));
                        IvParameterSpec iv = new IvParameterSpec(Base64
                                .getDecoder()
                                .decode(sharedPreferences.getString("iv", "")));

                        secretKey = cryptoUtils.getKeyFromPassword(password.getText().toString(), salt);


                        if (cryptoUtils.attemptDecryptFile(password.getText().toString(), "random", salt, iv)) {


                            Intent loggedIn = new Intent(getApplicationContext(), GridViewActivity.class);
                            loggedIn.putExtra("key", Base64
                                    .getEncoder()
                                    .encodeToString(secretKey.getEncoded()));
                            startActivity(loggedIn);
                        } else {
                            Toast.makeText(getApplicationContext(), "Could not decrypt\nPlease verify that the details are correct", Toast.LENGTH_LONG).show();
                        }
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Please register first", Toast.LENGTH_LONG).show();

                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,Register.class));
            }
        });

    }
}