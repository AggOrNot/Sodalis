package com.martin.sodalis;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

/**
 * primary fragment that holds the meat of the entire app. Shows the text from the Companion,
 * plays the voice from the Companion, loads the replies for every scene, checks for modifiers to
 * every scene, and most importantly creates the next scene id and reply id based on whichever
 * response the user chooses. Cool stuff! Also prompts the paywall and appearance stuff, so this is
 * literally where the money magic happens :). This fragment (wow lol) is constantly evolving and
 * will basically be in progress for the entirety of the app. I used to hate how I named it practice
 * fragment, but now I kind of like it. We'll see! There is a lot of stuff here that I feel like I
 * need to keep, but I probably don't need anymore if someone other than me took a look at it.
 * There are many lines that are commented out purposely to be used later or that may be needed at
 * some point.
 *
 */

public class PracticeFragment extends Fragment {

    private TextView companionText;
    private TextView userReplyA;
    private TextView userReplyB;
    private TextView userReplyC;
    private TextView userReplyD;
    private TextView greetingUserReplyA;
    private TextView greetingUserReplyB;
    private TextView greetingUserReplyC;
    private TextView greetingUserReplyD;
    private TextView timedEventText;
    private TextView continuePrompt;
    private TextView videoDismiss;

    private ImageView backgroundImage;

    private VideoView videoViewTester;

    private View practiceView;
    private View typingCircle1;
    private View typingCircle2;
    private View typingCircle3;

    private RelativeLayout videoViewLayout;

    private Animation fadeRepeat;
    private Animation fadeIn;
    private Animation fadeOut;
    private Animation typingAnim1;
    private Animation typingAnim2;
    private Animation typingAnim3;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabaseRef;
    private FirebaseStorage firebaseStorage;

    private String userId;
    private String userSceneId;
    private String userName;
    private String companionName;
    private String tempStringHolder;
    private String tempSceneBuilder;
    private String tempGreeting;
    private String sceneReplyId;
    private String lastKeySceneId;
    private String timeGreeting;
    private String dayOfWeek;
    private String backgroundColorToUse;
    private String audioRef;
    private String videoRef;

    private String replyAChildBuilder;
    private String replyBChildBuilder;
    private String replyCChildBuilder;
    private String replyDChildBuilder;

    private boolean isNextAnywayPresent;
    private boolean isPaywallFlagPresent;
    private boolean isPurchaseFlagPresent;
    private boolean isBackgroundFlagPresent;
    private boolean isModifierFlagPresent;
    private boolean isTimedEventPresent;

    private NumberProgressBar numberProgressBar;
    private ProgressBar videoProgressBar;

    private CountDownTimer countDownTimer;

    private int sceneNumber;
    private int sceneIncrement = 1;
    private int progressAmount;
    private int progressMaxInSeconds;
    private int milliProgress;

    private long progressMaxInLong;
    private long ratingModifier;

    private CompanionText cText;

    private MediaPlayer mediaPlayer;

    private Handler mHandler = new Handler();

