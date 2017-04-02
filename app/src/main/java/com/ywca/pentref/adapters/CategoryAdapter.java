package com.ywca.pentref.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ywca.pentref.R;
import com.ywca.pentref.common.Category;

import java.util.List;

/**
 * An adapter that displays a list of category items on a GridView.
 */
public class CategoryAdapter extends BaseAdapter {
    private Context mContext;
    private List<Category> mCategories;

    public CategoryAdapter(Context context, List<Category> categories) {
        mContext = context;
        mCategories = categories;
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
        // Create the layout for the item of the grid view if it has not been created
        if (convertView == null) {
            // Inflate the layout for the grid view
            convertView = LayoutInflater.from(mContext).inflate(R.layout.category_row_layout, parent, false);

            final Category categoryItem = mCategories.get(position);

            ImageView icon = (ImageView) convertView.findViewById(R.id.category_icon);
            //icon.setVisibility(View.INVISIBLE);
            icon.setImageResource(categoryItem.getImageResourceId());

            TextView category = (TextView) convertView.findViewById(R.id.category_text_view);
            category.setText(categoryItem.getName());
        }

        return convertView;
    }
}