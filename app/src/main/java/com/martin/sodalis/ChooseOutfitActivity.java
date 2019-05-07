package com.martin.sodalis;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class ChooseOutfitActivity extends AppCompatActivity {

    private TextView coinsAmount;

    private ImageView coinsImage;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * Viewpager that holds all of the Companion outfit selections. Can be altered to fit however many
     * are needed, but the off screen page limit MUST be updated if more selections are added.
     */

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_outfit);

        // screen and status bar modifiers
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        // initialize coins and their listeners
        coinsAmount = findViewById(R.id.coins_amount);
        coinsImage = findViewById(R.id.coins_image);

        coinsAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iCoins = new Intent(getApplicationContext(), PurchaseCoinsActivity.class);
                startActivity(iCoins);
            }
        });
        coinsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iCoins = new Intent(getApplicationContext(), PurchaseCoinsActivity.class);
                startActivity(iCoins);
            }
        });

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3); // KEEPS VIDEOS SMOOTH

    } // end of oncreate

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
                    return new Outfit0Fragment();
                case 1:
                    return new Outfit1Fragment();
                case 2:
                    return new Outfit2Fragment();
                case 3:
                    return new Outfit3Fragment();
                default:
            }
            return null;
        }

        @Override
        public int getCount() {

            return 4;
        }
    }
}
