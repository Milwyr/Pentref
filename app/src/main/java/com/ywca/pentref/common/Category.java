package com.ywca.pentref.common;

import com.ywca.pentref.R;

/**
 * Includes an id, a category name. The icon resource id is determined by the category id.
 */
public class Category {
    private int id;
    private String name;

    public Category() {
    }

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

    // 5 to 7 is assigned for the Launching Acivity in a dodgy way
    public int getImageResourceId() {
        switch (this.id) {
            //For bookmark
            case -11:
                return R.drawable.ic_bookmarked_black_36dp;
            //For ALL POI
            case -10:
                return R.drawable.ic_place_black_36dp;
            case 1:
                return R.drawable.ic_menu_camera;
            case 2:
                return R.drawable.ic_bus_black_36dp;
            case 3:
                return R.drawable.ic_restaurant_black;
            case 4:
                return R.drawable.ic_store_black;
            case 5:
                return R.drawable.ic_bookmarked_black_36dp;
            case 6:
                return R.drawable.ic_sunny_black;
            case 7:
                return R.drawable.ic_bus_black_36dp;
            case 8:
                return R.drawable.ic_settings;
            default:
                return R.drawable.ic_ferry_black_36dp;
        }
    }
}