    private static final String TAG = "PracticeFragment";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // screen and status bar modifiers
        practiceView = inflater.inflate(R.layout.fragment_practice, container, false);
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);

        // initialize firebase instances
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();

        //StorageReference filepath = firebaseStorage.getReferenceFromUrl("URL reference");

        // initialize and prep all main UI elements. Most will be not visible until they are
        // ready to be used
        backgroundImage = practiceView.findViewById(R.id.background);

        videoViewLayout = practiceView.findViewById(R.id.video_view_layout);
        videoViewLayout.setVisibility(View.GONE);

        videoViewTester = practiceView.findViewById(R.id.video_view_tester);
        videoViewTester.setVisibility(View.GONE);

        typingCircle1 = practiceView.findViewById(R.id.typing_circle1);
        typingCircle2 = practiceView.findViewById(R.id.typing_circle2);
        typingCircle3 = practiceView.findViewById(R.id.typing_circle3);

        numberProgressBar = practiceView.findViewById(R.id.timer_bar_new);
        videoProgressBar = practiceView.findViewById(R.id.videoview_bar);

        timedEventText = practiceView.findViewById(R.id.timed_event_text);
        continuePrompt = practiceView.findViewById(R.id.continue_prompt);
        videoDismiss = practiceView.findViewById(R.id.videoview_dismiss_text);

        // prep animations
        fadeRepeat = AnimationUtils.loadAnimation(getContext(), R.anim.fade_repeat);
        fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        typingAnim1 = AnimationUtils.loadAnimation(getContext(), R.anim.typing_anim_main);
        typingAnim2 = AnimationUtils.loadAnimation(getContext(), R.anim.typing_anim_main);
        typingAnim2.setStartOffset(200);
        typingAnim3 = AnimationUtils.loadAnimation(getContext(), R.anim.typing_anim_main);
        typingAnim3.setStartOffset(300);

        companionText = practiceView.findViewById(R.id.companion_text_area);
        companionText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);

        // prep all reply areas
        userReplyA = practiceView.findViewById(R.id.user_reply_A);
        userReplyB = practiceView.findViewById(R.id.user_reply_B);
        userReplyC = practiceView.findViewById(R.id.user_reply_C);
        userReplyD = practiceView.findViewById(R.id.user_reply_D);

        greetingUserReplyA = practiceView.findViewById(R.id.greeting_user_reply_A);
        greetingUserReplyB = practiceView.findViewById(R.id.greeting_user_reply_B);

        userReplyA.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
        userReplyA.setTextColor(Color.parseColor("#ffffff"));
        greetingUserReplyA.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
        greetingUserReplyA.setTextColor(Color.parseColor("#ffffff"));

        userReplyB.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
        userReplyB.setTextColor(Color.parseColor("#ffffff"));
        greetingUserReplyB.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
        greetingUserReplyB.setTextColor(Color.parseColor("#ffffff"));

        userReplyC.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
        userReplyC.setTextColor(Color.parseColor("#ffffff"));

        userReplyD.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
        userReplyD.setTextColor(Color.parseColor("#ffffff"));

        userReplyA.setVisibility(View.GONE);
        userReplyB.setVisibility(View.GONE);
        userReplyC.setVisibility(View.GONE);
        userReplyD.setVisibility(View.GONE);

        greetingUserReplyA.setVisibility(View.GONE);
        greetingUserReplyB.setVisibility(View.GONE);

        typingCircle1.setVisibility(View.GONE);
        typingCircle2.setVisibility(View.GONE);
        typingCircle3.setVisibility(View.GONE);

        numberProgressBar.setVisibility(View.GONE);

        timedEventText.setVisibility(View.GONE);
        continuePrompt.setVisibility(View.GONE);

        mediaPlayer = new MediaPlayer();

        if (mFirebaseUser == null || getUid() == null) {

            // user is not signed in for some reason, send her to sign in activity
            Intent iSignIn = new Intent(getActivity(), SignInActivity.class);
            startActivity(iSignIn);

        } else {
            // User successfully signed in
            userId = getUid();

            Log.i(TAG, "User is signed in: " + userId);

            // get user scene id before doing anything else
            showTypingAnimation();

            mDatabaseRef.child("users").child(userId).child("userSceneId")
                    .addListenerForSingleValueEvent(

                            new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    // start work once id is confirmed, prevents null crashes
                                    if (dataSnapshot.exists()) {

                                        // sets scene id for use in current fragment
                                        userSceneId = dataSnapshot.getValue().toString();
                                        setUserSceneId(userSceneId);

                                        setSceneNumberFromDb(userSceneId);

                                        Log.i("getUserSceneId", "User's current scene is: " + userSceneId);

                                        //isModifierFlagPresent = checkForModifierFlag();

                                        isNextAnywayPresent = checkForNextAnyway();

                                        if (userSceneId.equals("scene1")) {

                                            //setUserSceneId(userSceneId);

                                            // strips scene id to be used for scene number counter
                                            //setSceneNumberFromDb(userSceneId);

                                            // strips scene id if needed
                                            //isNextAnywayPresent = checkForNextAnyway();

                                            // new logic of flag checking
                                            if (isModifierFlagPresent) { // is true
                                                checkForBackgroundFlag();
                                            }

                                            checkForBackgroundFlag();

                                            // obvious
                                            getAudioRef();
                                            getCompanionText();
                                            getUserReplyA();
                                            getUserReplyB();
                                            getUserReplyC();
                                            getUserReplyD();

                                        } else {
                                            greetingBuilder();
                                        }

                                    } else {
                                        Log.i("getUserSceneId", "User's current scene doesn't exist yet. " +
                                                "Wait for it");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.i("getUserSceneId", "Something went wrong getting user's current scene");
                                    // catch error here
                                }
                            }
                    );
        }

        // gets and sets companion name for replacement in any texts
        mDatabaseRef.child("users").child(userId).child("companionName")
                .addListenerForSingleValueEvent(

                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                companionName = dataSnapshot.getValue().toString();

                                Log.i(TAG, "Companion name is confirmed as: " + companionName);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        }
                );

        // gets and sets user name for replacement in any texts
        mDatabaseRef.child("users").child(userId).child("userName")
                .addListenerForSingleValueEvent(

                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                userName = dataSnapshot.getValue().toString();

                                Log.i(TAG, "User's name is: " + userName);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.i(TAG, "Something went wrong getting user's name");
                            }
                        }
                );

        // useReplyA onclick listener previously went here
        userReplyA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                continuePrompt.setVisibility(View.GONE);

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }

                // new logic of checking modifiers
                if (isModifierFlagPresent) { // is true

                    checkForRelationshipRating("userReplyA");
                }

                // old, working logic
                // checks scene for RR modifier
                //checkForRelationshipRating("userReplyA");

                hideUserReplies();
                companionText.startAnimation(fadeOut);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                        showTypingAnimation();

                        // preps new scene id and reply id to be used
                        setNewSceneReplyId(replyAChildBuilder);
                        createNewSceneId(tempSceneBuilder, sceneReplyId);

                        // checks for next anyway flag in scene
                        isNextAnywayPresent = checkForNextAnyway();

                        // builds new reply id based on next flag and from preps
                        buildNewSceneReplyId(userSceneId, sceneReplyId);

                        // obvious
                        //getAudioRef();
                        getCompanionText();
                        getUserReplyA();
                        getUserReplyB();
                        getUserReplyC();
                        getUserReplyD();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        });

        // userReplyB onclick listener previously went here
        userReplyB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isModifierFlagPresent) {
                    checkForRelationshipRating("userReplyB");
                }

                //checkForRelationshipRating("userReplyB");

                hideUserReplies();
                companionText.startAnimation(fadeOut);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                        showTypingAnimation();

                        setNewSceneReplyId(replyBChildBuilder);
                        createNewSceneId(tempSceneBuilder, sceneReplyId);

                        isNextAnywayPresent = checkForNextAnyway();
                        buildNewSceneReplyId(userSceneId, sceneReplyId);

                        //getAudioRef();
                        getCompanionText();
                        getUserReplyA();
                        getUserReplyB();
                        getUserReplyC();
                        getUserReplyD();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
            }
        });

        // userReplyC onclick listener previously went here
        userReplyC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isModifierFlagPresent) {
                    checkForRelationshipRating("userReplyC");
                }

                //checkForRelationshipRating("userReplyC");

                hideUserReplies();
                companionText.startAnimation(fadeOut);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                        showTypingAnimation();

                        setNewSceneReplyId(replyCChildBuilder);
                        createNewSceneId(tempSceneBuilder, sceneReplyId);

                        isNextAnywayPresent = checkForNextAnyway();
                        buildNewSceneReplyId(userSceneId, sceneReplyId);

                        //getAudioRef();
                        getCompanionText();
                        getUserReplyA();
                        getUserReplyB();
                        getUserReplyC();
                        getUserReplyD();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
            }
        });

        // userReplyD onclick listener previously went here
        userReplyD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isModifierFlagPresent) {
                    checkForRelationshipRating("userReplyD");
                }

                //checkForRelationshipRating("userReplyD");

                hideUserReplies();
                companionText.startAnimation(fadeOut);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                        showTypingAnimation();

                        setNewSceneReplyId(replyDChildBuilder);
                        createNewSceneId(tempSceneBuilder, sceneReplyId);

                        isNextAnywayPresent = checkForNextAnyway();
                        buildNewSceneReplyId(userSceneId, sceneReplyId);

                        //getAudioRef();
                        getCompanionText();
                        getUserReplyA();
                        getUserReplyB();
                        getUserReplyC();
                        getUserReplyD();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
            }
        });

        return practiceView; // end of oncreate
    }

    public void checkForBackgroundFlag() {

        mDatabaseRef.child("act1").child("testIntro_CompanionText").child(userSceneId)
                .child("backgroundFlag").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            // TODO: may be able to stop thread and make an if thread is stopped to
                            // then start a new thread with the new color. Then this can be used
                            // in the companion text getter or where ever else it's needed

                            isBackgroundFlagPresent = true;
                            Log.i("checkForBackground", "Background flag IS present: "
                            + isBackgroundFlagPresent);

                            backgroundColorToUse = dataSnapshot.getValue().toString();

                            Log.i("checkForBackground", "Background flag's value is: " +
                                    backgroundColorToUse);

                            Drawable[] backgrounds = backgroundSetter(backgroundColorToUse);
                            Crossfade(backgroundImage, backgrounds, 7000);

                        } else {

                            isBackgroundFlagPresent = false;
                            Log.i("checkForBackground", "Background flag is NOT present: "
                            + isBackgroundFlagPresent);

                            backgroundColorToUse = "blue";
                            Crossfade(backgroundImage, backgroundSetter(backgroundColorToUse), 7000);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.i("backgroundFlag", "Error somewhere");
                    }
                }
        );
    }

    public Drawable[] backgroundSetter(String backgroundFlagColor) {

        Drawable backgrounds[] = new Drawable[4];

        switch (backgroundFlagColor) {
            case "blue":
                Log.i("backgroundSetter", "Background color set as blue");
                backgrounds[3] = ContextCompat.getDrawable(getContext(), R.drawable.blue_bg1); // top right (start)
                backgrounds[2] = ContextCompat.getDrawable(getContext(), R.drawable.blue_bg2); // bottom right v ^ (cycle directions)
                backgrounds[1] = ContextCompat.getDrawable(getContext(), R.drawable.blue_bg3); // bottom left  v ^
                backgrounds[0] = ContextCompat.getDrawable(getContext(), R.drawable.blue_bg4); // top left (end)
                break;

            case "yellow":
                Log.i("backgroundSetter", "Background color set as yellow");
                backgrounds[3] = ContextCompat.getDrawable(getContext(), R.drawable.yellow_bg1);
                backgrounds[2] = ContextCompat.getDrawable(getContext(), R.drawable.yellow_bg2);
                backgrounds[1] = ContextCompat.getDrawable(getContext(), R.drawable.yellow_bg3);
                backgrounds[0] = ContextCompat.getDrawable(getContext(), R.drawable.yellow_bg4);
                break;

            case "green":
                Log.i("backgroundSetter", "Background color set as green");
                backgrounds[3] = ContextCompat.getDrawable(getContext(), R.drawable.green_bg1);
                backgrounds[2] = ContextCompat.getDrawable(getContext(), R.drawable.green_bg2);
                backgrounds[1] = ContextCompat.getDrawable(getContext(), R.drawable.green_bg3);
                backgrounds[0] = ContextCompat.getDrawable(getContext(), R.drawable.green_bg4);
                break;

            case "orange":
                Log.i("backgroundSetter", "Background color set as orange");
                backgrounds[3] = ContextCompat.getDrawable(getContext(), R.drawable.orange_bg1);
                backgrounds[2] = ContextCompat.getDrawable(getContext(), R.drawable.orange_bg2);
                backgrounds[1] = ContextCompat.getDrawable(getContext(), R.drawable.orange_bg3);
                backgrounds[0] = ContextCompat.getDrawable(getContext(), R.drawable.orange_bg4);
                break;

            case "purple":
                Log.i("backgroundSetter", "Background color set as purple");
                backgrounds[3] = ContextCompat.getDrawable(getContext(), R.drawable.purple_bg1);
                backgrounds[2] = ContextCompat.getDrawable(getContext(), R.drawable.purple_bg2);
                backgrounds[1] = ContextCompat.getDrawable(getContext(), R.drawable.purple_bg3);
                backgrounds[0] = ContextCompat.getDrawable(getContext(), R.drawable.purple_bg4);
                break;

            case "red":
                Log.i("backgroundSetter", "Background color set as red");
                backgrounds[3] = ContextCompat.getDrawable(getContext(), R.drawable.red_bg1);
                backgrounds[2] = ContextCompat.getDrawable(getContext(), R.drawable.red_bg2);
                backgrounds[1] = ContextCompat.getDrawable(getContext(), R.drawable.red_bg3);
                backgrounds[0] = ContextCompat.getDrawable(getContext(), R.drawable.red_bg4);
                break;
        }

        return backgrounds;
    }


    public void showTypingAnimation () {

        typingCircle1.setVisibility(View.VISIBLE);
        typingCircle2.setVisibility(View.VISIBLE);
        typingCircle3.setVisibility(View.VISIBLE);

        typingCircle1.startAnimation(typingAnim1);
        typingCircle2.startAnimation(typingAnim2);
        typingCircle3.startAnimation(typingAnim3);
    }

    public void hideTypingAnimation () {

        typingCircle1.clearAnimation();
        typingCircle2.clearAnimation();
        typingCircle3.clearAnimation();

        typingCircle1.setVisibility(View.GONE);
        typingCircle2.setVisibility(View.GONE);
        typingCircle3.setVisibility(View.GONE);
    }

    public void hideUserReplies () {

        userReplyA.clearAnimation();
        userReplyB.clearAnimation();
        userReplyC.clearAnimation();
        userReplyD.clearAnimation();

        userReplyA.setVisibility(View.GONE);
        userReplyB.setVisibility(View.GONE);
        userReplyC.setVisibility(View.GONE);
        userReplyD.setVisibility(View.GONE);
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // gets whatever the Companion says from the db and checks to see if it has any custom modifiers
    // and preps the UI and audio as needed.
    public void getCompanionText() {

        //fetchAudioUrlFromFirebase();

        checkForModifierFlag();

        // set scene id to be used later
        String localUserSceneId = userSceneIdGetter();

        Log.i("getCompanionText", "User's scene id: " + localUserSceneId);

        mDatabaseRef.child("act1").child("testIntro_CompanionText").child(localUserSceneId)
                .child("companionText")
                .addListenerForSingleValueEvent(

                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                hideTypingAnimation();

                                if (dataSnapshot.exists()) {

                                    // contains companion text from scene
                                    tempStringHolder = dataSnapshot.getValue().toString();

                                    // replaces any occurrences of the user's name to their correct name
                                    if (tempStringHolder.contains("username")) {

                                        mDatabaseRef.child("users").child(userId).child("userName")
                                                .addListenerForSingleValueEvent(

                                                        new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange
                                                                    (DataSnapshot dataSnapshot) {

                                                                userName = dataSnapshot.getValue()
                                                                        .toString();

                                                                Log.i("getCompanionText",
                                                                        "User's name is: "
                                                                                + userName);

                                                                String newString = tempStringHolder
                                                                        .replace("username",
                                                                                userName);

                                                                companionText.setText(newString);
                                                                companionText.startAnimation(fadeIn);

                                                                getCustomAudioRef();

                                                                playAudio();

                                                                setNewSceneNumber(userSceneId);
                                                            }

                                                            @Override
                                                            public void onCancelled
                                                                    (DatabaseError databaseError) {
                                                                Log.i("getCompanionText", "Something went wrong " +
                                                                        "getting user's name");
                                                            }
                                                        }
                                                );


                                    } else if (checkForCompanionNameInText(tempStringHolder)) { // is true

                                        Log.i("getCompanionText",
                                                "Companion name is present and is: " +
                                                        companionName);

                                        String newString = tempStringHolder
                                                .replace("companionName", companionName);

                                        companionText.setText(newString);
                                        companionText.startAnimation(fadeIn);

                                        getCustomAudioRef();
                                        playAudio();

                                        setNewSceneNumber(userSceneId);

                                    } else if (checkForTimeGreeting(tempStringHolder)) { // is true

                                        Log.i("getCompanionTet",
                                                "Time greeting is present and is: " +
                                                        timeGreeting);

                                        String newString = tempStringHolder
                                                .replace("timeGreeting", timeGreeting);

                                        if (tempStringHolder.contains("weekday")) {
                                            newString = tempStringHolder
                                                    .replace("weekday", dayOfWeek);
                                        }

                                        //getCustomAudioRef();
                                        playAudio();

                                        companionText.setText(newString);
                                        companionText.startAnimation(fadeIn);

                                        setNewSceneNumber(userSceneId);

                                    } else { // no occurrences, go to work

                                        getAudioRef();
                                        playAudio();

                                        companionText.setText(tempStringHolder);
                                        companionText.startAnimation(fadeIn);

                                        setNewSceneNumber(userSceneId);
                                    }

                                    // saves companion text to class
                                    cText = new CompanionText(companionText.toString());

                                } else {
                                    // else there's an issue with the scene id or something and this
                                    // will ideally catch it and launch the temporary no scene
                                    // activity to prevent complete crashes
                                    Intent iNoScene = new Intent(getActivity(), NoSceneActivity.class);
                                    startActivity(iNoScene);

                                    hideTypingAnimation();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // if there's an issue with the scene id or something and this
                                // will ideally catch it and launch the temporary no scene
                                // activity to prevent complete crashes
                                Intent iNoScene = new Intent(getActivity(), NoSceneActivity.class);
                                startActivity(iNoScene);

                                hideTypingAnimation();
                            }
                        }
                );
    }

    // logic for getting the corresponding user reply to the companion text. A and B are used the most
    public void getUserReplyA() {

        mDatabaseRef.child("act1").child("testIntro_CompanionText").child(userSceneId)
                .child("userReplyA").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // checks if user reply A exists in scene (it usually does)
                        if (dataSnapshot.exists()) {

                            // builds reply id based on scene number
                            replyAChildBuilder = String.valueOf((sceneNumber) + "A");

                            Log.i("getUserReplyA", "First child builder: "
                                    + replyAChildBuilder);

                            // sets reply id to be used later
                            setReplyAChildBuilder(replyAChildBuilder);

                            // preps reply id to be used
                            String localReplyIdToUse;

                            // runs every time besides the first
                            if (sceneNumber > 1) {

                                mDatabaseRef.child("users").child(userId).child("userSceneId")
                                        .addListenerForSingleValueEvent(

                                                new ValueEventListener() {

                                                    @Override
                                                    public void onDataChange
                                                            (DataSnapshot dataSnapshot) {

                                                        // gets scene id to be used locally and on time
                                                        if (dataSnapshot.exists()) {
                                                            // stores scene id locally
                                                            String localSceneIdToUse = dataSnapshot
                                                                    .getValue()
                                                                    .toString();

                                                            Log.i("getUserReplyA", "Local scene id to use: "
                                                                    + localSceneIdToUse);

                                                            String localReplyIdToUse;

                                                            // builds reply id based on scene id
                                                            if (getNewReplyId() != null) {

                                                                localReplyIdToUse = getNewReplyId()
                                                                        .replace("scene", "");

                                                                localReplyIdToUse = localReplyIdToUse.substring(0,
                                                                        localReplyIdToUse.length() - 1) ;

                                                                localReplyIdToUse = localReplyIdToUse + "A";

                                                                Log.e("tittyBoiz", "final local reply id to use: " +
                                                                localReplyIdToUse);

                                                            } else { // is null

                                                                Log.i("getUserReplyA",
                                                                        "Get new reply id is null");

                                                                localReplyIdToUse = userSceneIdGetter()
                                                                        .replace("scene", "");

                                                                // this so far seems to be for sure working
                                                                localReplyIdToUse = localReplyIdToUse + "_"
                                                                        + readAndReturnSceneNumber(localSceneIdToUse) + "A";
                                                            }

                                                            // shoots back the chain to see if it's correct
                                                            Log.i("getUserReplyA",
                                                                    "Reference chain: "
                                                                            + localSceneIdToUse + " " +
                                                                            "userReplyA" + " " + localReplyIdToUse);

                                                            mDatabaseRef.child("act1")
                                                                    .child("testIntro_CompanionText")
                                                                    .child(localSceneIdToUse)
                                                                    .child("userReplyA")
                                                                    .child(localReplyIdToUse)
                                                                    .addListenerForSingleValueEvent(
                                                                            new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange
                                                                                        (DataSnapshot dataSnapshot) {

                                                                                    String replyString = dataSnapshot
                                                                                            .getValue().toString();

                                                                                    if (checkForCompanionNameInText(replyString)) { // is true

                                                                                        Log.i("getUserReplyA",
                                                                                                "Companion name is present and is: " +
                                                                                                        companionName);

                                                                                        String newString = replyString
                                                                                                .replace("companionName", companionName);

                                                                                        // show the good stuff
                                                                                        userReplyA.setText
                                                                                                (newString);
                                                                                        slideFromRight(userReplyA);

                                                                                    } else { // no occurrence, go to work
                                                                                        userReplyA.setText
                                                                                                (replyString);
                                                                                        slideFromRight(userReplyA);
                                                                                    }

                                                                                }
                                                                                @Override
                                                                                public void onCancelled
                                                                                        (DatabaseError databaseError) {

                                                                                }
                                                                            }
                                                                    );
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled
                                                            (DatabaseError databaseError) {
                                                    }
                                                }
                                        );
                            } else {

                                Log.d("getUserReplyA", "Scene number less than 1 is firing");

                                localReplyIdToUse = replyAChildBuilder;

                                Log.i("getUserReplyA", "User reply A is: " + replyAChildBuilder);
                                Log.i("getUserReplyA", "Local reply A is: " + userSceneId);

                                Log.i("getUserReplyA", "Reference chain: " + userSceneId +
                                        " " + "userReplyA" + " " + localReplyIdToUse);

                                mDatabaseRef.child("act1").child("testIntro_CompanionText").child(userSceneId)
                                        .child("userReplyA").child(localReplyIdToUse).addListenerForSingleValueEvent(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                userReplyA.setText(dataSnapshot.getValue().toString());
                                                slideFromRight(userReplyA);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        }
                                );
                            }

                        } else {
                            userReplyA.clearAnimation();
                            userReplyA.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                }
        );
    }

    // exact same logic as user reply A, but writes B instead. (obviously)
    public void getUserReplyB() {

        mDatabaseRef.child("act1").child("testIntro_CompanionText").child(userSceneId)
                .child("userReplyB").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            replyBChildBuilder = String.valueOf((sceneNumber) + "B");

                            setReplyBChildBuilder(replyBChildBuilder);

                            String localReplyIdToUse;

                            if (sceneNumber > 1) {
                                mDatabaseRef.child("users").child(userId).child("userSceneId")
                                        .addListenerForSingleValueEvent(

                                                new ValueEventListener() {

                                                    @Override
                                                    public void onDataChange
                                                            (DataSnapshot dataSnapshot) {

                                                        // gets scene id to be used locally and on time
                                                        if (dataSnapshot.exists()) {

                                                            // stores scene id locally
                                                            String localSceneIdToUse = dataSnapshot
                                                                    .getValue()
                                                                    .toString();

                                                            Log.i("getUserReplyB", "Local scene id to use: "
                                                                    + localSceneIdToUse);

                                                            String localReplyIdToUse;

                                                            if (getNewReplyId() != null) { // IS present

                                                                localReplyIdToUse = getNewReplyId()
                                                                        .replace("scene", "");

                                                                localReplyIdToUse = localReplyIdToUse.substring(0,
                                                                        localReplyIdToUse.length() - 1);

                                                                localReplyIdToUse = localReplyIdToUse + "B";

                                                                Log.e("tittyBoiz", "final local reply id to use: " +
                                                                localReplyIdToUse);

                                                            } else { // is null
                                                                Log.i("getUserReplyB",
                                                                        "Get new reply id is null");

                                                                localReplyIdToUse = userSceneIdGetter()
                                                                        .replace("scene", "");

                                                                localReplyIdToUse = localReplyIdToUse + "_"
                                                                        + readAndReturnSceneNumber(localSceneIdToUse) + "B";
                                                            }

                                                            Log.i("getUserReplyB",
                                                                    "Reference chain: "
                                                                            + localSceneIdToUse + " " +
                                                                            "userReplyB" + " " + localReplyIdToUse);

                                                            //replyBChildBuilder = String.valueOf((sceneNumber - 1) + "B");

                                                            mDatabaseRef.child("act1")
                                                                    .child("testIntro_CompanionText")
                                                                    .child(localSceneIdToUse)
                                                                    .child("userReplyB")
                                                                    .child(localReplyIdToUse)
                                                                    .addListenerForSingleValueEvent(
                                                                            new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange
                                                                                        (DataSnapshot dataSnapshot) {

                                                                                    String replyString = dataSnapshot
                                                                                            .getValue().toString();

                                                                                    if (checkForCompanionNameInText(replyString)) { // is true

                                                                                        Log.i("getUserReplyB",
                                                                                                "Companion name is present and is: " +
                                                                                        companionName);

                                                                                        String newString = replyString
                                                                                                .replace("companionName", companionName);

                                                                                        userReplyB.setText
                                                                                                (newString);
                                                                                        slideFromLeft(userReplyB);

                                                                                    } else { // no occurrence, go to work
                                                                                        userReplyB.setText
                                                                                                (replyString);
                                                                                        slideFromLeft(userReplyB);
                                                                                    }
                                                                                }
                                                                                @Override
                                                                                public void onCancelled
                                                                                        (DatabaseError databaseError) {
                                                                                }
                                                                            }
                                                                    );
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled
                                                            (DatabaseError databaseError) {
                                                    }
                                                }
                                        );

                            } else {
                                localReplyIdToUse = replyBChildBuilder;

                                Log.i("getUserReplyB", "User reply B is: " + replyBChildBuilder);
                                Log.i("getUserReplyB", "Local reply B is: " + userSceneId);

                                Log.i("getUserReplyB", "Reference chain: " + userSceneId +
                                        " " + "userReplyB" + " " + localReplyIdToUse);

                                mDatabaseRef.child("act1").child("testIntro_CompanionText").child(userSceneId)
                                        .child("userReplyB").child(localReplyIdToUse).addListenerForSingleValueEvent(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                userReplyB.setText(dataSnapshot.getValue().toString());
                                                slideFromLeft(userReplyB);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        }
                                );
                            }

                        } else {
                            userReplyB.clearAnimation();
                            userReplyB.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                }
        );
    }

    // same logic as user reply A
    public void getUserReplyC() {

        mDatabaseRef.child("act1").child("testIntro_CompanionText").child(userSceneId)
                .child("userReplyC").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            replyCChildBuilder = String.valueOf((sceneNumber) + "C");

                            setReplyCChildBuilder(replyCChildBuilder);

                            String localReplyIdToUse;

                            if (sceneNumber > 1) {
                                mDatabaseRef.child("users").child(userId).child("userSceneId")
                                        .addListenerForSingleValueEvent(

                                                new ValueEventListener() {

                                                    @Override
                                                    public void onDataChange
                                                            (DataSnapshot dataSnapshot) {

                                                        // gets scene id to be used locally and on time
                                                        if (dataSnapshot.exists()) {


                                                            // stores scene id locally
                                                            String localSceneIdToUse = dataSnapshot
                                                                    .getValue()
                                                                    .toString();

                                                            Log.i("getUserReplyC", "Local scene id to use: "
                                                                    + localSceneIdToUse);

                                                            String localReplyIdToUse;

                                                            if (getNewReplyId() != null) { // IS present

                                                                localReplyIdToUse = getNewReplyId()
                                                                        .replace("scene", "");

                                                                localReplyIdToUse = localReplyIdToUse.substring(0,
                                                                        localReplyIdToUse.length() - 1);

                                                                localReplyIdToUse = localReplyIdToUse + "C";

                                                                Log.e("tittyBoiz", "final local reply id to use: " +
                                                                        localReplyIdToUse);

                                                            } else { // is null
                                                                Log.i("getUserReplyC",
                                                                        "Get new reply id is null");

                                                                localReplyIdToUse = userSceneIdGetter()
                                                                        .replace("scene", "");

                                                                localReplyIdToUse = localReplyIdToUse + "_"
                                                                        + readAndReturnSceneNumber(localSceneIdToUse) + "C";
                                                            }

                                                            Log.i("getUserReplyC",
                                                                    "Reference chain: "
                                                                            + localSceneIdToUse + " " +
                                                                            "userReplyC" + " " + localReplyIdToUse);

                                                            //replyCChildBuilder = String.valueOf((sceneNumber - 1) + "C");

                                                            mDatabaseRef.child("act1")
                                                                    .child("testIntro_CompanionText")
                                                                    .child(localSceneIdToUse)
                                                                    .child("userReplyC")
                                                                    .child(localReplyIdToUse)
                                                                    .addListenerForSingleValueEvent(
                                                                            new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange
                                                                                        (DataSnapshot dataSnapshot) {

                                                                                    String replyString = dataSnapshot
                                                                                            .getValue().toString();

                                                                                    if (checkForCompanionNameInText(replyString)) { // is true

                                                                                        Log.i("getUserReplyC",
                                                                                                "Companion name is present and is: " +
                                                                                                        companionName);

                                                                                        String newString = replyString
                                                                                                .replace("companionName", companionName);

                                                                                        userReplyC.setText
                                                                                                (newString);
                                                                                        slideFromRight(userReplyC);

                                                                                    } else { // no occurrence, go to work
                                                                                        userReplyC.setText
                                                                                                (replyString);
                                                                                        slideFromRight(userReplyC);
                                                                                    }
                                                                                }
                                                                                @Override
                                                                                public void onCancelled
                                                                                        (DatabaseError databaseError) {
                                                                                }
                                                                            }
                                                                    );
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled
                                                            (DatabaseError databaseError) {
                                                    }
                                                }
                                        );

                            } else {
                                localReplyIdToUse = replyCChildBuilder;

                                Log.i("getUserReplyC", "User reply C is: " + replyCChildBuilder);
                                Log.i("getUserReplyC", "Local reply C is: " + userSceneId);

                                Log.i("getUserReplyC", "Reference chain: " + userSceneId +
                                        " " + "userReplyC" + " " + localReplyIdToUse);

                                mDatabaseRef.child("act1").child("testIntro_CompanionText").child(userSceneId)
                                        .child("userReplyC").child(localReplyIdToUse).addListenerForSingleValueEvent(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                userReplyC.setText(dataSnapshot.getValue().toString());
                                                slideFromLeft(userReplyC);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        }
                                );
                            }

                        } else {
                            userReplyC.clearAnimation();
                            userReplyC.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                }
        );
    }

    // same logic as user reply A
    public void getUserReplyD() {

        mDatabaseRef.child("act1").child("testIntro_CompanionText").child(userSceneId)
                .child("userReplyD").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            replyDChildBuilder = String.valueOf((sceneNumber) + "D");

                            setReplyDChildBuilder(replyDChildBuilder);

                            String localReplyIdToUse;

                            if (sceneNumber > 1) {
                                mDatabaseRef.child("users").child(userId).child("userSceneId")
                                        .addListenerForSingleValueEvent(

                                                new ValueEventListener() {

                                                    @Override
                                                    public void onDataChange
                                                            (DataSnapshot dataSnapshot) {

                                                        // gets scene id to be used locally and on time
                                                        if (dataSnapshot.exists()) {

                                                            // stores scene id locally
                                                            String localSceneIdToUse = dataSnapshot
                                                                    .getValue()
                                                                    .toString();

                                                            Log.i("getUserReplyD", "Local scene id to use: "
                                                                    + localSceneIdToUse);

                                                            String localReplyIdToUse;

                                                            if (getNewReplyId() != null) { // IS present

                                                                localReplyIdToUse = getNewReplyId()
                                                                        .replace("scene", "");

                                                                localReplyIdToUse = localReplyIdToUse.substring(0,
                                                                        localReplyIdToUse.length() - 1);

                                                                localReplyIdToUse = localReplyIdToUse + "D";

                                                                Log.e("tittyBoiz", "final local reply id to use: " +
                                                                        localReplyIdToUse);

                                                            } else { // is null
                                                                Log.i("getUserReplyD",
                                                                        "Get new reply id is null");

                                                                localReplyIdToUse = userSceneIdGetter()
                                                                        .replace("scene", "");

                                                                localReplyIdToUse = localReplyIdToUse + "_"
                                                                        + readAndReturnSceneNumber(localSceneIdToUse) + "D";
                                                            }

                                                            Log.i("getUserReplyD",
                                                                    "Reference chain: "
                                                                            + localSceneIdToUse + " " +
                                                                            "userReplyD" + " " + localReplyIdToUse);

                                                            //replyDChildBuilder = String.valueOf((sceneNumber - 1) + "D");

                                                            mDatabaseRef.child("act1")
                                                                    .child("testIntro_CompanionText")
                                                                    .child(localSceneIdToUse)
                                                                    .child("userReplyD")
                                                                    .child(localReplyIdToUse)
                                                                    .addListenerForSingleValueEvent(
                                                                            new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange
                                                                                        (DataSnapshot dataSnapshot) {

                                                                                    String replyString = dataSnapshot
                                                                                            .getValue().toString();

                                                                                    if (checkForCompanionNameInText(replyString)) { // is true

                                                                                        Log.i("getUserReplyD",
                                                                                                "Companion name is present and is: " +
                                                                                                        companionName);

                                                                                        String newString = replyString
                                                                                                .replace("companionName", companionName);

                                                                                        userReplyD.setText
                                                                                                (newString);
                                                                                        slideFromLeft(userReplyD);

                                                                                    } else { // no occurrence, go to work
                                                                                        userReplyD.setText
                                                                                                (replyString);
                                                                                        slideFromLeft(userReplyD);
                                                                                    }
                                                                                }
                                                                                @Override
                                                                                public void onCancelled
                                                                                        (DatabaseError databaseError) {
                                                                                }
                                                                            }
                                                                    );
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled
                                                            (DatabaseError databaseError) {
                                                    }
                                                }
                                        );

                            } else {
                                localReplyIdToUse = replyDChildBuilder;

                                Log.i("getUserReplyD", "User reply D is: " + replyDChildBuilder);
                                Log.i("getUserReplyD", "Local reply D is: " + userSceneId);

                                Log.i("getUserReplyD", "Reference chain: " + userSceneId +
                                        " " + "userReplyD" + " " + localReplyIdToUse);

                                mDatabaseRef.child("act1").child("testIntro_CompanionText").child(userSceneId)
                                        .child("userReplyD").child(localReplyIdToUse).addListenerForSingleValueEvent(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                userReplyD.setText(dataSnapshot.getValue().toString());
                                                slideFromLeft(userReplyD);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        }
                                );
                            }

                        } else {
                            userReplyD.clearAnimation();
                            userReplyD.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                }
        );
    }

    // builds text and replies for when user returns to the app and their Companion greets them.
    // mostly for immersion since it would be a little jarring to immediately just resume talking
    // to your Companion literally right where you left off. This is different than other choices
    // games, but I like it.
    public void greetingBuilder() {

        checkForBackgroundFlag();

        // get random number for random greeting.
        // bound is number of greeting messages in db. MUST CHANGE IF MORE ARE ADDED!!
        int buildGreetingNumber = new Random().nextInt(5) + 1;

        // convert to string
        String greetingNumber = String.valueOf(buildGreetingNumber);

        Log.i("greetingBuilder", "Greeting Number: " + greetingNumber);

        // grab Companion greeting text based on the number produced
        mDatabaseRef.child("act1").child("greetingMessage").child(greetingNumber)
                .child("companionText")
                .addListenerForSingleValueEvent(

                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                hideTypingAnimation();

                                // contains companion text from scene
                                tempGreeting = dataSnapshot.getValue().toString();

                                // replaces any occurrences of the user's name to their correct name
                                if (tempGreeting.contains("username")) {

                                    mDatabaseRef.child("users").child(userId).child("userName")
                                            .addListenerForSingleValueEvent(

                                                    new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange
                                                                (DataSnapshot dataSnapshot) {

                                                            userName = dataSnapshot.getValue()
                                                                    .toString();

                                                            Log.i("greetingMessage",
                                                                    "User's name is: "
                                                                            + userName);

                                                            String newString = tempGreeting
                                                                    .replace("username",
                                                                            userName);

                                                            // will uncomment these once lines are recorded
                                                            //getAudioRef();
                                                            //playAudio();

                                                            companionText.setText(newString);
                                                            companionText.startAnimation(fadeIn);
                                                        }

                                                        @Override
                                                        public void onCancelled
                                                                (DatabaseError databaseError) {
                                                            Log.i("greetingMessage", "Something went wrong " +
                                                                    "getting user's name");
                                                        }
                                                    }
                                            );


                                    // might be able to delete this, I'm not sure if I'll ever write one with their name in it.
                                    // circle back.
                                } else if (checkForCompanionNameInText(tempGreeting)) { // is true

                                    Log.i("greetingMessage",
                                            "Companion name is present and is: " +
                                                    companionName);

                                    String newString = tempGreeting
                                            .replace("companionName", companionName);

                                    companionText.setText(newString);
                                    companionText.startAnimation(fadeIn);

                                    //getAudioRef();
                                    //playAudio();

                                } else if (checkForTimeGreeting(tempGreeting)) { // is true

                                    Log.i("greetingMessage",
                                            "Time greeting is present and is: " +
                                                    timeGreeting);

                                    String newString = tempGreeting
                                            .replace("timeGreeting", timeGreeting);

                                    if (tempGreeting.contains("weekday")) {
                                        newString = tempGreeting
                                                .replace("weekday", dayOfWeek);
                                    }

                                    //getAudioRef();
                                    //playAudio();

                                    companionText.setText(newString);
                                    companionText.startAnimation(fadeIn);

                                } else { // no occurrences, go to work

                                    //getAudioRef();
                                    //playAudio();

                                    companionText.setText(tempGreeting);
                                    companionText.startAnimation(fadeIn);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                hideTypingAnimation();
                            }
                        }
                );

        // get user replies
        mDatabaseRef.child("act1").child("greetingMessage").child(greetingNumber)
                .child("userReplyA").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            String replyString = dataSnapshot
                                    .getValue().toString();

                            if (checkForCompanionNameInText(replyString)) { // is true

                                Log.i("greetingBuilder",
                                        "Companion name is present in reply and is: " +
                                                companionName);

                                String newString = replyString
                                        .replace("companionName", companionName);

                                if (checkForTimeGreeting(replyString)) { // is true

                                    Log.i("greetingBuilder",
                                            "Time greeting is present in reply and is: "
                                                    + timeGreeting);

                                    newString = newString
                                            .replace("timeGreeting", timeGreeting);

                                    if (replyString.contains("weekday")) {
                                        newString = replyString
                                                .replace("weekday", dayOfWeek);
                                    }
                                }

                                greetingUserReplyA.setText(newString);
                                slideFromRight(greetingUserReplyA);

                            } else if (checkForTimeGreeting(replyString)) { // is true

                                Log.i("greetingBuilder",
                                        "Time greeting is present in reply and is: "
                                + timeGreeting);

                                String newString = replyString
                                        .replace("timeGreeting", timeGreeting);

                                if (replyString.contains("weekday")) {
                                    newString = replyString
                                            .replace("weekday", dayOfWeek);
                                }

                                greetingUserReplyA.setText(newString);
                                slideFromRight(greetingUserReplyA);

                            } else { // no occurrence, go to work
                                greetingUserReplyA.setText
                                        (replyString);
                                slideFromRight(greetingUserReplyA);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                }); // end of original value listener

        // might add more replies, but for now this is fine. It's mostly just to take you to your last
        // scene for now. That may change in the future, but this works until further notice.
        greetingUserReplyA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                companionText.startAnimation(fadeOut);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        greetingUserReplyA.clearAnimation();
                        greetingUserReplyA.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        showTypingAnimation();

                        // obvious
                        //getAudioRef();
                        getCompanionText();
                        getUserReplyA();
                        getUserReplyB();
                        getUserReplyC();
                        getUserReplyD();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        });
    }

    // sliding animation used for user replies A and C
    public void slideFromRight(View view){
        TranslateAnimation animate = new TranslateAnimation(practiceView.getWidth(), 0,0,0);
        animate.setDuration(200);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.VISIBLE);
    }

    // sliding animation used for user replies B and D
    public void slideFromLeft(View view){
        TranslateAnimation animate = new TranslateAnimation(-practiceView.getWidth(),0,0,0);
        animate.setDuration(200);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.VISIBLE);
    }

    public void setReplyAChildBuilder(String replyA) {
        Log.i("setReplyAChildBuilder", "Setting reply A builder as: " + replyA);
        replyAChildBuilder = replyA;
    }

    public String getReplyAChildBuilder() {
        Log.i("getReplyAChildBuilder", "Getting reply A builder as: " + replyAChildBuilder);
        return replyAChildBuilder;
    }

    public void setReplyBChildBuilder(String replyB) {
        Log.i("setReplyBChildBuilder", "Setting reply B builder as: " + replyB);
        replyBChildBuilder = replyB;
    }

    public String getReplyBChildBuilder() {
        Log.i("getReplyBChildBuilder", "Getting reply B builder as: " + replyBChildBuilder);
        return replyBChildBuilder;
    }

    public String setReplyCChildBuilder(String replyC) {
        Log.i("setReplyCChildBuilder", "Setting reply C builder as: " + replyC);
        return replyCChildBuilder = replyC;
    }

    public String setReplyDChildBuilder(String replyD) {
        Log.i("setReplyDChildBuilder", "Setting reply D builder as: " + replyD);
        return replyDChildBuilder = replyD;
    }

    public String setUserSceneId(String id) {
        Log.i("setUserSceneId", "Setting scene id as: " + id);
        userSceneId = id;
        return userSceneId;
    }

    // TODO: if I have to change the reference chain, may need to strip the length - 1 here in order
    // to get the correct scene number again

    // writes user's scene id in corresponding user node in db
    public void setSceneNumberFromDb(String sceneid) {
        // strip scene and everything after that
        Log.i("setSceneNumberFromDb", "User scene id is: " + sceneid);

        if (sceneid.contains("_")) { // strip scene chain if it has it

            sceneid = sceneid.split("_")[0];
        }

        // strip "scene" and leave just the number (int)
        sceneNumber = Integer.parseInt(sceneid.replace("scene", ""));

        Log.i("setSceneNumberFromDb", "Stripped scene number is: " + sceneNumber);
    }

    // start building new scene number
    public String setNewSceneNumber(String oldScene) {

        Log.i("setnewSceneNumber", "Old scene: " + oldScene);

        // increments because every scene increases in number, changes come from user reply letters
        sceneIncrement = sceneNumber + 1;

        String frontChainHolder;

        Log.i("setNewSceneNumber", "Scene number is: " + sceneNumber);
        Log.i("setNewSceneNumber", "Scene increment is: " + sceneIncrement);

        frontChainHolder = oldScene.replace(String.valueOf(sceneNumber),
                String.valueOf(sceneIncrement));

        // replace back of chain
        tempSceneBuilder = frontChainHolder.replace(String.valueOf(sceneNumber - 1),
                String.valueOf(sceneNumber));

        Log.i("setNewSceneNumber", "Temp scene builder is: " + tempSceneBuilder);

        if (isNextAnywayPresent) { // is true
            // I know this is redundant, but it's to be clear of what's happening.
            String nextAnywayBuilder = tempSceneBuilder.split("_")[0];

            tempSceneBuilder = nextAnywayBuilder;

            Log.i("setNewSceneNumber", "Next Anyway string split into: " + tempSceneBuilder);
        }

        // sets new scene number to prepare for next one
        sceneNumber += 1;

        return tempSceneBuilder;
    }

    // can probably make this void, but I'd rather keep it for now. Not sure how this will evolve
    public String setNewSceneReplyId(String replyId) {
        Log.i("setNewSceneReplyId", "Reply id is: " + replyId);
        sceneReplyId = replyId;
        return sceneReplyId;
    }

    // build next REPLY id to prep for next scene. Void same idea as above
    public String buildNewSceneReplyId(String sceneIdToUse, String oldReplyId) {

        String replyIdBuilder = oldReplyId.replace(String.valueOf(sceneIncrement - 1),
                String.valueOf(sceneIncrement));

        Log.i("buildNewSceneReplyId", "Reply id builder made: " + replyIdBuilder);

        String newReplyId = sceneIdToUse + "_" + replyIdBuilder;

        sceneReplyId = newReplyId;

        Log.i("buildNewSceneReplyId", "New reply id: " + newReplyId);

        return sceneReplyId;
    }

    // simple getter
    public String getNewReplyId() {
        Log.i("getNewReplyId", "Getting new reply id: " + sceneReplyId);
        return sceneReplyId;
    }

    // preps the literal scene to be used as a base. Strips away all other modifiers
    public int readAndReturnSceneNumber(String sceneToUse) {

        String strippedScene;
        int strippedSceneNumber;
        int underscoreCount = 0;

        for (char c : sceneToUse.toCharArray()) {
            if (c == '_') {
                underscoreCount++;
            }
        }

        if (underscoreCount > 0) {
            strippedScene = sceneToUse.split("_")[0]; // leaves sceneXX left

            Log.i("readReturnSceneNumber", "Stripped scene: " + strippedScene);
            strippedSceneNumber = Integer.parseInt(strippedScene.replace("scene", ""));
            Log.i("readReturnSceneNumber", "Stripped scene number: " + strippedSceneNumber);

        } else {
            Log.i("readReturnSceneNumber", "Scene to use: " + sceneToUse);
            strippedSceneNumber = Integer.parseInt(sceneToUse.replace("scene", ""));
            Log.i("readReturnSceneNumber", "Stripped scene number: " + strippedSceneNumber);
        }

        return strippedSceneNumber;
    }

    // TODO: if using new reference chain, may have to set which user reply was used in the on click
    // and send it here in order to build the next scene id which the correct chain

    // insanely important method. Creates the scene id that all other builders start from. Very simple
    // but that's intentional to make it more bulletproof. Takes in user's reply and uses that to create
    // the next one.
    public String createNewSceneId(String tempScene, String replyId) {

        // check scene for flag that it doesn't matter what the user chooses, we can build the id
        // independent of a choice
        isNextAnywayPresent = checkForNextAnyway();

        Log.d("createNewSceneId", "Temp scene: " + tempScene);
        Log.d("createNewSceneId", "Reply id: " + replyId);

        if (isNextAnywayPresent) { // is true

            tempSceneBuilder = tempSceneBuilder.replace((tempSceneBuilder.substring(tempSceneBuilder.length() - 1)),
                    String.valueOf(sceneIncrement));

            Log.d("createNewSceneId", "Next Anyway from create new id string split into: "
                    + tempSceneBuilder);

           updateUserSceneId(tempSceneBuilder);

        } else { // no next anyway, create reference chain normally

            int underscoreCount = 0; // counting occurrences, used to check if scene is nextanyway

            for (char c : userSceneId.toCharArray()) {
                if (c == '_') {
                    underscoreCount++;
                }
            }

            // means scene is fresh from a nextanyway scene
            if (underscoreCount < 1) {

                // grab reply id letter
                String lastCharOfReplyId = replyId.substring(replyId.length() - 1);

                Log.d("createNewSceneId", "Reply id letter: " + lastCharOfReplyId);

                // grab reply id number
                String strippedReplyId = replyId.substring(0, replyId.length()-1);

                Log.d("createNewSceneId", "Stripped reply id: " + strippedReplyId);

                // create last reply id without affecting anything else
                int replyidNumber = Integer.valueOf(strippedReplyId) - 1;

                if (replyidNumber > 1) { // everything after the first scene

                    // combine them all together
                    userSceneId = tempScene + "_" +
                            String.valueOf(replyidNumber) + lastCharOfReplyId;
                } else {

                    userSceneId = tempScene + "_" + replyId;
                }

                Log.d("createNewSceneId", "User's updated scene id: " + userSceneId);

            } else {

                userSceneId = tempScene;
            }

            updateUserSceneId(userSceneId); // update that bitch in db
        }

        return userSceneId;
    }

    // update user scene id in database
    public void updateUserSceneId(String newId) {

        Log.i("updateUserSceneId", "Updating user's scene id in database with: " + newId);

        mDatabaseRef.child("users").child(userId).child("userSceneId").setValue(newId);

        setUserSceneId(newId);
    }

    // get user scene id
    public String userSceneIdGetter() {
        Log.i("userSceneIdGetter", "Getting scene id as: " + userSceneId);
        return userSceneId;
    }

    // can probably change this to void, I like that behavior better anyway. Pretty important method,
    // checks for modifiers in the scene. Prevents checks from running when there's no reason to like
    // during a series of exposition scenes or something like that.
    public boolean checkForModifierFlag() {

        mDatabaseRef.child("act1").child("testIntro_CompanionText").child(userSceneId)
                .child("modifierFlag").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists() && dataSnapshot.getValue().equals(true)) {

                            Log.i("checkModifier", "Modifier flag exists");

                            isModifierFlagPresent = true;

                            checkForContinuePrompt();
                            checkForVideoFlag();
                            checkForPaywall();
                            checkForTimedEvent();
                            checkForKeySceneFlag();
                            checkForNextAnyway();

                        } else {
                            Log.i("checkModifier", "Modifier flag is not present");

                            isModifierFlagPresent = false;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

        return isModifierFlagPresent;
    }

    // check for next anyway flag in scene in database. Used for exposition or if I need to reset
    // any scene reference chains. Sends the user to the next scene regardless of what reply they
    // choose. Good stuff!
    public boolean checkForNextAnyway() {

        mDatabaseRef.child("act1").child("testIntro_CompanionText").child(userSceneId)
                .child("nextAnyway").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // obvious
                        if (dataSnapshot.exists() && dataSnapshot.getValue().equals(true)) {

                            Log.i("checkForNextAnyway",
                                    "Next anyway flag is present!");

                            isNextAnywayPresent = true;

                        } else {
                            Log.i("checkForNextAnyway", "No next anyway present flag, continue on");

                            isNextAnywayPresent = false;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
        return isNextAnywayPresent;
    }

    // ;)
    public void checkForPaywall() {

        mDatabaseRef.child("act1").child("testIntro_CompanionText").child(userSceneId)
                .child("paywallFlag").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists() && dataSnapshot.getValue().equals(true)) {

                            Log.i("checkForPaywall", "Time for paywall ;)");

                            isPaywallFlagPresent = true;

                            Intent iPaywall = new Intent(getActivity(), PaywallActivity.class);
                            startActivity(iPaywall);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                }
        );
    }

    // :D
    public void checkForPurchaseFlag() {

        mDatabaseRef.child("act1").child("testIntro_CompanionText").child(userSceneId)
                .child("purchaseFlag").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists() && dataSnapshot.getValue().equals(true)) {

                            Log.i("checkForPurchase", "Time for purchase :D");

                            isPurchaseFlagPresent = true;

                            Intent iPurchase = new Intent(getActivity(), PurchaseActivity.class);
                            startActivity(iPurchase);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    // :*
    // this going to be used more extensively later when I have enough content written to be able
    // to have separate story branches based on how in love the user is with their Companion.
    // I think I might veer away from completely platonic and always have some sort of tension b/t
    // the user and Companion, even if it is mostly one way. Just easier to write.
    public void checkForRelationshipRating(String whichUserReply) {

        mDatabaseRef.child("act1").child("testIntro_CompanionText").child(userSceneId)
                .child(whichUserReply).child("relationshipRating").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) { // check for modifier in scene

                            ratingModifier = (long) dataSnapshot.getValue();

                            Log.i("checkForRelationship",
                                    "Rating modifier is: " + ratingModifier);

                            mDatabaseRef.child("users").child(userId).child("relationshipRating")
                                    .addListenerForSingleValueEvent(

                                            // get user's current relationship rating
                                            new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    long currentRating = (long) dataSnapshot.getValue();

                                                    // RR = relationship rating
                                                    Log.i("checkForRelationship",
                                                            "Current RR: " + currentRating);

                                                    // create new rating
                                                    long newRating = currentRating + ratingModifier;

                                                    Log.i("checkForRelationship",
                                                            "New RR: " + newRating);

                                                    // apply new rating
                                                    mDatabaseRef.child("users")
                                                            .child(userId)
                                                            .child("relationshipRating").setValue(newRating);

                                                }
                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                }
                                            }
                                    );
                        } // else fuck off
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                }
        );
    }

    // simpley reads and stores Companion name if it's mentioned
    public boolean checkForCompanionNameInText(String dataSnapToUse) {

        boolean isCompanionNameInText;

        if (dataSnapToUse.contains("companionName")) {

            isCompanionNameInText = true;

            mDatabaseRef.child("users").child(userId).child("companionName")
                    .addListenerForSingleValueEvent(

                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    companionName = dataSnapshot.getValue().toString();
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.i("checkForCompanionName", "Shit went wrong");
                                }
                            }
                    );
        } else {
            isCompanionNameInText = false;
        }

        return isCompanionNameInText;
    }

    // checks and stores time greeting feature. Mostly used for immersion, it's a nice touch.
    public boolean checkForTimeGreeting(String datasnapToUse) {

        // morning 5-11, afternoon 11-5, evening 5-9, nighttime 9pm-2am, late night/early morning 2-5am.
        boolean isTimeGrettingPresent;

        if (datasnapToUse.contains("timeGreeting") || datasnapToUse.contains("weekday")) {

            isTimeGrettingPresent = true;

            Calendar localCalendar = Calendar.getInstance(TimeZone.getDefault());
            SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm:ss aa"); // 24 hour clock
            SimpleDateFormat sdfWeekDay = new SimpleDateFormat("EEEE"); // day of week

            String datetime = dateformat.format(localCalendar.getTime());
            Date date = new Date();
            String localDayOfWeek = sdfWeekDay.format(date);

            Log.i("checkForTime", "Formatted time is: " + datetime);
            Log.i("checkForTime", "Formatted date is: " + localDayOfWeek);

            int currentHour = Integer.parseInt(datetime.split(":")[0]);

            Log.i("checkForTime", "Current hour: " + currentHour);

            if (currentHour > 5 && currentHour <= 11) { // morning
                timeGreeting = "Good morning";
                Log.i("checkForTime", timeGreeting);

            } else if (currentHour > 11 && currentHour <=17) { // afternoon
                timeGreeting = "Good afternoon";
                Log.i("checkForTime", timeGreeting);

            } else if (currentHour > 17 && currentHour <= 21) { // evening
                timeGreeting = "Good evening";
                Log.i("checkForTime", timeGreeting);

            } else if (currentHour > 21 || currentHour <= 2) { // night
                timeGreeting = "Buenas noches";
                Log.i("checkForTime", timeGreeting);

                // not sure why this has a warning, it works fine
            } else if (currentHour > 2 && currentHour <= 5) { // early morning
                timeGreeting = "Good morning";
                Log.i("checkForTime", timeGreeting);
            }

            switch (localDayOfWeek) {
                case "Sunday":
                    dayOfWeek = "Sunday";
                    Log.i("checkForTime", dayOfWeek);
                    break;

                case "Monday":
                    dayOfWeek = "Monday";
                    Log.i("checkForTime", dayOfWeek);
                    break;

                case "Tuesday":
                    dayOfWeek = "Tuesday";
                    Log.i("checkForTime", dayOfWeek);
                    break;

                case "Wednesday":
                    dayOfWeek = "Wednesday";
                    Log.i("checkForTime", dayOfWeek);
                    break;

                case "Thursday":
                    dayOfWeek = "Thursday";
                    Log.i("checkForTime", dayOfWeek);
                    break;

                case "Friday":
                    dayOfWeek = "Friday";
                    Log.i("checkForTime", dayOfWeek);
                    break;

                case "Saturday":
                    dayOfWeek = "Saturday";
                    Log.i("checkForTime", dayOfWeek);
                    break;
            }

        } else {
            isTimeGrettingPresent = false;
        }

        return isTimeGrettingPresent;
    }

    // prompt to tell user in user replies to press to continue; used during a long exposition series
    // or similar. Not sure if I like this at all yet. Leaning towards no.
    public void checkForContinuePrompt() {

        mDatabaseRef.child("act1").child("testIntro_CompanionText").child(userSceneId)
                .child("continuePrompt").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            continuePrompt.setVisibility(View.VISIBLE);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    // checks for timed event and creates it if needed. This will be needed a lot in the future, I
    // really like it. Seems to work fine for now which is honestly insanely surprising
    public void checkForTimedEvent() {

        mDatabaseRef.child("act1").child("testIntro_CompanionText").child(userSceneId)
                .child("timedEvent").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            isTimedEventPresent = true;

                            // gets time length from db. Time is determined in each individual scene
                            progressMaxInLong = (long) dataSnapshot.getValue();

                            // comes as a long from db. Casts to int so it can be used later
                            progressMaxInSeconds = (int) progressMaxInLong;

                            Log.i("checkTimedEvent", "Event time: " + progressMaxInSeconds);

                            // display hint so user knows what's happening
                            timedEventText.setVisibility(View.VISIBLE);
                            timedEventText.startAnimation(fadeRepeat);

                            // set and show bar
                            numberProgressBar.setVisibility(View.VISIBLE);
                            numberProgressBar.setMax(progressMaxInSeconds * 10);
                            numberProgressBar.setProgress(progressMaxInSeconds * 10);

                            countDownTimer = new CountDownTimer(
                                    (progressMaxInSeconds * 1000), 100) {

                                // animate progress to smooth it out, and if user doesn't choose a
                                // reply in time have it automatically choose reply A
                                @Override
                                public void onTick(long millisUntilFinished) {

                                    milliProgress = Math.round(millisUntilFinished / 100);

                                    Log.i("progressAmount", "Milli progress: " + milliProgress);

                                    setProgressAnimate(numberProgressBar, milliProgress);

                                    // user ran out of time and didn't choose a reply. Goes to next
                                    // scene and choose reply A by default. Always make reply A the
                                    // one you want to be default in the scene.
                                    if (milliProgress == 3 || milliProgress == 2) {
                                        setNewSceneReplyId(getReplyAChildBuilder());
                                        createNewSceneId(tempSceneBuilder, getReplyAChildBuilder());

                                        buildNewSceneReplyId(userSceneId, getReplyAChildBuilder());

                                        getCompanionText();
                                        getUserReplyA();
                                        getUserReplyB();
                                        getUserReplyC();
                                        getUserReplyD();

                                    } else if (milliProgress == 1) {
                                        // clears everything once progress is done
                                        numberProgressBar.clearAnimation();
                                        numberProgressBar.setVisibility(View.GONE);

                                        timedEventText.setVisibility(View.GONE);
                                        timedEventText.clearAnimation();
                                    }
                                }

                                @Override
                                public void onFinish() {
                                    numberProgressBar.startAnimation(fadeOut);
                                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                        @Override
                                        public void onAnimationStart(Animation animation) {
                                        }

                                        @Override
                                        public void onAnimationEnd(Animation animation) {
                                            Log.i("progressAmount", "Progress Amount: " + progressAmount);
                                            progressAmount = 0;
                                            fadeOut.cancel();
                                            fadeOut.reset();
                                            numberProgressBar.clearAnimation();
                                        }

                                        @Override
                                        public void onAnimationRepeat(Animation animation) {
                                        }
                                    });
                                }
                            };
                            countDownTimer.start();

                        } else {
                            isTimedEventPresent = false;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    // checks for key scene in order to save user's place in case of crashes or to return to the main
    // story from a side story. Pretty simple but important. Sets it in user's db node.
    public void checkForKeySceneFlag() {

        mDatabaseRef.child("act1").child("testIntro_CompanionText").child(userSceneId)
                .child("keyScene").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // obvious
                        if (dataSnapshot.exists() && dataSnapshot.getValue().equals(true)) {

                            Log.i("checkForKeyScene",
                                    "Key scene flag is present!");

                            lastKeySceneId = userSceneId;

                            mDatabaseRef.child("users")
                                    .child(userId)
                                    .child("lastKeySceneId").setValue(lastKeySceneId);

                        } else {

                            isNextAnywayPresent = false;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    // logic to build audio reference if the Companion text includes any names. Will probably have
    // to add time greetings to this, but I might put those in their own thing since they're
    // relatively static and only referenced once each time the user opens the app. We'll see
    private void getCustomAudioRef() {

        if (companionText.getText().toString().contains(userName)) {

            audioRef = userSceneId + userName;

            if (userName.contains("í")) {
                String tempName = userName.replace("í", "i") + "A";
                audioRef = userSceneId + tempName;

                Log.i("getCustomAudioRef", "User's username audio ref: " + audioRef);

                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                StorageReference audioDownloadUrl = storageReference
                        .child("/" + tempName + "/" + audioRef + ".mp3");

                audioDownloadUrl.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                {
                    @Override
                    public void onSuccess(Uri downloadUrl)
                    {
                        Log.i("checkAudioRef", "Download url is: " + downloadUrl);

                        fetchAudioUrlFromFirebase(downloadUrl.toString());
                    }
                });

                audioDownloadUrl.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("checkAudioRef", "Download username url failed");
                    }
                });

            } else {

                Log.i("getCustomAudioRef", "User's username audio ref: " + audioRef);

                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                StorageReference audioDownloadUrl = storageReference
                        .child("/" + userName + "/" + audioRef + ".mp3");

                audioDownloadUrl.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                {
                    @Override
                    public void onSuccess(Uri downloadUrl)
                    {
                        Log.i("checkAudioRef", "Download url is: " + downloadUrl);

                        fetchAudioUrlFromFirebase(downloadUrl.toString());
                    }
                });

                audioDownloadUrl.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("checkAudioRef", "Download username url failed");
                    }
                });
            }

        } else if (companionText.getText().toString().contains(companionName)){

            audioRef = userSceneId + companionName;

            Log.i("getCustomAudioRef", "Companion's name audio ref: " + audioRef);

            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference audioDownloadUrl = storageReference
                    .child("/" + companionName + "/" + audioRef + ".mp3");

            audioDownloadUrl.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
            {
                @Override
                public void onSuccess(Uri downloadUrl)
                {
                    Log.i("checkAudioRef", "Download url is: " + downloadUrl);

                    fetchAudioUrlFromFirebase(downloadUrl.toString());
                }
            });

            audioDownloadUrl.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("checkAudioRef", "Download companion name url failed");
                }
            });
        }
    }

    // builds audio reference based on corresponding scene. Companion text has no modifiers, so a
    // custom reference isn't needed and it can just play straight up.
    private void getAudioRef() {

        Log.i("getAudioRef", "User has no text modifiers, play normal audio");

        mDatabaseRef.child("act1").child("testIntro_CompanionText").child(userSceneId)
                .child("audioRef").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            // get storage reference
                            audioRef = dataSnapshot.getValue().toString();

                            Log.i("checkAudioRef", "Audio reference is: " +
                                    audioRef);

                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                            StorageReference audioDownloadUrl = storageRef.child("/" + audioRef + ".mp3");

                            audioDownloadUrl.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                            {
                                @Override
                                public void onSuccess(Uri downloadUrl)
                                {
                                    Log.i("checkAudioRef", "Download url is: " + downloadUrl);

                                    fetchAudioUrlFromFirebase(downloadUrl.toString());
                                }
                            });

                        } else {
                            Log.i("checkAudioRef", "Audio ref ain't there :/");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    // gets audio from storage location in fb using the reference built earlier
    private void fetchAudioUrlFromFirebase(String audioRefFromDb) {

        Log.i("fetchAudio", "Audio ref from db is: " + audioRefFromDb);

        StorageReference storageRef = firebaseStorage
                .getReferenceFromUrl(audioRefFromDb);
        Task<Uri> tag = storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    // Download url of file
                    String url = uri.toString();

                    // give the file to the media player
                    mediaPlayer.setDataSource(url);
                    // wait for media player to prepare
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("TAG", e.getMessage());
                    }
                });

    }

    // }(____*)
    private void playAudio() {
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                // play file
                mediaPlayer.start();

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mediaPlayer.reset(); // clear audio to prepare for next one. VERY important
                    }
                });
            }
        });
    }

    // checks scene node for video reference
    public void checkForVideoFlag() {

        mDatabaseRef.child("act1").child("testIntro_CompanionText").child(userSceneId)
                .child("videoFlag").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // obvious
                        if (dataSnapshot.exists()) {

                            Log.i("checkVideoFlag",
                                    "Video flag is present!");

                            videoRef = "videoTester";

                            Log.i("getVideoRef", "User's video ref: " + videoRef);

                            // builds url reference based on user id and the reference gotten from the scene
                            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                            StorageReference videoDownloadUrl = storageReference
                                    .child("/" + userId + "/" + videoRef + ".mp4");

                            videoDownloadUrl.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                            {
                                @Override
                                public void onSuccess(Uri downloadUrl)
                                {
                                    Log.i("checkVideoRef", "Download url is: " + downloadUrl);

                                    fetchVideoUrlFromFirebase(downloadUrl.toString());
                                }
                            });

                            videoDownloadUrl.getDownloadUrl().addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i("checkVideoRef", "Download url failed");
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                }
        );
    }

    // downloads video file from storage using reference built earlier
    private void fetchVideoUrlFromFirebase(String videoRefFromDb) {

        Log.i("fetchVideo", "Video ref from db is: " + videoRefFromDb);

        StorageReference storageRef = firebaseStorage
                .getReferenceFromUrl(videoRefFromDb);
        Task<Uri> tag = storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Download url of file
                String uriString = uri.toString();

                // parse it to be loaded
                Uri uriParsed = Uri.parse(uriString);

                Log.i("fetchVideo", "Uri string is: " + uriString);

                playVideoView(uriParsed);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("TAG", e.getMessage());
                    }
                });
    }

    // this functionally works now, but I'm going to change it to the scalable video view since I
    // can't get this native one to crop properly. I'm pretty sure most of this can be re-used so
    // we'll keep it for now.
    private void playVideoView(Uri uri) {

        Log.i("videoView", "Video view is doing something");

        videoViewTester.setVideoURI(uri);

        videoViewTester.setVisibility(View.VISIBLE);
        videoViewLayout.setVisibility(View.VISIBLE);
        videoProgressBar.setVisibility(View.VISIBLE);

        videoViewTester.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // end progress dialog or whatever here
                videoProgressBar.setVisibility(View.GONE);

                Log.i("videoView", "Video view is prepared and doing stuff");
                videoViewTester.start();
                videoDismiss.setVisibility(View.VISIBLE);
            }
        });

        videoViewTester.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // this loops the video
                Log.i("videoView", "It's done and is going again");

                videoViewTester.start();
            }
        });

        videoViewTester.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("videoView", "Stopping the video with the onclick");

                videoViewTester.stopPlayback();
                videoViewTester.setVisibility(View.GONE);
                videoViewLayout.setVisibility(View.GONE);
                videoDismiss.setVisibility(View.GONE);
            }
        });
    }

    // animation to smooth out any progress bars being used
    private void setProgressAnimate(NumberProgressBar pb, int progressTo) {

        ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), progressTo);
        animation.setDuration(100); // lower is faster, in MS
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

                // TODO: may be able to put background flag checker here and just have it constantly check
                // everytime the thread goes through the array

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

        Thread backgroundThread = new Thread(new BackgroundGradientThread(getContext()));
        backgroundThread.start();
    }
} // end of file
