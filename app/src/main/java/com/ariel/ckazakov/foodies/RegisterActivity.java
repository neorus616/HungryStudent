package com.ariel.ckazakov.foodies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Objects;

/**
 * Activity for register.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText register_email, register_password, register_confirmPassword;
    private Button registerButton;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        register_email = findViewById(R.id.register_email);
        register_password = findViewById(R.id.register_password);
        register_confirmPassword = findViewById(R.id.confirmPassword);
        registerButton = findViewById(R.id.register_button);
        loadingBar = new ProgressDialog(this);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        /*
            If user somehow managed to get here, and he is logged in, send him to main activity
         */
        if (user != null)
            SendUserToMainActivity();
    }

    private void createNewAccount() {
        String email = register_email.getText().toString();
        String password = register_password.getText().toString();
        String confirmPassword = register_confirmPassword.getText().toString();

        if (email.isEmpty())
            Toast.makeText(this, "Wrong email input, please write your email", Toast.LENGTH_SHORT).show();
        else if (password.isEmpty())
            Toast.makeText(this, "Wrong password input, please write your password", Toast.LENGTH_SHORT).show();
        else if (confirmPassword.isEmpty())
            Toast.makeText(this, "Wrong confirm password input, please write your confirm password", Toast.LENGTH_SHORT).show();
        else if (!password.equals(confirmPassword))
            Toast.makeText(this, "passwords doesn't match! please correct this", Toast.LENGTH_SHORT).show();
        else {
            loadingBar.setTitle("Creating new account");
            loadingBar.setMessage("Please wait while account is created");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(Boolean.TRUE);
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "Error occurred: " + task.getException(), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Get new Instance ID token
                                String token = Objects.requireNonNull(task.getResult()).getToken();
                                DatabaseReference fcmRef = FirebaseDatabase.getInstance().getReference().child("FCM");
                                fcmRef.child(token).setValue(token);
                                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users");
                                userRef.child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()).child("token").setValue(token);
                            }
                        });
                        SendUserToCreateProfile();
                        Toast.makeText(RegisterActivity.this, "Account is created!", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(RegisterActivity.this, "Error occurred: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            });
        }
    }

    /**
     * Send the user to profile setup activity(and finish this one).
     */
    private void SendUserToCreateProfile() {
        Intent profileIntent = new Intent(this, ProfileSetupActivity.class);
        profileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(profileIntent);
        finish();
    }

    /**
     * Send the user to main activity(and finish this one).
     */
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}