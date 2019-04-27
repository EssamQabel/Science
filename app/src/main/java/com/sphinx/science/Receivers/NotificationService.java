package com.sphinx.science.Receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sphinx.science.Activities.RegisterActivity;
import com.sphinx.science.Entities.Request;
import com.sphinx.science.Entities.UserClient;
import com.sphinx.science.R;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class NotificationService extends Service {

    public static Boolean exists = false;
    public static ArrayList<String> clientsProjects = new ArrayList<>();
    private String TAG = "NotificationService";
    private FirebaseFirestore db;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Boolean isNotificationExists = false;

    private String CHANNEL_ID = "CHANNEL_ID";

    private Intent restartService26Api;

    private int makeNotifications = 0;


    public NotificationService() {
    }

    @Override
    public void onCreate() {
        createNotificationChannel();
//                  if (Build.VERSION.SDK_INT >= 26) {
//                           String CHANNEL_ID = "my_channel_01";
//                           NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
//                                   "Channel human readable title",
//                                   NotificationManager.IMPORTANCE_DEFAULT);
//
//                           ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
//
//                           Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                                   .setContentTitle("")
//                                   .setContentText("").build();
//
//                           startForeground(1, notification);
//                  }

        Intent intent = new Intent(this, RegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
           .setSmallIcon(R.drawable.ic_notification_icon)
           .setContentTitle("New Message")
           .setContentText("New message from Science")
           .setAutoCancel(true)
           .setContentIntent(pendingIntent)
           .setPriority(NotificationCompat.PRIORITY_DEFAULT);

//                  Notification n = new Notification.Builder(NotificationService.this)
//                          .setContentTitle("Notification Title")
//                          .setContentText("Notification Message")
//                          .setSmallIcon(R.drawable.ic_notification_icon).build();

        db = FirebaseFirestore.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();

                Log.i("NotificationService", "has user: " + user.getUid());

                // Get the requests of the client
                db.collection("customers")
                   .document(user.getUid())
                   .collection("requests").get()
                   .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                       @Override
                       public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                           if (!queryDocumentSnapshots.isEmpty()) {
                               for (DocumentSnapshot ds : queryDocumentSnapshots) {
                                   //if(ds.getBoolean("isDone") == false)
                                   Request request = ds.toObject(Request.class);
                                   clientsProjects.add(request.getName());
                               }
                               SharedPreferences.Editor editor = getSharedPreferences("SciencePrefName", MODE_PRIVATE).edit();

                               restartService26Api = new Intent("RestartServiceApi");
                               restartService26Api.putExtra("size", clientsProjects.size());
                               editor.putInt("size", clientsProjects.size());
                               for (int i = 0; i < clientsProjects.size(); i++) {
                                   Log.i("NotificationService", "inside the Broadcast, project " + i + ": " + clientsProjects.get(i));
                                   restartService26Api.putExtra("project" + i, clientsProjects.get(i));
                                   editor.putString("project" + i, clientsProjects.get(i));
                               }
                               ((UserClient) getApplicationContext()).restartServiceFor26Api = restartService26Api;
                               editor.apply();

                               for (String project : clientsProjects) {
                                   Log.i("NotificationService", "setting up project: " + project);

                                   db.collection("customers").document(user.getUid())
                                      .collection("requests").document(project)
                                      .collection("messages").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                       @Override
                                       public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                           if (e != null) {
                                               Log.w(TAG, "Listen failed.", e);
                                               return;
                                           }
                                           for (QueryDocumentSnapshot qds : queryDocumentSnapshots) {

                                           }
                                           Log.i("NotificationService", "A change has occurred");
                                           Log.i("NotificationService", "make notifications: " + makeNotifications);

                                           if (((UserClient) getApplicationContext()).isAppOpen == null && makeNotifications > clientsProjects.size() - 1) {
                                               NotificationManagerCompat.from(getApplicationContext()).notify(1, builder.build());
                                           }
                                           makeNotifications++;
                                       }
                                   });
                               }
                           }
                       }
                   });
                for (String project : clientsProjects)
                    Log.i(TAG, "NotificationService: Project Name:" + project);
            }
        };
        // Initialize the FirebaseAuth with the create mAuthListener
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
        exists = true;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel_name";
            String description = "channel_description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        exists = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i("NotificationService", "Called RestartServiceApi");
//                           Log.i("NotificationService", "clientsProjects.size() : " + ((UserClient)getApplicationContext()).restartServiceFor26Api.getIntExtra("size", 0));

            Intent restartService26Api = new Intent("RestartServiceApi");
            sendBroadcast(restartService26Api);
        } else {
            Log.i("NotificationService", "Called normal service");
            startService(new Intent(this, NotificationService.class));
        }
        super.onDestroy();
    }
}
