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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerButton;
import com.yqritc.scalablevideoview.ScalableVideoView;

import java.io.IOException;

public class Outfit0Fragment extends Fragment {

    private ShimmerButton chooseButton;
    private Shimmer shimmer;

    private Uri uriParsed;

    private ScalableVideoView scalableVideoView;

    private ProgressBar videoProgressBar;

    private Animation fadeOut;
    private Animation fadeIn;

    private DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;

    private String userId;
    private String appearanceBase;
    private String appearanceOutfitCombo;;

    private static final String TAG = "Outfit0";

    /**
     * pretty much identical to the appearance fragments, but shows outfit choices based on which
     * appearance was chosen. Next step is to read which appearance the user has and to shoot back
     * the correct outfit choices. Video situation is the same as the appearance fragments. Outfit
     * fragments don't have videos playing right now because there's no need. Same logic as
     * appearances so it's fine just to see if they work properly with all the UI and stuff.
     *
     */

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View outfit0View = inflater.inflate(R.layout.fragment_outfit0, container, false);

        // initialize views and buttons
        videoProgressBar = outfit0View.findViewById(R.id.videoview_bar);

        scalableVideoView = outfit0View.findViewById(R.id.video_view_tester);
        scalableVideoView.setVisibility(View.GONE);

        chooseButton = outfit0View.findViewById(R.id.premium_button0);

        shimmer = new Shimmer();
        shimmer.setDuration(1500);
        shimmer.start(chooseButton);

        videoProgressBar.setVisibility(View.VISIBLE);

        userId = getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();

        fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);

        // get user's base appearance from their node in order to build final appearance later
        databaseReference.child("users").child(userId).child("appearanceBase")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {
                                    appearanceBase = dataSnapshot.getValue().toString();

                                    appearanceOutfitCombo = getOutfitComboToDisplay(appearanceBase);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        }
                );

        //playVideoLocal();

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())

                        .setMessage("Is this the outfit you want?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d(TAG, "onClick: yes");

                                shimmer.cancel();
                                scalableVideoView.stop();
                                scalableVideoView.release();

                                Log.i(TAG, "Final appearance: " + appearanceBase + "outfit0");

                                // finalize user's appearance/outfit combo in their node
                                databaseReference.child("users").child(userId).child("appearanceFinal")
                                        .setValue(appearanceOutfitCombo);

                                Intent iNext = new Intent(getActivity(), ViewCompanionActivity.class);
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

        return outfit0View;
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

        // grab uri that was built earlier
        uriParsed = uri;

        // set uri as source for video
        scalableVideoView.setDataSource(getActivity(), uriParsed);

        scalableVideoView.prepareAsync(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.i("videoView0", "Video view is prepared");
                Log.i("videoView0", "Uri used is: " + uriParsed.toString());

                // video is loaded, begin fade out of progress bar to cleanly transition to video view
                videoProgressBar.startAnimation(fadeOut);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // fully hide progress bar and show video viewer
                        scalableVideoView.startAnimation(fadeIn);
                        scalableVideoView.setVisibility(View.VISIBLE);
                        videoProgressBar.setVisibility(View.GONE);

                        // mute video and set it to loop
                        scalableVideoView.start();
                        scalableVideoView.setVolume(0,0);
                        scalableVideoView.setLooping(true);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
            }
        });
    }// end of videoview

    private String getOutfitComboToDisplay(String appearanceBaseToUse) {

        // builds combo to play proper outfit pre-selection video
        String comboForVideo = appearanceBaseToUse + "outfit0";

        Log.i("comboVideoRef", "User's video ref: " + comboForVideo);

        // builds url reference based on appearance/outfit combo
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference videoDownloadUrl = storageReference
                .child("/" + "appearanceFinals" + "/" + comboForVideo + ".mp4");

        Log.i("comboVideoRef", "Storage reference is: " + videoDownloadUrl.toString());

        videoDownloadUrl.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri downloadUrl)
            {
                Log.i("comboVideoRef", "Download url is: " + downloadUrl);

                // play video for outfit preview
                try {
                    playVideoView(downloadUrl);
                } catch (IOException e) {
                }
            }
        });

        videoDownloadUrl.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("comboVideoRef", "Download url failed");
            }
        });

        return comboForVideo;
    }
}
