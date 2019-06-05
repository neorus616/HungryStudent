package com.ariel.ckazakov.foodies;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ariel.ckazakov.models.Comments;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

/**
 * Activity for comments section in the recipe
 */
public class CommentsActivity extends AppCompatActivity {

    private ImageButton postCommentButton;
    private EditText commentInput;
    private RecyclerView commentsList;

    private String postKey, currentUserUID;

    private DatabaseReference userRef, recipeRef;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserUID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postKey = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("postKey")).toString();
        recipeRef = FirebaseDatabase.getInstance().getReference().child("Recipes").child(postKey).child("Comments");

        commentInput = findViewById(R.id.comment_input);

        /*
            configurations for the recycle view
         */
        commentsList = findViewById(R.id.comment_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(Boolean.TRUE);
        linearLayoutManager.setStackFromEnd(Boolean.TRUE);
        commentsList.setLayoutManager(linearLayoutManager);

        postCommentButton = findViewById(R.id.post_comment_button);

        /*
            when ever an user click on the comment button, it sends his full name to a validation
            method.
         */
        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef.child(currentUserUID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String username = String.format("%s %s", Objects.requireNonNull(
                                    dataSnapshot.child("firstname").getValue()).toString(),
                                    Objects.requireNonNull(dataSnapshot.child("lastname").getValue()).toString());
                            ValidateComment(username);

                            commentInput.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*
            Firebase recycle view configurations for the comments
         */
        FirebaseRecyclerOptions<Comments> options = new FirebaseRecyclerOptions.Builder<Comments>().setQuery(recipeRef, Comments.class).build();
        FirebaseRecyclerAdapter<Comments, CommentsActivity.CommentsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comments, CommentsActivity.CommentsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentsActivity.CommentsViewHolder holder, int position, @NonNull Comments model) {
                holder.setUsername(model.getUsername());
                holder.setComment(model.getComment());
                holder.setDate(model.getDate());
                holder.setTime(model.getTime());
            }

            @NonNull
            @Override
            public CommentsActivity.CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_comments_layout, parent, false);
                return new CommentsActivity.CommentsViewHolder(view);
            }
        };
        commentsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    /**
     * validate that the comments isn't empty, and then saves it to the DB
     *
     * @param username - full name of the user
     */
    private void ValidateComment(String username) {
        if (commentInput.getText().toString().isEmpty())
            Toast.makeText(this, "You can't post empty comment!", Toast.LENGTH_SHORT).show();
        else {
            Calendar calendarDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy", Locale.US);
            final String saveCurrentDate = currentDate.format(calendarDate.getTime());
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm", Locale.US);
            final String saveCurrentTime = currentTime.format(calendarDate.getTime());
            /*
                for saving multiple comments in the same minute from same user
             */
            SimpleDateFormat currentTimeSec = new SimpleDateFormat("HH:mm:ss", Locale.US);
            final String saveCurrentTimeSec = currentTimeSec.format(calendarDate.getTime());

            final String rndKey = saveCurrentDate + saveCurrentTimeSec + currentUserUID;

            HashMap<String, Object> comments = new HashMap<>();
            comments.put("uid", currentUserUID);
            comments.put("comment", commentInput.getText().toString());
            comments.put("date", saveCurrentDate);
            comments.put("time", saveCurrentTime);
            comments.put("username", username);
            recipeRef.child(rndKey).updateChildren(comments).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful())
                        Toast.makeText(CommentsActivity.this, "Comment added successfully", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(CommentsActivity.this, "Error occurred, try again", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    /**
     * static class for Firebase recycler
     */
    public static class CommentsViewHolder extends RecyclerView.ViewHolder {
        View view;

        CommentsViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setComment(String comment) {
            TextView myComment = view.findViewById(R.id.comment_text);
            myComment.setText(comment);
        }

        public void setDate(String date) {
            TextView myDate = view.findViewById(R.id.comment_date);
            myDate.setText(String.format(" Date: %s", date));
        }

        public void setTime(String time) {
            TextView myTime = view.findViewById(R.id.comment_time);
            myTime.setText(String.format(" Time: %s", time));
        }

        public void setUsername(String username) {
            TextView myUsername = view.findViewById(R.id.comment_username);
            myUsername.setText(String.format("%s ", username));
        }
    }
}
