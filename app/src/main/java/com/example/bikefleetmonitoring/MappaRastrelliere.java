package com.example.bikefleetmonitoring;

import android.Manifest;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class MappaRastrelliere extends AppCompatActivity implements OnMapReadyCallback {
    GoogleMap map;
    Toolbar toolbar;
    private ClusterManager<MyCluster> clusterManager;
    String rastrelliereJson;
    String url = "http://" + Login.ip + ":3000/rastrelliere";

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mappa_rastrelliere);

        String[] neededPermissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        };
        ActivityCompat.requestPermissions(this, neededPermissions, 1);

        trovaElementiXML();
        inizializzaToolbar();

        richiestaGetRastrelliere();
    }

    private void trovaElementiXML() {
        toolbar = findViewById(R.id.toolbar);
    }

    private void inizializzaToolbar() {
        toolbar.setTitle("Rastrelliere disponibili");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MappaRastrelliere.this, Home.class);
                startActivity(intent);
            }
        });
    }

    private void richiestaGetRastrelliere() {
        //Istanzia la coda di richieste
        RequestQueue queue = Volley.newRequestQueue(MappaRastrelliere.this);

        // Stringa per fare la richiesta. Nel caso della posizione facciamo una richiesta POST all'url "http://192.168.1.122:3000/prova_posizione"
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    // Aggiungi codice da fare quando arriva la risposta dalla richiesta
                    rastrelliereJson = response;
                    visualizzaMappa();
                },
                error -> {
                    // Aggiungi codice da fare se la richiesta non è andata a buon fine
                    //------------------------------------------------------------------
                });
        // Aggiungiamo la richiesta alla coda.
        queue.add(stringRequest);
    }

    private void visualizzaMappa() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }


    // Get a handle to the GoogleMap object and display marker.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng coordinateMura = new LatLng(44.49712, 11.34248);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinateMura, 13.8f));

        /* Inizializziamo il cluster. */
        clusterManager = new ClusterManager<MyCluster>(this, map);

        // Point the map's listeners at the listeners implemented by the cluster manager.
        map.setOnCameraIdleListener(clusterManager);

        inizializzaCluster();

        new AsyncTaskGetMareker().execute();
    }

    private void inizializzaCluster() {
        //Al click dei marker all'interno dei cluster si aprirà la pagina "ViewBikes"
        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyCluster>() {
            @Override
            public boolean onClusterItemClick(MyCluster item) {
                Intent intent = new Intent(MappaRastrelliere.this, BiciDisponibili.class);
                intent.putExtra("id", item.getId());
                startActivity(intent);
                return true;
            }
        });

        //Quando clicchiamo sul cluster zoomera e si amplierà
        clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyCluster>() {
            @Override
            public boolean onClusterClick(Cluster<MyCluster> cluster) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        cluster.getPosition(), (float) Math.floor(map
                                .getCameraPosition().zoom + 1)), 300,
                        null);
                return true;
            }
        });
    }

    private class AsyncTaskGetMareker extends AsyncTask<String, String, JSONArray> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONArray doInBackground(String... strings) {

            try {
                return new JSONArray(rastrelliereJson);   //Prendo le rastrelliere passate dalla home
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //This will only happen if an exception is thrown above:
            return null;
        }

        //Prendiamo i marker dalle rastrelliere
        protected void onPostExecute(JSONArray result) {

            if (result != null) {
                JSONObject jsonObject;
                for (int i = 0; i < result.length(); i++) {
                    try {
                        jsonObject = result.getJSONObject(i);
                        drawMarker(new LatLng((Double) jsonObject.get("lat"), (Double) jsonObject.get("long")), jsonObject.get("name").toString(), Integer.parseInt(jsonObject.get("id").toString()));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //Mettiamo il singolo marker nel cluster manager, a visualizzarlo ci pensa lui
    private void drawMarker(LatLng point, String name, int id) {
        //Creiamo l'elemento da aggiungere al clusterManager
        MyCluster offsetItem = new MyCluster(point.latitude, point.longitude, name, name, id);

        //Aggiungiamo l'elemento al cluster manager
        clusterManager.addItem(offsetItem);

        //Forza una re-clusterizzazione nella mappa, senza di questo non appaiono la prima volta
        clusterManager.cluster();
    }
}