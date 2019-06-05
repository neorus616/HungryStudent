package com.ariel.ckazakov.foodies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Activity for user to change his profile.
 */
public class SettingsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ProgressDialog loadingBar;

    private EditText firstname, lastname;
    private Button updateAccountSettings;
    private CircleImageView userProfileImage;

    private DatabaseReference userRef;
    private FirebaseAuth firebaseAuth;
    private StorageReference UserProfileImageRef;

    private String currentUserUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserUid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserUid);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        loadingBar = new ProgressDialog(this);
        toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(Boolean.TRUE);

        firstname = findViewById(R.id.settings_firstname);
        lastname = findViewById(R.id.settings_lastname);
        updateAccountSettings = findViewById(R.id.update_account_settings);
        userProfileImage = findViewById(R.id.settings_profile_image);

        /*
            Write the current user first/last name and draw his profile image
         */
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String profileImage = Objects.requireNonNull(dataSnapshot.child("profileimage").getValue()).toString();
                    String profileFirstname = Objects.requireNonNull(dataSnapshot.child("firstname").getValue()).toString();
                    String profileLastname = Objects.requireNonNull(dataSnapshot.child("lastname").getValue()).toString();

                    Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(userProfileImage);
                    firstname.setText(profileFirstname);
                    lastname.setText(profileLastname);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /*
            When user click on his profile image, it redirect him to gallery to choose another one
         */
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, 1);
            }
        });

        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateProfile();
            }
        });


    }

    /**
     * After user choose a picture, crop it and upload to storage.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Cropping image");
                loadingBar.setMessage("Please wait, while we are cropping your profile image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                Uri resultUri = Objects.requireNonNull(result).getUri();

                StorageReference filePath = UserProfileImageRef.child(currentUserUid + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Profile image successfully stored in Firebase storage ...", Toast.LENGTH_SHORT).show();
                            Task<Uri> result = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getMetadata()).getReference()).getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUrl = uri.toString();
                                    userRef.child("profileimage").setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                                        startActivity(selfIntent);
                                                        Toast.makeText(SettingsActivity.this, "Profile image stored in Firebase Storage successfully ...", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(SettingsActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                    loadingBar.dismiss();
                                                }
                                            });
                                }
                            });
                        }
                    }
                });
            } else {
                Toast.makeText(SettingsActivity.this, "Error: The image has not been cut well. Try again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    /**
     * Validate if user typed first, last name and uploaded a picture, if he does, send him to
     * updateAccountSettings method.
     */
    private void validateProfile() {
        String newFirstname = firstname.getText().toString();
        String newLastname = lastname.getText().toString();
        if (newFirstname.isEmpty())
            Toast.makeText(this, "You can't leave first name empty!", Toast.LENGTH_SHORT).show();
        else if (newLastname.isEmpty())
            Toast.makeText(this, "You can't leave last name empty!", Toast.LENGTH_SHORT).show();
        else updateAccountSettings(newFirstname, newLastname);
    }

    /**
     * Update user first/last name and profile image in DB.
     *
     * @param newFirstname - new user first name
     * @param newLastname  - new user last name
     */
    private void updateAccountSettings(String newFirstname, String newLastname) {
        loadingBar.setTitle("Saving Information");
        loadingBar.setMessage("Please wait, while we are updating your account...");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("firstname", newFirstname);
        userMap.put("lastname", newLastname);
        userRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    SendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Your profile is updated Successfully.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "Error Occurred: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
                loadingBar.dismiss();
            }
        });
    }

    /**
     * Send the user to main activity(and finish this one).
     */
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
