package com.ywca.pentref.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ywca.pentref.R;
import com.ywca.pentref.common.Category;

import java.util.List;

/**
 * Created by Ronald on 2017/4/5.
 */

public class SpinnerCategoryAdapter extends BaseAdapter {
    private Context mContext;
    private List<Category> mCategories;

    public SpinnerCategoryAdapter(Context context, List<Category> categories) {
        mContext = context;
        mCategories = categories;

        //Add a new categories for ALL POI
        Category allCategory = new Category(-10,"All point of interest");
        mCategories.add(0,allCategory);
        //All a new categories for Bookmark
        Category bookmarkCategory = new Category(-11,"Bookmark");
        mCategories.add(bookmarkCategory);
    }

    @Override
    public int getCount() {
        return mCategories.size();
    }

    @Override
    public Object getItem(int position) {
        return (mCategories == null) ? 0 : mCategories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i("SpinnerAdapter","Ha");
        // Create the layout for the item of the grid view if it has not been created
        if (convertView == null) {
            // Inflate the layout for the grid view
            convertView = LayoutInflater.from(mContext).inflate(R.layout.category_spinner_row_layout, parent, false);

            final Category categoryItem = mCategories.get(position);

            ImageView icon = (ImageView) convertView.findViewById(R.id.spinner_category_icon);
            icon.setImageResource(categoryItem.getImageResourceId());

            TextView category = (TextView) convertView.findViewById(R.id.spinner_category_name);
            category.setText(categoryItem.getName());

        }else{
            Log.i("SpinnerAdapter","Else");
            final Category categoryItem = mCategories.get(position);

            ImageView icon = (ImageView) convertView.findViewById(R.id.spinner_category_icon);
            icon.setImageResource(categoryItem.getImageResourceId());

            TextView category = (TextView) convertView.findViewById(R.id.spinner_category_name);
            category.setText(categoryItem.getName());
        }

        return convertView;
    }


}
