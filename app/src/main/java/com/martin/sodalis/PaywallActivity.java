package com.martin.sodalis;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

/**
 * holds skip paywall that explains what is happening. Will prompt user to use their skips or for
 * them to buy skips if they want. Also has timer instead of skips
 */

public class PaywallActivity extends AppCompatActivity {

    private Button payToSkip;

    private TickerView timer;

    private CountDownTimer countDownTimer;

    private long timeHolderMilliseconds;
    private long timeHolder;

    private static final String TAG = "PaywallActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paywall);

        // screen and status bar modifiers
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        payToSkip = findViewById(R.id.paywall_skip);

        // initialize timer and set it to numbers only
        timer = findViewById(R.id.paywall_timer);
        timer.setCharacterList(TickerUtils.getDefaultNumberList());

        // timer needs to be redone also to hold time when user opens/closes app.
        setTimer();
        startTimer();

        // purchase mechanism goes here
        payToSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    } // end of oncreate

    // set timer amount method
    public void setTimer() {

        // set initial time to 0, in minutes
        int time = 720; // 12 hours

        // save match time
        timeHolderMilliseconds = 60 * time * 1000;
    }

    // begin timer method
    private void startTimer() {

        // create countdown timer
        countDownTimer = new CountDownTimer(timeHolderMilliseconds, 1000) {
            // 1000 = onTick function will be called every 1 second

            @Override
            public void onTick(long leftTimeInMilliseconds) {
                long seconds = leftTimeInMilliseconds / 1000;

                // save time in global time holder
                timeHolder = leftTimeInMilliseconds;

                // set text to show on timer, format: HH:MM:SS
                timer.setText(String.format("%02d:%02d:%02d", (seconds / 3600),
                        (seconds % 3600) / 60, (seconds % 60)));

                if (seconds == 0) {

                    timer.setText("00:00:00");
                }
            }

            @Override
            public void onFinish() {

            }

        }.start();
    }
}
