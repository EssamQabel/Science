package com.sphinx.science.Receivers;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.sphinx.science.Entities.UserClient;
import com.sphinx.science.Utility.Util;

/**
 * JobService to be scheduled by the JobScheduler.
 * start another service
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TestJobService extends JobService {
         private static final String TAG = "SyncService";

         @Override
         public boolean onStartJob(JobParameters params) {
//                  Intent service = new Intent(getApplicationContext(), NotificationService.class);
//                  getApplicationContext().startService(service);
                  getApplicationContext().startActivity(((UserClient)getApplicationContext()).restartServiceFor26Api);
                  Util.scheduleJob(getApplicationContext()); // reschedule the job
                  return true;
         }

         @Override
         public boolean onStopJob(JobParameters params) {
                  return true;
         }

}