package com.example.bikefleetmonitoring;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MappaRastrelliere extends AppCompatActivity implements OnMapReadyCallback {
    GoogleMap map;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rastrelliere);


        String[] neededPermissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        };
        ActivityCompat.requestPermissions(this, neededPermissions, 1);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);


    }

    // Get a handle to the GoogleMap object and display marker.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.addMarker(new MarkerOptions()
                .position(new LatLng(39.68423765153896, 8.646825125206847))
                .title("Snarcy"));
        new AsyncTaskGetMareker().execute();

    }

    public String getJSONFromAssets() {
        String json = null;
        try {
            InputStream inputData = getAssets().open("rastrelliere.json");
            int size = inputData.available();
            byte[] buffer = new byte[size];
            inputData.read(buffer);
            inputData.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private class AsyncTaskGetMareker extends AsyncTask<String, String, JSONArray> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONArray doInBackground(String... strings) {
            String rastrelliereJsonString = getJSONFromAssets();
            try {
                return new JSONArray(rastrelliereJsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //This will only happen if an exception is thrown above:
            return null;
        }

        //Prendiamo i marker dalle rastrelliere
        protected void onPostExecute(JSONArray result) {
            JSONArray features = null;
            if (result != null) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = result.getJSONObject(0);
                    features = jsonObject.getJSONArray("features");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (features != null){
                    for (int i = 0; i < features.length(); i++) {
                        try {
                            JSONObject c = features.getJSONObject(i);
                            JSONObject properties = c.getJSONObject("properties");
                            JSONObject geometry = c.getJSONObject("geometry");
                            JSONArray coordinates = geometry.getJSONArray("coordinates");
                            String name = properties.getString("Nome");
                            Double lang = (Double) coordinates.get(0);
                            Double lat = (Double) coordinates.get(1);

                            drawMarker(new LatLng(lat, lang), name);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }

        }

        //Disegniamo il singolo marker
        private void drawMarker(LatLng point, String name) {
            map.addMarker(new MarkerOptions()
                    .position(point)
                    .title(name));
        }
    }
}
