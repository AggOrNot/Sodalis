package com.martin.sodalis;

import android.content.DialogInterface;
import android.content.Intent;
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

import com.yqritc.scalablevideoview.ScalableVideoView;

public class Outfit1Fragment extends Fragment {

    private Button chooseButton;

    private Uri uriParsed;

    private ScalableVideoView scalableVideoView;

    private ProgressBar videoProgressBar;

    private static final String TAG = "Outfit1Fragment";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View outfit1View = inflater.inflate(R.layout.fragment_outfit1, container, false);

        videoProgressBar = outfit1View.findViewById(R.id.videoview_bar);

        scalableVideoView = outfit1View.findViewById(R.id.video_view_tester);
        scalableVideoView.setVisibility(View.GONE);

        chooseButton = outfit1View.findViewById(R.id.appearance_button1);

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

                                Log.i(TAG, "User signed out");
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


}
