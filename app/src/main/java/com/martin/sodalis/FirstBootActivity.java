package com.martin.sodalis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class FirstBootActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 2000;

    private ImageView mainLogo;
    private ImageView mainLogoBg;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabaseRef;

    private String userId;

    private boolean firstRun;
    private boolean userLoggedIn;
    private boolean prefsResult;

    private SharedPreferences loginPrefs;

    private static final String TAG = "FirstBootActivity";

    /**
     * extremely important activity that holds the fragment the user sees when they first download
     * the app, or sends them to the main activity if they're already signed in, etc. Currently
     * seems pretty bulletproof so make sure any modifications don't mess with it. I don't think the
     * shard prefs add enough functionality so I might take those out. I really prefer to just read
     * from the db. It seems fast enough but I could be wrong. Not sure yet!
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstboot);

        // screen and status bar modifiers
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // initialize images to be used
        mainLogo = findViewById(R.id.main_logo);
        mainLogo.setVisibility(View.GONE);
        mainLogoBg = findViewById(R.id.main_logo_bg);

        // initialize firebase instances
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        // user doesn't exist,
        if (mFirebaseUser == null || getUid() == null) {
            Log.i(TAG, "No user signed in, running first boot");
            // continue on my child
            mainLogo.setVisibility(View.VISIBLE);
            runSplash(); // runs fadeout animation and then starts the firstboot fragment/signup process

            loginPrefs = getSharedPreferences("prefs", 0);

            SharedPreferences.Editor editor = loginPrefs.edit();
            editor.putBoolean("userLoggedIn", false);
            editor.apply();

        } else {
            // User successfully signed in
            userId = getUid();
            Log.i(TAG, "User is signed in: " + userId);

            loginPrefs = getSharedPreferences("prefs", 0);

            userLoggedIn = loginPrefs.getBoolean("userLoggedIn", prefsResult);
            Log.i(TAG, "User logged in prefs: " + userLoggedIn);

            if (userLoggedIn) { // equals true

                // user is logged in. Starts normal app sequence
                Intent iUserLoggedIn = new Intent(this, MainActivity.class);
                startActivity(iUserLoggedIn);

                finish();

            } else { // user logged in but hasn't completed setup

                SharedPreferences.Editor editor = loginPrefs.edit();
                editor.putBoolean("userLoggedIn", false);
                editor.apply();

                // db check to see if user has FULLY completed setup. Runs setup again if not
                mDatabaseRef.child("users").child(userId).child("setupDone")
                        .addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {

                                    if (dataSnapshot.getValue().equals(true)) {
                                        Log.i(TAG, "User has completed setup, continue on normally");

                                        SharedPreferences.Editor editor = loginPrefs.edit();
                                        editor.putBoolean("userLoggedIn", true);
                                        editor.apply();

                                        Intent iAlreadyIn = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(iAlreadyIn);

                                        finish();

                                    } else { // user hasn't completed setup yet
                                        Log.i(TAG, "User hasn't completed setup, sending them to" +
                                                " their correct scene in the setup");

                                        // first boot fragment will read which part of the setup the user
                                        // still needs to complete if necessary.
                                        runSplash();
                                    }

                                } else {
                                    runSplash();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            SharedPreferences settings = getSharedPreferences("prefs", 0);
            firstRun = settings.getBoolean("firstRun", false);

            if (!firstRun) { // running first time

                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("firstRun",true);
                editor.apply();

                editor = loginPrefs.edit();
                editor.putBoolean("userLoggedIn", false);
                editor.apply();

            }
        }
    } // end of oncreate

    // fades out
    private void runSplash() {

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // fades out images and starts up the first boot fragment

                Log.i("runSplash", "Splash handler is running");
                fadeOutAndHideImage(mainLogo);
                fadeOutBg(mainLogoBg);
            }
        }, SPLASH_TIME_OUT);

        // just used for background fade image
        ImageView backgroundImage = findViewById(R.id.background);

        Drawable backgrounds[] = new Drawable[4]; // add one for every new drawable added
        backgrounds[3] = ContextCompat.getDrawable(this, R.drawable.blue_bg1); // top right (start)
        backgrounds[2] = ContextCompat.getDrawable(this, R.drawable.blue_bg2); // bottom right v ^ (cycle directions)
        backgrounds[1] = ContextCompat.getDrawable(this, R.drawable.blue_bg3); // bottom left  v ^
        backgrounds[0] = ContextCompat.getDrawable(this, R.drawable.blue_bg4); // top left (end)

        Crossfade(backgroundImage, backgrounds, 7000);
    }

    // fade out main logo
    private void fadeOutAndHideImage(final ImageView img) {

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(1500);

        fadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationEnd(Animation animation) {
                img.setVisibility(View.GONE);
                showFragment(); // show firstboot fragment once fade animation is completed
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            @Override
            public void onAnimationStart(Animation animation) {
            }
        });

        img.startAnimation(fadeOut);
    }

    // fade out background to give nice smooth effect
    private void fadeOutBg (final ImageView img) {

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(2500);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                img.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        img.startAnimation(fadeOut);
    }

    // get and set user id
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // show first boot fragment
    private void showFragment() {
        // Begin the transaction
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.placeholder_fragment, new FirstBootFragment()); // replace holder with actual firstboot fragment
        ft.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 10001) && (resultCode == Activity.RESULT_OK)) {
            // recreate your fragment here
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.placeholder_fragment, new FirstBootFragment());
            ft.commit();
        }
    }

    // fade backgrounds, do not modify
    public void Crossfade(final ImageView image, final Drawable layers[], final int speedInMs) {
        class BackgroundGradientThread implements Runnable {
            Context mainContext;
            TransitionDrawable crossFader;
            boolean first = true;

            BackgroundGradientThread(Context c) {
                mainContext = c;
            }

            public void run() {
                Handler mHandler = new Handler(mainContext.getMainLooper());
                boolean reverse = false;

                while (true) {
                    if (!reverse) {
                        for (int i = 0; i < layers.length - 1; i++) {
                            Drawable tLayers[] = new Drawable[2];
                            tLayers[0] = layers[i];
                            tLayers[1] = layers[i + 1];

                            final TransitionDrawable tCrossFader = new TransitionDrawable(tLayers);
                            tCrossFader.setCrossFadeEnabled(true);

                            Runnable transitionRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    image.setImageDrawable(tCrossFader);
                                    tCrossFader.startTransition(speedInMs);
                                }
                            };

                            mHandler.post(transitionRunnable);

                            try {
                                Thread.sleep(speedInMs);
                            } catch (Exception e) {
                            }
                        }
                        reverse = true;

                    } else if (reverse) {
                        for (int i = layers.length - 1; i > 0; i--) {
                            Drawable tLayers[] = new Drawable[2];
                            tLayers[0] = layers[i];
                            tLayers[1] = layers[i - 1];

                            final TransitionDrawable tCrossFader = new TransitionDrawable(tLayers);
                            tCrossFader.setCrossFadeEnabled(true);

                            Runnable transitionRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    image.setImageDrawable(tCrossFader);
                                    tCrossFader.startTransition(speedInMs);
                                }
                            };

                            mHandler.post(transitionRunnable);

                            try {
                                Thread.sleep(speedInMs);
                            } catch (Exception e) {
                            }
                        }
                        reverse = false;
                    }
                }
            }
        }

        Thread backgroundThread = new Thread(new BackgroundGradientThread(this));
        backgroundThread.start();
    }
}
