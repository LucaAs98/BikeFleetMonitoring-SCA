package com.example.bikefleetmonitoring;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

public class Home extends AppCompatActivity {
    public static String session = null;
    AppCompatButton btnPrenotaBici, btnNoleggio, btnLogout;
    TextView tvMessaggioIniziale, tvVediCodPrenot;
    Toolbar toolbar;
    boolean prenotato;
    boolean noleggioIniziato;

    String url = "http://" + Login.ip + ":3000/delPren";
    String url2 = "http://" + Login.ip + ":3000/vis_pren";
    String codP = null, idBici; //Codice alfanumerico della prenotazione


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        trovaElementiXML();
        inizializzaToolbar();

        noleggioIniziato = false;

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
        btnNoleggio = findViewById(R.id.btnNoleggio);
        btnLogout = findViewById(R.id.btnLogout);
        tvMessaggioIniziale = findViewById(R.id.tvMessaggioIniziale);
        tvVediCodPrenot = findViewById(R.id.tvVediCodPrenot);
        toolbar = findViewById(R.id.toolbar);
    }

    private void inizializzaToolbar() {
        toolbar.setTitle("Home");
    }

    private void inizializzaPrenotato() {
        btnNoleggio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noleggioIniziato) {
                    richiestaGetTerminaNoleggio();
                    Intent intent = new Intent(Home.this, GeolocalizationService.class);
                    stopService(intent);
                    intent = new Intent(Home.this, Home.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(Home.this, IniziaNoleggio.class);
                    intent.putExtra("codice", codP);
                    intent.putExtra("bicicletta", idBici);
                    startActivity(intent);
                }
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


        if (noleggioIniziato) {
            tvMessaggioIniziale.setText("Ciao " + Home.session + "!\nStai già noleggiando una bici, vuoi terminarla?");

        } else {
            tvMessaggioIniziale.setText("Ciao " + Home.session + "!\nStai prenotando una bici, vuoi iniziare il noleggio?");
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
            int color = typedValue.data;
            btnNoleggio.setBackgroundColor(color);
            btnNoleggio.setTextColor(Color.BLACK);
            btnNoleggio.setText("Sblocca bici");
        }
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
        btnNoleggio.setVisibility(View.GONE);


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
                            idBici = arrCodici.getJSONObject(0).getString("bicicletta");
                            noleggioIniziato = arrCodici.getJSONObject(0).getBoolean("iniziato");
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

    private void richiestaGetTerminaNoleggio() {
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