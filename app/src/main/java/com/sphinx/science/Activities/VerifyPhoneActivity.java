package com.sphinx.science.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sphinx.science.Entities.Client;
import com.sphinx.science.R;
import com.sphinx.science.Entities.UserClient;

import java.util.concurrent.TimeUnit;

import static com.sphinx.science.Utility.Constants.PREF_FILE_NAME;

public class VerifyPhoneActivity extends AppCompatActivity {

         private SharedPreferences sharedPreferences;
         private SharedPreferences.Editor editor;
         private String TAG = "VerifyPhoneActivity";
         private String phoneNumber, clientName, email;
         private String verificationId;
         private String code;
         private FirebaseAuth mAuth;
         private ProgressBar progressBar;
         private EditText editText;
         private FirebaseFirestore db;
         private PhoneAuthProvider.OnVerificationStateChangedCallbacks
                 mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                  @Override
                  public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                           Log.i("AUTHENTICATION", "verification code received");
                           super.onCodeSent(s, forceResendingToken);
                           verificationId = s;
                  }

                  @Override
                  public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                           code = phoneAuthCredential.getSmsCode();
                           if (code != null) {
                                    editText.setText(code);
                                    Log.i("AUTHENTICATION", "code is being verified");
                                    verifyCode(code);
                           } else {
                                    Log.i("AUTHENTICATION", "code is null");
                                    signInWithCredential(phoneAuthCredential);
                           }
                  }

                  @Override
                  public void onVerificationFailed(FirebaseException e) {
                           Toast.makeText(VerifyPhoneActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                  }
         };

         @Override
         protected void onCreate(Bundle savedInstanceState) {
                  super.onCreate(savedInstanceState);
                  setContentView(R.layout.activity_verify_phone);

                  mAuth = FirebaseAuth.getInstance();
                  db = FirebaseFirestore.getInstance();

                  progressBar = findViewById(R.id.progressbar);
                  editText = findViewById(R.id.editTextCode);

                  // Shared Preferences to save the verificationId and the code, to be used to sing in
                  sharedPreferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
                  editor = sharedPreferences.edit();

                  phoneNumber = getIntent().getStringExtra("phonenumber");
                  clientName = getIntent().getStringExtra("clientName");
                  email = getIntent().getStringExtra("email");

                  Log.i("AUTHENTICATION", "phonenumber: " + phoneNumber);
                  Log.i("AUTHENTICATION", "clientName: " + clientName);
                  Log.i("AUTHENTICATION", "email: " + email);

                  sendVerificationCode(phoneNumber);

                  findViewById(R.id.buttonSignIn).setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {

                                    String code = editText.getText().toString().trim();

                                    if (code.isEmpty() || code.length() < 6) {

                                             editText.setError("Enter code...");
                                             editText.requestFocus();
                                             return;
                                    }
                                    verifyCode(code);
                           }
                  });

         }

         private void verifyCode(String code) {
                  PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                  Log.i("AUTHENTICATION", "real verificationId: " + verificationId);
                  Log.i("AUTHENTICATION", "real code: " + code);
                  signInWithCredential(credential);
         }

         private void signInWithCredential(final PhoneAuthCredential credential) {
                  Log.i("AUTHENTICATION", "sign in with credential");
                  mAuth.signInWithCredential(credential)
                          .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                   @Override
                                   public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {

                                                     // Put the credential data
                                                     editor.putBoolean("hasCredential", true);
                                                     editor.putString("verificationId", verificationId);
                                                     editor.putString("code", code);
                                                     editor.apply();

                                                     String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                                     final Client client = new Client();
                                                     client.setPhone(phoneNumber);
                                                     client.setUser_id(id);
                                                     client.setEmail(email);
                                                     client.setUsername(clientName);
                                                     client.setNumberOfRequests(0d);
                                                     client.setTokenId("");

                                                     DocumentReference clientRef = db.collection("customers")
                                                             .document(id);

                                                     clientRef.set(client)
                                                             .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                      @Override
                                                                      public void onSuccess(Void aVoid) {
                                                                               ((UserClient) (getApplicationContext())).setClient(client);
                                                                               Intent intent = new Intent(VerifyPhoneActivity.this, RegisterActivity.class);
                                                                               intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                                                               startActivity(intent);
                                                                      }
                                                             })
                                                             .addOnFailureListener(new OnFailureListener() {
                                                                      @Override
                                                                      public void onFailure(@NonNull Exception e) {
                                                                               Toast.makeText(VerifyPhoneActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                                                                      }
                                                             });
                                            } else {
                                                     Log.e(TAG, task.getException().getMessage());
                                                     Toast.makeText(VerifyPhoneActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                   }
                          });
         }

         private void sendVerificationCode(String number) {
                  Log.i("AUTHENTICATION", "verification code sent");

                  progressBar.setVisibility(View.VISIBLE);
                  PhoneAuthProvider.getInstance().verifyPhoneNumber(
                          number,
                          60,
                          TimeUnit.SECONDS,
                          TaskExecutors.MAIN_THREAD,
                          mCallBack
                  );

         }
}