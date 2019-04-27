package com.sphinx.science.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.internal.InternalTokenResult;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.sphinx.science.Adapters.MessageAdapter;
import com.sphinx.science.Entities.Client;
import com.sphinx.science.Entities.Message;
import com.sphinx.science.R;
import com.sphinx.science.Entities.UserClient;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.os.Environment.DIRECTORY_DOCUMENTS;
import static android.os.Environment.DIRECTORY_PICTURES;
import static com.sphinx.science.Utility.Constants.READ_EXTERNAL_STORAGE_REQUEST_CODE;
import static com.sphinx.science.Utility.Constants.SELECT_FILE_REQUEST_CODE;
import static com.sphinx.science.Utility.Constants.WRITE_EXTERNAL_STORAGE_REQUEST_CODE;

public class RequestChatActivity extends StartServiceOnPauseActivity {

    private static final String TAG = "RequestChatActivity";
    private static final int menuProjectFinishedItemID = 1001;
    public static Activity activity;
    ArrayList<Message> messages = new ArrayList<>();
    MessageAdapter messageAdapter;
    private Uri fileUri;
    private String projectName;
    private TextView mMessage;
    private ListView messagesListView;
    private Button sendMsg;
    private Button sendAttachment;
    private ImageView downloadRequirementsButton;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private Client client;
    private String clientId;
    private int numberOfMessages = 0;

    private String fileExtension;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_chat);
        activity = this;

        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        client = ((UserClient) getApplicationContext()).getClient();
        projectName = getIntent().getStringExtra("projectName");
        clientId = getIntent().getStringExtra("clientId");
        messagesListView = findViewById(R.id.messagesListView);
        mMessage = findViewById(R.id.input_message);
        sendMsg = findViewById(R.id.sendMsg);
        sendAttachment = findViewById(R.id.sendAttachment);
        downloadRequirementsButton = findViewById(R.id.downloadRequirementsButton);

