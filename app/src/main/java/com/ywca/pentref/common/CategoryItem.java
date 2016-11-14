package com.ywca.pentref.common;

/**
 * Created by Milton on 14/11/2016.
 */
public class CategoryItem {
    private int imageResourceId;
    private String categoryDescription;

    public CategoryItem(int imageResourceId, String categoryDescription) {
        this.imageResourceId = imageResourceId;
        this.categoryDescription = categoryDescription;
    }

    public int getImageResourceId() {
        return this.imageResourceId;
    }

    public String getCategoryDescription() {
        return this.categoryDescription;
    }
}