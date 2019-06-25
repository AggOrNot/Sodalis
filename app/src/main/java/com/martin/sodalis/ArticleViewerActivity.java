package com.martin.sodalis;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ArticleViewerActivity extends AppCompatActivity {

    private ImageView exit;

    private TextView contentHeadline;
    private TextView contentText1;
    private TextView contentText2;
    private TextView contentText3;
    private TextView contentText4;
    private TextView contentText5;
    private TextView exitText;

    private String userId;
    private String userSceneId;

    private DatabaseReference databaseReference;

    private static final String articleContent = "articleContent";
    private static final String TAG = "ArticleViewerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article_viewer);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        userId = getUid();

        if ((userId = getUid()) != null) {

            databaseReference.child("users").child(userId).child("userSceneId")
                    .addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {
                                userSceneId = dataSnapshot.getValue().toString();

                                getArticleText();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }

        exit = findViewById(R.id.exit);
        exitText = findViewById(R.id.exit_text);

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        exitText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        contentHeadline = findViewById(R.id.content_headline);
        contentText1 = findViewById(R.id.content_text1);
        contentText2 = findViewById(R.id.content_text2);
        contentText3 = findViewById(R.id.content_text3);
        contentText4 = findViewById(R.id.content_text4);
        contentText5 = findViewById(R.id.content_text5);
    } // end of oncreate

    private void getArticleText() {
        databaseReference.child("act1").child("testIntro_CompanionText").child(userSceneId)
                .child(articleContent).child("articleHeadline")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            contentHeadline.setText(dataSnapshot.getValue().toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        databaseReference.child("act1").child("testIntro_CompanionText").child(userSceneId)
                .child(articleContent).child("articleText1")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            contentText1.setText(dataSnapshot.getValue().toString());

                            Log.i(TAG, "Article text 1 exists");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        databaseReference.child("act1").child("testIntro_CompanionText").child(userSceneId)
                .child(articleContent).child("articleText2")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            contentText2.setText(dataSnapshot.getValue().toString());

                            Log.i(TAG, "Article text 2 exists");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        databaseReference.child("act1").child("testIntro_CompanionText").child(userSceneId)
                .child(articleContent).child("articleText3")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            contentText3.setText(dataSnapshot.getValue().toString());

                            Log.i(TAG, "Article text 3 exists");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        databaseReference.child("act1").child("testIntro_CompanionText").child(userSceneId)
                .child(articleContent).child("articleText4")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            contentText4.setText(dataSnapshot.getValue().toString());

                            Log.i(TAG, "Article text 4 exists");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        databaseReference.child("act1").child("testIntro_CompanionText").child(userSceneId)
                .child(articleContent).child("articleText5")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            contentText5.setText(dataSnapshot.getValue().toString());

                            Log.i(TAG, "Article text 5 exists");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
