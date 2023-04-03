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
    private ArrayList<String> qrCodeIDs;
    private ArrayList<String> qrCodeHashes;
    private String email;

    public User() {
    }

    public User(@NonNull String displayName, @NonNull String username, int totalPoints, int totalScans, int topQRCode, @NonNull String email, ArrayList<String> qrCodeIDs, ArrayList<String> qrCodeHashes) {
        this.displayName = displayName;
        this.username = username;
        this.totalPoints = totalPoints;
        this.totalScans = totalScans;
        this.topQRCode = topQRCode;
        this.email = email;
        this.qrCodeIDs = qrCodeIDs;
        this.qrCodeHashes = qrCodeHashes;
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

    public ArrayList<String> getQrCodeIDs() {
        return qrCodeIDs;
    }

    public void setQrCodeIDs(ArrayList<String> qrCodeIDs) {
        this.qrCodeIDs = qrCodeIDs;
    }

    public ArrayList<String> getQrCodeHashes() {
        return qrCodeHashes;
    }

    public void setQrCodeHashes(ArrayList<String> qrCodeHashes) {
        this.qrCodeHashes = qrCodeHashes;
    }
}
