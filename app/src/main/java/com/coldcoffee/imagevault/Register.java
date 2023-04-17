package com.coldcoffee.imagevault;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

public class Register extends AppCompatActivity {
    EditText username;
    TextView logintext;
    EditText Email;
    EditText password1;
    EditText password2;
    Button signupButton;
    AppDB db;
    CryptoUtils crypto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = Room.databaseBuilder(getApplicationContext(),
                AppDB.class, "user").allowMainThreadQueries().build();

        crypto = new CryptoUtils(getApplicationContext());

        username = findViewById(R.id.username);
        Email = findViewById(R.id.Email);
        password1 = findViewById(R.id.Password1);
        password2 = findViewById(R.id.Password2);
        signupButton = findViewById(R.id.signupButton);
        logintext = findViewById(R.id.logintext);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(username.getText().toString().equals("") || username.getText().equals(null)
                        || Email.getText().toString().equals("") || Email.getText().equals(null)
                        || password1.getText().toString().equals("") ||  password1.getText().equals(null))
                    Toast.makeText(Register.this, "Failed to sign up", Toast.LENGTH_LONG).show();
                else{
                    db.userDao().insertUser(new User(username.getText().toString(),crypto.generateIv().getIV(),crypto.generateSalt()));
                    try {
                        crypto.storeSecretKey(username.getText().toString(), crypto.getKeyFromPassword(password1.getText().toString(),db.userDao().getSalt(username.getText().toString())), password1.getText().toString());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
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
