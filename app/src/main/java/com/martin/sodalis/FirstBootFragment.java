package com.martin.sodalis;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.carlosmuvi.segmentedprogressbar.SegmentedProgressBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * big boy fragment that holds the entire sign up sequence. The order of things can be changed but I
 * kind of like it how it is. We'll see. Launches activities for specific stuff like entering email
 * info and choosing names. Is pretty stable in launching the correct activity user left if they
 * come back. Not sure if hardcoding the sequences is the best practice, but for some reason it seemed
 * right considering so much other stuff is db read from the user's node. Ideally someone could
 * clarify that for me!!!
 *
 */

public class FirstBootFragment extends Fragment {

    private TextView companionText;
    private TextView userReplyA;
    private TextView userReplyB;
    private TextView userReplyC;
    private TextView userReplyD;
    private TextView alreadyHaveAccount;

    private View firstBootView;
    private View typingCircle1;
    private View typingCircle2;
    private View typingCircle3;

    private Animation fadeIn;
    private Animation fadeOut;
    private Animation typingAnim1;
    private Animation typingAnim2;
    private Animation typingAnim3;

    private String userId;
    private String userName;

    private int sceneNumberFlag = 0;

    private boolean needAReply = true;

    private SegmentedProgressBar segmentedProgressBar;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabaseRef;

    private MediaPlayer mediaPlayer;

    private static final String TAG = "FirstBootFragment";
    private static int SPLASH_TIME_OUT = 4000; // I know you're not supposed to do this but I like it

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        firstBootView = inflater.inflate(R.layout.fragment_firstboot, container, false);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        // CUSTOM typing circles. These are cool idgaf
        typingCircle1 = firstBootView.findViewById(R.id.typing_circle1);
        typingCircle2 = firstBootView.findViewById(R.id.typing_circle2);
        typingCircle3 = firstBootView.findViewById(R.id.typing_circle3);

        // progress bar I big ripped from IG, but it's perfect again idgaf
        segmentedProgressBar = firstBootView.findViewById(R.id.segmented_progressbar);
        segmentedProgressBar.setVisibility(View.GONE);

        // initialize animations
        fadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        typingAnim1 = AnimationUtils.loadAnimation(getContext(), R.anim.typing_anim);
        typingAnim2 = AnimationUtils.loadAnimation(getContext(), R.anim.typing_anim);
        typingAnim2.setStartOffset(200);
        typingAnim3 = AnimationUtils.loadAnimation(getContext(), R.anim.typing_anim);
        typingAnim3.setStartOffset(300);