//                  CollectionReference messagesCollectionRef = db.collection("customers").document(client.getUser_id())
//                          .collection("requests").document(projectName)
//                          .collection("messages");

        Log.i(TAG, "client.getUser_id(): " + clientId);
        Log.i(TAG, "projectName: " + projectName);

        if (((UserClient) getApplicationContext()).getAdmin())

            getChatMessages();
        hideSoftKeyboard();
        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMsg.setClickable(false);
                insertNewMessage(false, null);
                if (!sendMsg.isClickable()) {

                    Handler handler = new Handler();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            sendMsg.setClickable(true);
                        }
                    };
                    handler.postDelayed(runnable, 200);
                }
            }
        });
        sendAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isReadExternalStoragePermissionGranted())
                    selectFiles();
            }
        });
        downloadRequirementsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isWriteExternalStoragePermissionGranted()) {
                    try {
                        db.collection("customers").document(clientId)
                           .collection("requests").document(projectName).get()
                           .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                               @Override
                               public void onSuccess(DocumentSnapshot documentSnapshot) {
                                   fileExtension = documentSnapshot.getString("url");

                                   Log.i("Attachments", "clicked");
                                   final File fileStorageDir = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS), "Science");
                                   if (!fileStorageDir.exists()) {
                                       Log.i("Attachments", "the dir was not created");
                                       //noinspection ResultOfMethodCallIgnored
                                       fileStorageDir.mkdirs();
                                   }
                                   Log.i("Attachments", "the dir exists");

                                   String date = DateFormat.getDateTimeInstance().format(new Date());
                                   //final String fileTitle = getString(R.string.app_name) + "-file-" + date.replace(" ", "").replace(":", "").replace(".", "") + fileExtension;

                                   Log.i("Attachments", "prepared the file");

                                   StorageReference fileRef = storage.getReference().child("customers").child(clientId).child("requests").child(projectName);

                                   fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                       @Override
                                       public void onSuccess(Uri uri) {
                                           Log.i("Attachments", uri.toString());
                                           DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
//                                                                                                  Uri downloadUri = Uri.parse("http://www.zidsworld.com/wp-content/uploads/2018/06/cat_1530281469.jpg");
                                           final DownloadManager.Request request = new DownloadManager.Request(uri);

                                           Log.i("Attachments", "making the request");

                                           request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                                              .setDestinationInExternalPublicDir(DIRECTORY_DOCUMENTS + File.separator + "Science" + File.separator, projectName)
                                              .setTitle(projectName).setDescription("")
                                              .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                                           Log.i("Attachments", "requested the request");

                                           final long downloadId = dm.enqueue(request);

                                           final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
                                               public void onReceive(Context ctxt, Intent intent) {

                                                   //Fetching the download id received with the broadcast
                                                   long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                                                   //Checking if the received broadcast is for our enqueued download by matching download id
                                                   if (downloadId == id) {
                                                       Log.i("Attachments", "download has finished");
                                                       File file = new File(DIRECTORY_DOCUMENTS + File.separator + "Science" + File.separator + projectName);
                                                       File directory = new File(DIRECTORY_DOCUMENTS + File.separator + "Science" + File.separator);
                                                       Log.i("Attachments", "file to open: " + file.getPath());
                                                       //openFile(file);

                                                       // openFolder()
                                                       Intent openFolderIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                                       Uri uri = Uri.fromFile(file);
                                                       Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".myProvider", directory);
                                                       openFolderIntent.setDataAndType(photoURI, "*/*");
                                                       openFolderIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                       openFolderIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                       startActivity(openFolderIntent);
                                                       //------------------------

                                                   }
                                               }
                                           };

                                           registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                                       }
                                   }).addOnFailureListener(new OnFailureListener() {
                                       @Override
                                       public void onFailure(@NonNull Exception exception) {
                                           Toast.makeText(RequestChatActivity.this, "Something wrong happened", Toast.LENGTH_SHORT).show();
                                       }
                                   });
                               }
                           });
                    } catch (IllegalStateException ex) {
                        Toast.makeText(getApplicationContext(), "Storage Error", Toast.LENGTH_LONG).show();
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        // just in case, it should never be called anyway
                        Toast.makeText(getApplicationContext(), "Unable to save file", Toast.LENGTH_LONG).show();
                        ex.printStackTrace();
                    }
                }
            }
        });

        final Handler handler = new Handler();
        final int delay = 1000; //milliseconds

        handler.postDelayed(new Runnable() {
            public void run() {
                db.collection("customers").document(clientId)
                   .collection("requests").document(projectName)
                   .collection("messages").get()
                   .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                       @Override
                       public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                           int newNumberOfMessages = 0;
                           for (int i = 0; i < queryDocumentSnapshots.size(); i++)
                               newNumberOfMessages++;
                           if (newNumberOfMessages > numberOfMessages)
                               getChatMessages();
                       }
                   });
                handler.postDelayed(this, delay);
            }
        }, delay);

    }

    private void getChatMessages() {


        db.collection("customers").document(clientId)
           .collection("requests").document(projectName)
           .collection("messages")
           .orderBy("timestamp", Query.Direction.ASCENDING)
           .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                int numberOfMessagesTemp = numberOfMessages;

                for (int i = numberOfMessages; i < queryDocumentSnapshots.size(); i++) {
                    messages.add(queryDocumentSnapshots.getDocuments().get(i).toObject(Message.class));
                    numberOfMessagesTemp++;
                }

//                                    for (DocumentSnapshot ds : queryDocumentSnapshots) {
//                                             messages.add(ds.toObject(Message.class));
//                                             numberOfMessagesTemp++;
//                                    }
                MessageAdapter emptyMessageAdapter = new MessageAdapter(getApplicationContext(), new ArrayList<Message>(), projectName);
                messagesListView.setAdapter(emptyMessageAdapter);

                numberOfMessages = numberOfMessagesTemp;
                messageAdapter = new MessageAdapter(getApplicationContext(), messages, projectName);
                messagesListView.setAdapter(messageAdapter);
                messagesListView.setSelection(messagesListView.getCount() - 1);
            }
        });
    }

    private void insertNewMessage(final Boolean isAttachment, final Uri fileUri) {
        String message;
        // "customers" needs to be changed to "clients"
        final StorageReference projectMessagesRefStorage = storage.getReference().child("customers")
           .child(((UserClient) getApplicationContext()).getClient().getUser_id()).child("requests").child(projectName)
           .child("messages");

        if (isAttachment)
            message = getFileName(fileUri);
        else
            message = mMessage.getText().toString();

        if (!message.equals("")) {
            message = message.replaceAll(System.getProperty("line.separator"), "");
            mMessage.setText("");
            final String finalMessage = message;

            final DocumentReference newMessageDoc = db.collection("customers").document(clientId)
               .collection("requests").document(projectName)
               .collection("messages").document();

            final Message newChatMessage = new Message();
            newChatMessage.setMsg(message);
            if (isAttachment)
                newChatMessage.setAttachment(true);

            if (((UserClient) (getApplicationContext())).getAdmin())
                newChatMessage.setName("Science");
            else
                newChatMessage.setName(client.getUsername());
            newChatMessage.setMessageId(newMessageDoc.getId());

            newMessageDoc.set(newChatMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        if (isAttachment)
                            projectMessagesRefStorage.child(newMessageDoc.getId()).putFile(fileUri);
                        getChatMessages();
                        clearMessage();
                    } else {
                        mMessage.setText(finalMessage);
                        View parentLayout = findViewById(android.R.id.content);
                        Snackbar.make(parentLayout, "Something went wrong.", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private boolean isReadExternalStoragePermissionGranted() {
        if (ContextCompat.checkSelfPermission(RequestChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
           == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        //If the version is greater than or equal to jelly bean version, then we must request the permission
        //because as before jelly bean, the permission is already granted.
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityCompat.requestPermissions(RequestChatActivity.this,
               new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST_CODE);
        }
        return false;
    }

    private boolean isWriteExternalStoragePermissionGranted() {
        if (ContextCompat.checkSelfPermission(RequestChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
           == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        //If the version is greater than or equal to jelly bean version, then we must request the permission
        //because as before jelly bean, the permission is already granted.
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityCompat.requestPermissions(RequestChatActivity.this,
               new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
        return false;
    }

    private void openFile(File url) {

        try {

//                           File file = new File(getFilesDir(), "myfile.pdf");
//                           String absoluteFilePath = file.getAbsolutePath();
//                           String mimeType = "application/pdf";
//                           Uri uri = Uri.parse("content://"+"Your_package_name"+"/" + absoluteFilePath);
//                           intent.setDataAndType(uri, mimeType);
//                           Intent intentChooser = Intent.createChooser(intent, "Choose Pdf Application");
//                           startActivity(intentChooser);

//                           Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", url);

            Uri uri = Uri.parse("content://" + "com.sphinx.science" + "/" + url.getAbsolutePath());


            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
                // Word document
                intent.setDataAndType(uri, "application/msword");
            } else if (url.toString().contains(".pdf")) {
                Log.i("Attachments", "opening pdf");
                // PDF file
                intent.setDataAndType(uri, "application/pdf");
            } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
                // Powerpoint file
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
            } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
                // Excel file
                intent.setDataAndType(uri, "application/vnd.ms-excel");
            } else if (url.toString().contains(".zip") || url.toString().contains(".rar")) {
                // WAV audio file
                intent.setDataAndType(uri, "application/x-wav");
            } else if (url.toString().contains(".rtf")) {
                // RTF file
                intent.setDataAndType(uri, "application/rtf");
            } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
                // WAV audio file
                intent.setDataAndType(uri, "audio/x-wav");
            } else if (url.toString().contains(".gif")) {
                // GIF file
                intent.setDataAndType(uri, "image/gif");
            } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
                // JPG file
                intent.setDataAndType(uri, "image/jpeg");
            } else if (url.toString().contains(".txt")) {
                // Text file
                intent.setDataAndType(uri, "text/plain");
            } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") ||
               url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
                // Video files
                intent.setDataAndType(uri, "video/*");
            } else {
                intent.setDataAndType(uri, "*/*");
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No application found which can open the file", Toast.LENGTH_SHORT).show();
        }
    }

    /*
             public void openFolder()
             {
                      Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                      Uri uri = Uri.parse();
                      intent.setDataAndType(uri, "text/csv");
                      startActivity(Intent.createChooser(intent, "Open folder"));
             }
    */
    private void selectFiles() {
        Intent intent = new Intent();
        String[] supportedMimeTypes = {"image/jpeg", "audio/mpeg4-generic", "text/html", "audio/mpeg", "audio/aac", "audio/wav", "audio/ogg"
           , "audio/midi", "audio/x-ms-wma"
           , "video/mp4", "video/x-msvideo", "video/mp4", "video/x-msvideo", "video/x-ms-wmv", "image/png", "image/jpeg", "image/gif"
           , "text/xml", "ext/plain", "application/pdf", "application/vnd.android.package-archive", "application/msword"};

        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
        intent.putExtra("android.content.extra.FANCY", true);
        intent.putExtra("android.content.extra.SHOW_FILESIZE", true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType(supportedMimeTypes.length == 1 ? supportedMimeTypes[0] : "*/*");
            if (supportedMimeTypes.length > 0) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, supportedMimeTypes);
            }
        } else {
            String mimeTypes = "";
            for (String mimeType : supportedMimeTypes) {
                mimeTypes += mimeType + "|";
            }
            intent.setType(mimeTypes.substring(0, mimeTypes.length() - 1));
        }
        startActivityForResult(intent, SELECT_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            fileUri = data.getData();
            insertNewMessage(true, fileUri);
        } else
            Toast.makeText(this, "Please select a file!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (((UserClient) getApplicationContext()).getAdmin()) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_menu, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.projectFinishedMenuItem:

                DocumentReference projectRef = db.collection("customers").document(clientId)
                   .collection("requests").document(projectName);
                Map<String, Object> updates = new HashMap<>();
                updates.put("isDone", true);
                projectRef.update(updates);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void clearMessage() {
        mMessage.setText("");
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}