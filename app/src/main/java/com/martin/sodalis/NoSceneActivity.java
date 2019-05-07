package com.martin.sodalis;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NoSceneActivity extends AppCompatActivity {

    private Button doneButton;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabaseRef;

    private String userId;
    private String userSceneId;
    private String lastKeySceneId;

    private static final String TAG = "NoSceneActivity";

    /**
     * activity to be used when there's no scene written in the db. Prevents crashes and catches user
     * so they don't just have 'sodalis has stopped'. In theory should send them to their latest key
     * scene in the db. Sort of works for now :/
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_scene);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        MainActivity mainActivity = new MainActivity();
        userId = mainActivity.getUid();

        doneButton = findViewById(R.id.noscene_button);

        doneButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                readWriteLastKeySceneId();
            }
        });

    } // end of oncreate

    // gets user's last 'key' scene from db. Need to decide how often key scenes appear at all also
    public void readWriteLastKeySceneId() {

        mDatabaseRef.child("users").child(userId).child("lastKeySceneId")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {

                                    lastKeySceneId = dataSnapshot.getValue().toString();

                                    Log.i("readLastKeyScene", "User's last key scene: "
                                            + lastKeySceneId);

                                    setUserSceneIdFromKeyScene(lastKeySceneId);

                                    // seems to kind of work...for now
                                    PracticeFragment practiceFragment = new PracticeFragment();
                                    practiceFragment.getCompanionText();
                                    practiceFragment.getUserReplyA();
                                    practiceFragment.getUserReplyB();
                                    practiceFragment.getUserReplyC();
                                    practiceFragment.getUserReplyD();

                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );
    }

    public void setUserSceneIdFromKeyScene(String newScene) {

        mDatabaseRef.child("users")
                .child(userId)
                .child("userSceneId").setValue(newScene);
    }
}
