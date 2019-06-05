package com.ariel.ckazakov.foodies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Objects;

/**
 * Activity for login.
 */
public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private EditText loginPassword, loginEmail;
    private TextView newAccountMessage;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        newAccountMessage = findViewById(R.id.error_login);
        loginButton = findViewById(R.id.login_button);
        loginPassword = findViewById(R.id.login_password);
        loginEmail = findViewById(R.id.login_email);
        loadingBar = new ProgressDialog(this);


        newAccountMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectUserToRegister();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginVerification();
            }
        });

    }

    /**
     * Verificate user email and password, sign in if correct, show error otherwise.
     */
    private void loginVerification() {
        String email = loginEmail.getText().toString();
        String password = loginPassword.getText().toString();

        if (email.isEmpty())
            Toast.makeText(this, "Wrong email input, please write your email", Toast.LENGTH_SHORT).show();
        else if (password.isEmpty())
            Toast.makeText(this, "Wrong password input, please write your password", Toast.LENGTH_SHORT).show();
        else {
            loadingBar.setTitle("Login");
            loadingBar.setMessage("Please wait while you've login..");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(Boolean.TRUE);
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Error occurred: " + task.getException(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        SendUserToMainActivity();
                    } else
                        Toast.makeText(LoginActivity.this, "Error occurred: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            });
        }
    }

    /**
     * Send the user to main activity(and finish this one).
     */
    private void SendUserToMainActivity() {
        Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivity);
        finish();
    }

    /**
     * Send the user to register activity(and finish this one).
     */
    private void redirectUserToRegister() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
        finish();
    }
}
