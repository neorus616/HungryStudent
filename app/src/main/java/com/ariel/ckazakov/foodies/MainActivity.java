package com.ariel.ckazakov.foodies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ExpandableListView;

import com.ariel.ckazakov.models.Recipe;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Main activity(first page the user see's when open the app).
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference recipeRef, userRef, likesRef;

    private CircleImageView navProfileImage;
    private TextView navProfileUser;
    private String currentUserID;
    private Boolean likeChecker = Boolean.FALSE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        recipeRef = FirebaseDatabase.getInstance().getReference().child("Recipes");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        /*
            If user is not logged in, he limited to some functions
         */
        if (firebaseAuth.getCurrentUser() != null) {
            setContentView(R.layout.activity_main);
            currentUserID = firebaseAuth.getCurrentUser().getUid();
        } else {
            setContentView(R.layout.activity_main_guest);
            currentUserID = "";
        }
        toolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Home");

        drawerLayout = findViewById(R.id.drawable_layout);
        /*
            Navigation bar
         */
        navigationView = findViewById(R.id.navigation_view);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        navigationView.getMenu().setGroupVisible(R.id.TimeGroup, false);
        navigationView.getMenu().setGroupVisible(R.id.CostGroup, false);
        navProfileImage = navView.findViewById(R.id.profile_image);
        navProfileUser = navView.findViewById(R.id.username);
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

                        navProfileUser.setText(fullname);
                        Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(navProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
//        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);


        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(Boolean.FALSE);

        /*
            configurations for the recycle view
         */
        recyclerView = findViewById(R.id.all_users_post_list);
        recyclerView.setHasFixedSize(Boolean.TRUE);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(Boolean.TRUE);
        linearLayoutManager.setStackFromEnd(Boolean.TRUE);
        recyclerView.setLayoutManager(linearLayoutManager);

        navigationView.setNavigationItemSelectedListener(this);
        displayAllRecipes();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawable_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Show all recipes via recycle view.
     * Ordered by Likes or Dates(counter).
     */
    private void displayAllRecipes() {
        Query sortBy = recipeRef.orderByChild("counter");
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().get("content") != null) {
            if (Objects.requireNonNull(getIntent().getExtras().get("content")).toString().equals("new"))
                sortBy = recipeRef.orderByChild("counter");
            else if (Objects.requireNonNull(getIntent().getExtras().get("content")).toString().equals("cost"))
                if (Objects.requireNonNull(getIntent().getExtras().get("cost")).toString().equals("10"))
                    sortBy = recipeRef.orderByChild("cost").equalTo(10);
                else if (Objects.requireNonNull(getIntent().getExtras().get("cost")).toString().equals("20"))
                    sortBy = recipeRef.orderByChild("cost").equalTo(20);
                else sortBy = recipeRef.orderByChild("cost").equalTo(30);
            else if (Objects.requireNonNull(getIntent().getExtras().get("content")).toString().equals("time"))
                if (Objects.requireNonNull(getIntent().getExtras().get("time")).toString().equals("10"))
                    sortBy = recipeRef.orderByChild("cookingTime").equalTo(10);
                else if (Objects.requireNonNull(getIntent().getExtras().get("time")).toString().equals("20"))
                    sortBy = recipeRef.orderByChild("cookingTime").equalTo(20);
                else sortBy = recipeRef.orderByChild("cookingTime").equalTo(30);
            else sortBy = recipeRef.orderByChild("likes");
        }
        /*
            Firebase recycle view configurations for the recipes
         */
        FirebaseRecyclerOptions<Recipe> options = new FirebaseRecyclerOptions.Builder<Recipe>().setQuery(sortBy, Recipe.class).build();
        FirebaseRecyclerAdapter<Recipe, RecipeViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Recipe, RecipeViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RecipeViewHolder holder, int position, @NonNull Recipe model) {
                final String postKey = getRef(position).getKey();
                final String userKey = model.getUid();

                holder.setFullname(model.getFullName());
                holder.setTime(String.format(" %s", model.getTime()));
                holder.setDate(String.format(" %s", model.getDate()));
                holder.setTitle(model.getTitle());
                holder.setRecipeImage(model.getRecipeImage());
                holder.setProfileImage(model.getProfileImage());
                /*
                    If user is not logged in, he can't see the like and comments button
                 */
                if (!currentUserID.isEmpty())
                    holder.setLikeButtonStatus(postKey);
                else {
                    holder.likeRecipeButton.setVisibility(View.INVISIBLE);
                    holder.commentRecipeButton.setVisibility(View.INVISIBLE);
                }

                /*
                    If user click on profile image of the recipe, he redirected to the publisher profile page
                 */
                holder.user_post_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(MainActivity.this, ProfileActivity.class);
                        clickPostIntent.putExtra("userKey", userKey);
                        startActivity(clickPostIntent);
                    }
                });

                /*
                                if user click on recipe, it redirect him to the recipe page
                             */
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(MainActivity.this, FullRecipeActivity.class);
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
                        Intent commentsIntent = new Intent(MainActivity.this, CommentsActivity.class);
                        commentsIntent.putExtra("postKey", postKey);
                        startActivity(commentsIntent);
                    }
                });
            }

            @NonNull
            @Override
            public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_recipes_layout, parent, Boolean.FALSE);
                return new RecipeViewHolder(view);
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return Boolean.TRUE;
        return super.onOptionsItemSelected(item);
    }

    /**
     * Send the user to find recipes activity.
     */
    private void SendUserToFindRecipesActivity() {
        Intent findRecipesActivity = new Intent(MainActivity.this, FindRecipesActivity.class);
        startActivity(findRecipesActivity);
    }

    /**
     * Send the user to login activity(and finish this one).
     */
    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    /**
     * Send the user to main activity with 'new' recipes activity(and finish this one).
     */
    private void SendUserToMainActivityNew() {
        Intent mainActivity = new Intent(MainActivity.this, MainActivity.class);
        mainActivity.putExtra("content", "new");
        startActivity(mainActivity);
        finish();
    }

    /**
     * Send the user to main activity with 'hot' recipes activity(and finish this one).
     */
    private void SendUserToMainActivityHot() {
        Intent mainActivity = new Intent(MainActivity.this, MainActivity.class);
        mainActivity.putExtra("content", "hot");
        startActivity(mainActivity);
        finish();
    }

    /**
     * Send the user to main activity with 'cost' recipes activity(and finish this one).
     */
    private void SendUserToMainActivityCost(int cost) {
        Intent mainActivity = new Intent(MainActivity.this, MainActivity.class);
        mainActivity.putExtra("content", "cost");
        mainActivity.putExtra("cost", Integer.toString(cost));
        startActivity(mainActivity);
        finish();
    }

    /**
     * Send the user to main activity with 'cost' recipes activity(and finish this one).
     */
    private void SendUserToMainActivityTime(int time) {
        Intent mainActivity = new Intent(MainActivity.this, MainActivity.class);
        mainActivity.putExtra("content", "time");
        mainActivity.putExtra("time", Integer.toString(time));
        startActivity(mainActivity);
        finish();
    }

    /**
     * Send the user to profile setup activity(and finish this one).
     */
    private void SendUserToProfileSetupActivity() {
        Intent profileSetupIntent = new Intent(MainActivity.this, ProfileSetupActivity.class);
        profileSetupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(profileSetupIntent);
        finish();
    }

    /**
     * Send the user to follow activity.
     */
    private void SendUserToFollowsActivity() {
        Intent followActivity = new Intent(MainActivity.this, FollowActivity.class);
        startActivity(followActivity);
    }

    /**
     * Send the user to find friends activity.
     */
    private void SendUserToFindFriendsActivity() {
        Intent findFriendsActivity = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsActivity);
    }

    /**
     * Send the user to profile activity.
     */
    private void SendUserToProfileActivity() {
        Intent profileActivity = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(profileActivity);
    }

    /**
     * Send the user to settings activity.
     */
    private void SendUserToSettingsActivity() {
        Intent settingsActivity = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsActivity);
    }

    /**
     * Send the user to recipe activity.
     */
    private void SendUserToRecipeActivity() {
        Intent postActivity = new Intent(MainActivity.this, RecipeActivity.class);
        startActivity(postActivity);
    }

    /**
     * Send the user to register activity(and finish this one).
     */
    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        navigationView.getMenu().setGroupVisible(R.id.TimeGroup, false);
        navigationView.getMenu().setGroupVisible(R.id.CostGroup, false);


        switch (menuItem.getItemId()) {
            case R.id.nav_logout:
                firebaseAuth.signOut();
                SendUserToLoginActivity();
                break;
            case R.id.nav_post:
                SendUserToRecipeActivity();
                break;
            case R.id.nav_settings:
                SendUserToSettingsActivity();
                break;
            case R.id.nav_profile:
                SendUserToProfileActivity();
                break;
            case R.id.nav_find_friends:
                SendUserToFindFriendsActivity();
                break;
            case R.id.nav_follows:
                SendUserToFollowsActivity();
                break;
            case R.id.nav_login:
                SendUserToLoginActivity();
                break;
            case R.id.nav_register:
                SendUserToRegisterActivity();
                break;
            case R.id.nav_new:
                SendUserToMainActivityNew();
                break;
            case R.id.nav_hot:
                SendUserToMainActivityHot();
                break;
            case R.id.nav_cost:
                navigationView.getMenu().setGroupVisible(R.id.CostGroup, true);
                navigationView.getMenu().setGroupVisible(R.id.TimeGroup, false);
                return true;
            case R.id.nav_time:
                navigationView.getMenu().setGroupVisible(R.id.TimeGroup, true);
                navigationView.getMenu().setGroupVisible(R.id.CostGroup, false);
                return true;
            case R.id.nav_time10:
                SendUserToMainActivityTime(10);
                break;
            case R.id.nav_time20:
                SendUserToMainActivityTime(20);
                break;
            case R.id.nav_time30:
                SendUserToMainActivityTime(30);
                break;
            case R.id.nav_cost10:
                SendUserToMainActivityCost(10);
                break;
            case R.id.nav_cost20:
                SendUserToMainActivityCost(20);
                break;
            case R.id.nav_cost30:
                SendUserToMainActivityCost(30);
                break;
            case R.id.search:
                SendUserToFindRecipesActivity();
                break;
        }
        return Boolean.FALSE;
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
        TextView numOfComments;
        int countLikes;
        int countComments;
        String currentUserUid;
        DatabaseReference likesRef, recipeRef;

        RecipeViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            likeRecipeButton = view.findViewById(R.id.likeButton);
            commentRecipeButton = view.findViewById(R.id.commentButton);
            numOfLikes = view.findViewById(R.id.display_num_likes);
            numOfComments = view.findViewById(R.id.display_num_comments);
            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            recipeRef = FirebaseDatabase.getInstance().getReference().child("Recipes");
            /*
                if user connected, show the like counter, hide otherwise
             */
            if (FirebaseAuth.getInstance().getCurrentUser() != null)
                currentUserUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            else {
                numOfComments.setVisibility(View.INVISIBLE);
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

            recipeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    countComments = (int) dataSnapshot.child(postKey).child("Comments").getChildrenCount();
                    numOfComments.setText(String.format("%s Comments", String.valueOf(countComments)));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}