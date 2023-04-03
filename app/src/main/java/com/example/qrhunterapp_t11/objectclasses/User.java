package com.example.qrhunterapp_t11.objectclasses;

import androidx.annotation.NonNull;

import java.util.ArrayList;

/**
 * Class that defines a user
 *
 * @author Afra
 */
public class User {

    private String displayName;
    private String username;
    private int totalPoints;
    private int totalScans;
    private int topQRCode;
    private String email;

    private ArrayList hashes;
    private ArrayList ids;

    public User() {
    }

    public User(@NonNull String displayName, @NonNull String username, int totalPoints, int totalScans, int topQRCode, @NonNull String email) {
        this.displayName = displayName;
        this.username = username;
        this.totalPoints = totalPoints;
        this.totalScans = totalScans;
        this.topQRCode = topQRCode;
        this.email = email;
        this.hashes = new ArrayList<>();
        this.ids = new ArrayList<>();
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

    public int getTotalScans() {
        return totalScans;
    }

    public void setTotalScans(int totalScans) {
        this.totalScans = totalScans;
    }

    public int getTopQRCode() {
        return topQRCode;
    }

    public void setTopQRCode(int topQRCode) {
        this.topQRCode = topQRCode;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    public ArrayList getHashes() {
        return hashes;
    }

    public ArrayList getIds() {
        return ids;
    }
}
