package com.ywca.pentref.models;

import org.joda.time.LocalDateTime;

/**
 * A Poi (Point of Interest) encapsulates information about a review contributed by a user.
 */
public class Review {
    private String poiId;
    private String userId;
    private String userName;
    private float rating;
    private String title;
    private String description;
    //    private List<String> photoUris;
    private String timestamp;

    public Review() {
    }

    public Review(String poiId, String userId, String userName, float rating, String title,
                  String description/*, List<String> photoUris*/, String timestamp) {
        this.poiId = poiId;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.title = title;
        this.description = description;
        //this.photoUris = photoUris;
        this.timestamp = timestamp;
    }

    public String getPoiId() {
        return this.poiId;
    }

    public String getUserId() {
        return this.userId;
    }

    public float getRating() {
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

    public String getTimestamp() {
        return this.timestamp;
    }
}