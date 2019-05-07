package com.martin.sodalis;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class PurchaseActivity extends AppCompatActivity {

    private Button oneSkip;
    private Button threeSkips;
    private Button sixSkips;
    private Button unlimitedSkips;

    /**
     * activity that holds purchase skips. Will update with google in-app purchases later
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_skips);

        // force screen to stay in portrait
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // keep screen on indefinitely
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        oneSkip = findViewById(R.id.purchase_one);
        threeSkips = findViewById(R.id.purchase_three);
        sixSkips = findViewById(R.id.purchase_six);
        unlimitedSkips =  findViewById(R.id.purchase_unlimited);

        oneSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        threeSkips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        sixSkips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        unlimitedSkips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    } // end of oncreate
}
