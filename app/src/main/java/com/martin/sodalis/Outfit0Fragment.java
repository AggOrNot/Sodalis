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
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

    private DatabaseReference databaseReference;

    private String userId;
    private String appearanceBase;

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

        videoProgressBar = outfit0View.findViewById(R.id.videoview_bar);

        scalableVideoView = outfit0View.findViewById(R.id.video_view_tester);
        scalableVideoView.setVisibility(View.GONE);

        chooseButton = outfit0View.findViewById(R.id.premium_button0);

        shimmer = new Shimmer();
        shimmer.setDuration(1500);
        shimmer.start(chooseButton);

        userId = getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        // get user's base appearance from their node in order to build final appearance later
        databaseReference.child("users").child(userId).child("appearanceBase")
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {
                                    appearanceBase = dataSnapshot.getValue().toString();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        }
                );

        //playVideoLocal();

        /*try {
            playVideoView();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

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
                                //scalableVideoView.stop();
                                //scalableVideoView.release();

                                Log.i(TAG, "Final appearance: " + appearanceBase + "outfit0");

                                // finalize user's appearance/outfit combo in their node
                                databaseReference.child("users").child(userId).child("appearanceFinal")
                                        .setValue(appearanceBase + "outfit0");

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
}
