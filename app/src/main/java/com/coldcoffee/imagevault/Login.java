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

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Login extends AppCompatActivity {

    EditText usernameField;
    EditText passwordField;
    Button loginButton;
    TextView signup;
    AppDB db;
    CryptoUtils crypto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = Room.databaseBuilder(getApplicationContext(),
                AppDB.class, "user").allowMainThreadQueries().build();
        Intent intent = new Intent(getApplicationContext(), SimpleImageActivity.class);
        crypto = new CryptoUtils(getApplicationContext());

        usernameField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signup = findViewById(R.id.signupText);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (usernameField.getText().equals(null) || usernameField.getText().toString().equals("")
                || passwordField.getText().equals(null)  || passwordField.getText().toString().equals("")) {
                    Toast.makeText(Login.this, "Please enter valid credentials", Toast.LENGTH_SHORT).show();
                } else {
                    //TODO Check if SQL Injectable
                    String username = usernameField.getText().toString();
                    String password = passwordField.getText().toString();
                    try {
                        User user = db.userDao().findByName(username);
                        SecretKey s = crypto.getSecretKey(username, // WTF
                                password,
                                new OpenSecrets(new IvParameterSpec(user.iv), user.salt));
                        if(!user.equals(null) &&
                                crypto.getKeyFromPassword(password,user.salt) // 0_0
                                        .equals(s)){
                            intent.putExtra("username",username);
                            intent.putExtra("password",password);
                            startActivity(intent);
                        }

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }


                }
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this,Register.class));
            }
        });

    }
}