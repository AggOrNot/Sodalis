package com.martin.sodalis;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yqritc.scalablevideoview.ScalableVideoView;


public class ViewCompanionActivity extends AppCompatActivity
        implements ViewCompanionCloseFragment.OnVideoLoadedListener {

    private Button changeAppearanceButton;

    private TextView nameTextview;

    private ImageView backButton;
    private ImageView toggleViews;

    private String userId;
    private String companionNameMain;
    private String appearanceFinal;

    private Uri uriParsed;

    private ScalableVideoView scalableVideoView;

    private ProgressBar videoProgressBar;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;
    private FirebaseStorage firebaseStorage;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private static final String TAG = "ViewCompanionActivity";

    /**
     * activity that reads user's Companion appearance from their data node and displays it. User can
     * go to their appearance and outfit selection screens from here if they want to change their
     * Companion's appearance. Plays local videos for now, but is equipped to read correct videos
     * from db once architecture is finished.
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_companion);

        // screen and status bar modifiers
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        // initialize firebase instances
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();

        // initialize and read user's Companion name from db and display. Hide until loaded
        nameTextview = findViewById(R.id.companion_name);
        nameTextview.setVisibility(View.GONE);

        if ((userId = getUid()) != null) {

            mDatabaseRef.child("users").child(userId).child("companionName")
                    .addValueEventListener(

                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    companionNameMain = dataSnapshot.getValue().toString();

                                    Log.i(TAG, "Companion name is: " +
                                            companionNameMain);

                                    nameTextview.setText(companionNameMain);
                                    nameTextview.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            }
                    );

        } else {
            // show sodalis I guess? I can probably come up with something better at some point
            nameTextview.setVisibility(View.VISIBLE);
        }

        // initialize and set listener for changing user's Companion appearance
        changeAppearanceButton = findViewById(R.id.change_appearance_button);

        changeAppearanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // send user to choose outfit activity. Not sure if they will be able to redo their base
                Intent iChange = new Intent(getApplicationContext(), ChooseOutfitActivity.class);
                startActivity(iChange);
            }
        });

        // back button
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(1); // KEEPS VIDEOS SMOOTH

        // toggle view Companion button
        toggleViews = findViewById(R.id.toggle_views);
        toggleViews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check to see what the current position of the viewpager is
                int fragmentPosition = mViewPager.getCurrentItem();
                Log.i(TAG, "Current fragment item is: " + fragmentPosition);

                // switch to the other position depending on what it's at
                switch (fragmentPosition) {
                    case 0:
                        mViewPager.setCurrentItem(1);
                        // log to double check. Works well!
                        Log.i(TAG, "Setting new current frag as: " + mViewPager.getCurrentItem());
                        break;
                    case 1:
                        mViewPager.setCurrentItem(0);
                        Log.i(TAG, "Setting new current frag as: " + mViewPager.getCurrentItem());
                        break;
                }
            }
        });
    } // end of oncreate

    // get userid from db
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // adapter to hold the two fragments
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    // create new fragment and use the attacher built earlier. Still behaves the
                    // same as the regular fragment down there.
                    Fragment viewCompanionCloseFragment = new ViewCompanionCloseFragment();
                    onAttachFragment(viewCompanionCloseFragment);

                    return viewCompanionCloseFragment;
                case 1:
                    return new ViewCompanionFarFragment();
                default:
            }
            return null;
        }

        @Override
        public int getCount() {

            return 2;
        }
    }

    // put stuff to do here once the video is loaded in the view close fragment
    public void onVideoLoaded(boolean isVideoLoaded) {
        if (isVideoLoaded) { // is true
            // show toggle view button to user so it can be used
            toggleViews.setVisibility(View.VISIBLE);
            Log.i(TAG, "Fragment communication worked. Video loaded and toggle displayed");
        }
    }

    // set up listener when the fragment is generated
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof ViewCompanionCloseFragment) {
            ViewCompanionCloseFragment viewCompanionCloseFragment = (ViewCompanionCloseFragment) fragment;
            viewCompanionCloseFragment.setVideoLoadedListener(this);
        }
    }
}
