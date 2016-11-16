package com.ywca.pentref.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherFragment extends Fragment {
    private ProgressDialog progress;

    public WeatherFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        progress = new ProgressDialog(getActivity());
        progress.setTitle("Loading");
        progress.setMessage("Weather is loading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog

        WebView webView = new WebView(getActivity());
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://www.yahoo.com/news/weather/hong-kong/tai-o/tai-o-2165422");
        webView.getSettings().setJavaScriptEnabled(true);
        return webView;

    }

    private class WebViewClient extends android.webkit.WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
            progress.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);
            progress.dismiss();
        }

    }
}