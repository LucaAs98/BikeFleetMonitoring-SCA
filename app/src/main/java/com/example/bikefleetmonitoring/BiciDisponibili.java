package com.example.bikefleetmonitoring;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class BiciDisponibili extends AppCompatActivity {

    RecyclerView recyclerViewBici;
    ArrayList<DettagliBici> dettagliBici;
    AdapterDettagliBici adapterDettagliBici;

    Toolbar toolbar;

    String url = "http://10.0.0.1:3000/rastrelliere";
    String url2 = "http://10.0.0.1:3000/listabici";
    AsyncTask<Void, Void, Void> mTask;
    int idRastrelliera;
    String jsonString;
    Intent intent;
    JSONArray arr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bici_disponibili);

        findXmlElements();
        initializeToolbar();

        intent = getIntent();
        idRastrelliera = intent.getIntExtra("id", 0);

        dettagliBici = new ArrayList<>();

        recyclerViewBici.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBici.setHasFixedSize(true);
        getListaBici();




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

                /* Carica le rastrelliere dal db e va alla mappa delle rastrelliere. */
                mTask = getRastrelliere();
                mTask.execute();
            }
        });
    }

    public static String getJsonFromServer(String url) throws IOException {

        BufferedReader inputStream = null;

        URL jsonUrl = new URL(url);
        URLConnection dc = jsonUrl.openConnection();

        dc.setConnectTimeout(5000);
        dc.setReadTimeout(5000);

        inputStream = new BufferedReader(new InputStreamReader(
                dc.getInputStream()));


        // read the JSON results into a string
        return inputStream.readLine();
    }


    public AsyncTask<Void, Void, Void> getRastrelliere() {
        return new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    jsonString = getJsonFromServer(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                intent = new Intent(BiciDisponibili.this, MappaRastrelliere.class);
                intent.putExtra("rastrelliere_json", jsonString);
                startActivity(intent);
            }
        };
    }


    private JSONArray executeQweryBici(String url2, int idRastrelliera) throws IOException, JSONException {

        BufferedReader inputStream = null;

        url2 = url2 + "?id=" + idRastrelliera;

        URL jsonUrl = new URL(url2);
        URLConnection dc = jsonUrl.openConnection();

        dc.setConnectTimeout(5000);
        dc.setReadTimeout(5000);

        inputStream = new BufferedReader(new InputStreamReader(
                dc.getInputStream()));


        // read the JSON results into a string
        String jsonS = inputStream.readLine();
        JSONArray arr = new JSONArray(jsonS);


        // read the JSON results into a string
        return arr;
    }


    private void getListaBici() {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    arr = executeQweryBici(url2, idRastrelliera);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        int id = 0;
                        try {
                            id = arr.getJSONObject(i).getInt("id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        dettagliBici.add(new DettagliBici("Bici " + id, id));

                    }
                }
                adapterDettagliBici = new AdapterDettagliBici(dettagliBici);
                recyclerViewBici.setAdapter(adapterDettagliBici);

            }
        }.execute();

    }

}
