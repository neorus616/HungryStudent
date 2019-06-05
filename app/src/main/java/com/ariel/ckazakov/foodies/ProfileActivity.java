package com.ariel.ckazakov.foodies;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView fullname;
    private CircleImageView profilePic;
    private Button followButton, unfollowButton, myPosts, myFollows;

    private DatabaseReference userRef, followRef, recipeRef;
    private FirebaseAuth firebaseAuth;

    private String currentUserUid, userKey;
    private long numOfFollowers = 0, numOfRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        fullname = findViewById(R.id.profile_fullname_public);
        profilePic = findViewById(R.id.profile_pic_public);
        followButton = findViewById(R.id.follow_button);
        unfollowButton = findViewById(R.id.unfollow_button);
        myPosts = findViewById(R.id.myPosts);
        myFollows = findViewById(R.id.myFollows);
        followRef = FirebaseDatabase.getInstance().getReference().child("Follows");
        recipeRef = FirebaseDatabase.getInstance().getReference().child("Recipes");

        if (getIntent().getExtras() != null && getIntent().getExtras().get("userKey") != null)
            userKey = Objects.requireNonNull(getIntent().getExtras().get("userKey")).toString();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserUid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        myPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToMyPostsActivity();
            }
        });

        /*
        other profiles
         */
        if (userKey != null && !userKey.equals(currentUserUid)) {
            recipeRef.orderByChild("uid").startAt(userKey).endAt(userKey + "\uf8ff").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        numOfRecipes = (int) dataSnapshot.getChildrenCount();
                        myPosts.setText(String.format("%s Recipes", String.valueOf(numOfRecipes)));
                    } else {
                        myPosts.setText("0 Recipes");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userKey);
            followButton.setVisibility(View.VISIBLE);
            unfollowButton.setVisibility(View.INVISIBLE);
            followRef.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (Objects.requireNonNull(snapshot.getValue()).toString().contains("Follower"))
                            numOfFollowers++;
                    }
                    myFollows.setText(String.format("%s Followers", String.valueOf(numOfFollowers)));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            followRef.child(currentUserUid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.child(userKey).child("Follow").getValue() == null) {
                            followButton.setVisibility(View.VISIBLE);
                            unfollowButton.setVisibility(View.INVISIBLE);
                        } else {
                            followButton.setVisibility(View.INVISIBLE);
                            unfollowButton.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        /*
        my profile
         */
        } else {
            recipeRef.orderByChild("uid").startAt(currentUserUid).endAt(currentUserUid + "\uf8ff").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        numOfRecipes = (int) dataSnapshot.getChildrenCount();
                        myPosts.setText(String.format("%s Recipes", String.valueOf(numOfRecipes)));
                    } else {
                        myPosts.setText("0 Recipes");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            followRef.child(currentUserUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (Objects.requireNonNull(snapshot.getValue()).toString().contains("Follower"))
                            numOfFollowers++;
                    }
                    myFollows.setText(String.format("%s Followers", String.valueOf(numOfFollowers)));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserUid);
            followButton.setVisibility(View.INVISIBLE);
            unfollowButton.setVisibility(View.INVISIBLE);

            myFollows.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendUserToFollowsActivity();
                }
            });
        }

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstname = Objects.requireNonNull(dataSnapshot.child("firstname").getValue()).toString();
                    String lastname = Objects.requireNonNull(dataSnapshot.child("lastname").getValue()).toString();
                    String profileImage = Objects.requireNonNull(dataSnapshot.child("profileimage").getValue()).toString();

                    fullname.setText(String.format("%s %s", firstname, lastname));
                    Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(profilePic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followButton.setVisibility(View.INVISIBLE);
                FollowPerson();
            }
        });

        unfollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followButton.setVisibility(View.VISIBLE);
                UnfollowPerson();
            }
        });

    }

    private void UnfollowPerson() {
        followRef.child(currentUserUid).child(userKey).child("Follow").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                followRef.child(userKey).child(currentUserUid).child("Follower").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        unfollowButton.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
    }

    private void FollowPerson() {
        followRef.child(currentUserUid).child(userKey).child("Follow").setValue(Boolean.TRUE).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    followRef.child(userKey).child(currentUserUid).child("Follower").setValue(Boolean.TRUE).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            unfollowButton.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
    }

    private void SendUserToFollowsActivity() {
        Intent followActivity = new Intent(ProfileActivity.this, FollowActivity.class);
        startActivity(followActivity);
    }

    private void SendUserToMyPostsActivity() {
        Intent myPostsActivity = new Intent(ProfileActivity.this, AllRecipesActivity.class);
        myPostsActivity.putExtra("userKey", userKey);
        startActivity(myPostsActivity);
    }
}
