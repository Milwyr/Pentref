package com.ywca.pentref.models;

import android.net.Uri;

import java.util.List;

/**
 * A Poi (Point of Interest) encapsulates information about a review contributed by a user.
 */
public class Review {
    private int rating;
    private String userName;
    private String title;
    private String description;
    private List<Uri> photoUris;

    public Review(int rating, String userName, String title, String description, List<Uri> photoUris) {
        this.rating = rating;
        this.userName = userName;
        this.title = title;
        this.description = description;
        this.photoUris = photoUris;
    }

    public int getRating() {
        return rating;
    }

    public String getUserName() {
        return userName;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<Uri> getPhotoUris() {
        return this.photoUris;
    }
}