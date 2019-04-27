package com.sphinx.science.Activities;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.sphinx.science.Adapters.ClientsAdapter;
import com.sphinx.science.Entities.Client;
import com.sphinx.science.R;

import java.util.ArrayList;

public class AdminActivity extends StartServiceOnPauseActivity {

    // A static field to refer to the AdminActivity
    public static Activity activity;

    // The clients array to be displayed
    private ArrayList<Client> clients;

    // The FirebaseFirestore object
    private FirebaseFirestore db;

    // UI elements
    private TextView noClientsTextView;
    private ProgressBar mProgressBar;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Initialize objects
        db = FirebaseFirestore.getInstance();
        clients = new ArrayList<>();
        activity = this;

        // Setting up elements
        listView = findViewById(R.id.listViewAdminActivity);
        mProgressBar = findViewById(R.id.progressBarAdminActivity);
        noClientsTextView = findViewById(R.id.noClientsTextView);

        // To show loading progress bar
        showDialog();

        // Get all the documents from the firestore, and display them to the admin
        db.collection("customers").get()
           .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
               @Override
               public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                   // If there are no clients, show "No Clients Yet" message
                   if (queryDocumentSnapshots.isEmpty()) {
                       noClientsTextView.setText("No Clients Yet");
                       noClientsTextView.setVisibility(View.VISIBLE);
                       hideDialog();
                   }
                   // Else there are some clients, and detect if the client is an admin, or a regular client
                   else {
                       for (DocumentSnapshot ds : queryDocumentSnapshots) {
                           Client client = ds.toObject(Client.class);
                           if (!client.getUsername().equals("ADMINPASSWORD"))
                               clients.add(client);
                       }
                       if (clients.size() < 1) {
                           noClientsTextView.setText("No Clients Yet");
                           noClientsTextView.setVisibility(View.VISIBLE);
                           hideDialog();
                       } else {
                           ClientsAdapter clientsAdapter = new ClientsAdapter(getApplicationContext(), clients);
                           listView.setAdapter(clientsAdapter);
                           hideDialog();
                       }
                   }
               }
           })
           .addOnFailureListener(new OnFailureListener() {
               @Override
               public void onFailure(@NonNull Exception e) {
                   Toast.makeText(AdminActivity.this, "Error connecting the the Internet", Toast.LENGTH_SHORT).show();
               }
           });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        // Create the admin main menu
        inflater.inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            // Add admin button
            case R.id.addAdminMenuItem:
                Intent intent = new Intent(this, AddAdminActivity.class);
                startActivity(intent);
                return true;
            // Sign out button
            case R.id.signOutButton:
                Log.i("ADMINS", "signing out!");
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
