package com.example.bikefleetmonitoring;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GeolocalizationService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    String urlAggiungiPosizione = "http://" + Login.ip + ":3000/addPosizione";
    String urlIntersezioneGeofence = "http://" + Login.ip + ":3000/intersezione_geofence";
    String urlInsertDelay = "http://" + Login.ip + ":3000/insert_delay";
    public static ArrayList<Pair<Double, Double>> pairLatLngArr;
    String idBici;
    int idNotifica = 0;
    String nomeGeofence = "";
    ArrayList<String> listaGeofence = new ArrayList<>();
    Timestamp time;
    int lastActivityType = DetectedActivity.UNKNOWN;
    boolean bikingWalking = false;
    String fileScritturaLettura = "notification_delay.txt";   // File all'interno della quale andremo a scrivere tutti i delay delle notifiche

    /* Activity Recognition */
    private Context mContext;
    private ActivityRecognitionClient mActivityRecognitionClient;

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

                //Prendiamo il tempo del momento in cui l'utente è stato geolocalizzato
                time = new Timestamp((new Date()).getTime());

                pairLatLngArr.add(new Pair<>(lng, lat));

                //Dopo aver preso l'ultima posizione dell'utente chiediamo di aggiornare la posizione della bici sul db
                richiestaPostPosizioneUtente();

                //Controlliamo se l'utente sta camminando o andando in bici
                updateDetectedActivitiesList();

                //Controlliamo quale geofence interseca l'utente per l'invio della notifica
                checkGeofenceIntersecata(lat, lng, time);

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

    private void checkGeofenceIntersecata(double lat, double lng, Timestamp prevTime) {

        RequestQueue queue = Volley.newRequestQueue(GeolocalizationService.this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlIntersezioneGeofence + "?lng=" + lng + "&lat=" + lat,
                response -> {

                    // Aggiungi codice da fare quando arriva la risposta dalla richiesta
                    try {
                        JSONArray arr = new JSONArray(response);
                        if (arr.length() > 0) {
                            ArrayList<Integer> geofenceToNotify = checkGeofence(arr, true);

                            for (Integer n : geofenceToNotify) {
                                sendNotification(arr.getJSONObject(n), prevTime);
                            }
                            System.out.println(printMaxActivity(lastActivityType, 0));

                            if (bikingWalking) {
                                geofenceToNotify = checkGeofence(arr, false);

                                for (Integer n : geofenceToNotify) {
                                    sendNotification(arr.getJSONObject(n), prevTime);
                                }
                            }
                        } else {
                            listaGeofence.clear();
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


    private void sendNotification(JSONObject obj, Timestamp prevTime) throws JSONException {
        long differenceTime;
        String titoloGeofence = "";

        nomeGeofence = obj.getString("name");
        String messageGeofence = obj.getString("message");
        boolean tipoArea = obj.getBoolean("vietato");

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, Home.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        if (tipoArea) {
            titoloGeofence = "Attenzione! Ingresso in area di geofence vietata!";
        } else {
            titoloGeofence = "Attenzione! Ingresso in area di geofence PoI!";
        }

        if (messageGeofence.equals("null")) {
            messageGeofence = "Ingresso nell'area: " + nomeGeofence;
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "BikeFleetMonitoring")
                .setSmallIcon(R.mipmap.ic_bike)
                .setContentTitle(titoloGeofence)
                .setContentText(messageGeofence)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(idNotifica, builder.build());

        // Calcolo ritardo notifica
        long finalTime = (new Date()).getTime();
        differenceTime = finalTime - prevTime.getTime();
        System.out.println(differenceTime + " millisecondi. Tempo iniziale: " + prevTime.getTime() + " Tempo finale: " + finalTime);
        sendDelay(differenceTime);
        idNotifica++;
        listaGeofence.add(nomeGeofence);

    }
    private void sendDelay(long delay) {

        RequestQueue queue = Volley.newRequestQueue(GeolocalizationService.this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlInsertDelay + "?delay=" + delay + "&user=" + "Mauro",
                response -> {
                },
                error -> {
                    // Aggiungi codice da fare se la richiesta non è andata a buon fine
                });

        // Aggiungiamo la richiesta alla coda.

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(stringRequest);
    }
    private ArrayList<Integer> checkGeofence(JSONArray arr, boolean vietato) {
        String name = null;
        ArrayList<Integer> geoToPrint = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            try {
                name = arr.getJSONObject(i).getString("name");
                if (!listaGeofence.contains(name) && arr.getJSONObject(i).getBoolean("vietato") == vietato) {
                    geoToPrint.add(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        clearArrayGeofence(arr);

        return geoToPrint;
    }

    private void clearArrayGeofence(JSONArray arr) {
        HashMap<String, Boolean> listageofenceAttuali = new HashMap<>();

        for (String s : listaGeofence) {
            listageofenceAttuali.put(s, false);
        }


        for (int i = 0; i < arr.length(); i++) {
            String name = null;
            try {
                name = arr.getJSONObject(i).getString("name");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (listaGeofence.contains(name)) {
                listageofenceAttuali.replace(name, true);
            }
        }


        for (String s : listageofenceAttuali.keySet()) {
            if (!listageofenceAttuali.get(s)) {
                listaGeofence.remove(s);
            }
        }
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
        mContext = this;
        mActivityRecognitionClient = new ActivityRecognitionClient(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        idBici = intent.getStringExtra("id");
        pairLatLngArr = new ArrayList<>();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(0);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        checkSettingsAndStartLocationUpdates();
        //startActivityRecognition();
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

    /**
     * Registers for activity recognition updates using
     * {@link ActivityRecognitionClient#requestActivityUpdates(long, PendingIntent)}.
     * Registers success and failure callbacks.
     */
    public void startActivityRecognition() {
        @SuppressLint("MissingPermission") Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent());

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(mContext,
                        getString(R.string.activity_updates_enabled),
                        Toast.LENGTH_SHORT)
                        .show();
                updateDetectedActivitiesList();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(mContext,
                        getString(R.string.activity_updates_not_enabled),
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Processes the list of freshly detected activities. Asks the adapter to update its list of
     * DetectedActivities with new {@code DetectedActivity} objects reflecting the latest detected
     * activities.
     */
    protected void updateDetectedActivitiesList() {
        ArrayList<DetectedActivity> detectedActivities = Utils.detectedActivitiesFromJson(
                PreferenceManager.getDefaultSharedPreferences(mContext)
                        .getString(Constants.KEY_DETECTED_ACTIVITIES, ""));

        HashMap<Integer, Integer> detectedActivitiesMap = new HashMap<>();
        for (DetectedActivity activity : detectedActivities) {
            detectedActivitiesMap.put(activity.getType(), activity.getConfidence());
        }
        // Every time we detect new activities, we want to reset the confidence level of ALL
        // activities that we monitor. Since we cannot directly change the confidence
        // of a DetectedActivity, we use a temporary list of DetectedActivity objects. If an
        // activity was freshly detected, we use its confidence level. Otherwise, we set the
        // confidence level to zero.
        ArrayList<DetectedActivity> tempList = new ArrayList<>();
        for (int i = 0; i < Constants.MONITORED_ACTIVITIES.length; i++) {
            int confidence = detectedActivitiesMap.containsKey(Constants.MONITORED_ACTIVITIES[i]) ?
                    detectedActivitiesMap.get(Constants.MONITORED_ACTIVITIES[i]) : 0;

            tempList.add(new DetectedActivity(Constants.MONITORED_ACTIVITIES[i], confidence));
        }

        int activityType;
        int prevActivityType = DetectedActivity.UNKNOWN;
        int confidence;
        int maxConfidence = 0;
        for (DetectedActivity act : tempList) {
            activityType = act.getType();
            confidence = act.getConfidence();

            if (confidence > maxConfidence) {
                prevActivityType = activityType;
                maxConfidence = confidence;
            }
        }

        bikingWalking = lastActivityType == DetectedActivity.STILL && prevActivityType == DetectedActivity.WALKING;
        lastActivityType = prevActivityType;
        Toast.makeText(this, printMaxActivity(prevActivityType,0) + bikingWalking , Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(Constants.KEY_DETECTED_ACTIVITIES)) {
            updateDetectedActivitiesList();
        }
    }

    private String printMaxActivity(int activity, int confidence) {
        String stringaDaStampare = "";
        switch (activity) {
            case 3:
                stringaDaStampare = "STILL";
                break;
            case 7:
            case 8:
                stringaDaStampare = "WALKING";
                break;
            default:
                stringaDaStampare = "UNKNOWN";
                break;
        }

        return "DetectedActivity [type=" + stringaDaStampare + ", confidence=" + confidence + "]";
    }
}
