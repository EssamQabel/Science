package com.sphinx.science.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sphinx.science.R;
import com.sphinx.science.Entities.Request;
import com.sphinx.science.Entities.UserClient;

import static com.sphinx.science.Utility.Constants.READ_EXTERNAL_STORAGE_REQUEST_CODE;
import static com.sphinx.science.Utility.Constants.SELECT_FILE_REQUEST_CODE;


public class RequestActivity extends StartServiceOnPauseActivity {

    private final String TAG = "RequestActivity";
    private String fileExtension;

    //UI elements
    private Button selectFileButton;
    private Button uploadButton;
    private ProgressDialog progressDialog;
    private TextView enterProjectNameTextView;
    private ImageView isFileSelectedImageView;

    private Uri pdfUri;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        enterProjectNameTextView = findViewById(R.id.enterProjectNameTextView);
        isFileSelectedImageView = findViewById(R.id.isFileSelected);
        progressDialog = new ProgressDialog(RequestActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Uploading files...");

        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        selectFileButton = findViewById(R.id.selectFileButton);
        selectFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isReadExternalStoragePermissionGranted())
                    selectFiles();
            }
        });

        uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadButton.setClickable(false);
                if (pdfUri != null)
                    uploadFile(pdfUri);
                else {
                    uploadButton.setClickable(false);
                    Toast.makeText(RequestActivity.this, "Select a file", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadFile(Uri fileUrl) {

//                  db.collection("customers").document("Essam")
//                          .collection("requests").get()
//                  .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                           @Override
//                           public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                                    if(queryDocumentSnapshots.isEmpty())
//                                             // No requests yet;
//                                             ;
//                                    else{
//                                             /*
//
//                                             // call an adapter that would take RequestUrl array, and display their data
//
//                                             ArrayList<RequestUrl> requestUrls = new ArrayList<RequestUrl>();
//
//                                             foreach(QueryDocumentSnapshot q in queryDocumentSnapshots)
//                                                      requestUrls.add( new RequestUrl( q.get( "enterProjectNameTextView" ), q.get( "url" ));
//
//                                             RequestUrlsAdapter requestUrlsAdapter = new RequestUrlAdapter();
//
//                                             setupPageViewWithAdapter(requestUrlsAdapter);
//
//                                             */
//                                    }
//
//                           }
//                  });

        final String projectName = enterProjectNameTextView.getText().toString();
        if (projectName.length() > 28)
            Toast.makeText(this, "Project name must be less than 28 character", Toast.LENGTH_LONG).show();
        else {
            final String userId = ((UserClient) (getApplicationContext())).getClient().getUser_id();
            final StorageReference fileRef = storage.getReference().child("customers").child(userId).child("requests").child(projectName);

            fileRef.putFile(fileUrl)
               .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                   @Override
                   public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                       fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                           @Override
                           public void onSuccess(Uri uri) {
                               final DocumentReference clientDocumentReference = db.collection("customers").document(userId);

                               db.runTransaction(new Transaction.Function<Void>() {
                                   @Override
                                   public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                                       DocumentSnapshot clientSnapshot = transaction.get(clientDocumentReference);
                                       double numberOfRequests = 0;
                                       if (clientSnapshot.getDouble("numberOfRequests") == null) {
                                           numberOfRequests = 1;

                                       } else
                                           numberOfRequests = clientSnapshot.getDouble("numberOfRequests") + 1;
                                       transaction.update(clientDocumentReference, "numberOfRequests", numberOfRequests);
                                       return null;
                                   }
                               }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                   @Override
                                   public void onSuccess(Void aVoid) {
                                       Log.d(TAG, "Transaction success!");
                                   }
                               })
                                  .addOnFailureListener(new OnFailureListener() {
                                      @Override
                                      public void onFailure(@NonNull Exception e) {
                                          Log.w(TAG, "Transaction failure.", e);
                                      }
                                  });

                               Request request = new Request(projectName, fileExtension, false);
                               db.collection("customers").document(userId)
                                  .collection("requests").document(projectName)
                                  .set(request)
                                  .addOnSuccessListener(new OnSuccessListener<Void>() {
                                      @Override
                                      public void onSuccess(Void aVoid) {
                                          Toast.makeText(RequestActivity.this, "File is successfully uploaded", Toast.LENGTH_SHORT).show();
                                      }
                                  })
                                  .addOnFailureListener(new OnFailureListener() {
                                      @Override
                                      public void onFailure(@NonNull Exception e) {
                                          Toast.makeText(RequestActivity.this, "File was not uploaded", Toast.LENGTH_SHORT).show();
                                      }
                                  });
                           }
                       });
                       //Freeze the app, to give the app a chance to show the progress dialog finishes
                       Handler handler = new Handler();
                       handler.postDelayed(new Runnable() {
                           @Override
                           public void run() {
                               progressDialog.dismiss();
                               ProfileActivity.activity.finish();
                               Intent intent = new Intent(RequestActivity.this, ProfileActivity.class);
                               startActivity(intent);
                               finish();
                           }
                       }, 250);
                   }
               })
               .addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       uploadButton.setClickable(false);
                       //Our file is not successfully uploaded.
                       Toast.makeText(RequestActivity.this, "File was not uploaded", Toast.LENGTH_SHORT).show();
                   }
               })
               .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                   @Override
                   public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                       //Track the progress of uploading.
                       int currentProgress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                       progressDialog.setProgress(currentProgress);
                       progressDialog.show();
                   }
               });
        }
    }

    private boolean isReadExternalStoragePermissionGranted() {
        if (ContextCompat.checkSelfPermission(RequestActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
           == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        //If the version is greater than or equal to jelly bean version, then we must request the permission
        //because as before jelly bean, the permission is already granted.
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityCompat.requestPermissions(RequestActivity.this,
               new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST_CODE);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            selectFiles();
        else
            Toast.makeText(this, "Storage permission is not granted!!", Toast.LENGTH_SHORT).show();
    }

    private void selectFiles() {
        Intent intent = new Intent();
        String[] supportedMimeTypes = {"application/pdf", "application/msword"};

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
            isFileSelectedImageView.setImageResource(R.drawable.donesign);
            pdfUri = data.getData();

//                           String filePath = pdfUri.getPath();
//                           Log.i("Attachments", filePath);
//                           fileExtension = filePath.substring(filePath.lastIndexOf("."));
//                           Log.i("Attachments", fileExtension);
        } else
            Toast.makeText(this, "Please select a file!", Toast.LENGTH_SHORT).show();
    }
}