        // first text from user's companion :')
        companionText = firstBootView.findViewById(R.id.companion_text_area);
        companionText.setText(R.string.firstboot_welcome_1);
        companionText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);

        // user reply text views. Great system!
        userReplyA = firstBootView.findViewById(R.id.user_reply_A);
        userReplyB = firstBootView.findViewById(R.id.user_reply_B);
        userReplyC = firstBootView.findViewById(R.id.user_reply_C);
        userReplyD = firstBootView.findViewById(R.id.user_reply_D);

        alreadyHaveAccount = firstBootView.findViewById(R.id.already_have_account);

        alreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent iSignIn = new Intent(getActivity(),
                        SignInActivity.class);
                startActivity(iSignIn);

                getActivity().finish();
            }
        });

        if (mFirebaseUser == null || getUid() == null) {
            // continue on my child
            Log.i(TAG, "User is completely new, continuing on");
        } else {
            // User already has account, but hasn't completed setup. Already checked for completion
            // in first boot activity

            userId = getUid();
            Log.i(TAG, "User is signed in: " + userId);

            // Ok here checks the flags to send the user to which ever screen they last did and if
            // they exited the app. Super important because this forces them to make all the correct
            // nodes even after if they create an email and password and didn't finish the sign up.
            // then we can make sure they complete their node and start the 'story'.

            mDatabaseRef.child("users").child(userId).child("setupLastCompleted")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            Log.i(TAG, "User has setup but is not reading");

                            if (dataSnapshot.exists()) {

                                Log.i(TAG, "User's last setup is: " + dataSnapshot.getValue().toString());

                                String lastSceneCompleted = dataSnapshot.getValue().toString();

                                switch (lastSceneCompleted) {
                                    // go to scene + 1
                                    case "scene3":
                                        scene4ChooseName();
                                        break;

                                    case "scene4":
                                        scene5();
                                        break;

                                    case "scene5":
                                        scene6();
                                        break;

                                    case "scene6":
                                        scene7();
                                        break;
                                } // other scenes are omitted because the user's saved data isn't relevant
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                            Log.i(TAG, "User's last setup has error");
                        }
                    });
        }

        return firstBootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        // Setup any handles to view objects here
        userReplyA.setText(R.string.next);
        userReplyA.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
        userReplyA.setTextColor(Color.parseColor("#ffffff"));

        userReplyB.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
        userReplyB.setTextColor(Color.parseColor("#ffffff"));

        userReplyC.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
        userReplyC.setTextColor(Color.parseColor("#ffffff"));

        userReplyD.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
        userReplyD.setTextColor(Color.parseColor("#ffffff"));

        userReplyB.setVisibility(View.GONE);
        userReplyC.setVisibility(View.GONE);
        userReplyD.setVisibility(View.GONE);

        typingCircle1.setVisibility(View.GONE);
        typingCircle2.setVisibility(View.GONE);
        typingCircle3.setVisibility(View.GONE);

        if (mFirebaseUser == null || getUid() == null) {

            // this is the user's first time using the app. Play the first voice recording. The voice
            // of the Sodalis OS.
            mediaPlayer= MediaPlayer.create(getContext(), R.raw.intro1);
            mediaPlayer.start();
        }

        if (getNeedAReply()) {

            userReplyA.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }

                    companionText.startAnimation(fadeOut);
                    hideUserReplies();
                    alreadyHaveAccount.setVisibility(View.GONE);

                    // do all the typing animation stuff and load the next frag
                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            showTypingAnimation();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    typingAnim3.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }
                        @Override
                        public void onAnimationEnd(Animation animation) {

                            hideTypingAnimation();

                            scene2PrivPolicy();
                        }
                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            });
        }
    }

    // ok each scene should be fairly descriptive when they matter.
    public void scene2PrivPolicy () {

        // double check to make user has an account already if on a new device or new download
        alreadyHaveAccount.setVisibility(View.GONE);

        hideTypingAnimation();

        // play voice prompt. This is the voice of the sodalis OS
        mediaPlayer= MediaPlayer.create(getContext(), R.raw.intro2);
        mediaPlayer.start();

        // set Companion text response. This is super important and will be re-written a lot
        companionText.setText(R.string.firstboot_welcome_2);
        companionText.startAnimation(fadeIn);

        userReplyA.setText(R.string.ok);
        slideFromRight(userReplyA);

        // fill dat insta progress bar! :D 1000 feels right btw, can be changed.
        segmentedProgressBar.setVisibility(View.VISIBLE);
        segmentedProgressBar.playSegment(1000); // fill 1 second

        // still have to set the terms of service link. Get that squarespace up!
        userReplyA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }

                companionText.startAnimation(fadeOut);
                hideUserReplies();

                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        showTypingAnimation().setAnimationListener(new Animation.AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                scene3BasicInfo();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
            }
        });
    }

    // create account 'scene'. Creates account in fb db with email and password and inputs all the
    // corresponding nodes. Prompts choose user's name when complete
    public void scene3BasicInfo () {

        hideTypingAnimation();

        if (userId == null) {
            mediaPlayer= MediaPlayer.create(getContext(), R.raw.intro3);
            mediaPlayer.start();
        }

        companionText.setText(R.string.firstboot_welcome_3);
        companionText.startAnimation(fadeIn);

        userReplyA.setText(R.string.ok);
        slideFromRight(userReplyA);

        segmentedProgressBar.playSegment(1000); // fill 2

        if (mFirebaseUser == null) {

            // Not signed in, user still needs to create account, get it rolling
            userReplyA.setText(R.string.create_account);
            userReplyA.setVisibility(View.VISIBLE);

            userReplyA.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }

                    Intent iInfoEntry = new Intent(getActivity(), InfoEntryActivity.class);
                    startActivityForResult(iInfoEntry, 10001);

                    userReplyB.setText(R.string.done_making_account);
                    userReplyB.setVisibility(View.VISIBLE);

                    userReplyB.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            hideUserReplies();

                            userId = getUid();

                            if (mediaPlayer != null) {
                                mediaPlayer.stop();
                                mediaPlayer.reset();
                            }

                            Log.i(TAG, "User reply B was clicked");

                            companionText.startAnimation(fadeOut);

                            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    showTypingAnimation().setAnimationListener(new Animation.AnimationListener() {

                                        @Override
                                        public void onAnimationStart(Animation animation) {
                                        }

                                        @Override
                                        public void onAnimationEnd(Animation animation) {
                                            scene4ChooseName();
                                        }

                                        @Override
                                        public void onAnimationRepeat(Animation animation) {
                                        }
                                    });

                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                }
                            });
                        }
                    });

                }
            });

        }  else {

            mDatabaseRef.child("users").child(userId).addValueEventListener(

                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (!(dataSnapshot.exists())) {
                                Log.i(TAG, "User doesn't exist yet");

                            } else {
                                userReplyA.setText(R.string.done);
                                slideFromRight(userReplyA);

                                userReplyA.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        if (mediaPlayer != null) {
                                            mediaPlayer.stop();
                                            mediaPlayer.reset();
                                        }

                                        companionText.startAnimation(fadeOut);
                                        hideUserReplies();

                                        fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                            @Override
                                            public void onAnimationStart(Animation animation) {
                                            }

                                            @Override
                                            public void onAnimationEnd(Animation animation) {
                                                showTypingAnimation().setAnimationListener(new Animation.AnimationListener() {

                                                    @Override
                                                    public void onAnimationStart(Animation animation) {
                                                    }

                                                    @Override
                                                    public void onAnimationEnd(Animation animation) {
                                                        scene4ChooseName();
                                                    }

                                                    @Override
                                                    public void onAnimationRepeat(Animation animation) {
                                                    }
                                                });

                                            }

                                            @Override
                                            public void onAnimationRepeat(Animation animation) {
                                            }
                                        });
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
    }

    // chooses user's name from the default list and the launches the next scene (male/female).
    // animations make it a little annoying but it works.
    public void scene4ChooseName() {

        hideTypingAnimation();

        // stop voice if the user can read super fast. Play normal one once it's ready
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();

            mediaPlayer= MediaPlayer.create(getContext(), R.raw.intro4);
            mediaPlayer.start();
        } else {
            mediaPlayer= MediaPlayer.create(getContext(), R.raw.intro4);
            mediaPlayer.start();
        }

        // hard coded stuff. No name input but come on! I don't have natural lang people working for now.
        companionText.setText(R.string.firsboot_welcome_4);
        companionText.startAnimation(fadeIn);

        userReplyA.setText(R.string.choose_name);
        slideFromRight(userReplyA);

        segmentedProgressBar.playSegment(1000); //  fill 3

        if (mFirebaseUser == null) {
            scene3BasicInfo();
        }

        userId = getUid();

        Log.i(TAG, "Firebase user is valid, checking for userid");

        Log.i(TAG, "User id is: " + userId);

        // should be the current scene - 1
        mDatabaseRef.child("users").child(userId).child("setupLastCompleted").setValue("scene3");

        mDatabaseRef.child("users").child(userId).child("userName")
                .addValueEventListener(

                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (!(dataSnapshot.getValue().toString().equals("Default Name"))) {
                                    // user has name good to go
                                    userName = dataSnapshot.getValue().toString();

                                    Log.i(TAG, "User's name is: " + userName);

                                    userReplyA.setText(userName);

                                    userReplyA.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            if (mediaPlayer != null) {
                                                mediaPlayer.stop();
                                                mediaPlayer.reset();
                                            }

                                            hideUserReplies();
                                            companionText.startAnimation(fadeOut);

                                            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                                @Override
                                                public void onAnimationStart(Animation animation) {

                                                }

                                                @Override
                                                public void onAnimationEnd(Animation animation) {

                                                    showTypingAnimation()
                                                            .setAnimationListener(new Animation.AnimationListener() {
                                                                @Override
                                                                public void onAnimationStart(Animation animation) {

                                                                }

                                                                @Override
                                                                public void onAnimationEnd(Animation animation) {

                                                                    scene5();
                                                                }

                                                                @Override
                                                                public void onAnimationRepeat(Animation animation) {

                                                                }
                                                            });

                                                }

                                                @Override
                                                public void onAnimationRepeat(Animation animation) {

                                                }
                                            });
                                        }
                                    });

                                } else {

                                    companionText.setText(R.string.firsboot_welcome_4);

                                    userReplyA.setText(R.string.choose_name);

                                    userReplyA.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            if (mediaPlayer != null) {
                                                mediaPlayer.stop();
                                                mediaPlayer.reset();
                                            }

                                            Intent iChooseName = new Intent(getActivity(),
                                                    SelectNameActivity.class);
                                            startActivity(iChooseName);
                                        }
                                    });
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                                companionText.setText(R.string.firsboot_welcome_4);

                                userReplyA.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        if (mediaPlayer != null) {
                                            mediaPlayer.stop();
                                            mediaPlayer.reset();
                                        }

                                        Intent iChooseName = new Intent(getActivity(),
                                                SelectNameActivity.class);
                                        startActivity(iChooseName);
                                    }
                                });
                            }
                        }
                );

        userReplyB.setVisibility(View.GONE);
    }

    public void scene5() { // male or female

        hideTypingAnimation();

        mediaPlayer= MediaPlayer.create(getContext(), R.raw.intro5);
        mediaPlayer.start();

        companionText.setText(R.string.firstboot_welcome_5);
        companionText.startAnimation(fadeIn);

        userReplyA.setText(R.string.firstboot_reply_5A);
        slideFromRight(userReplyA);

        userReplyB.setText(R.string.firstboot_reply_5B);
        slideFromLeft(userReplyB);

        // should be the current scene - 1
        mDatabaseRef.child("users").child(userId).child("setupLastCompleted").setValue("scene4");

        segmentedProgressBar.playSegment(1000); // fill 4

        userId = getUid();

        userReplyA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }

                hideUserReplies();

                String companionVoice = "female";

                mDatabaseRef.child("users").child(userId).child("companionVoice").setValue(companionVoice);

                Log.i(TAG, "Companion voice: " + companionVoice);

                companionText.startAnimation(fadeOut);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        showTypingAnimation().setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                scene6();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        });

        userReplyB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }

                hideUserReplies();

                String companionVoice = "male";

                mDatabaseRef.child("users").child(userId).child("companionVoice").setValue(companionVoice);

                Log.i(TAG, "Companion voice: " + companionVoice);

                companionText.startAnimation(fadeOut);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        showTypingAnimation().setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                scene6();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        });
    }

    // how often speak with companion, this doesn't actually do anything yet, mostly for immersion
    public void scene6() {

        hideTypingAnimation();

        mediaPlayer= MediaPlayer.create(getContext(), R.raw.intro6);
        mediaPlayer.start();

        companionText.setText(R.string.firstboot_welcome_6);
        companionText.startAnimation(fadeIn);

        userReplyA.setText(R.string.firstboot_reply_6A);
        slideFromRight(userReplyA);

        userReplyB.setText(R.string.firstboot_reply_6B);
        slideFromLeft(userReplyB);

        userReplyC.setText(R.string.firstboot_reply_6C);
        slideFromRight(userReplyC);

        // should be the current scene - 1
        mDatabaseRef.child("users").child(userId).child("setupLastCompleted").setValue("scene5");

        segmentedProgressBar.playSegment(1000); // fill 5

        userReplyA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }

                hideUserReplies();

                companionText.startAnimation(fadeOut);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        showTypingAnimation().setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                scene7();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        });

        userReplyB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }

                hideUserReplies();

                companionText.startAnimation(fadeOut);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        showTypingAnimation().setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                scene7();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        });

        userReplyC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }

                hideUserReplies();

                companionText.startAnimation(fadeOut);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        showTypingAnimation().setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                scene7();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        });
    }

    // quick rundown. One more immersion exposition thing really
    public void scene7() {

        hideTypingAnimation();

        mediaPlayer= MediaPlayer.create(getContext(), R.raw.intro7);
        mediaPlayer.start();

        companionText.setText(R.string.firstboot_welcome_7);
        companionText.startAnimation(fadeIn);

        userReplyA.setText(R.string.firstboot_reply_7A);
        slideFromRight(userReplyA);

        userReplyB.setText(R.string.firstboot_reply_7B);
        slideFromLeft(userReplyB);

        userReplyC.setText(R.string.firstboot_reply_7C);
        slideFromRight(userReplyC);

        // should be the current scene - 1
        mDatabaseRef.child("users").child(userId).child("setupLastCompleted").setValue("scene6");

        segmentedProgressBar.playSegment(1000); // fill 6

        userReplyA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }

                hideUserReplies();

                companionText.startAnimation(fadeOut);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        showTypingAnimation().setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                scene8();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        });

        userReplyB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }

                hideUserReplies();

                companionText.startAnimation(fadeOut);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        showTypingAnimation().setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                scene8();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        });

        userReplyC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }

                hideUserReplies();

                companionText.startAnimation(fadeOut);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        showTypingAnimation().setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                scene8();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        });
    }

    public void scene8() { // thank you/please wait

        hideUserReplies();
        hideTypingAnimation();

        mediaPlayer= MediaPlayer.create(getContext(), R.raw.intro8);
        mediaPlayer.start();

        companionText.setText(R.string.firstboot_welcome_8);
        companionText.startAnimation(fadeIn);

        // should be the current scene - 1
        mDatabaseRef.child("users").child(userId).child("setupDone").setValue("true");

        mDatabaseRef.child("users").child(userId).child("setupLastCompleted").setValue(null);

        segmentedProgressBar.setVisibility(View.GONE);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {

                        if (mediaPlayer != null) {
                            mediaPlayer.stop();
                            mediaPlayer.reset();
                        }

                        Intent iDoneWithIntro = new Intent(new Intent(getActivity(),
                                BootUp.class));
                        startActivity(iDoneWithIntro);

                        getActivity().finish();
                    }
                }, SPLASH_TIME_OUT);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public boolean getNeedAReply() {
        return needAReply;
    }

    public Animation showTypingAnimation() {

        typingCircle1.setVisibility(View.VISIBLE);
        typingCircle2.setVisibility(View.VISIBLE);
        typingCircle3.setVisibility(View.VISIBLE);

        typingCircle1.startAnimation(typingAnim1);
        typingCircle2.startAnimation(typingAnim2);
        typingCircle3.startAnimation(typingAnim3);

        return typingAnim3;
    }

    public void hideTypingAnimation() {

        typingCircle1.clearAnimation();
        typingCircle2.clearAnimation();
        typingCircle3.clearAnimation();

        typingCircle1.setVisibility(View.GONE);
        typingCircle2.setVisibility(View.GONE);
        typingCircle3.setVisibility(View.GONE);
    }

    public void hideUserReplies() {

        userReplyA.clearAnimation();
        userReplyB.clearAnimation();
        userReplyC.clearAnimation();
        userReplyD.clearAnimation();

        userReplyA.setVisibility(View.GONE);
        userReplyB.setVisibility(View.GONE);
        userReplyC.setVisibility(View.GONE);
        userReplyD.setVisibility(View.GONE);
    }

    public void slideFromRight(View view){
        TranslateAnimation animate = new TranslateAnimation(firstBootView.getWidth(), 0,0,0);
        animate.setDuration(200);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.VISIBLE);
    }

    public void slideFromLeft(View view){
        TranslateAnimation animate = new TranslateAnimation(-firstBootView.getWidth(),0,0,0);
        animate.setDuration(200);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.VISIBLE);
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
