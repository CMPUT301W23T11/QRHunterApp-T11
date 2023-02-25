package com.example.qrhunterapp_t11;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

// https://www.youtube.com/watch?v=IrwhjDtpIU0
// https://www.youtube.com/watch?v=lPfQN-Sfnjw&list=PLrnPJCHvNZuB_7nB5QD-4bNg6tpdEUImQ&index=3

public class TakePhotoActivity extends AppCompatActivity {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private Button captureButton;
    public ImageCapture imageCapture;

    //fb upload
    private StorageReference mStorageRef;
    CollectionReference uploadsReference = FirebaseFirestore.getInstance().collection("uploads");
    private Uri mImageUri;
    private String imageUrl;
    private String msTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);

        // initialize FB references
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads"); // StorageReference points to a collection called "uploads" on DB

        // initialize camera views
        previewView = findViewById(R.id.preview);
        captureButton = findViewById(R.id.captureButton);

        // initialize camera
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, getExecutor());

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capturePhoto();
            }
        });
    }

    private void capturePhoto() {
        long timestamp = System.currentTimeMillis(); //TODO this probably won't correspond to the system time created later in uploadPhoto(); however I assume this is the file's name on *local* storage, so probably not a priority to fix

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build(),
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(TakePhotoActivity.this, "Photo has been saved successfully.", Toast.LENGTH_SHORT).show();
                        mImageUri = outputFileResults.getSavedUri(); // get uri (address) of image to know which file to upload later
                        uploadFile();
                        finish();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(TakePhotoActivity.this, "Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
        );
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        // Camera selector use case
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Preview use case
        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Image capture use case
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);
    }

    private String getFileExtension(Uri uri) { // returns extension of file we picked (.jpeg for ex.)
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
    private void uploadFile() { // uploads the file to FB
        if (mImageUri != null) { // if the image exists, upload it
            msTime = System.currentTimeMillis() + " ";

            StorageReference fileReference = mStorageRef.child(msTime + "." + getFileExtension(mImageUri)); // we are grabbing the current time in ms, to ensure each photo upload has a unique name; .child() concatenates file to mStorageReference ("uploads" folder)
            fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) { // when file is successfully uploaded
                            Toast.makeText(TakePhotoActivity.this, "Photo uploaded", Toast.LENGTH_SHORT);
                            Log.d("PhotoUpload", "Photo upload was successful.");


                            // https://stackoverflow.com/a/55503926/14445107 - Wilmer Villca
                            if (taskSnapshot.getMetadata() != null) {
                                if (taskSnapshot.getMetadata().getReference() != null) {
                                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            imageUrl = uri.toString(); // wont work
                                            //Log.d("UrlRetrieved", "Image url successfully retrieved: " + imageUrl);

                                            // https://stackoverflow.com/questions/64799124/how-to-get-file-from-file-i-upload-to-firebase-storage
                                            //String testUrl = taskSnapshot.getResult().getUploadSessionUri().toString();
                                            PhotoUploader upload = new PhotoUploader(msTime, imageUrl); //TODO change "default" name of photos to custom ones to avoid conflicts later?
                                            Log.d("UrlRetrieved", "Image url at document creation is: " + imageUrl); //TODO remove redundant key and attribute (in database collection?)

                                            // make entry in database, that contains the metadata of our upload
                                            //String uploadId = mDatabaseRef.push().getKey(); // create new entry in DB, with unique id uploadId
                                            //mDatabaseRef.child(uploadId).setValue(upload); // then take unique id and set its data to upload file (contains name of upload, and the image uri)
                                            uploadsReference.document(upload.getName()).set(upload);
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("UrlRetrieved", "Image url FAILED to retrieve.");
                                        }
                                    });
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) { // when file fails to upload
                            Log.d("PhotoUpload", "Something went wrong uploading the photo: " + e.getMessage());
                        }
                    });
        } else {
            Log.d("PhotoUpload", "Something went wrong uploading the photo; no mImageUri?");
        }
    }
}