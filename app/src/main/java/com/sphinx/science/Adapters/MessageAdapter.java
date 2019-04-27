package com.sphinx.science.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sphinx.science.Activities.RequestChatActivity;
import com.sphinx.science.Entities.Client;
import com.sphinx.science.Entities.Message;
import com.sphinx.science.Activities.ProfileActivity;
import com.sphinx.science.R;
import com.sphinx.science.Entities.UserClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.support.constraint.Constraints.TAG;

public class MessageAdapter extends ArrayAdapter<Message> {

         private String projectName;
         private Client client;

         private FirebaseStorage storage;

         public MessageAdapter(Context context, ArrayList<Message> messages, String projectName) {
                  super(context, 0, messages);
                  this.projectName = projectName;
                  client = ((UserClient) ProfileActivity.activity.getApplicationContext()).getClient();
                  storage = FirebaseStorage.getInstance();
         }


         @NonNull
         @Override
         public View getView(int position, View convertView, @NonNull ViewGroup parent) {

                  final Message message = getItem(position);
                  //Check whither the current view to display already created or not, if not then inflate a new one.

                  Log.i(TAG, "Name:  " + message.getName());
                  if (((UserClient) RequestChatActivity.activity.getApplicationContext()).getAdmin()) {
                           if (message.getName().equals("Science"))
                                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.science_message_list_item, parent, false);
                           else
                                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.client_message_list_item, parent, false);
                  } else {
                           if (message.getName().equals("Science"))
                                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.client_message_list_item, parent, false);
                           else
                                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.science_message_list_item, parent, false);
                  }

                  // TextView senderName = convertView.findViewById(R.id.senderName);
                  final TextView msg = convertView.findViewById(R.id.msg);

                  // senderName.setText(message.getName());
                  msg.setText(message.getMsg());

                  if (message.getAttachment()) {
                           SpannableString content = new SpannableString(message.getMsg());
                           content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                           msg.setText(content);
                           convertView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                             // "customers" needs to be changed to "clients"
                                             final StorageReference projectMessagesRefStorage = storage.getReference().child("customers")
                                                     .child(client.getUser_id()).child("requests").child(projectName).child("messages").child(message.getMessageId());

                                             File localFile = null;
                                             try {
                                                      localFile = File.createTempFile(message.getMessageId(), "file");
                                                      projectMessagesRefStorage.getFile(localFile);
                                             } catch (IOException e) {
                                                      e.printStackTrace();
                                             }
                                    }
                           });
                  }


                  return convertView;

         }
}
