package com.example.qrhunterapp_t11;

import androidx.annotation.NonNull;

public class User {

    private String displayName;
    private String username;
    private int totalPoints;

    public User() {
    }

    public User(@NonNull String displayName, @NonNull String username, int totalPoints) {
        this.displayName = displayName;
        this.username = username;
        this.totalPoints = totalPoints;
    }

    @NonNull
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@NonNull String displayName) {
        this.displayName = displayName;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }
}
