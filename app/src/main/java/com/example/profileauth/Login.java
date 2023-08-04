package com.example.profileauth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class Login extends AppCompatActivity {
TextInputLayout log_email, log_pass;
TextInputEditText log_input_email, log_input_pass;
TextView signuptv;
Button login_btn;
DbHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        log_email = findViewById(R.id.log_email);
        log_pass = findViewById(R.id.log_pass);
        log_input_email = findViewById(R.id.log_input_email);
        log_input_pass = findViewById(R.id.log_input_pass);
        signuptv = findViewById(R.id.signuptv);
        login_btn = findViewById(R.id.login_btn);
        dbHelper = new DbHelper(this);
        signuptv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, MainActivity.class));
            }
        });
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = Objects.requireNonNull(log_email.getEditText()).getText().toString().trim();
                String pass = Objects.requireNonNull(log_pass.getEditText()).getText().toString();
                if (email.isEmpty()) {
                    log_email.setError("This field is required");
                }
                if (pass.isEmpty()) {
                    log_pass.setError("This field is required");
                }
                else {
                    boolean loggedin = dbHelper.loginUserHelper(email, pass);
                    if (loggedin) {
                        Toast.makeText(getApplicationContext(), "login success!!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Login.this, Profile.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Login Failed...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}