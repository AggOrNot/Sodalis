package com.martin.sodalis;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class BootUp extends AppCompatActivity {

    private NumberProgressBar progressbar;

    private ImageView backgroundImage;
    private ImageView mainLogo;

    private Animation mainLogoGrow;
    private Animation fadeOut;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabaseRef;

    private String userId;
    private String userName;
    private String companionNameMain;
    private String userSceneId;

    private int i = 0;
    private int progressAmount;
    private int crossfadeSpeedInMs = 3500; // 7000 is default speed, same as all others

    private boolean userLoggedIn;
    private boolean prefsResult;

    private SharedPreferences loginPrefs;

    private static final String TAG = "BootUpActivity";

    private Handler mHandler = new Handler();

    /**
     * generates user's nodes for their scene id and generates actual name for their Companion. Only
     * shows on user's first set up/account creation. Boot up sequence doesn't really do anything
     * real. It's mostly to look good and give the impression that something super computationally
     * intense is happening. Pretty much the only interesting thing that happens here is the
     * choice of the Companion name. Which is super cool and I love.
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bootup);

        // screen and status bar modifiers
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // initialize progress bar
        progressbar = findViewById(R.id.boot_bar);
        progressbar.setProgress(i);

        // initialize images to be animated later
        backgroundImage = findViewById(R.id.background);
        mainLogo = findViewById(R.id.main_logo);

        // initialize animations
        mainLogoGrow = AnimationUtils.loadAnimation(this, R.anim.grow_anim);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        // initialize firebase instances
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        loginPrefs = getSharedPreferences("prefs", 0);

        // get and set user id to be used later
        userId = getUid();

        Log.i(TAG, "User is signed in: " + userId);

        SharedPreferences settings = getSharedPreferences("prefs", 0);
        boolean firstRun = settings.getBoolean("firstRun", false);

        if (!firstRun) { // running first time

            // mark user has done first run and set the scene id node in the user's section in db
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("firstRun",true);
            editor.apply();

            mDatabaseRef.child("users").child(userId).child("userSceneId").setValue("scene1");

            userSceneId = "scene1";

            Log.i(TAG, "User's scene id: " + userSceneId);

        } else { // running every other time

            mDatabaseRef.child("users").child(userId).child("userSceneId")
                    .addListenerForSingleValueEvent(

                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (!(dataSnapshot.exists())) {
                                        // don't do stuff, scene id hasn't been established yet
                                        mDatabaseRef.child("users").child(userId).child("userSceneId").setValue("scene1");

                                        userSceneId = "scene1";

                                        Log.i(TAG, "User's scene id: " + userSceneId);

                                    } else {
                                        userSceneId = dataSnapshot.getValue().toString();

                                        Log.i(TAG, "User's scene id is already: " + userSceneId);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            }
                    );

        }

        mDatabaseRef.child("users").child(userId).child("userName")
                .addValueEventListener(

                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if ((!dataSnapshot.getValue().toString().equals("Default Name")) ) {
                                    // user has name good to go
                                    userName = dataSnapshot.getValue().toString();

                                    Log.i(TAG, "User's name is: " + userName);
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                                Log.i(TAG, "User does not have a name???");
                            }
                        }
                );

        mDatabaseRef.child("users").child(userId).child("companionName")
                .addValueEventListener(

                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.getValue().toString().equals("Default Name")) {
                                    // companion has not picked its name yet, go forward with creating it
                                    companionNameMain = getCompanionName();

                                } else {

                                    // companion already has its name
                                    companionNameMain = dataSnapshot.getValue().toString();

                                    Log.i(TAG, "Companion name has been set as: " +
                                            companionNameMain);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

        // run progress bar/boot up sequence
        run();

        // backgrounds to be used in fade
        Drawable backgrounds[] = new Drawable[6]; // add one for every new drawable added
        backgrounds[5] = ContextCompat.getDrawable(this, R.drawable.green_bg3);
        backgrounds[4] = ContextCompat.getDrawable(this, R.drawable.yellow_bg4); // no idea where this goes
        backgrounds[3] = ContextCompat.getDrawable(this, R.drawable.blue_bg1); // top right (start)
        backgrounds[2] = ContextCompat.getDrawable(this, R.drawable.purple_bg2); // bottom right v ^ (cycle directions)
        backgrounds[1] = ContextCompat.getDrawable(this, R.drawable.red_bg3); // bottom left  v ^
        backgrounds[0] = ContextCompat.getDrawable(this, R.drawable.yellow_bg4);// top left (end)

        Crossfade(backgroundImage, backgrounds, crossfadeSpeedInMs);

    } // end of oncreate

    // generates Companion name based on user's male/female selections earlier
    public String getCompanionName() {

        // get user's companion voice choice
        mDatabaseRef.child("users").child(userId).child("companionVoice")
                .addListenerForSingleValueEvent(

                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {

                                    String companionChoice = dataSnapshot.getValue().toString();

                                    Log.i(TAG, "User's voice choice is: " + companionChoice);

                                    User user = new User();

                                    // gets random name from array of default names
                                    switch (companionChoice) {
                                        case "female":
                                            String[] femaleChoices = getApplicationContext().getResources()
                                                    .getStringArray(R.array.companion_names_female);

                                            companionNameMain = femaleChoices[new Random()
                                                    .nextInt(femaleChoices.length)];

                                            Log.i(TAG, "Companion chose its name as: " + companionNameMain);

                                            mDatabaseRef.child("users").child(userId).child("companionName")
                                                    .setValue(companionNameMain);

                                            user.companionName = companionNameMain;

                                            break;

                                        case "male":
                                            String[] maleChoices = getApplicationContext().getResources()
                                                    .getStringArray(R.array.companion_names_male);

                                            companionNameMain = maleChoices[new Random()
                                                    .nextInt(maleChoices.length)];

                                            Log.i(TAG, "Companion chose its name as: " + companionNameMain);

                                            mDatabaseRef.child("users").child(userId).child("companionName")
                                                    .setValue(companionNameMain);

                                            user.companionName = companionNameMain;

                                            break;

                                        default:
                                            Log.i(TAG, "Idk something must have went wrong");
                                            break;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.i(TAG, "Something went wrong getting user's choice");
                            }
                        }
                );

        return companionNameMain;
    }

    // get and set user id
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // progress bar/faux boot up sequence
    public void run() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                progressbar.setMax(100 * 100);

                progressAmount = progressbar.getProgress();

                // tbh just chose these values arbitrarily based on how they look. Can be changed to whatever
                while (progressAmount < (100 * 100)) {

                    progressAmount += 1;

                    if (progressAmount == (3 * 100)) {
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } else if (progressAmount == (11 * 100)) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } else if (progressAmount == (19 * 100)) {
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } else if (progressAmount == (27 * 100)) {
                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } else if (progressAmount == (33 * 100)) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } else if (progressAmount == (41 * 100)) {
                        try {
                            Thread.sleep(2500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } else if (progressAmount == (63 * 100)) {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } else if (progressAmount == (79 * 100)) {
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } else if (progressAmount == (99 * 100)) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            setProgressAnimate(progressbar, progressAmount);
                        }
                    });

                } // end of while loop

                // fade out images, play bootup sound, and finish activity to get it going
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainLogo.startAnimation(mainLogoGrow);
                        mainLogoGrow.setAnimationListener(new Animation.AnimationListener() {

                            // play sound early to avoid it getting cut off
                            @Override
                            public void onAnimationStart(Animation animation) {
                                MediaPlayer mp = MediaPlayer.create(BootUp.this, R.raw.welcome_sound);
                                mp.start();
                            }

                            // fade out image and start main activity
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                mainLogo.setAlpha((float) 0.0);
                                SharedPreferences.Editor editor = loginPrefs.edit();
                                editor.putBoolean("userLoggedIn", true);
                                editor.apply();

                                Intent iMain = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(iMain);

                                finish();

                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                        progressbar.startAnimation(fadeOut);
                    }
                });
            }
        }).start();
    }

    // prepare progress bar properties
    private void setProgressAnimate(NumberProgressBar pb, int progressTo) {

        ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), progressTo);
        animation.setDuration(250); // lower is faster, in MS
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
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
} // end of file
