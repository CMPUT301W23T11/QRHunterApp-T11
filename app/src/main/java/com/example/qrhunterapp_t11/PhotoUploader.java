package com.example.qrhunterapp_t11;

// https://www.youtube.com/watch?v=lPfQN-Sfnjw&list=PLrnPJCHvNZuB_7nB5QD-4bNg6tpdEUImQ&index=3
// USED WITHOUT MAJOR MODIFICATION
public class PhotoUploader {
    private String mName; // hardcode?
    private String mImageUrl;

    public PhotoUploader() {
        // empty constructor needed
    }

    public PhotoUploader(String name, String imageUrl) {
        if (name.trim().equals("")) {
            name = "No Name";
        }

        mName = name;
        mImageUrl = imageUrl;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }
}
