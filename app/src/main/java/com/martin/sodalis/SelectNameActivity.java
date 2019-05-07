package com.martin.sodalis;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/** activity that selects user's name from list of default names
 *
 */

public class SelectNameActivity extends AppCompatActivity {

    private Spinner nameSpinner;

    private String userName;
    private String userId;

    private TextView noName;

    private Button done;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase fbDatabase;
    private DatabaseReference mDatabaseRef;

    private static final String TAG = "SelectNameActivity";
    private static final String USERS_CHILD = "users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_name);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // initialize UI elements
        nameSpinner = findViewById(R.id.choose_name_spinner);

        done = findViewById(R.id.done_button);

        noName = findViewById(R.id.wheres_myname);

        userId = getUid();

        // initialize firebase instances
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        fbDatabase = FirebaseDatabase.getInstance();

        noName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(SelectNameActivity.this)

                        // user doesn't see their name, explain why
                        .setMessage(R.string.no_name_text)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d(TAG, "onClick: OK");
                            }
                        })
                        .setCancelable(true)
                        .show();
            }
        });

        // get and set user's name from the spinner and close activity
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                userName = nameSpinner.getSelectedItem().toString();

                saveUserName(userName);

                Log.i(TAG, "Setting user's name as: " + userName);

                finish();
            }
        });

    } // end of oncreate

    // set user's name in their db node
    public void saveUserName(String userName) {

        mDatabaseRef = fbDatabase.getReference(USERS_CHILD);
        mDatabaseRef.child(userId).child("userName").setValue(userName);
    }

    // get user's id
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
