package com.example.bikefleetmonitoring;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GeolocalizationService extends Service {
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    String urlAggiungiPosizione = "http://" + Login.ip + ":3000/addPosizione";
    String urlIntersezioneGeofence = "http://" + Login.ip + ":3000/intersezione_geofence";
    public static ArrayList<Pair<Double, Double>> pairLatLngArr = new ArrayList<>();
    String idBici;

    /* All'interno troviamo "OnLocationResult()" il metodo più importante che viene chiamato ogni
     * volta che il sistema effettua una geolocalizzazione dell'utente. */
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();

                pairLatLngArr.add(new Pair<>(lng, lat));

                //Dopo aver preso l'ultima posizione dell'utente chiediamo di aggiornare la posizione della bici sul db
                richiestaPostPosizioneUtente();

                checkGeofenceIntersecata(lat, lng);
            }
        }
    };

    /* Richiesta POST per aggiungere la posizione dell'utente. */
    private void richiestaPostPosizioneUtente() {
        //Istanzia la coda di richieste
        RequestQueue queue = Volley.newRequestQueue(GeolocalizationService.this);

        // Stringa per fare la richiesta. Nel caso della posizione facciamo una richiesta POST all'url "http://192.168.1.122:3000/addPosizione"
        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlAggiungiPosizione,
                response -> {
                    // Aggiungi codice da fare quando arriva la risposta dalla richiesta
                },
                error -> {
                    // Aggiungi codice da fare se la richiesta non è andata a buon fine
                }) {
            //Utile ad inserire i parametri alla richiesta. Messi nel body
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                String lng = pairLatLngArr.get(pairLatLngArr.size() - 1).first.toString();
                String lat = pairLatLngArr.get(pairLatLngArr.size() - 1).second.toString();

                //Mettiamo i parametri nel body della richiesta, passiamo latitudine, longitudine ed id della bici noleggiata
                params.put("lat", lat);
                params.put("long", lng);
                params.put("id", idBici);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        // Aggiungiamo la richiesta alla coda.
        queue.add(stringRequest);
    }

    private void checkGeofenceIntersecata(double lat, double lng) {

        RequestQueue queue = Volley.newRequestQueue(GeolocalizationService.this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlIntersezioneGeofence + "?lng=" + lng + "&lat=" + lat,
                response -> {
                    // Aggiungi codice da fare quando arriva la risposta dalla richiesta
                    try {
                        JSONArray arr = new JSONArray(response);
                        if (arr.length() > 0) {
                            String name = arr.getJSONObject(0).getString("name");

                            Log.d("NOME GEOFENCE VIETATA INTERSECATA ----------------->", name);
                        } else {
                            Log.d("ATTENZIONE!!!!!!!!!!!!!!!!!!!!!!! ----------------->", "NESSUNA GEOFENCE INTERSECATA");
                        }
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

    /* Metodi da non toccare, utili per la geolocalizzazione continua. */
    private void checkSettingsAndStartLocationUpdates() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //Settings of device are satisfied and we can start location updates
                startLocationUpdates();
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    /*try {
                        apiException.startResolutionForResult(GeolocalizationService.this, 1001);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }*/
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        idBici = intent.getStringExtra("id");

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        checkSettingsAndStartLocationUpdates();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        stopLocationUpdates();
    }
}
