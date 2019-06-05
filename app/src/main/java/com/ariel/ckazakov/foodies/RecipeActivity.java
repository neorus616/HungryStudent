package com.ariel.ckazakov.foodies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Activity for post a new recipe.
 */
public class RecipeActivity extends AppCompatActivity {

    private ImageButton recipeImage;
    private Button button;
    private ToggleButton c10, c20, c30, t10, t20, t30;
    private EditText recipe, title, tips;

    private Toolbar toolbar;
    private ProgressDialog loadingBar;

    private StorageReference db;
    private DatabaseReference userRef, recipeRef;
    private FirebaseAuth firebaseAuth;
    private String downloadUrl, saveCurrentTime, saveCurrentDate;
    private long postCounter = 0;
    private Uri imageUri;

    private ListView listView;
    private EditText getValue;
    private ImageButton addButton;
    private List<String> listElementsArrayList;
    int finalCost;
    int finalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        listView = findViewById(R.id.ingredient_list);
        addButton = findViewById(R.id.button_ingredient);
        getValue = findViewById(R.id.add_ingredient);
        /*
            list the ingredients.
         */
        listElementsArrayList = new ArrayList<>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<>
                (RecipeActivity.this, android.R.layout.simple_list_item_1,
                        listElementsArrayList);

        listView.setAdapter(adapter);

        /*
            when user click on the + button, it add the ingredient to the list, and reset the input line.
         */
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                listElementsArrayList.add(getValue.getText().toString());
                adapter.notifyDataSetChanged();
                getValue.setText("");
            }
        });

        /*
            listener that help to scroll inside the list and not on the page itself.
         */
        listView.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }

        });

        /*
            when ever user click on ingredient in a list, it remove it.
         */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listElementsArrayList.remove(position);
                adapter.notifyDataSetChanged();
            }
        });


        recipeImage = findViewById(R.id.recipeImage);
        button = findViewById(R.id.createRecipe);
        c10 = findViewById(R.id.c10);
        c20 = findViewById(R.id.c20);
        c30 = findViewById(R.id.c30);
        t10 = findViewById(R.id.t10);
        t20 = findViewById(R.id.t20);
        t30 = findViewById(R.id.t30);
        recipe = findViewById(R.id.recipe);
        title = findViewById(R.id.title);
        tips = findViewById(R.id.tips);

        toolbar = findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(Boolean.TRUE);
        getSupportActionBar().setDisplayShowHomeEnabled(Boolean.TRUE);
        getSupportActionBar().setTitle("Update post");

        loadingBar = new ProgressDialog(this);

        db = FirebaseStorage.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        recipeRef = FirebaseDatabase.getInstance().getReference().child("Recipes");
        firebaseAuth = FirebaseAuth.getInstance();

        recipeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePic();
            }
        });

        t10.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    t20.setPressed(false);
                    t30.setPressed(false);
                    finalTime = 10;
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
                    finalTime = 20;
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
                    finalTime = 30;
                }
                return true;
            }
        });

        c10.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    c20.setPressed(false);
                    c30.setPressed(false);
                    finalCost = 10;
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
                    finalCost = 20;
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
                    finalCost = 30;
                }
                return true;
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateRecipe();
            }
        });
    }

    /**
     * Check if recipe contain image, recipe content, ingredients and title.
     */
    private void validateRecipe() {

        if (imageUri == null)
            Toast.makeText(this, "You must upload an image of the recipe", Toast.LENGTH_SHORT).show();
        else if (recipe.getText().toString().isEmpty())
            Toast.makeText(this, "You must write the recipe", Toast.LENGTH_SHORT).show();
        else if (listElementsArrayList.isEmpty())
            Toast.makeText(this, "You must add at least one ingredient", Toast.LENGTH_SHORT).show();
        else if (title.getText().toString().isEmpty())
            Toast.makeText(this, "You must write the title", Toast.LENGTH_SHORT).show();
        else if (!t10.isPressed() && !t20.isPressed() && !t30.isPressed())
            Toast.makeText(this, "You must choose time", Toast.LENGTH_SHORT).show();
        else if (!c10.isPressed() && !c20.isPressed() && !c30.isPressed())
            Toast.makeText(this, "You must choose cost", Toast.LENGTH_SHORT).show();
        else {
            if (tips.getText().toString().isEmpty())
                tips.setText("");
            loadingBar.setTitle("Posting recipe");
            loadingBar.setMessage("Please wait while we validate your recipe..");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(Boolean.TRUE);
            saveRecipePicToDB();
        }
    }

    /**
     * Saves the recipe image to the storage.
     */
    private void saveRecipePicToDB() {
        Calendar calendarDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy", Locale.US);
        saveCurrentDate = currentDate.format(calendarDate.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm", Locale.US);
        saveCurrentTime = currentTime.format(calendarDate.getTime());

        final StorageReference path = db.child("Recipe Images")
                .child(saveCurrentDate + saveCurrentTime + imageUri.getLastPathSegment() + ".jpg");
        path.putFile(imageUri).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                loadingBar.setMessage("Uploaded " + ((int) progress) + "%...");
            }
        }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return path.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    loadingBar.dismiss();
                    Uri downloadUri = task.getResult();
                    downloadUrl = Objects.requireNonNull(downloadUri).toString();
                    Toast.makeText(RecipeActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    SaveRecipeToDB();
                } else {
                    Toast.makeText(RecipeActivity.this, "upload failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Saves the recipe to the DB.
     */
    private void SaveRecipeToDB() {
        recipeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    postCounter = dataSnapshot.getChildrenCount();
                } else postCounter = 0;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        userRef.child(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String currentUserUid = firebaseAuth.getCurrentUser().getUid();
                    String userFullName = Objects.requireNonNull(dataSnapshot.child("firstname").getValue()).toString()
                            + " " + Objects.requireNonNull(dataSnapshot.child("lastname").getValue()).toString();
                    HashMap<String, Object> posts = new HashMap<>();
                    posts.put("uid", currentUserUid);
                    posts.put("recipeimage", downloadUrl);
                    posts.put("recipe", recipe.getText().toString());
                    posts.put("time", saveCurrentTime);
                    posts.put("date", saveCurrentDate);
                    posts.put("profileimage", Objects.requireNonNull(dataSnapshot.child("profileimage").getValue()).toString());
                    posts.put("fullname", userFullName);
                    posts.put("title", title.getText().toString());
                    posts.put("ingredients", listElementsArrayList);
                    posts.put("counter", postCounter);
                    posts.put("cost", finalCost);
                    posts.put("cookingTime", finalTime);
                    posts.put("tips", tips.getText().toString());
                    recipeRef.child(currentUserUid + "" + saveCurrentDate + "" + saveCurrentTime).updateChildren(posts).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                SendUserToMainActivity();
                                Toast.makeText(RecipeActivity.this, "Recipe updated successfully", Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(RecipeActivity.this, "Error occurred: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
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
     * Redirect user to gallery to choose an image for the recipe
     */
    private void choosePic() {
        Intent picIntent = new Intent();
        picIntent.setAction(Intent.ACTION_GET_CONTENT);
        picIntent.setType("image/*");
        startActivityForResult(picIntent, 1);
    }

    /**
     * After user pick an image, it set the image instead of the placeholder.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            recipeImage.setImageURI(imageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            SendUserToMainActivity();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Send the user to main activity.
     */
    private void SendUserToMainActivity() {
        Intent mainActivity = new Intent(RecipeActivity.this, MainActivity.class);
        startActivity(mainActivity);
    }
}