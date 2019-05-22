package com.martin.sodalis;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yqritc.scalablevideoview.ScalableVideoView;

import java.io.IOException;

public class ViewCompanionCloseFragment extends Fragment {

    private String userId;
    private String appearanceBase;

    private Uri uriParsed;

    private ScalableVideoView scalableVideoView;

    private ProgressBar videoProgressBar;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;
    private FirebaseStorage firebaseStorage;

    private static final String TAG = "ViewCloseFragment";

    /**
     * uses appearanceBase in db to show close up of Companion's appearance (appearanceBase). I
     * think this should be the default for now.
     *
     */

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View viewClose = inflater.inflate(R.layout.fragment_view_companion_close, container, false);

        // initialize firebase instances
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();

        // initialize progress bar and make visible until video is loaded (infinite bar)
        videoProgressBar = viewClose.findViewById(R.id.videoview_bar);
        videoProgressBar.setVisibility(View.VISIBLE);

        // initialize video view and make INvisible until video is loaded and ready to play
        scalableVideoView = viewClose.findViewById(R.id.video_view);
        scalableVideoView.setVisibility(View.GONE);

        if ((userId = getUid()) != null) {

            // read user's Companion Appearance to be used to load video from correct storage location
            mDatabaseRef.child("users").child(userId).child("appearanceBase")
                    .addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.exists()) {

                                        // get appearance base reference and save it
                                        appearanceBase = dataSnapshot.getValue().toString();

                                        Log.i(TAG, "Appearance base is: " + appearanceBase);

                                        // begin logic for getting correct video location and playing it
                                        getDownloadUrlForAppearance(appearanceBase);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            }
                    );

        }

        //playVideoLocal();

        return viewClose;
    }

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

    // actual method for playing the video
    private void playVideoView(Uri uri) throws IOException {

        Log.i(TAG, "Video view is doing something");

        // grab parsed uri
        uriParsed = uri;

        // set data source as the uri we got
        scalableVideoView.setDataSource(getActivity(), uriParsed);

        // play video on async so they can all play smoothly on their own thread
        scalableVideoView.prepareAsync(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.i(TAG, "Video view is prepared");
                Log.i(TAG, "Uri used is: " + uriParsed.toString());

                // hide progress bar and show video viewer
                scalableVideoView.setVisibility(View.VISIBLE);
                videoProgressBar.setVisibility(View.GONE);

                // mute video and set it to loop
                scalableVideoView.start();
                scalableVideoView.setVolume(0,0);
                scalableVideoView.setLooping(true);
            }
        });
    } // end of videoview

    // gets correct download url from corresponding storage location
    private void getDownloadUrlForAppearance(String appearanceBaseToUse) {

        Log.i(TAG, "User's video ref: " + appearanceBaseToUse);

        // builds url reference based on appearance base
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference videoDownloadUrl = storageReference
                .child("/" + "appearanceBases" + "/" + appearanceBaseToUse + ".mp4");

        Log.i(TAG, "Storage reference is: " + videoDownloadUrl.toString());

        videoDownloadUrl.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri downloadUrl)
            {
                Log.i(TAG, "Download url is: " + downloadUrl);

                // play video with uri from correct storage location
                try {
                    playVideoView(downloadUrl);
                } catch (IOException e) {
                    Log.i(TAG, "Play video view failed");
                }
            }
        });

        videoDownloadUrl.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "Download url failed");
            }
        });
    }

    // get userid from db
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
