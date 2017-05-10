package com.ywca.pentref.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.ywca.pentref.R;
import com.ywca.pentref.adapters.CategoryAdapter;
import com.ywca.pentref.common.Category;
import com.ywca.pentref.common.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * This {@link AppCompatActivity} displays a few fragments and allows the user to choose,
 * and it is launched before {@link MainActivity}.
 */
public class ChoosePageActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_page);

        // The icons are decided in the Category class
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(1, getResources().getString(R.string.discover)));
        categories.add(new Category(5, getResources().getString(R.string.bookmarks)));
        categories.add(new Category(6, getResources().getString(R.string.weather)));
        categories.add(new Category(7, getResources().getString(R.string.transport_schedule)));
        categories.add(new Category(8, getResources().getString(R.string.settings)));

        GridView gridView = (GridView) findViewById(R.id.grid_view);
        CategoryAdapter adapter = new CategoryAdapter(this, categories);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getId() == R.id.grid_view) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(Utility.FRAGMENT_INDEX_EXTRA_KEY, i);
            startActivity(intent);

            // Exit the app so this page won't be navigated back to ChoosePageActivity
            //finish();
        }
    }
}