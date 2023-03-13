package com.example.qrhunterapp_t11;

import androidx.annotation.NonNull;

/**
 * A simple object that contains the name and image url of an uploaded photo, that is added to the "uploads" collection
 * in the Firebase database. Each document corresponds to a photo in FB Storage, and can be accessed later when retrieving
 * the image data. This class isn't technically necessary, but it's probably the easiest way of adding the pairs of data to
 * the FB database.
 *
 * @author Aidan Lynch and Coding in Flow
 * @reference <a href="https://www.youtube.com/watch?v=lPfQN-Sfnjw&list=PLrnPJCHvNZuB_7nB5QD-4bNg6tpdEUImQ&index=4">how to set up this class; used</a>
 * without major modification
 */
public class PhotoUploader {
    private String mName;
    private String mImageUrl;

    /**
     * Empty constructor needed to interact with Firebase
     */
    public PhotoUploader() {
    }

    /**
     * @param name     the name of the image that will be stored in the document, which is simply the system time of when the photo is taken
     *                 in milliseconds; this also corresponds to the name of the actual photo in FB Storage
     * @param imageUrl the image's link in FB Storage, which can be accessed later when attempting to download the photo
     */
    public PhotoUploader(@NonNull String name, @NonNull String imageUrl) {
        mName = name;
        mImageUrl = imageUrl;
    }

    /**
     * @return the image's name
     */
    @NonNull
    public String getName() {
        return mName;
    }

    /**
     * @param name the string that is to be set as the image's name
     */
    public void setName(@NonNull String name) {
        mName = name;
    }

    /**
     * @return the image url of the photo in Firebase Storage
     */
    @NonNull
    public String getImageUrl() {
        return mImageUrl;
    }
}
