package com.example.bikefleetmonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BiciDisponibili extends AppCompatActivity {

    RecyclerView recyclerViewBici;
    ArrayList<DettagliBici> dettagliBici;
    AdapterDettagliBici adapterDettagliBici;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bici_disponibili);

        findXmlElements();
        initializeToolbar();

        dettagliBici = new ArrayList<>();

        recyclerViewBici.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBici.setHasFixedSize(true);

        for (int i = 0; i < 20; i++) {
            dettagliBici.add(new DettagliBici("Bici " + i));
        }

        adapterDettagliBici = new AdapterDettagliBici(dettagliBici);
        recyclerViewBici.setAdapter(adapterDettagliBici);


    }

    private void findXmlElements() {
        recyclerViewBici = findViewById(R.id.recycleViewBikes);
        toolbar = findViewById(R.id.toolbar);
    }

    private void initializeToolbar() {
        toolbar.setTitle("Bici disponibili");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BiciDisponibili.this, MappaRastrelliere.class);
                startActivity(intent);
            }
        });
    }
}
