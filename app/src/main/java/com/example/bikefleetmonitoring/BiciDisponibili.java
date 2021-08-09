package com.example.bikefleetmonitoring;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BiciDisponibili extends AppCompatActivity {

    RecyclerView recyclerViewBici;
    ArrayList<DettagliBici> dettagliBici = new ArrayList<>();
    AdapterDettagliBici adapterDettagliBici;

    Toolbar toolbar;

    String urlGetListaBici = "http://" + Login.ip + ":3000/lista_bici";
    int idRastrelliera;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bici_disponibili);

        findXmlElements();
        initializeToolbar();

        intent = getIntent();
        idRastrelliera = intent.getIntExtra("id", 0);

        recyclerViewBici.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBici.setHasFixedSize(true);

        urlGetListaBici = urlGetListaBici + "?id=" + idRastrelliera;
        richiestaGetListaBici();
    }

    //Chiediamo la lista delle bici per stamparle quando clicchiamo una rasterlliera
    private void richiestaGetListaBici() {

        //Istanzia la coda di richieste
        RequestQueue queue = Volley.newRequestQueue(BiciDisponibili.this);

        // Stringa per fare la richiesta. Nel caso della lista bici facciamo una richiesta GET all'url "http://192.168.1.122:3000/lista_bici"
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlGetListaBici,
                response -> {
                    // Aggiungi codice da fare quando arriva la risposta dalla richiesta
                    try {
                        JSONArray arrBiciRastrelliera = new JSONArray(response);
                        setAdapterBici(arrBiciRastrelliera);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Aggiungi codice da fare se la richiesta non è andata a buon fine
                });
        // Aggiungiamo la richiesta alla coda.
        queue.add(stringRequest);
    }

    private void setAdapterBici(JSONArray arrBiciRastrelliera) {
        int id;
        for (int i = 0; i < arrBiciRastrelliera.length(); i++) {
            id = 0;
            try {
                id = arrBiciRastrelliera.getJSONObject(i).getInt("id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dettagliBici.add(new DettagliBici("Bici " + id, id));
        }
        adapterDettagliBici = new AdapterDettagliBici(dettagliBici, idRastrelliera);
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
                intent = new Intent(BiciDisponibili.this, MappaRastrelliere.class);
                startActivity(intent);
            }
        });
    }
}
