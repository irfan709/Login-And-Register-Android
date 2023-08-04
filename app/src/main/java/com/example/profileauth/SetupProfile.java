package com.example.profileauth;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupProfile extends AppCompatActivity {
    CircleImageView reg_img;
    TextInputLayout reg_uname, reg_pass;
    TextInputEditText reg_input_uname, reg_input_pass;
    Button register_btn;
    byte[] selectedImage;
    DbHelper dbHelper;
    private static final int SELECT_PICTURE = 1;
    private static final int REQUEST_PERMISSIONS = 2;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_profile);
        reg_img = findViewById(R.id.reg_img);
        reg_uname = findViewById(R.id.reg_uname);
        reg_pass = findViewById(R.id.reg_pass);
        reg_input_uname = findViewById(R.id.reg_input_uname);
        reg_input_pass = findViewById(R.id.reg_input_pass);
        register_btn = findViewById(R.id.register_btn);
        dbHelper = new DbHelper(this);
        reg_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestStoragePermission();
            }
        });
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        Uri selectedImageUri = data.getData();
                        if (selectedImageUri != null) {
                            Bitmap bitmap = getBitmapFromUri(selectedImageUri);
                            if (bitmap != null) {
                                reg_img.setImageBitmap(bitmap);
                                selectedImage = getBytesFromBitmap(bitmap);
                            } else {
                                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void registerUser() {
        if (!validateUsername() | !validatePassword()) {
            return;
        } else {
            Intent intent = getIntent();
            String email = getIntent().getStringExtra("email");
            String username = Objects.requireNonNull(reg_input_uname.getText()).toString();
            String password = Objects.requireNonNull(reg_input_pass.getText()).toString();
            boolean b = dbHelper.registerUserHelper(email, username, password, selectedImage);
            if (b) {
                Toast.makeText(SetupProfile.this, "Registered successfully!!", Toast.LENGTH_SHORT).show();
                reg_input_uname.setText("");
                reg_input_pass.setText("");
                reg_img.setImageResource(R.drawable.ic_launcher_background);
                Intent intent1 = new Intent(SetupProfile.this, Profile.class);
                intent1.putExtra("email", email);
                startActivity(intent1);
                finishAffinity();
            }
        }
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showPermissionExplanationDialog();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
            }
        } else {
            openGallery();
        }
    }
    private void showPermissionExplanationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Storage Permission")
                .setMessage("This app needs access to your device storage")
                .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
                    }
                })
                .setNegativeButton("deny", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false)
                .show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) || !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showPermissionSettingsDialog();
                }
            }
        }
    }
    private void showPermissionSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Storage settings")
                .setMessage("To use this app you need to grant the storage permission")
                .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openAppSettings();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setCancelable(false)
                .show();
    }
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }
    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            if (parcelFileDescriptor != null) {
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();
                return image;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
            return stream.toByteArray();
        } else {
            return new byte[0];
        }
    }
    public boolean validateUsername() {
        String username = Objects.requireNonNull(reg_uname.getEditText()).getText().toString();
        if (username.isEmpty()) {
            reg_uname.setError("Field can't be empty");
            reg_input_uname.requestFocus();
            return false;
        }
        else if (username.length() > 20) {
            reg_uname.setError("Username length length too long");
            reg_input_uname.requestFocus();
            return false;
        }
        else {
            reg_uname.setError(null);
            return true;
        }
    }
    public boolean validatePassword() {
        String pass = Objects.requireNonNull(reg_pass.getEditText()).getText().toString();
        if (pass.isEmpty()) {
            reg_pass.setError("Field can't be empty");
            reg_input_pass.requestFocus();
            return false;
        }
        else if (!pass.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_\\-+=~`{}\\[\\]|:;\"'<>,.?\\\\/])[^\\s]{6,}$"
        )) {
            reg_pass.setError("Password too weak");
            reg_input_pass.requestFocus();
            return false;
        }
        else {
            reg_pass.setError(null);
            return true;
        }
    }
}