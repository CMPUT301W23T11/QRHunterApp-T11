package com.example.qrhunterapp_t11;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

/**
 * The activity responsible for taking a photo of the QR object or location, and then uploading it to Firebase.
 *
 * @author Aidan Lynch
 * @reference <a href="https://www.youtube.com/watch?v=IrwhjDtpIU0">how configure Camera X for taking photos</a>
 * @reference <a href="https://youtu.be/lPfQN-Sfnjw">how to upload images to Firebase database and storage</a>
 */
public class TakePhotoActivity extends AppCompatActivity {
    private static final int REQUEST = 112; // leave here?
    private final Context mContext = TakePhotoActivity.this;
    public ImageCapture imageCapture;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private StorageReference mStorageRef;
    private Uri mImageUri;
    private String imageUrl;
    private String msTime;
    CollectionReference uploadsReference = FirebaseFirestore.getInstance().collection("uploads");

    /**
     * Checks whether a permission is granted; in this case permission to access and write to the phone's storage.
     *
     * @param context     Interface for global information about application environment.
     * @param permissions Vararg of permission strings.
     * @return Whether the permission has been granted.
     */
    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Called when activity launches; starts by intializing the storage references for firebase, the preview view, capture button
     * and camera provider.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);

        // initialize FB references
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads"); // StorageReference points to a collection called "uploads" on DB

        // initialize camera views
        previewView = findViewById(R.id.preview);
        Button captureButton = findViewById(R.id.captureButton);

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
            public void onClick(View v) { // when the capture button is clicked, take the photo
                if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT <= 29) {
                    String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    if (!hasPermissions(mContext, PERMISSIONS)) {
                        ActivityCompat.requestPermissions((Activity) mContext, PERMISSIONS, REQUEST);
                    } else {
                        capturePhoto();
                    }
                } else {
                    capturePhoto();
                }
            }
        });
    }

    /**
     * Handler for when the user accepts or rejects the initial prompt for storage access.
     *
     * @param requestCode  The request code passed in.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *                     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                capturePhoto();
            } else {
                Toast.makeText(mContext, "The app was not allowed to write in your storage", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Method that deals with capturing the photo, storing it intermediately on the device, and then uploading it to Firebase database (document containing photo name and url), and Firebase storage (the actual .jpeg).
     * TODO currently the images are not compressed, but that's a detail that can be added in the later stages; apparently firebase can do this automatically?
     */
    private void capturePhoto() {
        long timestamp = System.currentTimeMillis(); //NOTE: this doesn't currently correspond to the same msTime that will be set for the photo on firebase; but since this is only for storing the image locally, it may not really matter

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timestamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        imageCapture.takePicture( // creates a new image capture use case from the given configuration; creates and stores the photo locally
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
                        mImageUri = outputFileResults.getSavedUri(); // get uri (local address) of image to know which file to upload later
                        uploadFile(new OnUploadListener() {
                            //use OnUploadListener to retrieve url String from uploadFile method
                            @Override
                            public void onUpload(@NonNull String url) {
                                Intent intent = new Intent();
                                intent.putExtra("url", imageUrl);
                                setResult(Activity.RESULT_OK, intent); // send url String back to the CameraFragment
                                finish(); // close the photo capture activity
                            }
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(TakePhotoActivity.this, "Error saving photo: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
        );
    }

    /**
     * Helper function for building the ImageCapture object.
     *
     * @return an Executor object that executes submitted Runnable tasks.
     */
    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    /**
     * Configure CameraX for camera facing, the preview and the ImageCapture.
     *
     * @param cameraProvider a singleton which can be used to bind the lifecycle of cameras to any LifecycleOwner within an application's process
     */
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

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

    /**
     * Returns a string of the extension of a given image file.
     *
     * @param uri the local address of the image
     * @return the file extension, for example ".jpeg"
     */
    private String getFileExtension(Uri uri) { //
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    /**
     * Function for uploading the image to Firebase database and storage.
     *
     * @reference <a href="https://stackoverflow.com/a/55503926/14445107">Wilmer Villca - using a successListener to get the image url of the uploaded photo</a>
     */
    private void uploadFile(final OnUploadListener listener) {
        if (mImageUri != null) { // if the image exists, upload it
            msTime = System.currentTimeMillis() + " ";
            // we are grabbing the current time in ms, to ensure each photo upload has a unique name; .child() concatenates file to mStorageReference ("uploads" folder)
            StorageReference fileReference = mStorageRef.child(msTime + "." + getFileExtension(mImageUri));
            fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) { // when file is successfully uploaded
                            Toast.makeText(TakePhotoActivity.this, "Photo uploaded", Toast.LENGTH_SHORT).show();
                            Log.d("PhotoUpload", "Photo upload was successful.");

                            if (taskSnapshot.getMetadata() != null) {
                                if (taskSnapshot.getMetadata().getReference() != null) {
                                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) { // retrieve image url of photo upload
                                            Log.d("UrlRetrieved", "Image url successfully retrieved: " + imageUrl); //TODO remove redundant key and attribute (in database collection?)
                                            imageUrl = uri.toString(); // this is *NOT* the image uri used earlier
                                            PhotoUploader upload = new PhotoUploader(msTime, imageUrl);
                                            listener.onUpload(imageUrl);
                                            // make entry in database, that contains the name and url of our image upload
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

    /**
     * A listener interface used to retrieve the url string after the photo has been uploaded to the db
     *
     * @reference <a href="https://stackoverflow.com/questions/51086755/java-android-how-to-call-onsuccess-method">from Fangming</a>
     */
    public interface OnUploadListener {
        void onUpload(@NonNull String url);
    }
}