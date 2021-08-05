package com.example.bikefleetmonitoring;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;


public class MyLocationDemoActivity extends AppCompatActivity {

    private static final String TAG = "MyLocationDemoActivity";
    int LOCATION_REQUEST_CODE = 10001;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    String fileScritturaLettura = "position.txt";   // File all'interno della quale andremo a scrivere tutte le posizioni che ci serviranno quando dobbiamo recuperare il tracciato
    // Ricora! Ora è implementato in modo tale che quando riapre l'app il file positions è vuoto!
    int numPosizione = 0;                           // Numero della posizione rilevata. Ci serve principalmente per formattare bene la stringa con le posizioni rilevate.
    String url1 = "http://192.168.1.122:3000/prova_posizione";
    JSONArray jsonArr = new JSONArray();

    /* All'interno troviamo "OnLocationResult()" il metodo più importante che viene chiamato ogni
     * volta che il sistema effettua una geolocalizzazione dell'utente. */
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                String message;       //Messaggio nella quale verranno inserite latitudine e longitudine, sarà poi formattato per trasformarlo in JSON corretto (Vedi writeFileOnInternalStorage)
                JSONObject json = new JSONObject();
                try {
                    json.put("lat", location.getLatitude());
                    json.put("long", location.getLongitude());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                message = json.toString();

                /* Iniziamo a formattare "message" per avere un JSON corretto. Il risultato sarà una
                 * lista di posizioni numerate in base a quando sono state rilevate. */
                message = "pos_" + numPosizione + ":" + message;

                // Se la posizione non è la prima, mettiamo una virgola prima per separare la nuova posizione dalla precedente.
                if (numPosizione > 0) {
                    message = "," + message;
                }
                writeFileOnInternalStorage(message);           //Scriviamo sul file la posizione nuova
                numPosizione += 1;                             //Incrementiamo la posizione da scrivere
                readFileFromInternalStorage();
                richiestaPostPosizioneUtente();
            }
        }
    };

    /* Metodo per leggere dal file, utilizzato per vedere se i dati vengono scritti correttamente. Non sarà utile
     *  implementarlo nella versione definitiva. Guarda però al suo interno perchè c'è scritto come formattare la
     *  stringa prima di trasformarla in JSON corretto. */
    public void readFileFromInternalStorage() {
        try {
            FileInputStream fileInputStream = openFileInput(fileScritturaLettura);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuffer = new StringBuilder();
            String lines, auxline = "";
            while ((lines = bufferedReader.readLine()) != null) {
                stringBuffer.append(lines).append("\n");
                auxline = lines;
            }
            jsonArr.put(new JSONObject("{" + auxline + "}"));
            Log.d("Stringa: ", stringBuffer.toString());
            Log.d("JSON: ", jsonArr.toString());

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    /*public String toGeoJSON(JSONArray jsonArray) {
        JSONObject featureCollection = new JSONObject();
        try {
            featureCollection.put("type", "featureCollection");
            JSONArray featureList = new JSONArray();
            // iterate through your list
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject explrObject = jsonArray.getJSONObject(i);
                // {"geometry": {"type": "Point", "coordinates": [-94.149, 36.33]}
                JSONObject point = new JSONObject();
                point.put("type", "Point");
                // construct a JSONArray from a string; can also use an array or list
                JSONArray coord = new JSONArray("[" + explrObject.get("long") + "," + explrObject.get("lat") + "]");
                point.put("coordinates", coord);
                JSONObject feature = new JSONObject();
                feature.put("geometry", point);
                featureList.put(feature);
                featureCollection.put("features", featureList);
            }
        } catch (JSONException e) {
            Log.d("can't save json object: ", e.toString());
        }
        return featureCollection.toString();
    }*/

    /* Metodo utile alla scrittura su file situato in memoria interna del telefono. Ogni volta che viene richiamato
     *  appende al contenuto già presente nel file. */
    public void writeFileOnInternalStorage(String message) {
        try {
            FileOutputStream fileOutputStream = openFileOutput(fileScritturaLettura, MODE_APPEND);      //Scrive in modo tale da appendere a ciò che è stato già scritto (MODE_APPEND)
            fileOutputStream.write(message.getBytes());     //Scrittura vera e propria
            fileOutputStream.close();                       //Chiusura del file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Metodo chiamato una volta sola all'inizio, per pulire il file se presente. */
    public void initializeFileOnInternalStorage() {
        String init = "";
        try {
            FileOutputStream fileOutputStream = openFileOutput(fileScritturaLettura, MODE_PRIVATE);     //La prima volta che apre l'app viene sovrascritto il file (MODE_PRIVATE)
            fileOutputStream.write(init.getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_location_demo);
        String[] neededPermissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        };
        ActivityCompat.requestPermissions(this, neededPermissions, 1);
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        initializeFileOnInternalStorage();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /* Richiesta POST per aggiungere la posizione dell'utente. */
    private void richiestaPostPosizioneUtente() {
        //Istanzia la coda di richieste
        RequestQueue queue = Volley.newRequestQueue(MyLocationDemoActivity.this);

        // Stringa per fare la richiesta. Nel caso della posizione facciamo una richiesta POST all'url "http://192.168.1.122:3000/prova_posizione"
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url1, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Aggiungi codice da fare quando arriva la risposta dalla richiesta

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Aggiungi codice da fare se la richiesta non è andata a buon fine
            }
        }) {
            //Utile ad inserire i parametri alla richiesta. Messi nel body
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                try {
                    String lng = jsonArr.getJSONObject(jsonArr.length() - 1).getJSONObject("pos_" + (jsonArr.length() - 1)).get("long").toString();
                    String lat = jsonArr.getJSONObject(jsonArr.length() - 1).getJSONObject("pos_" + (jsonArr.length() - 1)).get("lat").toString();
                    //Mettiamo i parametri nel body della richiesta
                    params.put("lat", lat);
                    params.put("long", lng);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    /**
     * Mai toccati, sono metodi utili alla geolocalizzazione continua.
     **/
    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            getLastLocation();
            checkSettingsAndStartLocationUpdates();
        } else {
            askLocationPermission();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    private void checkSettingsAndStartLocationUpdates() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //Settings of device are satisfied and we can start location updates
                startLocationUpdates();
            }
        });
        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    try {
                        apiException.startResolutionForResult(MyLocationDemoActivity.this, 1001);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
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

    private void getLastLocation() {
        @SuppressLint("MissingPermission") Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    //We have a location
                    /* Stampa sul log della posizione, possiamo tenerla commentata. */
                    /* Log.d(TAG, "onSuccess: " + location.toString());
                    Log.d(TAG, "onSuccess: " + location.getLatitude());
                    Log.d(TAG, "onSuccess: " + location.getLongitude()); */
                } else {
                    Log.d(TAG, "onSuccess: Location was null...");
                }
            }
        });
        locationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: " + e.getLocalizedMessage());
            }
        });
    }

    private void askLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d(TAG, "askLocationPermission: you should show an alert dialog...");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
//                getLastLocation();
                checkSettingsAndStartLocationUpdates();
            } else {
                //Permission not granted
            }
        }
    }
}