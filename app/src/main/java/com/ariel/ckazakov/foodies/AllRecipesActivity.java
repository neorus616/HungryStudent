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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ariel.ckazakov.models.Recipe;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Activity to show all recipes of an user.
 */
public class AllRecipesActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private RecyclerView recipeList;

    private DatabaseReference recipeRef, userRef, likesRef;
    private FirebaseAuth firebaseAuth;
    private String currentUid, userKey;
    private Query query;
    private Boolean likeChecker = Boolean.FALSE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_recipes);

        firebaseAuth = FirebaseAuth.getInstance();
        recipeRef = FirebaseDatabase.getInstance().getReference().child("Recipes");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        currentUid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        /*
            If user clicked on his posts, or other user posts
         */
        if (getIntent().getExtras() != null && getIntent().getExtras().get("userKey") != null) {
            userKey = Objects.requireNonNull(getIntent().getExtras().get("userKey")).toString();
            query = recipeRef.orderByChild("uid").startAt(userKey).endAt(userKey + "\uf8ff");
        } else
            query = recipeRef.orderByChild("uid").startAt(currentUid).endAt(currentUid + "\uf8ff");

        toolbar = findViewById(R.id.my_posts_bar_layout);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(Boolean.TRUE);
        getSupportActionBar().setDisplayShowHomeEnabled(Boolean.TRUE);
        if (userKey == null)
            getSupportActionBar().setTitle("My Recipes");
        else
            getSupportActionBar().setTitle("Recipes");


        recipeList = findViewById(R.id.all_recipes_list);
        /*
            configurations for the recycle view
         */
        recipeList.setHasFixedSize(Boolean.TRUE);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(Boolean.TRUE);
        linearLayoutManager.setStackFromEnd(Boolean.TRUE);
        recipeList.setLayoutManager(linearLayoutManager);

        DisplayAllRecipes();
    }

    /**
     * Show all recipes of an user via recycle view.
     */
    private void DisplayAllRecipes() {
        /*
            Firebase recycle view configurations for the recipes
         */
        FirebaseRecyclerOptions<Recipe> options = new FirebaseRecyclerOptions.Builder<Recipe>().setQuery(query, Recipe.class).build();
        FirebaseRecyclerAdapter<Recipe, MyPostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Recipe, MyPostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull AllRecipesActivity.MyPostsViewHolder holder, int position, @NonNull Recipe model) {
                final String postKey = getRef(position).getKey();

                holder.setFullname(model.getFullName());
                holder.setTime(String.format(" %s", model.getTime()));
                holder.setDate(String.format(" %s", model.getDate()));
                holder.setTitle(model.getTitle());
                holder.setRecipeImage(model.getRecipeImage());
                holder.setProfileImage(model.getProfileImage());

                holder.setLikeButtonStatus(postKey);

                /*
                    if user click on recipe, it redirect him to the recipe page
                */
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(AllRecipesActivity.this, FullRecipeActivity.class);
                        clickPostIntent.putExtra("postKey", postKey);
                        startActivity(clickPostIntent);
                    }
                });

                /*
                    if user click on comment image, it redirect him to the recipe comments page
                */
                holder.commentRecipeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentsIntent = new Intent(AllRecipesActivity.this, CommentsActivity.class);
                        commentsIntent.putExtra("postKey", postKey);
                        startActivity(commentsIntent);
                    }
                });

                /*
                    if user click on like\dislike button, it remove the like if he likes it already,
                    and add the like otherwise
                */
                holder.likeRecipeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        likeChecker = Boolean.TRUE;
                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (likeChecker)
                                    if (dataSnapshot.child(Objects.requireNonNull(postKey)).hasChild(currentUid)) {
                                        likesRef.child(postKey).child(currentUid).removeValue();
                                        likeChecker = Boolean.FALSE;
                                    } else {
                                        likesRef.child(postKey).child(currentUid).setValue(Boolean.TRUE);
                                        likeChecker = Boolean.FALSE;
                                    }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }

            @NonNull
            @Override
            public AllRecipesActivity.MyPostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_recipes_layout, parent, false);
                return new AllRecipesActivity.MyPostsViewHolder(view);
            }
        };
        recipeList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    /**
     * static class for Firebase recycler
     */
    public static class MyPostsViewHolder extends RecyclerView.ViewHolder {
        View view;

        ImageButton likeRecipeButton, commentRecipeButton;
        TextView numOfLikes;
        TextView numOfComments;
        int countLikes;
        int countComments;
        String currentUserUid;
        DatabaseReference likesRef;
        DatabaseReference postsRef;

        public MyPostsViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;

            likeRecipeButton = view.findViewById(R.id.likeButton);
            commentRecipeButton = view.findViewById(R.id.commentButton);
            numOfLikes = view.findViewById(R.id.display_num_likes);
            numOfComments = view.findViewById(R.id.display_num_comments);
            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            postsRef = FirebaseDatabase.getInstance().getReference().child("Recipes");
            currentUserUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        }

        /**
         * change the icon whenever the user likes the post or not, and show the amount of likes
         * the recipe has.
         *
         * @param postKey - uid of the recipe
         */
        public void setLikeButtonStatus(final String postKey) {
            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(postKey).hasChild(currentUserUid)) {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likeRecipeButton.setImageResource(R.drawable.like);
                        numOfLikes.setText(String.format("%s Likes", String.valueOf(countLikes)));
                    } else {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likeRecipeButton.setImageResource(R.drawable.dislike);
                        numOfLikes.setText(String.format("%s Likes", String.valueOf(countLikes)));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            postsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        countComments = (int) dataSnapshot.child(postKey).child("Comments").getChildrenCount();
                        numOfComments.setText(String.format("%s Comments", String.valueOf(countComments + 1)));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public void setFullname(String fullname) {
            TextView username = itemView.findViewById(R.id.post_username);
            username.setText(fullname);
        }

        public void setTime(String time) {
            TextView recipeTime = itemView.findViewById(R.id.post_time);
            recipeTime.setText(time);
        }

        public void setDate(String date) {
            TextView recipeDate = itemView.findViewById(R.id.post_date);
            recipeDate.setText(date);
        }

        public void setTitle(String title) {
            TextView titlePost = itemView.findViewById(R.id.post_title);
            titlePost.setText(title);
        }

        public void setProfileImage(String profileimage) {
            CircleImageView image = itemView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(image);
        }

        public void setRecipeImage(String recipeimage) {
            ImageView recipeImage = itemView.findViewById(R.id.post_image);
            Picasso.get().load(recipeimage).placeholder(R.drawable.add_post_high).into(recipeImage);
        }
    }
}