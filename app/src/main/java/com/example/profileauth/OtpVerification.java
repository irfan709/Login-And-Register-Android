package com.example.profileauth;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class OtpVerification extends AppCompatActivity {

    private EditText otpEditText;

    private String email;
    private String generatedOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        otpEditText = findViewById(R.id.otpEditText);
        Button verifyOTPButton = findViewById(R.id.verifyOTPButton);

        // Get the email and OTP from the previous activity
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        generatedOTP = intent.getStringExtra("otp");

        verifyOTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredOTP = otpEditText.getText().toString().trim();
                if (!enteredOTP.isEmpty()) {
                    if (enteredOTP.equals(generatedOTP)) {
                        Toast.makeText(OtpVerification.this, "OTP verification successful", Toast.LENGTH_SHORT).show();
                        redirectToSetupProfileActivity();
                    } else {
                        Toast.makeText(OtpVerification.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(OtpVerification.this, "Enter OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void redirectToSetupProfileActivity() {
        Intent intent = new Intent(OtpVerification.this, SetupProfile.class);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }
}
