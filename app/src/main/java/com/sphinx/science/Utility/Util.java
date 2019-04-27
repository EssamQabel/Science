package com.sphinx.science.Utility;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.sphinx.science.Receivers.TestJobService;

public class Util {

         // schedule the start of the service every 10 - 30 seconds
         public static void scheduleJob(Context context) {
                  Log.i("NotificationService", "Inside scheduleJob");
                  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                           Log.i("NotificationService", "Inside scheduleJob");
                           ComponentName serviceComponent = new ComponentName(context, TestJobService.class);
                           JobInfo.Builder builder = null;
                           builder = new JobInfo.Builder(0, serviceComponent);
                           builder.setMinimumLatency(1000); // wait at least
                           builder.setOverrideDeadline(5 * 1000); // maximum delay
                           //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
                           //builder.setRequiresDeviceIdle(true); // device should be idle
                           builder.setRequiresCharging(false); // we don't care if the device is charging or not
                           JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
                           jobScheduler.schedule(builder.build());
                  }
         }
}