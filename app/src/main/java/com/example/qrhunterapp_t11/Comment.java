package com.example.qrhunterapp_t11;

/**
 * This Class represents a Comment that will belong to a QRCode object. Contains the comment contents and the profile who posted it.
 *
 * @author Sarah Thomson
 */

public class Comment {
    private String comment;
    private String profile;

    public Comment(String comment, String profile) {
        this.comment = comment;
        this.profile = profile;
    }

    // For the database
    public Comment() {
    }

    public String getComment() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getProfile() {
        return profile;
    }
    public void setProfile(String profile) {
        this.profile = profile;
    }
}
