package com.sphinx.science.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sphinx.science.R;

import java.util.HashMap;
import java.util.Map;

public class AddAdminActivity extends AppCompatActivity {

    // UI elements
    TextView editText;
    Button addAdminButton;
    // The firebase database
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_admin);

        // Initialize the firebase object
        db = FirebaseFirestore.getInstance();

        // The editText that will have the number
        editText = findViewById(R.id.editText);

        // The submit button
        addAdminButton = findViewById(R.id.addAdminButton);

        // Handle adding the admin
        addAdminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText() != "") {
                    // Add the data to "admins/"
                    Map<String, Object> data = new HashMap<>();
                    data.put("phone", editText.getText().toString());
                    db.collection("admins").add(data)
                       .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                           @Override
                           public void onSuccess(DocumentReference documentReference) {
                               // After finishing adding the admin, return back to the AdminActivity
                               finish();
                           }
                       })
                       .addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception e) {
                               Toast.makeText(AddAdminActivity.this, "Failed to add admin", Toast.LENGTH_SHORT).show();
                           }
                       });
                }
            }
        });
    }
}
