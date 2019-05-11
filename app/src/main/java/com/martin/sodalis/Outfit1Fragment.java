package com.martin.sodalis;

import android.content.DialogInterface;
import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yqritc.scalablevideoview.ScalableVideoView;

public class Outfit1Fragment extends Fragment {

    private Button chooseButton;

    private Uri uriParsed;

    private ScalableVideoView scalableVideoView;

    private ProgressBar videoProgressBar;

    private DatabaseReference databaseReference;

    private String userId;
    private String appearanceBase;

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

        //videoProgressBar.setVisibility(View.VISIBLE);

        //playVideoLocal();

        /*try {
            playVideoView();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

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

                                Log.i(TAG, "Final appearance: " + appearanceBase + "outfit1");

                                // finalize user's appearance/outfit combo in their node
                                databaseReference.child("users").child(userId).child("appearanceFinal")
                                        .setValue(appearanceBase + "outfit1");

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
}
