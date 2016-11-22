package com.ywca.pentref.common;

/**
 * Includes an id, a category name and the image resource id.
 */
public class CategoryItem {
    private int categoryId;
    private String categoryName;
    private int imageResourceId;

    public CategoryItem(int categoryId, String categoryName, int imageResourceId) {
        this.imageResourceId = imageResourceId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public int getCategoryId() {
        return this.categoryId;
    }

    public String getCategoryName() {
        return this.categoryName;
    }

    public int getImageResourceId() {
        return this.imageResourceId;
    }
}