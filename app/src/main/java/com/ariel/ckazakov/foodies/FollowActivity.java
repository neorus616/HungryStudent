package com.ariel.ckazakov.foodies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ariel.ckazakov.models.Follow;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Activity for displaying all user follow's.
 */
public class FollowActivity extends AppCompatActivity {

    private RecyclerView myFollowList;
    private DatabaseReference followRef, userRef;
    private FirebaseAuth firebaseAuth;
    private String currentUserUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserUid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        followRef = FirebaseDatabase.getInstance().getReference().child("Follows").child(currentUserUid);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        myFollowList = findViewById(R.id.follow_list);
        /*
            configurations for the recycle view
         */
        myFollowList.setHasFixedSize(Boolean.TRUE);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(Boolean.TRUE);
        linearLayoutManager.setStackFromEnd(Boolean.TRUE);
        myFollowList.setLayoutManager(linearLayoutManager);

        DisplayAllFollows();
    }

    /**
     * Sorting all follows of current user and displaying them
     */
    private void DisplayAllFollows() {
        /*
            Firebase recycle view configurations for the users
        */
        FirebaseRecyclerOptions<Follow> options = new FirebaseRecyclerOptions.Builder<Follow>().setQuery(followRef, Follow.class).build();
        FirebaseRecyclerAdapter<Follow, FollowActivity.FollowViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Follow, FollowActivity.FollowViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FollowActivity.FollowViewHolder holder, int position, @NonNull final Follow model) {
                final String usersUid = getRef(position).getKey();

                userRef.child(Objects.requireNonNull(usersUid)).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            final String userName = Objects.requireNonNull(dataSnapshot.child("firstname").getValue()).toString() +
                                    " " + Objects.requireNonNull(dataSnapshot.child("lastname").getValue()).toString();
                            final String profileImage = Objects.requireNonNull(dataSnapshot.child("profileimage").getValue()).toString();
                            holder.setFullname(userName);
                            holder.setProfileimage(profileImage);

                            /*
                                if user click on profile image, it redirect him to the user profile page
                             */
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent clickPostIntent = new Intent(FollowActivity.this, ProfileActivity.class);
                                    clickPostIntent.putExtra("userKey", usersUid);
                                    startActivity(clickPostIntent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public FollowActivity.FollowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_layout, parent, false);
                return new FollowActivity.FollowViewHolder(view);
            }
        };
        myFollowList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    /**
     * static class for Firebase recycler
     */
    public static class FollowViewHolder extends RecyclerView.ViewHolder {
        View view;

        FollowViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        void setProfileimage(String profileimage) {
            CircleImageView myImage = view.findViewById(R.id.all_users_profile_image);
            Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(myImage);
        }

        void setFullname(String fullname) {
            TextView myFullname = view.findViewById(R.id.user_fullname);
            myFullname.setText(fullname);
        }

        public void setFollow(String follow) {

        }
    }
}
