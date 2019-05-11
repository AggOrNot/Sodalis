package com.martin.sodalis;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignInActivity extends AppCompatActivity {

    private EditText userEmailField;
    private EditText userPasswordField;

    private CheckBox showPassword;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase fbDatabase;

    private static final String TAG = "SignInActivity";
    private static final String USERS_CHILD = "users";

    private ProgressDialog progressDialog;

    private SharedPreferences loginPrefs;

    /**
     * activity used for signing back into the app if user decides to sign out for some reason or
     * gets a new phone or whatever.
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // initialize fields
        userEmailField = findViewById(R.id.signin_email);
        userPasswordField = findViewById(R.id.signin_password);

        showPassword = findViewById(R.id.show_password_box);

        // initialize firebase auth and database instances to be used later
        mAuth = FirebaseAuth.getInstance();
        fbDatabase = FirebaseDatabase.getInstance();

        // change hint from weirdo font to normal one
        userPasswordField.setTypeface(Typeface.DEFAULT);

        loginPrefs = getSharedPreferences("prefs", 0);

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

        // initialize sign in button and click listener
        Button doneButton = findViewById(R.id.info_done_button);

        doneButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // sign in using input fields
                signIn(userEmailField.getText().toString(), userPasswordField.getText().toString());
            }
        });

        // initialize firebase auth and database instances to be used later
        mAuth = FirebaseAuth.getInstance();
        fbDatabase = FirebaseDatabase.getInstance();

    } // end of oncreate

    // sing into firebase using email and password
    private void signIn(final String email, final String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        progressDialog = ProgressDialog.show(this, "",
                "Signing in...", true);

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");

                            SharedPreferences.Editor editor = loginPrefs.edit();
                            editor.putBoolean("userLoggedIn", true);
                            editor.apply();

                            Toast.makeText(SignInActivity.this, "Welcome back!",
                                    Toast.LENGTH_SHORT).show();

                            // send user to main activity after successful sign in
                            Intent iMain = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(iMain);

                            finish();

                        } else {
                            // If sign in fails, display a message to the user and sign out
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed",
                                    Toast.LENGTH_SHORT).show();
                        }

                        progressDialog.hide();
                    }
                });
        // [END sign_in_with_email]
    }

    // check to see if any forms were left empty, show error if they are and prevent sign in
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

        return valid;
    }
}
