package com.martin.sodalis;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

/**
 * activity where creates their account with their email and password. Also enters their birthday to
 * 'verify' that they're over 18. I have no idea why that dialog fragment was so difficult to get right.
 * I feel like I should just try and write a totally custom one so it'll actually look good. Also this
 * activity writes the user into the db once their account is created. Builds all the nodes needed
 * to complete the setup.
 */

public class InfoEntryActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText userEmailField;
    private EditText userPasswordField;

    private TextView noBday;
    private TextView displayDate;

    private Button bdayButton;

    private CheckBox showPassword;

    private int pickerYear;
    private int pickerMonth;
    private int pickerDay;
    private int i = 0;
    private int relationshipRating = 0;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase fbDatabase;
    private DatabaseReference mDatabaseRef;

    private static final String TAG = "InfoEntryActivity";
    private static final String USERS_CHILD = "users";
    private String userName = "Default Name";
    private String companionName = "Default Name";

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infoentry);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // initialize fields and UI elements
        userEmailField = findViewById(R.id.info_email);
        userPasswordField = findViewById(R.id.info_password);

        noBday = findViewById(R.id.wheres_mybday);
        displayDate = findViewById(R.id.bday_shown);

        showPassword = findViewById(R.id.show_password_box);

        // initialize firebase auth and database instances to be used later
        mAuth = FirebaseAuth.getInstance();
        fbDatabase = FirebaseDatabase.getInstance();

        // change hint from weirdo font to normal one
        userPasswordField.setTypeface(Typeface.DEFAULT);

        // show password toggle box
        showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    // box ain't checked
                    userPasswordField.setInputType(129);
                    userPasswordField.setTypeface(Typeface.DEFAULT);

                } else {
                    // box is checked
                    userPasswordField.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    userPasswordField.setTypeface(Typeface.DEFAULT);
                }
            }
        });

        Button doneButton = findViewById(R.id.info_done_button);

        doneButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                validateForm();

                createAccount(userEmailField.getText().toString(),
                        userPasswordField.getText().toString());
            }
        });

        // birthday not shown textview and click listener
        noBday.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(InfoEntryActivity.this)

                        // users under 18 are no bueno explanation, try to get button in middle
                        .setMessage(R.string.where_bday_dialog)
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

        // birthday button and click listener
        bdayButton = findViewById(R.id.bday_button);

        bdayButton.setOnClickListener(this);

        // initialize firebase auth and database instances to be used later
        mAuth = FirebaseAuth.getInstance();
        fbDatabase = FirebaseDatabase.getInstance();

        // Get Current Date
        Calendar c = Calendar.getInstance();
        pickerYear = c.get(Calendar.YEAR) - 18;
        pickerMonth = c.get(Calendar.MONTH);
        pickerDay = c.get(Calendar.DAY_OF_MONTH);

    } // end of oncreate

    @Override
    public void onClick(View view) {

        if (view == bdayButton) {

            Log.i(TAG, "Bday button is firing");

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            //updateDisplay();

                            displayDate.setText(
                                    new StringBuilder()
                                            // Month is 0 based, add 1
                                            .append(monthOfYear + 1).append("/")
                                            .append(dayOfMonth).append("/")
                                            .append(year).append(""));

                        }
                    }, pickerYear, pickerMonth, pickerDay);
            datePickerDialog.show();

            noBday.setText(R.string.wheres_mybday);
        }
    }

    private void updateDisplay() {

        Log.i(TAG, "Display date updater is firing");

        displayDate.setText(
                new StringBuilder()
                        // Month is 0 based so add 1
                        .append(pickerMonth + 1).append("/")
                        .append(pickerDay).append("/")
                        .append(pickerYear).append(""));
    }

    // create user's account method
    private void createAccount(final String email, final String password) {
        Log.d(TAG, "createAccount:" + email);

        // check if any fields are empty
        if (!validateForm()) {
            return;
        }

        progressDialog = ProgressDialog.show(this, "",  "Creating Account...", true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                            signIn(email, password);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(InfoEntryActivity.this,
                                    "Authentication failed. Email is already in use or another error occurred.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signIn(final String email, final String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");

                            mUser = task.getResult().getUser();

                            // get user's id to send to write new user
                            String userId = mUser.getUid();

                            writeNewUser(userId,                                       // userid
                                    userEmailField.getText().toString().toLowerCase(), // email
                                    userName,                                          // set for later
                                    displayDate.getText().toString(),                  // bday
                                    companionName,                                     // set for later
                                    relationshipRating);                               // set to 0 for later

                            Toast.makeText(InfoEntryActivity.this, R.string.auth_success,
                                    Toast.LENGTH_SHORT).show();

                            setResult(Activity.RESULT_OK);

                            finish();

                        } else {
                            // If sign in fails, display a message to the user and sign out
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(InfoEntryActivity.this, "Authentication failed",
                                    Toast.LENGTH_SHORT).show();
                            signOut();
                            finish();
                        }

                        progressDialog.hide();
                    }
                });
        // [END sign_in_with_email]
    }

    // writes new user to db
    private void writeNewUser(String userId, String email, String userName,
                              String bday, String companionName, int relationshipRating) {

        // get object from user class to be filled
        User user = new User(userId, email, userName, bday, companionName, relationshipRating);

        // creates new node in db under "users"
        mDatabaseRef = fbDatabase.getReference(USERS_CHILD);
        mDatabaseRef.child(userId).setValue(user);

        mDatabaseRef.child(userId).child("setupLastCompleted").setValue("scene3");
    }

    // check to see if any forms were left empty, stop sign in if they are
    private boolean validateForm() {
        boolean valid = true;

        String email = userEmailField.getText().toString();

        if (TextUtils.isEmpty(email)) {
            userEmailField.setError("Required");
            valid = false;
        } else {
            userEmailField.setError(null);
        }

        String password = userPasswordField.getText().toString();

        if (TextUtils.isEmpty(password)) {
            userPasswordField.setError("Required");
            valid = false;
        } else {
            userPasswordField.setError(null);
        }

        if (displayDate.getText().toString().equals("") ||
                displayDate.getText().toString().equals(null)) {
            valid = false;
            Toast.makeText(InfoEntryActivity.this, "Please enter your birthday to continue",
                    Toast.LENGTH_SHORT).show();
        }

        return valid;
    }

    private void signOut() {
        mAuth.signOut();
    }
}

