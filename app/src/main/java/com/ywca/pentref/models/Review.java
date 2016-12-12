package com.ywca.pentref.models;

import org.joda.time.LocalDateTime;

import java.util.List;

/**
 * A Poi (Point of Interest) encapsulates information about a review contributed by a user.
 */
public class Review {
    private String userId;
    private double rating;
    private String userName;
    private String title;
    private String description;
//    private List<String> photoUris;
    private LocalDateTime timestamp;

    public Review(String userId, int rating, String userName, String title,
                  String description/*, List<String> photoUris*/, LocalDateTime timestamp) {
        this.userId = userId;
        this.rating = rating;
        this.userName = userName;
        this.title = title;
        this.description = description;
        //this.photoUris = photoUris;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return this.userId;
    }

    public double getRating() {
        return rating;
    }

    public String getUserName() {
        if (this.userName == null || this.userName.isEmpty()) {
            return "John Doe";
        }
        return userName;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

//    public List<String> getPhotoUris() {
//        return this.photoUris;
//    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }
}