package com.ywca.pentref.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ywca.pentref.R;
import com.ywca.pentref.common.CategoryItem;

import java.util.List;

/**
 * An adapter that displays a list of category items on a GridView.
 */
public class CategoryAdapter extends BaseAdapter {
    private Context mContext;
    private List<CategoryItem> mCategoryItems;

    public CategoryAdapter(Context context, List<CategoryItem> categoryItems) {
        mContext = context;
        mCategoryItems = categoryItems;
    }

    @Override
    public int getCount() {
        return mCategoryItems.size();
    }

    @Override
    public Object getItem(int position) {
        return (mCategoryItems == null) ? 0 : mCategoryItems.get(position);
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

            final CategoryItem categoryItem = mCategoryItems.get(position);

            ImageView icon = (ImageView) convertView.findViewById(R.id.category_icon);
            icon.setImageResource(categoryItem.getImageResourceId());

            TextView category = (TextView) convertView.findViewById(R.id.category_text_view);
            category.setText(categoryItem.getCategoryName());
        }

        return convertView;
    }
}