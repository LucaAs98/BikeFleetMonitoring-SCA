package com.example.bikefleetmonitoring;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Home extends AppCompatActivity {
    public static String session = null;
    AppCompatButton btnPrenotaBici, btnVediPrenotazione, btnLogout;
    TextView tvMessaggioIniziale, tvVediCodPrenot;
    Toolbar toolbar;
    FirebaseAuth fAuth;
    boolean prenotato = false;

    String url = "http://192.168.1.8:3000/rastrelliere";
    AsyncTask<Void, Void, Void> mTask;
    String jsonString;
    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        trovaElementiXML();
        fAuth = FirebaseAuth.getInstance();
        inizializzaToolbar();

        if (prenotato) {
            inizializzaPrenotato();
        } else {
            inizializzaNonPrenotato();
        }

        /* Se premiamo il pulsante di Logout andiamo al login e togliamo l'istanza all'utente. */
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(Home.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
    }


    private void trovaElementiXML() {
        btnPrenotaBici = findViewById(R.id.btnPrenotaBici);
        btnVediPrenotazione = findViewById(R.id.btnVediPrenotazione);
        btnLogout = findViewById(R.id.btnLogout);
        tvMessaggioIniziale = findViewById(R.id.tvMessaggioIniziale);
        tvVediCodPrenot = findViewById(R.id.tvVediCodPrenot);
        toolbar = findViewById(R.id.toolbar);
    }

    private void inizializzaToolbar() {
        toolbar.setTitle("Home");
    }

    private void inizializzaPrenotato() {
        btnVediPrenotazione.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prenotato = false;      /* Ora non funziona!!!!!!!!!
                 * sarebbe da ricaricare la pagina eliminando la prenotazione della bici! */
                Intent intent = new Intent(Home.this, Home.class);
                startActivity(intent);
            }
        });

        tvVediCodPrenot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, VediCodicePrenot.class);
                startActivity(intent);
            }
        });
        btnPrenotaBici.setVisibility(View.GONE);
        String utente = "$UTENTE";

        if (fAuth.getCurrentUser() != null) {
            utente = fAuth.getCurrentUser().getEmail();
        }
        tvMessaggioIniziale.setText("Ciao " + utente + "!\nStai già noleggiando una bici, vuoi annullare la tua prenotazione?");
    }

    public void inizializzaNonPrenotato() {
        btnPrenotaBici.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Carica le rastrelliere dal db e va alla mappa delle rastrelliere. */
                mTask = getRastrelliere();
                mTask.execute();
            }
        });
        tvVediCodPrenot.setVisibility(View.GONE);
        btnVediPrenotazione.setVisibility(View.GONE);

        String utente = "$UTENTE";

        if (fAuth.getCurrentUser() != null) {
            utente = fAuth.getCurrentUser().getEmail();
        }

        tvMessaggioIniziale.setText("Ciao " + utente + "!\nInizia subito a noleggiare una bici vicino a te!");
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
                intent = new Intent(Home.this, MappaRastrelliere.class);
                intent.putExtra("rastrelliere_json", jsonString);
                startActivity(intent);
            }
        };
    }
}
