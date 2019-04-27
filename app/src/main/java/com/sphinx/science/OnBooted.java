package com.sphinx.science;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.sphinx.science.Entities.UserClient;
import com.sphinx.science.Receivers.NotificationService;
import com.sphinx.science.Utility.Util;

import static android.content.Context.MODE_PRIVATE;

public class OnBooted extends BroadcastReceiver {


         @Override
         public void onReceive(Context context, Intent intent) {

                  // Get the shared preferences object
                  SharedPreferences prefs = context.getSharedPreferences("SciencePrefName", MODE_PRIVATE);

                  // Get the size of projects, to be used in the below for loop, to determine the number of iterations
                  Intent newIntent;
                  int size = prefs.getInt("size", 0);
                  Log.i("NotificationService", "prefs.size " + size);

                  // Make a NotificationService intent
                  newIntent = new Intent(context, NotificationService.class);

                  // For all projects stored in the SharedPrefs, get the name of the project, and bundle it to the newIntent
                  for( int i = 0; i < size; i++)
                  {
                           Log.i("NotificationService", "inside the Broadcast, project " + i + ": " + intent.getStringExtra("project"+i));
                           newIntent.putExtra("project"+i, prefs.getString("project"+i, ""));
                  }
                  Log.i("NotificationService", "newIntent. get size: " + newIntent.getIntExtra("size", 0));

                  // If the phone android Oreo or later, then user JobScheduler
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                           Log.i("NotificationService", "Broadcast api 26");
                           Util.scheduleJob(context);
                  }
                  // Else start the notification service
                  else{
                           Log.i("NotificationService", "Broadcast other");
                           context.startService(newIntent);
                  }
         }
}
