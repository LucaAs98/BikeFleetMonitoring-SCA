package com.example.bikefleetmonitoring;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ViewBooks extends AppCompatActivity {

    RecyclerView recyclerViewBikes;
    ArrayList<BikeDetails> bikeDetails;
    AdapterBikeDetails adapterBikeDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewbooks);

        recyclerViewBikes = findViewById(R.id.recycleViewBikes);
        bikeDetails = new ArrayList<>();

        recyclerViewBikes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBikes.setHasFixedSize(true);

        for (int i = 0; i < 20; i++) {
            bikeDetails.add(new BikeDetails("Bici " + i));
        }

        adapterBikeDetails = new AdapterBikeDetails(bikeDetails);
        recyclerViewBikes.setAdapter(adapterBikeDetails);

    }
}
