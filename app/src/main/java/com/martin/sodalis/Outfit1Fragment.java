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
import android.widget.Button;
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

public class Outfit1Fragment extends Fragment {

    private Button chooseButton;

    private Uri uriParsed;

    private ScalableVideoView scalableVideoView;

    private ProgressBar videoProgressBar;

    private Animation fadeOut;
    private Animation fadeIn;

    private DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;

    private String userId;
    private String appearanceBase;
    private String appearanceOutfitCombo;

    private static final String TAG = "Outfit1Fragment";

    /**
     * exact same logic as first outfit, but isn't a premium outfit and the user can choose
     * this one for free. Otherwise indistinguishable
     *
     */

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View outfit1View = inflater.inflate(R.layout.fragment_outfit1, container, false);

        videoProgressBar = outfit1View.findViewById(R.id.videoview_bar);

        scalableVideoView = outfit1View.findViewById(R.id.video_view_tester);
        scalableVideoView.setVisibility(View.GONE);

        chooseButton = outfit1View.findViewById(R.id.appearance_button1);

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

        chooseButton = outfit1View.findViewById(R.id.appearance_button1);

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())

                        .setMessage("Is this the outfit you want?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d(TAG, "onClick: yes");

                                scalableVideoView.stop();
                                scalableVideoView.release();

                                Log.i(TAG, "Final appearance: " + appearanceBase + "outfit1");

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

        return outfit1View;
    }

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

                videoProgressBar.startAnimation(fadeOut);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        scalableVideoView.setVisibility(View.VISIBLE);
                        videoProgressBar.setVisibility(View.GONE);

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
        String comboForVideo = appearanceBaseToUse + "outfit1";

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

                // start download url logic for video preview
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
