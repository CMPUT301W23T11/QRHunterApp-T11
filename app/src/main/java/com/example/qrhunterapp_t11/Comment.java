package com.example.qrhunterapp_t11;

public class Comment {
    private String comment;
    private String profile;

    public Comment(String comment, String profile) {
        this.comment = comment;
        this.profile = profile;
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
