package com.ywca.pentref.fragments;

import android.app.Fragment;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.ywca.pentref.R;
import com.ywca.pentref.adapters.BookmarksAdapter;
import com.ywca.pentref.common.Contract;
import com.ywca.pentref.common.PentrefProvider;
import com.ywca.pentref.models.Poi;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a list of all the bookmarked {@link Poi}.
 */
public class BookmarksFragment extends Fragment {
    private RecyclerView mRecyclerView;

    public BookmarksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootVIew = inflater.inflate(R.layout.fragment_bookmarks, container, false);

        mRecyclerView = (RecyclerView) rootVIew.findViewById(R.id.bookmarks_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // TODO: Has to use read data
        new AsyncTask<Void, Void, List<Poi>>() {
            @Override
            protected List<Poi> doInBackground(Void... voids) {
                List<Poi> pois = new ArrayList<>();

                String selection = Contract.Poi.TABLE_NAME + "." + Contract.Poi._ID + " IN " +
                        "(SELECT " + Contract.Bookmark.TABLE_NAME + "." +
                        Contract.Bookmark.COLUMN_POI_ID + " FROM " +
                        Contract.Bookmark.TABLE_NAME + ")";

                Cursor cursor = getActivity().getContentResolver().query(
                        Contract.Poi.CONTENT_URI, null, selection, null, null);

                if (cursor != null) {
                    pois = PentrefProvider.convertToPois(cursor);
                    cursor.close();
                }

                return pois;
            }

            @Override
            protected void onPostExecute(List<Poi> pois) {
                super.onPostExecute(pois);
                mRecyclerView.setAdapter(new BookmarksAdapter(R.layout.bookmark_row_layout, pois));
            }
        }.execute();

        return rootVIew;
    }

}