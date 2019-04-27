package com.sphinx.science.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sphinx.science.Entities.Client;
import com.sphinx.science.R;
import com.sphinx.science.Entities.UserClient;

import static android.text.TextUtils.isEmpty;

public class LoginActivity extends AppCompatActivity implements
   View.OnClickListener {

    private static final String TAG = "LoginActivity";

    //Firebase
    private FirebaseAuth.AuthStateListener mAuthListener;

    // widgets
    private EditText mEmail, mPassword;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mProgressBar = findViewById(R.id.progressBar);

        setupFirebaseAuth();
        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.link_register).setOnClickListener(this);

        hideSoftKeyboard();
    }


    private void showDialog() {
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog() {
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    // Setting up the FirebaseAuth
    private void setupFirebaseAuth() {
        // Used to sighOut the current user, TODO: DEVELOPMENT  ONLY
        // FirebaseAuth.getInstance().signOut();
        Log.d(TAG, "setupFirebaseAuth: started.");

        // Creating an AuthListener to be used in initializing the FirebaseAuth in Line: 132
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // Get the current user
                FirebaseUser user = firebaseAuth.getCurrentUser();

                // If there is a user then continue initializing
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    // Get a Firestore instance
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                             /*
                                             FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                                                     .setTimestampsInSnapshotsEnabled(true)
                                                     .build();
                                             db.setFirestoreSettings(settings);
                                             */
                    // Get a user document reference
                    DocumentReference userRef = db.collection("customers")
                       .document(user.getUid());

                    // Try to get data from the Firestore to ensure that the
                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                Log.d(TAG, "onComplete: successfully set the client client.");
                                Client client = task.getResult().toObject(Client.class);
                                ((UserClient) (getApplicationContext())).setClient(client);
                                Log.d(TAG, "" + client.toString());

                                Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });

                } else {
                    // Client is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        // Initialize the FirebaseAuth with the create mAuthListener in Line: 78
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        //Delete the authentication listener upon exiting
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }

    // Handles signing in process
    private void signIn() {
        //check if the fields are filled out
        if (!isEmpty(mEmail.getText().toString())
           && !isEmpty(mPassword.getText().toString())) {
            Log.d(TAG, "onClick: attempting to authenticate.");

            showDialog();// Show a loading bar

            // Initiate the singing in process
            FirebaseAuth.getInstance().signInWithEmailAndPassword(mEmail.getText().toString(),
               mPassword.getText().toString())
               .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                   @Override
                   public void onComplete(@NonNull Task<AuthResult> task) {
                       hideDialog();
                   } //If the user has signed in
               })
               .addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       // If some failure happened, show an error message
                       Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                       hideDialog();
                   }
               });
        } else {
            Toast.makeText(LoginActivity.this, "You didn't fill in all the fields.", Toast.LENGTH_SHORT).show();
        }
    }

    // Called when the user clicks a button
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //If the user clicked the register button, direct them to the RegisterActivity
            case R.id.link_register: {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);//Change MainActivity to RegisterActivity
                startActivity(intent);
                break;
            }
            // If the user clicked on login button
            case R.id.email_sign_in_button: {
                signIn();
                break;
            }
        }
    }
}