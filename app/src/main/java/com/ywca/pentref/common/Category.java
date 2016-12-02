package com.ywca.pentref.common;

import com.ywca.pentref.R;

/**
 * Includes an id, a category name. The icon resource id is determined by the category id.
 */
public class Category {
    private int id;
    private String name;

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    // TODO: Add better icons
    public int getImageResourceId() {
        switch (this.id) {
            case 1:
                return R.drawable.ic_menu_camera;
            case 2:
                return R.drawable.ic_bus_black_36dp;
            case 3:
                return R.drawable.ic_menu_share;
            case 4:
                return R.drawable.ic_bookmark_black_36dp;
            default:
                return R.drawable.ic_ferry_black_36dp;
        }
    }
}