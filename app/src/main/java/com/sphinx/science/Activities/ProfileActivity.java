package com.sphinx.science.Activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.sphinx.science.Adapters.RequestAdapter;
import com.sphinx.science.Entities.Profile;
import com.sphinx.science.Receivers.NotificationService;
import com.sphinx.science.R;
import com.sphinx.science.Entities.Request;
import com.sphinx.science.Entities.UserClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends StartServiceOnPauseActivity {

    // A reference to this activity
    public static Activity activity;

    private String TAG = "ProfileActivity";

    // The user id of the client which the admin clicked on in the AdminActivity
    private String userId;

    // An array of the client's request
    private ArrayList<Request> requests;

    // The Firebase Firestore object
    private FirebaseFirestore db;

    // UI elements
    private TextView noRequestsTextView;
    private Button addRequestButton;
    private ProgressBar mProgressBar;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize objects
        db = FirebaseFirestore.getInstance();
        requests = new ArrayList<>();
        activity = this;

        // Get the UI elements
        noRequestsTextView = findViewById(R.id.noRequestsTextView);
        addRequestButton = findViewById(R.id.addRequestButton);
        mProgressBar = findViewById(R.id.profileProgressBar);
        listView = findViewById(R.id.listView);

        // To show loading progress bar
        showDialog();

        //Create a profile for the user
        // If the current user is an admin, then the get the clientUserId from the client on which
        // the admin clicked on in AdminActivity
        if ((((UserClient) getApplicationContext()).getAdmin())) {
            userId = getIntent().getStringExtra("clientUserId");
            addRequestButton.setVisibility(View.INVISIBLE);
        }
        // Else, the current user is a client, then get their userId
        else
            userId = ((UserClient) getApplicationContext()).getClient().getUser_id();

        // Get the user client token id, and store it in the client's document in the firestore
        FirebaseInstanceId.getInstance().getInstanceId()
           .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
               @Override
               public void onComplete(@NonNull Task<InstanceIdResult> task) {
                   if (!task.isSuccessful()) {
                       Log.w(TAG, "getInstanceId failed", task.getException());
                       return;
                   }
                   String tokenId = task.getResult().getToken();
                   // Get new Instance ID token, and save it in the client's document
                   db.collection("customers")
                      .document(userId).update("token_id", tokenId);
                   // If the current user is an admin, then save the admin token to be used in notifying admins about clients' messages
                   if ((((UserClient) getApplicationContext()).getAdmin())) {
                       Map<String, String> adminTokenId = new HashMap<>();
                       adminTokenId.put("token_id", tokenId);
                       db.collection("adminsTokens").add(adminTokenId);
                   }
               }
           });

        // Get the requests of the client
        db.collection("customers")
           .document(userId)
           .collection("requests").get()
           .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
               @Override
               public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                   // If there are none, then display a no projects message
                   if (queryDocumentSnapshots.isEmpty()) {
                       hideDialog();
                       noRequestsTextView.setText("No Project Requests");
                       noRequestsTextView.setVisibility(View.VISIBLE);
                   }
                   // Else, there are some projects requests
                   else {
                       // Get each project request
                       for (DocumentSnapshot ds : queryDocumentSnapshots) {
                           Request request = ds.toObject(Request.class);
                           Log.i(TAG, "NotificationService: Project Name from ProfileActivity: " + request.getName());
                           requests.add(request);

                           // Add the requests in the NotificationService clientsProjects array, to check for any messages
                           // to notify the client about
                           NotificationService.clientsProjects.add(request.getName());
                       }
                       Log.i(TAG, "NotificationService, exists = " + NotificationService.exists.toString());

                       // If the NotificationService does not exists then start a new one
                       if (!NotificationService.exists) {
                           Log.i("NotificationService", "the Service is being called");
                           startService(new Intent(getApplicationContext(), NotificationService.class));
                       }

                       // Populate and link the RequestAdapter to display the project requests
                       RequestAdapter requestAdapter = new RequestAdapter(getApplicationContext(), requests, userId);
                       listView.setAdapter(requestAdapter);
                       Log.d(TAG, "" + requests.size());
                       hideDialog();
                   }
               }
           });

        // Add the functionality of the add request button, to go to the "RequestActivity"
        addRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, RequestActivity.class);
                startActivity(intent);
            }
        });
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
}
