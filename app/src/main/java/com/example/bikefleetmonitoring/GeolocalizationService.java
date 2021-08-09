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
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GeolocalizationService extends Service {
    private static final String TAG = "MyLocationDemoActivity";
    int LOCATION_REQUEST_CODE = 10001;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    String fileScritturaLettura = "position.txt";   // File all'interno della quale andremo a scrivere tutte le posizioni che ci serviranno quando dobbiamo recuperare il tracciato
    // Ricora! Ora è implementato in modo tale che quando riapre l'app il file positions è vuoto!
    int numPosizione = 0;                           // Numero della posizione rilevata. Ci serve principalmente per formattare bene la stringa con le posizioni rilevate.
    String urlAggiungiPosizione = "http://" + Login.ip + ":3000/addPosizione";
    public static ArrayList<Pair<Double, Double>> pairLatLngArr = new ArrayList<>();
    private JSONArray jsonArr = new JSONArray();
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

                //Dopo aver preso l'ultima posizione dell'utente chiediamo di aggiornare la posizione della bici sul db
                richiestaPostPosizioneUtente();
            }
        }
    };

    /* Creazione iniziale della coppia di lat e long per essere compatibile con ST_GeomFromGeoJSON, questo non basta,
     * verrà modificata ulteriormente in Home alla richiesta di termine noleggio! */
    private void creaGeoJSONCorretto() {
        double lng, lat;
        try {
            for (int i = 0; i < jsonArr.length(); i++) {
                lng = jsonArr.getJSONObject(jsonArr.length() - 1).getJSONObject("pos_" + (i)).getDouble("long");
                lat = jsonArr.getJSONObject(jsonArr.length() - 1).getJSONObject("pos_" + (i)).getDouble("lat");
                pairLatLngArr.add(new Pair<>(lng, lat));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /* Metodo per leggere dal file, salviamo il tutto in modo tale da poter salvare lo storico sul db. */
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
            /*Log.d("Stringa: ", stringBuffer.toString());
            Log.d("JSON AUX: ", jsonArr.toString());
            Log.d("JSON: ", pairLatLngArr.toString());*/

            creaGeoJSONCorretto();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    /* Metodo utile alla scrittura su file situato in memoria interna del telefono. Ogni volta che viene richiamato
     * appende al contenuto già presente nel file. */
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
                try {
                    String lng = jsonArr.getJSONObject(jsonArr.length() - 1).getJSONObject("pos_" + (jsonArr.length() - 1)).get("long").toString();
                    String lat = jsonArr.getJSONObject(jsonArr.length() - 1).getJSONObject("pos_" + (jsonArr.length() - 1)).get("lat").toString();

                    //Mettiamo i parametri nel body della richiesta, passiamo latitudine, longitudine ed id della bici noleggiata
                    params.put("lat", lat);
                    params.put("long", lng);
                    params.put("id", idBici);
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
        // Aggiungiamo la richiesta alla coda.
        queue.add(stringRequest);
    }

    /* Metodi da non toccare, utili per la geolocalizzazione continua. */
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

        initializeFileOnInternalStorage();

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
