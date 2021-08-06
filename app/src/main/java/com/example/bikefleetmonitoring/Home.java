package com.example.bikefleetmonitoring;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class Home extends AppCompatActivity {
    public static String session = null;
    AppCompatButton btnPrenotaBici, btnAnnullaPrenotazione, btnLogout;
    TextView tvMessaggioIniziale, tvVediCodPrenot;
    Toolbar toolbar;
    boolean prenotato;

    String url = "http://192.168.1.122:3000/delPren";
    String url2 = "http://192.168.1.122:3000/vis_pren";
    String codP = null; //codice alfanumerico della prenotazione


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        trovaElementiXML();

        inizializzaToolbar();

        /* Se premiamo il pulsante di Logout andiamo al login e togliamo l'istanza all'utente. */
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Home.session = "";
                Intent intent = new Intent(Home.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        url2 = url2 + "?cod_u=" + Home.session;
        richiestaGetCodicePren();

    }

    private void trovaElementiXML() {
        btnPrenotaBici = findViewById(R.id.btnPrenotaBici);
        btnAnnullaPrenotazione = findViewById(R.id.btnAnnullaPrenotazione);
        btnLogout = findViewById(R.id.btnLogout);
        tvMessaggioIniziale = findViewById(R.id.tvMessaggioIniziale);
        tvVediCodPrenot = findViewById(R.id.tvVediCodPrenot);
        toolbar = findViewById(R.id.toolbar);
    }

    private void inizializzaToolbar() {
        toolbar.setTitle("Home");
    }

    private void inizializzaPrenotato() {
        btnAnnullaPrenotazione.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                richiestaGetAnnullaPren();
                Intent intent = new Intent(Home.this, Home.class);
                startActivity(intent);
            }
        });

        tvVediCodPrenot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, VediCodicePrenot.class);
                intent.putExtra("codice", codP);
                startActivity(intent);
            }
        });
        btnPrenotaBici.setVisibility(View.GONE);


        tvMessaggioIniziale.setText("Ciao " + Home.session + "!\nStai già noleggiando una bici, vuoi annullare la tua prenotazione?");
    }

    public void inizializzaNonPrenotato() {
        btnPrenotaBici.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, MappaRastrelliere.class);
                startActivity(intent);
            }
        });
        tvVediCodPrenot.setVisibility(View.GONE);
        btnAnnullaPrenotazione.setVisibility(View.GONE);


        tvMessaggioIniziale.setText("Ciao " + Home.session + "!\nInizia subito a noleggiare una bici vicino a te!");
    }

    /* Richiesta GET per ................ */
    private void richiestaGetCodicePren() {

        //Istanzia la coda di richieste
        RequestQueue queue = Volley.newRequestQueue(Home.this);

        // Stringa per fare la richiesta. Nel caso della posizione facciamo una richiesta POST all'url "http://192.168.1.122:3000/prova_posizione"
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url2,
                response -> {
                    // Aggiungi codice da fare quando arriva la risposta dalla richiesta
                    JSONArray arrCodici = null;
                    try {
                        arrCodici = new JSONArray(response);
                        if (arrCodici.length() == 1) {
                            codP = arrCodici.getJSONObject(0).getString("codice");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    setPrenotato();
                },
                error -> {
                    // Aggiungi codice da fare se la richiesta non è andata a buon fine
                    //------------------------------------------------------------------
                });
        // Aggiungiamo la richiesta alla coda.
        queue.add(stringRequest);
    }

    private void setPrenotato() {
        prenotato = codP != null;
        if (prenotato) {
            inizializzaPrenotato();
        } else {
            inizializzaNonPrenotato();
        }
    }

    private void richiestaGetAnnullaPren() {
        url = url + "?codPren=" + codP;

        //Istanzia la coda di richieste
        RequestQueue queue = Volley.newRequestQueue(Home.this);

        // Stringa per fare la richiesta. Nel caso della posizione facciamo una richiesta POST all'url "http://192.168.1.122:3000/prova_posizione"
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    // Aggiungi codice da fare quando arriva la risposta dalla richiesta

                },
                error -> {
                    // Aggiungi codice da fare se la richiesta non è andata a buon fine
                    //------------------------------------------------------------------
                });
        // Aggiungiamo la richiesta alla coda.
        queue.add(stringRequest);

    }
}
