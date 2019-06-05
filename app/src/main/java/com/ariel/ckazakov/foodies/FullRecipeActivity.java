package com.ariel.ckazakov.foodies;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

/**
 * Activity for displaying the recipe.
 */
public class FullRecipeActivity extends AppCompatActivity {

    private ImageView fullRecipeImage;
    private TextView fullRecipe, listIngredients, fullTips, fullCost, fullTime;
    private Button deleteButton, editButton;

    private String postKey, currentUserUid, dbUserUid, recipe, image, tips;
    private int cost, cookingTime;
    private List<String> listElementsArrayList;

    private DatabaseReference fullrecipedb, adminRef;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_recipe);

        /*
            when the user was redirected from another activity, it passed the uid of the recipe
         */
        postKey = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("postKey")).toString();
        fullrecipedb = FirebaseDatabase.getInstance().getReference().child("Recipes").child(postKey);
        firebaseAuth = FirebaseAuth.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            currentUserUid = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        else
            currentUserUid = "null";
        adminRef = FirebaseDatabase.getInstance().getReference().child("Admins");

        fullRecipeImage = findViewById(R.id.fullRecipeImage);
        fullRecipe = findViewById(R.id.fullRecipe);
        fullTips = findViewById(R.id.fullTips);
        fullCost = findViewById(R.id.fullCost);
        fullTime = findViewById(R.id.fullTime);
        listIngredients = findViewById(R.id.listFullIngredients);
        /*
            Set all buttons invisible at first
         */
        deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setVisibility(View.INVISIBLE);
        editButton = findViewById(R.id.editButton);
        editButton.setVisibility(View.INVISIBLE);

        /*
            Organize the recipe post.
         */
        fullrecipedb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    recipe = Objects.requireNonNull(dataSnapshot.child("recipe").getValue()).toString();
                    tips = Objects.requireNonNull(dataSnapshot.child("tips").getValue()).toString();
                    cost = Integer.valueOf(Objects.requireNonNull(dataSnapshot.child("cost").getValue()).toString());
                    cookingTime = Integer.valueOf(Objects.requireNonNull(dataSnapshot.child("cookingTime").getValue()).toString());
                    image = Objects.requireNonNull(dataSnapshot.child("recipeimage").getValue()).toString();
                    dbUserUid = Objects.requireNonNull(dataSnapshot.child("uid").getValue()).toString();
                    /*
                        Using genericType to grab a list of the ingredients.
                     */
                    GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>() {
                    };
                    listElementsArrayList = dataSnapshot.child("ingredients").getValue(genericTypeIndicator);

                    StringBuilder ingredients = new StringBuilder();
                    if (listElementsArrayList != null) {
                        for (int i = 0; i < listElementsArrayList.size() - 1; i++)
                            ingredients.append(listElementsArrayList.get(i)).append("\n");
                        ingredients.append(listElementsArrayList.get(listElementsArrayList.size() - 1));
                    }
                    listIngredients.setText(ingredients);
                    fullRecipe.setText(recipe);
                    fullTips.setText(tips);
                    fullTime.setText(String.valueOf(cookingTime));
                    fullCost.setText(String.valueOf(cost));
                    Picasso.get().load(image).into(fullRecipeImage);

                    /*
                        If the current user is admin, he can edit/delete the recipe.
                     */
                    adminRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child(currentUserUid).exists()) {
                                deleteButton.setVisibility(View.VISIBLE);
                                editButton.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    /*
                        If the current user is the one who posted it, he can edit/delete the recipe.
                     */
                    if (currentUserUid.equals(dbUserUid)) {
                        deleteButton.setVisibility(View.VISIBLE);
                        editButton.setVisibility(View.VISIBLE);
                    }

                    /*
                        When user click on "Edit" button, it send the 'recipe' to 'EditPost' method.
                     */
                    editButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditPost(recipe);
                        }
                    });

                    /*
                        When user click on "Edit" button, it send the 'recipe' to 'EditPost' method.
                    */
                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DeletePost();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    /**
     * takes the current recipe content, and open a view to the user that allow him to edit it.
     *
     * @param recipe - content of the recipe
     */
    private void EditPost(String recipe) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FullRecipeActivity.this);
        builder.setTitle("Edit Post:");

        final EditText newRecipe = new EditText(FullRecipeActivity.this);
        newRecipe.setText(recipe);
        builder.setView(newRecipe);

        /*
            Update button(replace old with new recipe content)
         */
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fullrecipedb.child("recipe").setValue(newRecipe.getText().toString());
                Toast.makeText(FullRecipeActivity.this, "Recipe Updated!", Toast.LENGTH_SHORT).show();
            }
        });
        /*
            Cancel button.
         */
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.holo_blue_dark);
    }

    /**
     * Remove the recipe from DB.
     */
    private void DeletePost() {
        fullrecipedb.removeValue();
        SendUserToMainActivity();
        Toast.makeText(this, "Post has been deleted", Toast.LENGTH_SHORT).show();
    }

    /**
     * Send the user to main activity.
     */
    private void SendUserToMainActivity() {
        Intent mainActivity = new Intent(FullRecipeActivity.this, MainActivity.class);
        startActivity(mainActivity);
    }
}
