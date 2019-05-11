package com.martin.sodalis;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerButton;
import com.yqritc.scalablevideoview.ScalableVideoView;

import java.io.IOException;

public class Appearance0Fragment extends Fragment {

    private ShimmerButton chooseButton;
    private Shimmer shimmer;

    private Uri uriParsed;

    //private VideoView videoViewTester;
    private ScalableVideoView scalableVideoView;

    private ProgressBar videoProgressBar;

    private String userId;

    private DatabaseReference databaseReference;

    private static final String TAG = "Appearance0";

    /**
     * shows one of the video views for choosing a Companion appearance. First one will be premium
     * for now. Is hardcoded to play a local video for now because I burn through my firebase
     * allotment if I load the videos everytime I start the activity. Commented methods work though
     * getting the video from the fb storage. Next step is to store the user's selection and have it
     * correspond with the correct storage place. And come up with a system that keeps the user's
     * selections organized so they can be easily accessed later. Contains UI to view and buy more
     * 'coins' also. Need to come up with a better name than coins obviously. Masculine button
     * functionality will be added next also actually.
     *
     */

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View appearance0View = inflater.inflate(R.layout.fragment_appearance0, container, false);

        videoProgressBar = appearance0View.findViewById(R.id.videoview_bar);

        //videoViewTester = appearance0View.findViewById(R.id.video_view_tester);
        scalableVideoView = appearance0View.findViewById(R.id.video_view_tester);
        scalableVideoView.setVisibility(View.GONE);

        chooseButton = appearance0View.findViewById(R.id.premium_button0);

        shimmer = new Shimmer();
        shimmer.setDuration(1500);
        shimmer.start(chooseButton);

        videoProgressBar.setVisibility(View.VISIBLE);

        userId = getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        playVideoLocal();

        /*try {
            playVideoView();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

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
                                //videoViewTester.stopPlayback();

                                Log.i(TAG, "Setting user's appearance base: " + "appearance0");

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

    /*private void playVideoView() throws IOException {

        Log.i("videoView0", "Video view is doing something");

        uriParsed = Uri.parse("https://firebasestorage.googleapis.com/v0/b/sodalis-53c9d.appspot.com/o/venice.mp4?alt=media&token=152d3535-e0f5-48ca-8737-2fb2c863858b");

        scalableVideoView.setDataSource(getActivity(), uriParsed);

        scalableVideoView.prepareAsync(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.i("videoView0", "Video view is prepared");
                Log.i("videoView0", "Uri used is: " + uriParsed.toString());

                scalableVideoView.setVisibility(View.VISIBLE);
                videoProgressBar.setVisibility(View.GONE);

                scalableVideoView.start();
                scalableVideoView.setVolume(0,0);
                scalableVideoView.setLooping(true);

                if (scalableVideoView.isPlaying()) {
                    videoProgressBar.setVisibility(View.GONE);
                }
            }
        });
    } */ // end of videoview

    // TODO: if user's coins are less than the appearance costs, launch the purchase coins activity
    // only needs to be done in the fragments that have premium appearances
}
