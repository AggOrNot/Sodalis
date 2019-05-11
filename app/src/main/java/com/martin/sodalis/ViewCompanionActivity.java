package com.martin.sodalis;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yqritc.scalablevideoview.ScalableVideoView;

import java.io.IOException;

public class ViewCompanionActivity extends AppCompatActivity {

    private Button changeAppearanceButton;

    private TextView nameTextview;

    private ImageView backButton;

    private String userId;
    private String companionNameMain;
    private String appearanceFinal;

    private Uri uriParsed;

    private ScalableVideoView scalableVideoView;

    private ProgressBar videoProgressBar;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;

    private static final String TAG = "ViewCompanionActivity";

    /**
     * activity that reads user's Companion appearance from their data node and displays it. User can
     * go to their appearance and outfit selection screens from here if they want to change their
     * Companion's appearance. Plays local videos for now, but is equipped to read correct videos
     * from db once architecture is finished.
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_companion);

        // screen and status bar modifiers
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        // initialize firebase instances
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        // initialize progress bar and make visible until video is loaded (infinite bar)
        videoProgressBar = findViewById(R.id.videoview_bar);
        videoProgressBar.setVisibility(View.VISIBLE);

        // initialize video view and make INvisible until video is loaded and ready to play
        scalableVideoView = findViewById(R.id.video_view);
        scalableVideoView.setVisibility(View.GONE);

        // initialize and read user's Companion name from db and display. Hide until loaded
        nameTextview = findViewById(R.id.companion_name);
        nameTextview.setVisibility(View.GONE);

        if ((userId = getUid()) != null) {

            mDatabaseRef.child("users").child(userId).child("companionName")
                    .addValueEventListener(

                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    companionNameMain = dataSnapshot.getValue().toString();

                                    Log.i(TAG, "Companion name is: " +
                                            companionNameMain);

                                    nameTextview.setText(companionNameMain);
                                    nameTextview.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            }
                    );

            // read user's Companion Appearance to be used to load video from correct storage location
            mDatabaseRef.child("users").child(userId).child("appearanceFinal")
                    .addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.exists()) {

                                        appearanceFinal = dataSnapshot.getValue().toString();

                                        Log.i(TAG, "Appearance final is: " + appearanceFinal);

                                        // might eventually play video view here
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            }
                    );

        } else {
            // show sodalis I guess? I can probably come up with something better at some point
            nameTextview.setVisibility(View.VISIBLE);
        }

        // play video
        playVideoLocal();

        // actual video view to be used later
        /*try {
            playVideoView();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        // initialize and set listener for changing user's Companion appearance
        changeAppearanceButton = findViewById(R.id.change_appearance_button);

        changeAppearanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // this will eventually be replaced with intent to appearance changes. Need to come
                // up with an alternative to a 'closet'. But same idea.
                scalableVideoView.stop();
                scalableVideoView.release();
                finish();
            }
        });

        // back button
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scalableVideoView.stop();
                scalableVideoView.release();
                finish();
            }
        });

    } // end of oncreate

    // temporary method for playing local video file so I don't burn through all my firebase data
    private void playVideoLocal() {

        try {
            scalableVideoView.setRawData(R.raw.boat);

            scalableVideoView.prepareAsync(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.i("viewCompanion", "Video view is prepared");
                    //Log.i("videoView3", "Uri used is: " + uriParsed.toString());

                    scalableVideoView.setVisibility(View.VISIBLE);
                    videoProgressBar.setVisibility(View.GONE);

                    scalableVideoView.setVolume(0,0);
                    scalableVideoView.setLooping(true);

                    scalableVideoView.start();

                    if (scalableVideoView.isPlaying()) {
                        videoProgressBar.setVisibility(View.GONE);
                    }
                }
            });

        } catch (IOException ioe) {
            //handle error
        }
    }

    // TODO: read video and storage location from user's node and parse to prepare for video player

    // actual method for playing the video
    private void playVideoView() throws IOException {

        Log.i(TAG, "Video view is doing something");

        uriParsed = Uri.parse("https://firebasestorage.googleapis.com/v0/b/sodalis-53c9d.appspot.com/o/venice.mp4?alt=media&token=152d3535-e0f5-48ca-8737-2fb2c863858b");

        scalableVideoView.setDataSource(getApplicationContext(), uriParsed);

        scalableVideoView.prepareAsync(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.i(TAG, "Video view is prepared");
                Log.i(TAG, "Uri used is: " + uriParsed.toString());

                scalableVideoView.setVisibility(View.VISIBLE);
                videoProgressBar.setVisibility(View.GONE);

                scalableVideoView.start();
                scalableVideoView.setVolume(0,0);
                scalableVideoView.setLooping(true);

                // make sure progress bar is hidden once video starts. Redundancy just in case idk!
                if (scalableVideoView.isPlaying()) {
                    videoProgressBar.setVisibility(View.GONE);
                }
            }
        });
    } // end of videoview

    // get userid from db
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
