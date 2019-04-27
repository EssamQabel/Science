package com.sphinx.science.Activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.sphinx.science.Receivers.NotificationService;

public class StartServiceOnPauseActivity extends AppCompatActivity {
         @Override
         protected void onCreate(@Nullable Bundle savedInstanceState) {
                  super.onCreate(savedInstanceState);
         }

         @Override
         protected void onDestroy() {
                  super.onDestroy();

                  if (!isMyServiceRunning(NotificationService.class)) {
                           Log.i("NotificationService", "NotificationService: insiede main onDestroy");
                           if (!NotificationService.exists) {
                                    Intent pushIntent = new Intent(getApplicationContext(), NotificationService.class);
                                    getApplicationContext().startService(pushIntent);
                           }
                  }
         }

         private boolean isMyServiceRunning(Class<?> serviceClass) {
                  ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                  for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                           if (serviceClass.getName().equals(service.service.getClassName())) {
                                    return true;
                           }
                  }
                  return false;
         }
}
