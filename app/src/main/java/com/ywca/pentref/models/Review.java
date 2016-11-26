package com.ywca.pentref.models;

import android.net.Uri;

import org.joda.time.LocalDateTime;

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
    private LocalDateTime localDateTime;

    public Review(int rating, String userName, String title, String description, List<Uri> photoUris, LocalDateTime localDateTime) {
        this.rating = rating;
        this.userName = userName;
        this.title = title;
        this.description = description;
        this.photoUris = photoUris;
        this.localDateTime = localDateTime;
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

    public LocalDateTime getLocalDateTime() {
        return this.localDateTime;
    }
}