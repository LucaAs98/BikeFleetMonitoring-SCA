package com.example.bikefleetmonitoring;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyCluster implements ClusterItem {

    //Attenzione a modificare questi, potrebbe crashare
    private final LatLng position;
    private final String title;
    private final String snippet;
    private final int id;

    public MyCluster(double lat, double lng, String title, String snippet, int id) {
        position = new LatLng(lat, lng);
        this.title = title;
        this.snippet = snippet;
        this.id = id;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }


    public int getId() {
        return id;
    }

}
