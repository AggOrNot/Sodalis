package com.martin.sodalis;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabaseRef;

    private String userId;
    private String userSceneId;

    private static final String TAG = "MainActivity";

    /**
     * main activity that double checks (redundancy!!) for user's sign in status, then grabs the user's
     * scene id BEFORE inflating the main fragment. Very important that this fires before the main
     * (practice) fragment.
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // screen and status bar modifiers
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        // initialize firebase instances
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        // check for user signed in status obv
        if (mFirebaseUser == null || getUid() == null) {

            // user is not signed in for some reason, send her to sign in activity
            Intent iSignIn = new Intent(this, SignInActivity.class);
            startActivity(iSignIn);

        } else {
            // User successfully signed in
            userId = getUid();

            Log.i(TAG, "User is signed in: " + userId);

            // get user scene id before doing anything else
            mDatabaseRef.child("users").child(userId).child("userSceneId")
                    .addValueEventListener(

                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    // start work once id is confirmed, prevents null crashes
                                    if (dataSnapshot.exists()) {

                                        // sets scene id for use in current fragment
                                        userSceneId = dataSnapshot.getValue().toString();

                                        Log.i(TAG, "User's current scene is: " + userSceneId);

                                    } else {
                                        Log.i(TAG, "User's current scene doesn't exist yet. " +
                                                "Wait for it");
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            }
                    );
        }

        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // I think this makes sure everything works properly with the transparent status bar and no
        // action bar but I honestly don't remember when I wrote this. I just remember there being
        // some weird behavior with those two combined with the no tabbed view pager.
        ViewCompat.setOnApplyWindowInsetsListener(mViewPager,
                new OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                  WindowInsetsCompat insets) {
                        insets = ViewCompat.onApplyWindowInsets(v, insets);
                        if (insets.isConsumed()) {
                            return insets;
                        }

                        boolean consumed = false;
                        for (int i = 0, count = mViewPager.getChildCount(); i <  count; i++) {
                            ViewCompat.dispatchApplyWindowInsets(mViewPager.getChildAt(i), insets);
                            if (insets.isConsumed()) {
                                consumed = true;
                            }
                        }
                        return consumed ? insets.consumeSystemWindowInsets() : insets;
                    }
                });

    } // end of oncreate

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new PracticeFragment();
                case 1:
                    return new MenuFragment();
                default:
            }
            return null;
        }

        @Override
        public int getCount() {

            return 2;
        }
    }
}
