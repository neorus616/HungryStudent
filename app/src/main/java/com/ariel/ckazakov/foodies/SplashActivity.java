package com.ariel.ckazakov.foodies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Main activity(first page the user see's when open the app).
 */
public class SplashActivity extends AppCompatActivity {

    private ToggleButton c10, c20, c30, t10, t20, t30;
    private String searchBy = null;
    private int searchByAmount = 0;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef;

    private CircleImageView navProfileImage;
    private TextView profileUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        /*
            If user is not logged in, he limited to some functions
         */
        String currentUserID;
        if (firebaseAuth.getCurrentUser() != null) {
            setContentView(R.layout.splash_screen);
            currentUserID = firebaseAuth.getCurrentUser().getUid();
            profileUser = findViewById(R.id.greeting_splash);
        } else {
            setContentView(R.layout.activity_main_guest);
            currentUserID = "";
            SendUserToMainActivityHot();
        }
        navProfileImage = findViewById(R.id.profile_image_splash);
        c10 = findViewById(R.id.c10_splash);
        c20 = findViewById(R.id.c20_splash);
        c30 = findViewById(R.id.c30_splash);
        t10 = findViewById(R.id.t10_splash);
        t20 = findViewById(R.id.t20_splash);
        t30 = findViewById(R.id.t30_splash);
        Button search = findViewById(R.id.searchRecipe_splash);
        Button showAllRecipes = findViewById(R.id.allRecipes_splash);

        /*
            Writes user first/last name and draw profile image to navigation bar
         */
        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    if (dataSnapshot.hasChild("firstname") && dataSnapshot.hasChild("lastname") && dataSnapshot.hasChild("profileimage")) {
                        String firstname = Objects.requireNonNull(dataSnapshot.child("firstname").getValue()).toString();
                        String lastname = Objects.requireNonNull(dataSnapshot.child("lastname").getValue()).toString();
                        String fullname = firstname + " " + lastname;
                        String profileImage = Objects.requireNonNull(dataSnapshot.child("profileimage").getValue()).toString();

                        profileUser.setText("Hello " + fullname);
                        Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(navProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        c10.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    c20.setPressed(false);
                    c30.setPressed(false);
                    t10.setPressed(false);
                    t20.setPressed(false);
                    t30.setPressed(false);
                    searchByAmount = 10;
                    searchBy = "cost";
                }
                return true;
            }
        });

        c20.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    c10.setPressed(false);
                    c30.setPressed(false);
                    t10.setPressed(false);
                    t20.setPressed(false);
                    t30.setPressed(false);
                    searchByAmount = 20;
                    searchBy = "cost";
                }
                return true;
            }
        });

        c30.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    c20.setPressed(false);
                    c10.setPressed(false);
                    t10.setPressed(false);
                    t20.setPressed(false);
                    t30.setPressed(false);
                    searchByAmount = 30;
                    searchBy = "cost";
                }
                return true;
            }
        });

        t10.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    t20.setPressed(false);
                    t30.setPressed(false);
                    c10.setPressed(false);
                    c20.setPressed(false);
                    c30.setPressed(false);
                    searchByAmount = 10;
                    searchBy = "time";
                }
                return true;
            }
        });

        t20.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    t10.setPressed(false);
                    t30.setPressed(false);
                    c10.setPressed(false);
                    c20.setPressed(false);
                    c30.setPressed(false);
                    searchByAmount = 20;
                    searchBy = "time";
                }
                return true;
            }
        });

        t30.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    t20.setPressed(false);
                    t10.setPressed(false);
                    c10.setPressed(false);
                    c20.setPressed(false);
                    c30.setPressed(false);
                    searchByAmount = 30;
                    searchBy = "time";
                }
                return true;
            }
        });


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchBy != null) {
                    if (searchBy.equals("time"))
                        SendUserToMainActivityTime(searchByAmount);
                    else if (searchBy.equals("cost"))
                        SendUserToMainActivityCost(searchByAmount);
                } else SendUserToMainActivityHot();
            }
        });

        showAllRecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToMainActivityHot();
            }
        });
    }

    /*
        Check if user has set first/last name and profile image, if he doesn't, he redirected to do so
     */
    private void CheckUserExistence() {
        final String currentUserID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(currentUserID).child("firstname").exists()) {
                    String firstname = Objects.requireNonNull(dataSnapshot.child(currentUserID).child("firstname").getValue()).toString();
                    if (firstname.isEmpty()) {
                        SendUserToProfileSetupActivity();
                    }
                } else
                    SendUserToProfileSetupActivity();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null)
            CheckUserExistence();
    }

    /**
     * Send the user to main activity with 'hot' recipes activity(and finish this one).
     */
    private void SendUserToMainActivityHot() {
        Intent mainActivity = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(mainActivity);
        finish();
    }

    /**
     * Send the user to main activity with 'cost' recipes activity(and finish this one).
     */
    private void SendUserToMainActivityCost(int cost) {
        Intent mainActivity = new Intent(SplashActivity.this, MainActivity.class);
        mainActivity.putExtra("content", "cost");
        mainActivity.putExtra("cost", Integer.toString(cost));
        startActivity(mainActivity);
        finish();
    }

    /**
     * Send the user to main activity with 'cost' recipes activity(and finish this one).
     */
    private void SendUserToMainActivityTime(int time) {
        Intent mainActivity = new Intent(SplashActivity.this, MainActivity.class);
        mainActivity.putExtra("content", "time");
        mainActivity.putExtra("time", Integer.toString(time));
        startActivity(mainActivity);
        finish();
    }

    /**
     * Send the user to profile setup activity(and finish this one).
     */
    private void SendUserToProfileSetupActivity() {
        Intent profileSetupIntent = new Intent(SplashActivity.this, ProfileSetupActivity.class);
        profileSetupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(profileSetupIntent);
        finish();
    }
}