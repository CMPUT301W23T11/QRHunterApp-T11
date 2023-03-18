package com.example.qrhunterapp_t11;

import androidx.annotation.NonNull;

/**
 * Class that defines a user
 *
 * @author Afra
 */
public class User {

    private String displayName;
    private String username;
    private int totalPoints;
    private String email;

    public User() {
    }

    public User(@NonNull String displayName, @NonNull String username, int totalPoints, @NonNull String email) {
        this.displayName = displayName;
        this.username = username;
        this.totalPoints = totalPoints;
        this.email = email;
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

    @NonNull
    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }
}
