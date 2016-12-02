package com.ywca.pentref.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
 * This {@link AppCompatActivity} is launched every time when the app is launched,
 * before {@link MainActivity} is launched.
 */
public class LaunchingActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private final int REQUEST_CODE_MAIN_ACTIVITY = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launching);

        // TODO: Use the appropriate icons
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(1, getResources().getString(R.string.discover)));
        categories.add(new Category(2, getResources().getString(R.string.bookmarks)));
        categories.add(new Category(3, getResources().getString(R.string.weather)));
        categories.add(new Category(4, getResources().getString(R.string.transport_schedule)));

        GridView gridView = (GridView) findViewById(R.id.grid_view);
        CategoryAdapter adapter = new CategoryAdapter(this, categories);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Exit the activity when MainActivity navigates back to this activity,
        // which is normally triggered by onBackPress().
        if (requestCode == REQUEST_CODE_MAIN_ACTIVITY) {
            finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getId() == R.id.grid_view) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(Utility.FRAGMENT_INDEX_EXTRA_KEY, i);
            startActivityForResult(intent, REQUEST_CODE_MAIN_ACTIVITY);
        }
    }
}