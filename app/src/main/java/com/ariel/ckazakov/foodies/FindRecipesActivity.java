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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import me.mvdw.recyclerviewmergeadapter.adapter.RecyclerViewMergeAdapter;

/**
 * Activity for searching recipes.
 */
public class FindRecipesActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton searchButton;
    private EditText searchInput;
    private RecyclerView searchResult;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference recipeRef, likesRef;
    private String currentUserID;
    private Boolean likeChecker = Boolean.FALSE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_recipes);

        firebaseAuth = FirebaseAuth.getInstance();
        recipeRef = FirebaseDatabase.getInstance().getReference().child("Recipes");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        currentUserID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        toolbar = findViewById(R.id.find_recipes_appbar_layout);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(Boolean.TRUE);
        getSupportActionBar().setTitle("Search recipes");

        searchButton = findViewById(R.id.search_recipes_button);
        searchInput = findViewById(R.id.search_box_input_recipes);
        searchResult = findViewById(R.id.search_result_recipes);
        /*
            configurations for the recycle view
         */
        searchResult.setHasFixedSize(Boolean.TRUE);
        searchResult.setLayoutManager(new LinearLayoutManager(this));

        /*
            when ever an user click on the 'Search' button, it sends his input to 'SearchRecipes' method
         */
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchRecipes(searchInput.getText().toString());
            }
        });

    }

    /**
     * split user input, and sorting from DB by title with input.
     * show results via recycle view
     *
     * @param searchIn - user input
     */
    private void SearchRecipes(String searchIn) {
        Toast.makeText(this, "Searching..", Toast.LENGTH_SHORT).show();
        String[] searches = searchIn.split(" ");
        RecyclerViewMergeAdapter mergeAdapter = new RecyclerViewMergeAdapter();
        for (String search : searches) {
            /*
                Firebase recycle view configurations for the recipes
             */
            Query searchRecipesQuery = recipeRef.orderByChild("title").startAt(search).endAt(search + "\uf8ff");
            FirebaseRecyclerOptions<Recipe> options = new FirebaseRecyclerOptions.Builder<Recipe>()
                    .setQuery(searchRecipesQuery, Recipe.class).build();
            FirebaseRecyclerAdapter<Recipe, RecipeViewHolder> firebaseRecyclerAdapter =
                    new FirebaseRecyclerAdapter<Recipe, RecipeViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull RecipeViewHolder holder, int position, @NonNull Recipe model) {
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
                                    Intent clickPostIntent = new Intent(FindRecipesActivity.this, FullRecipeActivity.class);
                                    clickPostIntent.putExtra("postKey", postKey);
                                    startActivity(clickPostIntent);
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
                                                if (dataSnapshot.child(Objects.requireNonNull(postKey)).hasChild(currentUserID)) {
                                                    likesRef.child(postKey).child(currentUserID).removeValue();
                                                    likeChecker = Boolean.FALSE;
                                                } else {
                                                    likesRef.child(postKey).child(currentUserID).setValue(Boolean.TRUE);
                                                    likeChecker = Boolean.FALSE;
                                                }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                                }
                            });

                            /*
                                if user click on comment image, it redirect him to the recipe comments page
                             */
                            holder.commentRecipeButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent commentsIntent = new Intent(FindRecipesActivity.this, CommentsActivity.class);
                                    commentsIntent.putExtra("postKey", postKey);
                                    startActivity(commentsIntent);
                                }
                            });
                        }

                        @NonNull
                        @Override
                        public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_recipes_layout, parent, false);
                            return new RecipeViewHolder(view);
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
    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        View view;

        TextView fullname, date, time, title;
        CircleImageView user_post_image;
        ImageView post_image;

        ImageButton likeRecipeButton, commentRecipeButton;
        TextView numOfLikes;
        int countLikes;
        String currentUserUid;
        DatabaseReference likesRef, recipeRef;

        RecipeViewHolder(View itemView) {
            super(itemView);

            view = itemView;

            likeRecipeButton = view.findViewById(R.id.likeButton);
            commentRecipeButton = view.findViewById(R.id.commentButton);
            numOfLikes = view.findViewById(R.id.display_num_likes);
            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            recipeRef = FirebaseDatabase.getInstance().getReference().child("Recipes");
            /*
                if user connected, show the like counter, hide otherwise
             */
            if (FirebaseAuth.getInstance().getCurrentUser() != null)
                currentUserUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            else {
                numOfLikes.setVisibility(View.INVISIBLE);
                currentUserUid = "";
            }

            fullname = view.findViewById(R.id.post_username);
            date = view.findViewById(R.id.post_date);
            time = view.findViewById(R.id.post_time);
            title = view.findViewById(R.id.post_title);
            post_image = view.findViewById(R.id.post_image);
            user_post_image = view.findViewById(R.id.post_profile_image);
        }

        void setFullname(String fullname) {
            TextView username = itemView.findViewById(R.id.post_username);
            username.setText(fullname);
        }

        void setTime(String time) {
            TextView recipeTime = itemView.findViewById(R.id.post_time);
            recipeTime.setText(time);
        }

        void setDate(String date) {
            TextView recipeDate = itemView.findViewById(R.id.post_date);
            recipeDate.setText(date);
        }

        void setTitle(String title) {
            TextView titlePost = itemView.findViewById(R.id.post_title);
            titlePost.setText(title);
        }

        void setProfileImage(String profileimage) {
            CircleImageView image = itemView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(image);
        }

        void setRecipeImage(String recipeimage) {
            ImageView recipeImage = itemView.findViewById(R.id.post_image);
            Picasso.get().load(recipeimage).placeholder(R.drawable.add_post_high).into(recipeImage);
        }

        /**
         * change the icon whenever the user likes the post or not, and show the amount of likes
         * the recipe has.
         *
         * @param postKey - uid of the recipe
         */
        void setLikeButtonStatus(final String postKey) {
            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(postKey).hasChild(currentUserUid)) {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likeRecipeButton.setImageResource(R.drawable.like);
                        numOfLikes.setText(String.format("%s Likes", String.valueOf(countLikes)));
                        recipeRef.child(postKey).child("likes").setValue(countLikes);
                    } else {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likeRecipeButton.setImageResource(R.drawable.dislike);
                        numOfLikes.setText(String.format("%s Likes", String.valueOf(countLikes)));
                        recipeRef.child(postKey).child("likes").setValue(countLikes);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
