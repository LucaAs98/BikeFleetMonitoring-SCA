package com.example.bikefleetmonitoring;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Home extends AppCompatActivity {
    public static String session = null;
    AppCompatButton btnPrenotaBici, btnNoleggio, btnLogout;
    TextView tvMessaggioIniziale, tvVediCodPrenot;
    Toolbar toolbar;
    boolean prenotato, noleggioIniziato;
    int idRastrellieraVicino;

    String urlTerminaNoleggio = "http://" + Login.ip + ":3000/termina_noleggio";
    String urlGetCodPren = "http://" + Login.ip + ":3000/vis_pren";
    String urlRastrellieraVicino = "http://" + Login.ip + ":3000/checkDistance";
    String codP = null, idBici;


    /**** Prendi la posizione reale!!!!!!!!!!!!  ***/
    double longUtente = 11.344264, latUtente = 44.48761;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        trovaElementiXML();
        inizializzaToolbar();
        createNotificationChannel();

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

        urlGetCodPren = urlGetCodPren + "?cod_u=" + Home.session;
        richiestaGetCodicePren();
    }

    /* Richiesta GET per richiedere il codice di prenotazione dell'utente e successivamente si setta
     * se quest'ultimo ha effettuato una prenotazione o meno. A seconda di questo verrà visualizzata una schermata differente.  */
    private void richiestaGetCodicePren() {

        //Istanzia la coda di richieste
        RequestQueue queue = Volley.newRequestQueue(Home.this);

        // Stringa per fare la richiesta. Nel caso della posizione facciamo una richiesta POST all'url "http://192.168.1.122:3000/prova_posizione"
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlGetCodPren,
                response -> {
                    JSONArray arrCodici;
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

        queue.add(stringRequest);
    }

    /* Settiamo se l'utente è prenotato o meno se in base al codice prenotazione. */
    private void setPrenotato() {
        prenotato = codP != null;   //Se non è stato trovato nessun codice corrispondente ad una prenotazione che non sia anche nello storico
        if (prenotato) {
            inizializzaPrenotato();     //Inizializziamo la schermata come se fosse prenotato
        } else {
            inizializzaNonPrenotato();  //Inizializziamo la schermata come se non fosse prenotato
        }
    }

    /* Inizializziamo la schermata in base al fatto che l'utente abbia una prenotazione.
     * La schermata cambierà ancora di più se l'utente ha un noleggio attivo in questo momento oppure no. */
    private void inizializzaPrenotato() {
        /* Quando clicca il pulsante principale, bisogna controllare che l'utente abbia avviato il noleggio o meno.
         * Se l'ha avviato allora significa che vogliamo terminare il noleggio. Altrimenti vogliamo attivare il noleggio.*/
        btnNoleggio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noleggioIniziato) {
                    //Mettiamo nell'url la latitudine e la longitudine della posizione dell'utente.
                    urlRastrellieraVicino = urlRastrellieraVicino + "?lng=" + longUtente + "&lat=" + latUtente;
                    /* Controlliamo che l'utente si trovi vicino ad una rastrelliera. Successivamente
                    facciamo terminare il noleggio */
                    richiestaGetRastrellieraVicino();
                } else {
                    //Se non è iniziato il noleggio, vogliamo farlo iniziare inserendo il codice di noleggio.
                    Intent intent = new Intent(Home.this, IniziaNoleggio.class);
                    intent.putExtra("codice", codP);
                    intent.putExtra("bicicletta", idBici);
                    startActivity(intent);
                }
            }
        });

        /* Se il noleggio è iniziato visualizziamo il messaggio corretto. Altrimenti visualizziamo
         * un altro messaggio e settiamo anche l'onClick della textView per il codice. Inoltre in questo
         * caso dobbiamo anche cambiare lo stile del bottone principale. */
        if (noleggioIniziato) {
            tvMessaggioIniziale.setText("Ciao " + Home.session + "!\nStai già noleggiando una bici, vuoi terminarla?");
            tvVediCodPrenot.setVisibility(View.GONE);
        } else {
            /* TextView per visualizzare il codice della prenotazione. Questo è visualizzabile solo quando
             * il noleggio non è ancora iniziato, ma è avvenuta la prenotazione. */
            tvVediCodPrenot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Home.this, VediCodicePrenot.class);
                    intent.putExtra("codice", codP);
                    startActivity(intent);
                }
            });
            tvMessaggioIniziale.setText("Ciao " + Home.session + "!\nStai prenotando una bici, vuoi iniziare il noleggio?");

            //Cambiamo lo stile del bottone principale
            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
            int color = typedValue.data;
            btnNoleggio.setBackgroundColor(color);
            btnNoleggio.setTextColor(Color.BLACK);
            btnNoleggio.setText("Sblocca bici");
        }

        //Nascondiamo l'altro bottone di quando l'utente non è prenotato
        btnPrenotaBici.setVisibility(View.GONE);
    }

    /* Inizializziamo la schermata in base al fatto che l'utente non abbia una prenotazione. */
    public void inizializzaNonPrenotato() {
        //Con il bottone principale l'utente va alla mappa delle rastrelliere.
        btnPrenotaBici.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, MappaRastrelliere.class);
                startActivity(intent);
            }
        });
        //Settiamo il messaggio corretto per quando l'utente non ha una prenotazione
        tvMessaggioIniziale.setText("Ciao " + Home.session + "!\nInizia subito a noleggiare una bici vicino a te!");

        //Nascondiamo i pulsanti inutili per questa situazione
        tvVediCodPrenot.setVisibility(View.GONE);
        btnNoleggio.setVisibility(View.GONE);
    }

    /* Prendiamo la rastrelliera vicino all'utente. Nel caso non ce se fossero verrà visualizzato un Toast. */
    private void richiestaGetRastrellieraVicino() {
        //Istanzia la coda di richieste
        RequestQueue queue = Volley.newRequestQueue(Home.this);

        // Stringa per fare la richiesta. Nel caso della rastrelliera vicino facciamo una richiesta GET all'url "http://192.168.1.122:3000/checkDistance"
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlRastrellieraVicino,
                response -> {
                    JSONArray arrRastr = null;
                    // Aggiungi codice da fare quando arriva la risposta dalla richiesta
                    try {
                        arrRastr = new JSONArray(response);

                        //Se è stata trovata una rastrelliera sarà nella prima posizione
                        if (arrRastr.length() > 0) {
                            //Terminiamo la geolocalizzazione
                            Intent intent = new Intent(Home.this, GeolocalizationService.class);
                            stopService(intent);

                            JSONObject rastrelliera = arrRastr.getJSONObject(0);
                            idRastrellieraVicino = rastrelliera.getInt("id");

                            //Terminiamo il noleggio
                            richiestaPostTerminaNoleggio();

                            //Ricarichiamo la pagina
                            intent = new Intent(Home.this, Home.class);
                            startActivity(intent);
                        } else {
                            //Nessuna rastrelliera trovata vicino all'utente
                            Toast.makeText(Home.this, "Non sei abbastanza vicino ad una rastrelliera!", Toast.LENGTH_SHORT).show();
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

    /* Facciamo la richiesta di terminare il noleggio, passando il codice noleggio, la geometria delle
     *  posizioni che l'utente ha accumulato per salvarle nello storico, l'id della bici e l'id della
     * rastrelliera nella quale stiamo mettendo la bici. */
    private void richiestaPostTerminaNoleggio() {
        //Istanzia la coda di richieste
        RequestQueue queue = Volley.newRequestQueue(Home.this);

        // Stringa per fare la richiesta. Nel caso del termine del noleggio facciamo una richiesta POST all'url "http://192.168.1.122:3000/termina_noleggio"
        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlTerminaNoleggio,
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

                /* Modifichiamo l'array delle coppie latitudine e longitudine in modo tale da avere
                 * una stringa formattata in un formato compatibile con ST_GeomFromGeoJSON*/
                String JSONgeom = String.valueOf(GeolocalizationService.pairLatLngArr).replaceAll("Pair", "");
                JSONgeom = JSONgeom.replace('{', '[');
                JSONgeom = JSONgeom.replaceAll(", ", ",");
                JSONgeom = JSONgeom.replace('}', ']');
                JSONgeom = JSONgeom.replace(' ', ',');

                //Passiamo i parametri
                params.put("codNoleggio", codP);
                params.put("geom", JSONgeom);
                params.put("bici", idBici);
                params.put("rastrelliera", String.valueOf(idRastrellieraVicino));
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

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        CharSequence name = "BikeFleetMonitoring";
        String description = "BikeFleetMonitoring";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("BikeFleetMonitoring", name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}