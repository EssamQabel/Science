package com.sphinx.science.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sphinx.science.Activities.ProfileActivity;
import com.sphinx.science.R;
import com.sphinx.science.Entities.Request;
import com.sphinx.science.Activities.RequestChatActivity;

import java.util.ArrayList;

public class RequestAdapter extends ArrayAdapter<Request> {

         private String clientId;
         public RequestAdapter(Context context, ArrayList<Request> requests, String clientId) {
                  super(context, 0, requests);
                  this.clientId = clientId;
         }

         @NonNull
         @Override
         public View getView(int position, View convertView, @NonNull ViewGroup parent) {

                  // Get the current earthquake to display
                  final Request request = getItem(position);

                  //Check whither the current view to display already created or not, if not then inflate a new one.
                  if(convertView == null)
                           convertView = LayoutInflater.from(getContext()).inflate(R.layout.request_list_item, parent, false);

                  TextView projectNameTextView = convertView.findViewById(R.id.projectNameTextView);
                  ImageView isDoneImageView = convertView.findViewById(R.id.isDoneImageView);

                  projectNameTextView.setText(request.getName());
                  if( request.getIsDone())
                           isDoneImageView.setBackgroundResource(R.drawable.donesign);
                  else
                           isDoneImageView.setBackgroundResource(R.drawable.notdonesign);

                  convertView.findViewById(R.id.projectBoard).setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {
                                    Intent intent = new Intent(ProfileActivity.activity, RequestChatActivity.class);
                                    intent.putExtra("projectName", request.getName());
                                    intent.putExtra("clientId", clientId);
                                    ProfileActivity.activity.startActivity(intent);
                           }
                  });

                  return convertView;

         }
}
