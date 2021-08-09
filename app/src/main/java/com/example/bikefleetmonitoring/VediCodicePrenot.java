package com.example.bikefleetmonitoring;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

import java.util.HashMap;
import java.util.Map;

public class VediCodicePrenot extends AppCompatActivity {
    Toolbar toolbar;
    AppCompatButton btnCopiaCodice, btnAnnullaPrenotazione;
    TextView tvCodice;
    String codP, urlCancellaPrenotazione = "http://" + Login.ip + ":3000/cancella_prenotazione";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vedi_codice_prenot);
        Intent intent = getIntent();
        codP = intent.getStringExtra("codice");

        trovaElementiXML();
        inizializzaToolbar();
        tvCodice.setText(codP); //Settiamo il codice da visualizzare con quello passato dalla Home

        //Copiamo il codice in automatico quando clicchiamo il bottone corrispondente.
        btnCopiaCodice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Codice noleggio", codP);
                clipboard.setPrimaryClip(clip);

                Context context = getApplicationContext();
                CharSequence testoToast = "Codice copiato negli appunti!";
                int durataToast = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, testoToast, durataToast);
                toast.show();
            }
        });

        //Settiamo cosa fare quando viene cliccato il bottone per annullare la prenotazione
        btnAnnullaPrenotazione.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Facciamo la richiesta per annullare la prenotazione e torniamo alla Home
                richestaPostCancellaPrenotazione();

                Intent intent = new Intent(VediCodicePrenot.this, Home.class);
                startActivity(intent);
            }
        });
    }

    /* Richiediamo la cancellazione della prenotazione (non la fine del noleggio) semplicemente passando
     * il codice di prenotazione. */
    private void richestaPostCancellaPrenotazione() {
        //Istanzia la coda di richieste
        RequestQueue queue = Volley.newRequestQueue(VediCodicePrenot.this);

        // Stringa per fare la richiesta. Nel caso della cancellazione di una prenotazione facciamo una richiesta POST all'url "http://192.168.1.122:3000/cancella_prenotazione"
        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlCancellaPrenotazione,
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

                params.put("cod_prenotazione", tvCodice.getText().toString());
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

    private void trovaElementiXML() {
        toolbar = findViewById(R.id.toolbar);
        btnCopiaCodice = findViewById(R.id.btnCopiaCodice);
        btnAnnullaPrenotazione = findViewById(R.id.btnAnnullaPrenotazione);
        tvCodice = findViewById(R.id.tvCodice);
    }

    private void inizializzaToolbar() {
        toolbar.setTitle("Codice bici prenotata");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VediCodicePrenot.this, Home.class);
                startActivity(intent);
            }
        });
    }
}
