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

    private static final String TAG = "Appearance0";

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

        playVideoLocal();

        /*try {
            playVideoView();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

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

    // TODO: if user's coins are less than the apperance costs, launch the purchase coins activity
    // only needs to be done in the fragments that have premium appearances
}
