package com.martin.sodalis;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerButton;
import com.yqritc.scalablevideoview.ScalableVideoView;

import java.io.IOException;

public class Appearance0Fragment extends Fragment {

    private ShimmerButton chooseButton;
    private Shimmer shimmer;

    private Uri uriParsed;

    private ScalableVideoView scalableVideoView;

    private ProgressBar videoProgressBar;

    private String userId;

    private DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;

    private static final String TAG = "Appearance0";

    /**
     * shows one of the video views for choosing a Companion appearance. First one will be premium
     * for now just to show what it would look like. Hardcoded to play a local video for now because
     * I burn through my firebase allotment if I load the videos every time I start the activity.
     * Commented methods work getting the video from the fb storage. Next step is to store the user's
     * selection and have it correspond with the correct storage place. And come up with a system
     * that keeps the user's selections organized so they can be easily accessed later. Contains UI
     * to view and buy more 'coins' also. Need to come up with a better name than coins obviously.
     * Masculine button functionality will be added last.
     *
     */

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View appearance0View = inflater.inflate(R.layout.fragment_appearance0, container, false);

        videoProgressBar = appearance0View.findViewById(R.id.videoview_bar);

        scalableVideoView = appearance0View.findViewById(R.id.video_view_tester);
        scalableVideoView.setVisibility(View.GONE);

        chooseButton = appearance0View.findViewById(R.id.premium_button0);

        shimmer = new Shimmer();
        shimmer.setDuration(1500);
        shimmer.start(chooseButton);

        videoProgressBar.setVisibility(View.VISIBLE);

        userId = getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();

        //playVideoLocal();

        getAppearanceRefToDisplay();

        // confirm user's selection before moving on to next activity
        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())

                        .setMessage("Is this the appearance you want?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d(TAG, "onClick: yes");

                                shimmer.cancel();
                                scalableVideoView.stop();
                                scalableVideoView.release();

                                Log.i(TAG, "Setting user's appearance base: appearance0");

                                // set selection in user's db node to be read later
                                databaseReference.child("users").child(userId).child("appearanceBase")
                                        .setValue("appearance0");

                                Intent iNext = new Intent(getActivity(), ChooseOutfitActivity.class);
                                startActivity(iNext);

                                getActivity().finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .setCancelable(true)
                        .show();
            }
        });

        return appearance0View;
    } // end of oncreate

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void playVideoLocal() {

        try {
            scalableVideoView.setRawData(R.raw.venice);

            scalableVideoView.prepareAsync(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.i("videoView0", "Video view is prepared");
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

    private void playVideoView(Uri uri) throws IOException {

        Log.i("videoView0", "Video view is doing something");

        uriParsed = uri;

        scalableVideoView.setDataSource(getActivity(), uriParsed);

        scalableVideoView.prepareAsync(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.i("videoView0", "Video view is prepared");
                Log.i("videoView0", "Uri used is: " + uriParsed.toString());

                // hide progress bar and show video viewer
                scalableVideoView.setVisibility(View.VISIBLE);
                videoProgressBar.setVisibility(View.GONE);

                scalableVideoView.start();
                scalableVideoView.setVolume(0,0);
                scalableVideoView.setLooping(true);
            }
        });
    } // end of videoview

    private void getAppearanceRefToDisplay() {

        Log.i("appearanceBaseRef0", "User's video ref: " + "appearance0");

        // builds url reference based on appearance option
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference videoDownloadUrl = storageReference
                .child("/" + "appearanceBases" + "/" + "appearance0" + ".mp4");

        Log.i("appearanceBaseRef0", "Storage reference is: " + videoDownloadUrl.toString());

        videoDownloadUrl.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri downloadUrl)
            {
                Log.i("appearanceBaseRef0", "Download url is: " + downloadUrl);

                try {
                    playVideoView(downloadUrl);
                } catch (IOException e) {
                    Log.i("appearanceBaseRef0", "Play video view failed");
                }
            }
        });

        videoDownloadUrl.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("appearanceBaseRef0", "Download url failed");
            }
        });
    }

    // TODO: if user's coins are less than the appearance costs, launch the purchase coins activity
    // only needs to be done in the fragments that have premium appearances
}
