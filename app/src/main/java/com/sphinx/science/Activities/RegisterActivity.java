package com.sphinx.science.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.sphinx.science.Entities.Client;
import com.sphinx.science.R;
import com.sphinx.science.Entities.UserClient;
import com.sphinx.science.Utility.CountryData;

import java.util.ArrayList;
import java.util.List;

import static com.sphinx.science.Utility.Constants.PREF_FILE_NAME;


public class RegisterActivity extends AppCompatActivity implements
   View.OnClickListener {
    private static final String TAG = "RegisterActivity";

    private ArrayList<String> adminPhones;

    //widgets
    private EditText mClientName, mPhone, mEmail;
    private RelativeLayout mProgressBar;
    private RelativeLayout mainLayout;

    //Firebase
    private FirebaseFirestore mDb;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Spinner spinner;

    // For resigning in
    private String verificationId;
    private String code;
    private Boolean hasCredential = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Set isAppOpen to control the  NotificationService
        ((UserClient) getApplicationContext()).isAppOpen = true;

        //Set up variables
        mClientName = findViewById(R.id.clientName);
        mPhone = findViewById(R.id.phone);
        mEmail = findViewById(R.id.input_email);
        mProgressBar = findViewById(R.id.progressBar);
        mainLayout = findViewById(R.id.mainLayout);
        mAuth = FirebaseAuth.getInstance();

        // Spinner that allow the client to select a country
        spinner = findViewById(R.id.spinnerCountries);
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, CountryData.countryNames));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String code = CountryData.countryAreaCodes[spinner.getSelectedItemPosition()];
                mPhone.setText(String.format("+" + code));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //This is the FirebaseFirestore instance
        mDb = FirebaseFirestore.getInstance();

        // Initialize adminPhones ArrayList
        adminPhones = new ArrayList<>();

        //Assign the Login button listener to this Activity, to call onClick method
        findViewById(R.id.btn_register).setOnClickListener(this);

        // Hide the keyboard if it is shown, just in case!
        hideSoftKeyboard();

        // Sing in if there are any saved credentials
        //singInIfUserAlreadySigned();

        // setup firebase authentication main functionality
        setupAuth();
    }

    private void singInIfUserAlreadySigned() {
        // A SharedPreferences reference
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        // Get the "hasCredential" boolean
        hasCredential = sharedPreferences.getBoolean("hasCredential", false);
        // If there exists credential, then resign in.
        if (hasCredential) {
            // Get the saved verificationId
            verificationId = sharedPreferences.getString("verificationId", "");
            // Get the saved code
            code = sharedPreferences.getString("code", "");
            Log.i("AUTHENTICATION", "verficaitonId: " + verificationId);
            Log.i("AUTHENTICATION", "code: " + code);
            if (mAuth.getCurrentUser() == null) {
                // Create the credential object
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, "111111"); // TODO: 111111 is for testing, and should be replaced with real credentials code
                // Sing in with that credential
                signInWithPhoneAuthCredential(credential);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // If the clicked view is the register button then register the customer
            case R.id.btn_register: {
                Log.d(TAG, "onClick: attempting to register.");

                // Get the entered phone number
                String number = mPhone.getText().toString();
                Log.d(TAG, "onClick: the enter number: " + number);

                // Create the VerifyPhoneActivity intent
                Intent intent = new Intent(RegisterActivity.this, VerifyPhoneActivity.class);
                intent.putExtra("phonenumber", number);
                intent.putExtra("clientName", mClientName.getText().toString());
                intent.putExtra("email", mEmail.getText().toString());
                startActivity(intent);
                break;
            }
        }
    }

    private void setupAuth() {

        // The admin activity intent, for the admin
        final Intent adminIntent = new Intent(RegisterActivity.this, AdminActivity.class);
        adminIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // The profile activity intent, for the client
        final Intent profileIntent = new Intent(RegisterActivity.this, ProfileActivity.class);
        profileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Creating an AuthListener to be used in initializing the FirebaseAuth
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

                    // Get the client document reference
                    final DocumentReference userRef = db.collection("customers")
                       .document(user.getUid());

                    // If the current user does not exist in the database then redirect to the Register Screen
                    userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (!documentSnapshot.contains("phonenumber")) {
                                Handler handler = new Handler();
                                Runnable runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        displayViews();
                                        // Client is signed out
                                        Log.d(TAG, "onAuthStateChanged:signed_out");
                                    }
                                };
                                handler.postDelayed(runnable, 1000);
                            }
                        }
                    });


                    // Get admin collection reference
                    final CollectionReference adminsRef = db.collection("admins");

                    // Get admins phones
                    adminsRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            // Store the phones of the admins
                            List<DocumentSnapshot> references = queryDocumentSnapshots.getDocuments();
                            for (int i = 0; i < queryDocumentSnapshots.size(); i++)
                                adminPhones.add(references.get(i).getString("phone"));

                            // To display fetched admins phones to logging.
                            for (int i = 0; i < adminPhones.size(); i++)
                                Log.i("ADMINS", "admin " + i + ":" + adminPhones.get(i));

                            // Try to get data from the Firestore to ensure that the....
                            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful() && task.getResult() != null) {

                                        // Get the client object
                                        Client client = task.getResult().toObject(Client.class);
                                        if (client != null) {
                                            // This is used to indicate if the client is an admin, if so then do not go to the ProfileActivity
                                            Boolean isAdmin = false;

                                            // Set this application context to the client
                                            ((UserClient) (getApplicationContext())).setClient(client);
                                            Log.i("ADMINS", client.getPhone());

                                            // If the client's phone exists in adminPhones, then go to AdminActivity
                                            for (int i = 0; i < adminPhones.size(); i++) {
                                                Log.i("ADMINS", "client phone: " + client.getPhone() + " admin phone: " + adminPhones.get(i));
                                                if (client.getPhone().equals(adminPhones.get(i))) {
                                                    // Indicate that this client is an admin, to not go to the ProfileActivity, but to the AdminActivity
                                                    isAdmin = true;
                                                    Log.i("ADMINS", "This is an admin.");
                                                    //To adminActivity
                                                    ((UserClient) (getApplicationContext())).setAdmin(true);
                                                    startActivity(adminIntent);
                                                    finish();
                                                }
                                            }
                                            // If not an admin, then go to the ProfileActivity
                                            if (!isAdmin) {
                                                Log.i("ADMINS", "going to profile");
                                                // To ProfileActivity
                                                ((UserClient) (getApplicationContext())).setAdmin(false);
                                                startActivity(profileIntent);
                                                finish();
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    });
                } else {
                    displayViews();
                    // Client is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
           .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
               @Override
               public void onComplete(@NonNull Task<AuthResult> task) {
                   if (task.isSuccessful()) {
                       // Sign in success, update UI with the signed-in user's information
                       Log.d(TAG, "signInWithCredential:success");

                       FirebaseUser user = task.getResult().getUser();
                   } else {
                       // Sign in failed, display a message and update the UI
                       Log.w(TAG, "signInWithCredential:failure", task.getException());
                       if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                           // The verification code entered was invalid
                       }
                   }
               }
           });
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

    // The main method of handling registration
    public void registerNewEmail(final String email, String password) {

        // Show loading sign
        showDialog();

        // Start the process of creating user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
           .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
               //Called when the registration is completed
               @Override
               public void onComplete(@NonNull Task<AuthResult> task) {
                   Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                   // If the user registered successfully, then upload the customer data to FireStore
                   if (task.isSuccessful()) {
                       Log.d(TAG, "onComplete: AuthState: " + FirebaseAuth.getInstance().getCurrentUser().getUid());

                       // Setting the customer data
                       final Client client = new Client();
                       client.setPhone(mPhone.getText().toString());
                       client.setUsername(mClientName.getText().toString());
                       client.setEmail(email);
                       client.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
                       client.setNumberOfRequests(0d);

                       FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                          .setTimestampsInSnapshotsEnabled(true)
                          .build();
                       mDb.setFirestoreSettings(settings);

                       // A reference to the new customer document to be populated with data
                       DocumentReference newUserRef = mDb
                          .collection("customers")
                          .document(FirebaseAuth.getInstance().getUid());

                       // Populate the reference to the customer document with the customer data
                       newUserRef.set(client).addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               // Hide the loading bar
                               hideDialog();

                               //If the storing is successful
                               if (task.isSuccessful()) {
                                   // Redirect the client to the LoginActivity
                                   redirectLoginScreen();
                                   // If the storing failed then show an error
                               } else {
                                   View parentLayout = findViewById(android.R.id.content);
                                   Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                               }
                           }
                       });


                       // If the client registration failed
                   } else {
                       View parentLayout = findViewById(android.R.id.content);
                       Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                       hideDialog();
                   }
               }
           });
    }

    // Redirects the user to the login screen
    private void redirectLoginScreen() {
        Log.d(TAG, "redirectLoginScreen: redirecting to login screen.");

        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    //Show the loading bar
    private void showDialog() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    //Hide the loading bar
    private void hideDialog() {
        if (mProgressBar.getVisibility() == View.VISIBLE)
            mProgressBar.setVisibility(View.INVISIBLE);
    }

    // Hide the keyboard
    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void displayViews() {
        mProgressBar.setVisibility(View.INVISIBLE);
        mainLayout.setVisibility(View.VISIBLE);
    }
}
