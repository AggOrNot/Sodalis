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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yqritc.scalablevideoview.ScalableVideoView;

import java.io.IOException;

public class Appearance1Fragment extends Fragment {

    private Button chooseButton;

    private Uri uriParsed;

    private ScalableVideoView scalableVideoView;

    private ProgressBar videoProgressBar;

    private DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;

    private String userId;

    private static final String TAG = "Appearance1";

    /**
     * exact same logic as first appearance, but isn't a premium appearance and the user can choose
     * this one for free. Otherwise indistinguishable
     *
     */

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View appearance1View = inflater.inflate(R.layout.fragment_appearance1, container, false);

        videoProgressBar = appearance1View.findViewById(R.id.videoview_bar);

        scalableVideoView = appearance1View.findViewById(R.id.video_view_tester);
        scalableVideoView.setVisibility(View.GONE);

        chooseButton = appearance1View.findViewById(R.id.premium_button0);

        videoProgressBar.setVisibility(View.VISIBLE);

        userId = getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();

        playVideoLocal();

        //getAppearanceRefToDisplay();

        chooseButton = appearance1View.findViewById(R.id.appearance_button1);

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())

                        .setMessage("Is this the appearance you want?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d(TAG, "onClick: yes");

                                Log.i(TAG, "Setting user's appearance base: appearance1");

                                // set selection in user's db node to be read later
                                databaseReference.child("users").child(userId).child("appearanceBase")
                                        .setValue("appearance1");

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

        return appearance1View;
    } // end of oncreate

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void playVideoView(Uri uri) throws IOException {

        Log.i("videoView1", "Video view is doing something");

        uriParsed = uri;

        scalableVideoView.setDataSource(getActivity(), uriParsed);

        scalableVideoView.prepareAsync(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.i("videoView1", "Video view is prepared");
                Log.i("videoView1", "Uri used is: " + uriParsed.toString());

                scalableVideoView.setVisibility(View.VISIBLE);
                videoProgressBar.setVisibility(View.GONE);

                scalableVideoView.setVolume(0,0);
                scalableVideoView.setLooping(true);

                scalableVideoView.start();
            }
        });
    }

    private void playVideoLocal() {

        try {
            scalableVideoView.setRawData(R.raw.boat);

            scalableVideoView.prepareAsync(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.i("videoView1", "Video view is prepared");
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

    private void getAppearanceRefToDisplay() {

        Log.i("appearanceBaseRef1", "User's video ref: " + "appearance1");

        // builds url reference based on appearance/outfit combo
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference videoDownloadUrl = storageReference
                .child("/" + "appearanceBases" + "/" + "appearance1" + ".mp4");

        Log.i("appearanceBaseRef1", "Storage reference is: " + videoDownloadUrl.toString());

        videoDownloadUrl.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri downloadUrl)
            {
                Log.i("appearanceBaseRef1", "Download url is: " + downloadUrl);

                try {
                    playVideoView(downloadUrl);
                } catch (IOException e) {
                    Log.i("appearanceBaseRef1", "Play video view failed");
                }
            }
        });

        videoDownloadUrl.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("appearanceBaseRef1", "Download url failed");
            }
        });
    }
}
