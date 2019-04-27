package com.sphinx.science.Adapters;

import android.content.Context;
import android.content.Intent;
import android.icu.util.BuddhistCalendar;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.sphinx.science.Activities.AdminActivity;
import com.sphinx.science.Activities.ProfileActivity;
import com.sphinx.science.Activities.RequestChatActivity;
import com.sphinx.science.Entities.Client;
import com.sphinx.science.R;

import java.util.ArrayList;

public class ClientsAdapter extends ArrayAdapter<Client> {

         private FirebaseFirestore db;
         public ClientsAdapter( Context context, ArrayList<Client> clients) {
                  super(context, 0, clients);
         }


         @NonNull
         @Override
         public View getView(int position, View convertView, @NonNull ViewGroup parent) {

                  final Client client = getItem(position);
                  Log.i("ClientsAdapter", "clientId in ClientsAdapter " + client.getUser_id());
                  db = FirebaseFirestore.getInstance();

                  if(convertView == null)
                           convertView = LayoutInflater.from(getContext()).inflate(R.layout.client_list_item, parent, false);

                  TextView clientName = convertView.findViewById(R.id.clientNameAdminActivity);
                  TextView clientPhone = convertView.findViewById(R.id.clientPhoneAdminActivity);
                  TextView numberOfRequests= convertView.findViewById(R.id.numberOfRequestsAdminActivity);

                  clientName.setText(client.getUsername());
                  clientPhone.setText(client.getPhone());
                  String string = client.getNumberOfRequests().toString();
                  numberOfRequests.setText(string.substring(0, string.indexOf(".")));

                  convertView.findViewById(R.id.projectBoard).setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {
                                    Intent intent = new Intent(AdminActivity.activity, ProfileActivity.class);
                                    intent.putExtra("clientUserId", client.getUser_id());
                                    AdminActivity.activity.startActivity(intent);
                           }
                  });

                  return  convertView;
         }
}
