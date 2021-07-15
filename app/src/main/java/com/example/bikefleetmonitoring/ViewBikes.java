package com.example.bikefleetmonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ViewBikes extends AppCompatActivity {

    RecyclerView recyclerViewBikes;
    ArrayList<BikeDetails> bikeDetails;
    AdapterBikeDetails adapterBikeDetails;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_bikes);

        findXmlElements();
        initializeToolbar();

        bikeDetails = new ArrayList<>();

        recyclerViewBikes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBikes.setHasFixedSize(true);

        for (int i = 0; i < 20; i++) {
            bikeDetails.add(new BikeDetails("Bici " + i));
        }

        adapterBikeDetails = new AdapterBikeDetails(bikeDetails);
        recyclerViewBikes.setAdapter(adapterBikeDetails);
    }

    private void findXmlElements() {
        recyclerViewBikes = findViewById(R.id.recycleViewBikes);
        toolbar = findViewById(R.id.toolbar);
    }

    private void initializeToolbar() {
        toolbar.setTitle("Bici disponibili");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewBikes.this, MappaRastrelliere.class);
                startActivity(intent);
            }
        });
    }
}
