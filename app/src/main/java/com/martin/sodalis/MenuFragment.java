package com.martin.sodalis;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class MenuFragment extends Fragment {

    private TextView faq;
    private TextView privacyPolicy;
    private TextView contact;
    private TextView rateUs;
    private TextView logout;
    private TextView viewCompanion;

    private SharedPreferences loginPrefs;

    private static final String TAG = "MenuFragment";

    /**
     * settings fragment that holds the usual suspects. Most important stuff is probably the logout
     * and view Companion options. Links for faq, priv policy, rate us, and contact email will all
     * be updated once I get a website going and have those pages ready. For now they're just
     * placeholders obviously.
     *
     */

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View menuView = inflater.inflate(R.layout.fragment_menu, container, false);

        // initialize all textview buttons. They're global in case I need to use them elsewhere later.
        faq = menuView.findViewById(R.id.settings_faq);
        privacyPolicy = menuView.findViewById(R.id.settings_privacy);
        contact = menuView.findViewById(R.id.settings_contact);
        rateUs = menuView.findViewById(R.id.settings_rateus);
        logout = menuView.findViewById(R.id.logout);
        viewCompanion = menuView.findViewById(R.id.settings_view_appearance);

        loginPrefs = getActivity().getSharedPreferences("prefs", 0);

        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // link to faq
                Intent browserFaqIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.pbnation.com"));
                startActivity(browserFaqIntent);
            }
        });

        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // link to privacy policy
                Intent browserPolicyIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.pbnation.com/forum.php"));
                startActivity(browserPolicyIntent);
            }
        });

        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // link to support/contact page
                Intent browserSupportIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.pbnation.com/forumdisplay.php?f=13"));
                startActivity(browserSupportIntent);
            }
        });

        rateUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        viewCompanion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iView = new Intent(getActivity(), ViewCompanionActivity.class);
                startActivity(iView);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(getActivity())

                        // is logout supposed to be 1 or 2 words?? UGH
                        .setMessage("Logout from Sodalis?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d(TAG, "onClick: yes");

                                FirebaseAuth.getInstance().signOut();

                                SharedPreferences.Editor editor = loginPrefs.edit();
                                editor.putBoolean("userLoggedIn", false);
                                editor.apply();

                                Log.i(TAG, "User signed out");
                                Intent iSignIn = new Intent(getActivity(), SignInActivity.class);
                                startActivity(iSignIn);

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

        return menuView;
    }
}
