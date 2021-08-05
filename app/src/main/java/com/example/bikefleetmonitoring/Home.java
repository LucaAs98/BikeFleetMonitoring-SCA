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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Home extends AppCompatActivity {
    public static String session = null;
    AppCompatButton btnPrenotaBici, btnAnnullaPrenotazione, btnLogout;
    TextView tvMessaggioIniziale, tvVediCodPrenot;
    Toolbar toolbar;
    boolean prenotato;

    String url = "http://192.168.1.110:3000/rastrelliere";
    String url2 = "http://192.168.1.110:3000/vis_pren";
    AsyncTask<Void, Void, Void> mTask;
    String jsonString;
    String codP; //codice alfanumerico della prenotazione
    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        isPrenotato();

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
                intent.putExtra("codice",codP);
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
                /* Carica le rastrelliere dal db e va alla mappa delle rastrelliere. */
                mTask = getRastrelliere();
                mTask.execute();
            }
        });
        tvVediCodPrenot.setVisibility(View.GONE);
        btnAnnullaPrenotazione.setVisibility(View.GONE);



        tvMessaggioIniziale.setText("Ciao " + Home.session + "!\nInizia subito a noleggiare una bici vicino a te!");
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

    public static String getCodPren(String url2,String codU) throws IOException, JSONException {

        BufferedReader inputStream = null;
        String codP = null;
        url2 = url2 + "?cod_u=" + codU;

        URL jsonUrl = new URL(url2);
        URLConnection dc = jsonUrl.openConnection();

        dc.setConnectTimeout(5000);
        dc.setReadTimeout(5000);

        inputStream = new BufferedReader(new InputStreamReader(
                dc.getInputStream()));

        String jsonS = inputStream.readLine();
        JSONArray arr = new JSONArray(jsonS);


        if(arr.length() == 1 ) {
            codP = arr.getJSONObject(0).getString("codice");
        }


        // read the JSON results into a string
        return codP;
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


    public AsyncTask<Void, Void, Void> isPrenotato() {
        return new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    codP = getCodPren(url2,Home.session);


                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if(codP != null){
                    prenotato = true;
                }else {
                    prenotato = false;
                }
                if (prenotato) {
                    inizializzaPrenotato();
                } else {
                    inizializzaNonPrenotato();
                }
            }
        }.execute();
    }

}
