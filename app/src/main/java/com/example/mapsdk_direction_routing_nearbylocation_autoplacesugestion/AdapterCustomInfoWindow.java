package com.example.mapsdk_direction_routing_nearbylocation_autoplacesugestion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class AdapterCustomInfoWindow implements GoogleMap.InfoWindowAdapter {
    private Context context;
    private final View mWindow;

    public AdapterCustomInfoWindow(Context context) {
        this.context = context;
        this.mWindow = LayoutInflater.from(context).inflate(R.layout.info_window_adapter,null);
    }


    private void rendWindow(Marker marker, View view){
        String title = marker.getTitle();
        TextView tv_title = view.findViewById(R.id.tv_title_infoWindowAdapter);

        if(!title.equals("")){
            tv_title.setText(title);
        }


        String snippet = marker.getSnippet();
        TextView tv_snippet = view.findViewById(R.id.tv_snippet_infoWindowAdapter);

        if(snippet!=null && !snippet.isEmpty()){
            tv_snippet.setText(snippet);
        }


    }



    @Override
    public View getInfoWindow(Marker marker) {
        rendWindow(marker,mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        rendWindow(marker,mWindow);
        return mWindow;
    }
}

