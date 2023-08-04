package com.example.profileauth;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.Serializable;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfile extends AppCompatActivity {
    CircleImageView up_img;
    EditText up_uname, up_pass;
    Button update_btn;
    Serializable email;
    private byte[] selectedImage;
    DbHelper dbHelper;
    private static final int SELECT_PICTURE = 1;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        up_img = findViewById(R.id.up_img);
        up_uname = findViewById(R.id.up_uname);
        up_pass = findViewById(R.id.up_pass);
        update_btn = findViewById(R.id.update_btn);
        dbHelper = new DbHelper(this);
        Users users = new Users();
        up_uname.setText(Users.getName());
        up_pass.setText(Users.getPassword());
        byte[] imageData = Users.getImage();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        up_img.setImageBitmap(bitmap);
        selectedImage = imageData;

        email = getIntent().getSerializableExtra("user");
        Users userss = (Users) getIntent().getSerializableExtra("key_users");
        up_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(intent, SELECT_PICTURE);
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                galleryLauncher.launch(intent);
            }
        });
        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = up_uname.getText().toString();
                String pass = up_pass.getText().toString();
                if (name.isEmpty()) {
                    up_uname.setError("Field can't be empty");
                    return;
                }
                if (pass.isEmpty()) {
                    up_pass.setError("Field can't be empty");
                    return;
                }
                else {
                    boolean b = dbHelper.updateProfileHelper(Users.getEmail(), name, pass, selectedImage);
                    if (b) {
                        Toast.makeText(UpdateProfile.this, "Updated successfully!!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UpdateProfile.this, Profile.class);
                        intent.putExtra("email", Users.getEmail());
                        intent.putExtra("image", selectedImage);
                        startActivity(intent);
                        finishAffinity();

                    }
                }
            }
        });
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        Uri selectedImageUri = data.getData();
                        if (selectedImageUri != null) {
                            Bitmap bitmap1 = getBitmapFromUri(selectedImageUri);
                            if (bitmap1 != null) {
                                up_img.setImageBitmap(bitmap1);
                                selectedImage = getBytesFromBitmap(bitmap1);
                            } else {
                                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
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
}