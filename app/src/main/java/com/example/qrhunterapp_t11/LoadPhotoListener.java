package com.example.qrhunterapp_t11;

import java.util.ArrayList;

public interface LoadPhotoListener {
    void onFirebaseLoadSuccess(ArrayList<String> photos);
    void onFirebaseLoadFailed(String error);
}
