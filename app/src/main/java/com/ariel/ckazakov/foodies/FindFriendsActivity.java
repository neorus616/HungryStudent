package com.ariel.ckazakov.foodies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ariel.ckazakov.models.Profiles;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import me.mvdw.recyclerviewmergeadapter.adapter.RecyclerViewMergeAdapter;

/**
 * Activity for searching other users.
 */
public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private ImageButton searchButton;
    private EditText searchInput;
    private RecyclerView searchResult;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        toolbar = findViewById(R.id.find_friends_appbar_layout);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(Boolean.TRUE);
        getSupportActionBar().setTitle("Search for people and Friends");

        searchButton = findViewById(R.id.search_friends_button);
        searchInput = findViewById(R.id.search_box_input);
        searchResult = findViewById(R.id.search_result);
        /*
            configurations for the recycle view
         */
        searchResult.setHasFixedSize(Boolean.TRUE);
        searchResult.setLayoutManager(new LinearLayoutManager(this));

        /*
            when ever an user click on the 'Search' button, it sends his input to 'SearchFriends' method
         */
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchFriends(searchInput.getText().toString());
            }
        });
    }

    /**
     * split user input, and sorting from DB by first or last name with input.
     * show results via recycle view
     *
     * @param searchIn - user input
     */
    private void SearchFriends(String searchIn) {
        Toast.makeText(this, "Searching..", Toast.LENGTH_SHORT).show();
        searchIn = searchIn.toLowerCase();
        String[] searches = searchIn.split(" ");
        RecyclerViewMergeAdapter mergeAdapter = new RecyclerViewMergeAdapter();
        for (String search : searches) {
            /*
            Firebase recycle view configurations for the users
            */
            Query searchFriendsQuery = userRef.orderByChild("firstname").startAt(search).endAt(search + "\uf8ff");
            FirebaseRecyclerOptions<Profiles> options = new FirebaseRecyclerOptions.Builder<Profiles>().setQuery(searchFriendsQuery, Profiles.class).build();
            FirebaseRecyclerAdapter<Profiles, FindFriendsActivity.FindFriendsViewHolder> firebaseRecyclerAdapter =
                    new FirebaseRecyclerAdapter<Profiles, FindFriendsActivity.FindFriendsViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull FindFriendsActivity.FindFriendsViewHolder holder, int position, @NonNull Profiles model) {
                            final String userKey = getRef(position).getKey();

                            holder.setFullname(String.format("%s %s", model.getFirstname(), model.getLastname()));
                            holder.setProfileimage(model.getProfileimage());

                            /*
                                if user click on profile, it redirect him to the user profile page
                             */
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent clickPostIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                                    clickPostIntent.putExtra("userKey", userKey);
                                    startActivity(clickPostIntent);
                                }
                            });
                        }

                        @NonNull
                        @Override
                        public FindFriendsActivity.FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_layout, parent, false);
                            return new FindFriendsActivity.FindFriendsViewHolder(view);
                        }
                    };
            mergeAdapter.addAdapter(firebaseRecyclerAdapter);
            firebaseRecyclerAdapter.startListening();
        }
        /*
            look for all users when even user writes an empty string
         */
        if (!searchIn.isEmpty())
            for (String search : searches) {
                /*
                Firebase recycle view configurations for the users
                */
                Query searchFriendsQuery = userRef.orderByChild("lastname").startAt(search).endAt(search + "\uf8ff");
                FirebaseRecyclerOptions<Profiles> options = new FirebaseRecyclerOptions.Builder<Profiles>().setQuery(searchFriendsQuery, Profiles.class).build();
                FirebaseRecyclerAdapter<Profiles, FindFriendsActivity.FindFriendsViewHolder> firebaseRecyclerAdapter =
                        new FirebaseRecyclerAdapter<Profiles, FindFriendsActivity.FindFriendsViewHolder>(options) {
                            @Override
                            protected void onBindViewHolder(@NonNull FindFriendsActivity.FindFriendsViewHolder holder, int position, @NonNull Profiles model) {
                                final String userKey = getRef(position).getKey();

                                holder.setFullname(String.format("%s %s", model.getFirstname(), model.getLastname()));
                                holder.setProfileimage(model.getProfileimage());

                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent clickPostIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                                        clickPostIntent.putExtra("userKey", userKey);
                                        startActivity(clickPostIntent);
                                    }
                                });
                            }

                            @NonNull
                            @Override
                            public FindFriendsActivity.FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_layout, parent, false);
                                return new FindFriendsActivity.FindFriendsViewHolder(view);
                            }
                        };
                mergeAdapter.addAdapter(firebaseRecyclerAdapter);
                firebaseRecyclerAdapter.startListening();
            }
        searchResult.setAdapter(mergeAdapter);

    }

    /**
     * static class for Firebase recycler
     */
    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder {
        View view;

        FindFriendsViewHolder(@NonNull View itemView) {
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
    }
}
