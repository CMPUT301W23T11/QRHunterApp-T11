package com.example.qrhunterapp_t11;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentReference;

public class LeaderboardProfile {
    private final String username;
    private final String points;
    private final DocumentReference userReference;

    public LeaderboardProfile(@NonNull String username, @NonNull String points, @NonNull DocumentReference userReference) {
        this.username = username;
        this.points = points;
        this.userReference = userReference;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    @NonNull
    public String getPoints() {
        return points;
    }

    @NonNull
    public DocumentReference getUserReference() {
        return userReference;
    }

}
