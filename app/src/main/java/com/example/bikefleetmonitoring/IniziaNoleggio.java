package com.example.bikefleetmonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class IniziaNoleggio extends AppCompatActivity {
    Toolbar toolbar;
    EditText etCodiceNoleggio;
    AppCompatButton btnIniziaNoleggio;

    String urlRastrellieraVicino = "http://" + Login.ip + ":3000/checkDistance";
    String urlRastrCorrisp = "http://" + Login.ip + ":3000/rastrelliera_corrispondente";
    String urlNoleggio = "http://" + Login.ip + ":3000/avvia_noleggio";
    String codP;
    String idBici;
    String idRastrellieraVicino = "", idRastrellieraBici = "";

    double longUtente = 11.344264, latUtente = 44.48761;         //Prendi la posizione reale

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inizia_noleggio);

        trovaElementiXML();
        inizializzaToolbar();


        Intent intent = getIntent();
        codP = intent.getStringExtra("codice");
        idBici = intent.getStringExtra("bicicletta");

        urlRastrCorrisp = urlRastrCorrisp + "?bici=" + idBici;


        btnIniziaNoleggio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Controlla che il codice corrisponda, che l'utente si trovi in prossimità e setta nel db che è partito il noleggio. */
                if (!checkCodice()) {
                    etCodiceNoleggio.setError("Codice noleggio errato!");
                    return;
                }
                urlRastrellieraVicino = urlRastrellieraVicino + "?lng=" + longUtente + "&lat=" + latUtente;
                richiestaGetRastrellieraVicino();
            }
        });
    }


    private void trovaElementiXML() {
        toolbar = findViewById(R.id.toolbar);
        etCodiceNoleggio = findViewById(R.id.etCodiceNoleggio);
        btnIniziaNoleggio = findViewById(R.id.btnIniziaNoleggio);
    }

    private void inizializzaToolbar() {
        toolbar.setTitle("Sblocca Bici");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vaiAHome();
            }
        });
    }

    private boolean checkCodice() {
        return etCodiceNoleggio.getText().toString().equals(codP);
    }

    private void richiestaGetRastrellieraVicino() {

        //Istanzia la coda di richieste
        RequestQueue queue = Volley.newRequestQueue(IniziaNoleggio.this);

        // Stringa per fare la richiesta. Nel caso della posizione facciamo una richiesta POST all'url "http://192.168.1.122:3000/prova_posizione"
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlRastrellieraVicino,
                response -> {
                    JSONArray arrRastr = null;
                    // Aggiungi codice da fare quando arriva la risposta dalla richiesta
                    try {
                        arrRastr = new JSONArray(response);

                        if (arrRastr.length() > 0) {
                            JSONObject rastrelliera = arrRastr.getJSONObject(0);
                            idRastrellieraVicino = rastrelliera.getString("id");

                            richiestaGetRastrCorrispondente();

                        } else {
                            Toast.makeText(IniziaNoleggio.this, "Non sei abbastanza vicino ad una rastrelliera!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Aggiungi codice da fare se la richiesta non è andata a buon fine
                    //------------------------------------------------------------------
                });
        // Aggiungiamo la richiesta alla coda.
        queue.add(stringRequest);
    }

    private void richiestaGetRastrCorrispondente() {

        //Istanzia la coda di richieste
        RequestQueue queue = Volley.newRequestQueue(IniziaNoleggio.this);

        // Stringa per fare la richiesta. Nel caso della posizione facciamo una richiesta POST all'url "http://192.168.1.122:3000/prova_posizione"
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlRastrCorrisp,
                response -> {
                    JSONArray arrRastr = null;
                    // Aggiungi codice da fare quando arriva la risposta dalla richiesta
                    try {
                        arrRastr = new JSONArray(response);

                        if (arrRastr.length() > 0) {
                            JSONObject rastrelliera = arrRastr.getJSONObject(0);
                            idRastrellieraBici = rastrelliera.getString("rastrelliera");

                            if (idRastrellieraBici.equals(idRastrellieraVicino)) {
                                richiestaPostNoleggio();

                                //Facciamo partire la geolocalizzazione in background
                                Intent intent = new Intent(IniziaNoleggio.this, GeolocalizationService.class);
                                intent.putExtra("id", idBici);
                                startService(intent);

                                //Andiamo alla home
                                vaiAHome();
                            } else {
                                Toast.makeText(IniziaNoleggio.this, "Non sei vicino alla rastrelliera dove si trova la bici che vuoi noleggiare!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(IniziaNoleggio.this, "Non sei abbastanza vicino ad una rastrelliera!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Aggiungi codice da fare se la richiesta non è andata a buon fine
                    //------------------------------------------------------------------
                });
        // Aggiungiamo la richiesta alla coda.
        queue.add(stringRequest);
    }

    private void richiestaPostNoleggio() {
        //Istanzia la coda di richieste
        RequestQueue queue = Volley.newRequestQueue(IniziaNoleggio.this);

        // Stringa per fare la richiesta. Nel caso della posizione facciamo una richiesta POST all'url "http://192.168.1.122:3000/prova_posizione"
        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlNoleggio,
                response -> {
                    // Aggiungi codice da fare quando arriva la risposta dalla richiesta
                    Toast.makeText(IniziaNoleggio.this, "Il noleggio è andato a buon fine. ", Toast.LENGTH_LONG).show();
                },
                error -> {
                    // Aggiungi codice da fare se la richiesta non è andata a buon fine
                    Toast.makeText(IniziaNoleggio.this, "Il noleggio non è andato a buon fine. ", Toast.LENGTH_SHORT).show();
                }) {

            //Utile ad inserire i parametri alla richiesta. Messi nel body
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("codNoleggio", codP);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Aggiungiamo la richiesta alla coda.
        queue.add(stringRequest);
    }

    private void vaiAHome() {
        Intent intent = new Intent(IniziaNoleggio.this, Home.class);
        startActivity(intent);
    }
}
