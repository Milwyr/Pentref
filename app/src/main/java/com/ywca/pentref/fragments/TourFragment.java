package com.ywca.pentref.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ywca.pentref.R;
import com.ywca.pentref.adapters.ToursAdapter;
import com.ywca.pentref.models.Tour;

import java.util.ArrayList;
import java.util.List;


public class TourFragment extends BaseFragment {
    private RecyclerView mRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tour, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_tour_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Insert tour items into the recycleView
        List<Tour> toursList = new ArrayList<>();
        Tour tour1 = new Tour("Tour1","團1","path","datetime",2,"This is a test","站在海拔373公尺的太平山頂上，您就像是乘坐飛機橫越維多利亞港的上空，超過300萬居民的香港市區—維港兩岸的九龍半島、香港島北岸，將一一展現在您的眼前。就這樣，一幢幢摩天大樓筆直的聳立著，看似伸手可及。別忘了，香港是全球人口密度最高的城市之一。想像一下，當山下無數座大樓都同時亮起燈時，維港的海面、維港的上空都被映照得發紫，那會是多麼壯麗璀璨。","28822222");
        Tour tour2 = new Tour("Tour2","團2","path","datetime",2,"This is a test2","到太平山頂，最好是選擇接近黃昏的時候，這樣既能觀賞到白天的城市景觀，又可以靜待夜幕低垂時，整個城市在瞬間變幻的一刻。在太平山頂上有數個觀景台，包括盧吉道觀景台、獅子亭、位於山頂廣場頂層的免費觀景台Green Terrace，以及凌霄閣頂層的觀景台「凌霄閣摩天台428」。山頂廣場和凌霄閣同樣集觀景與購物享樂於一體，裡頭有多家可以讓您一邊用餐、一邊觀景的餐廳，從高級的西餐廳、露天咖啡廳，以至連鎖快餐店都有，豐儉由人。","28822222");
        toursList.add(tour1);
        toursList.add(tour2);
        mRecyclerView.setAdapter(new ToursAdapter(toursList));
        return rootView;

    }





}
