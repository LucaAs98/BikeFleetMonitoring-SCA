package com.example.bikefleetmonitoring;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import java.util.concurrent.ThreadLocalRandom;

public class ConfermaPrenotazione extends AppCompatActivity {
    Toolbar toolbar;
    final Calendar myCalendar = Calendar.getInstance();
    EditText btnGiornoDa;
    EditText btnOraDa;
    EditText btnGiornoA;
    EditText btnOraA;
    EditText editTextCliccato;
    AppCompatButton btnConfermaPrenot;
    String url1 = "http://" + Login.ip + ":3000/prenota";
    int idBici;
    int idRastrelliera;
    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    int lunghCodiceGenerato = 10;
    static SecureRandom rnd = new SecureRandom();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conferma_prenotazione);

        Intent intent = getIntent();
        idRastrelliera = intent.getIntExtra("id", 0);
        idBici = intent.getIntExtra("idBici", 0);

        trovaElementiXML();
        inizializzaToolbar();


        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        btnGiornoDa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextCliccato = btnGiornoDa;
                new DatePickerDialog(ConfermaPrenotazione.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        btnOraDa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(ConfermaPrenotazione.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        btnOraDa.setText(selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.show();
            }
        });

        btnGiornoA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextCliccato = btnGiornoA;
                new DatePickerDialog(ConfermaPrenotazione.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        btnOraA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(ConfermaPrenotazione.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        btnOraA.setText(selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.show();
            }
        });

        btnConfermaPrenot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(btnGiornoDa.getText().toString())) {
                    btnGiornoDa.setError(" campo 'Giorno inizio' è richiesto.");
                    return;
                }

                if (TextUtils.isEmpty(btnGiornoA.getText().toString())) {
                    btnGiornoA.setError("Il campo 'Giorno fine' è richiesto.");
                    return;
                }

                if (TextUtils.isEmpty(btnOraDa.getText().toString())) {
                    btnOraDa.setError("Il campo 'Ora inizio' è richiesto.");
                    return;
                }

                if (TextUtils.isEmpty(btnOraA.getText().toString())) {
                    btnOraA.setError("Il campo 'Ora fine' è richiesto.");
                    return;
                }

                richiestaPostPrenotazione();
                Intent intent = new Intent(ConfermaPrenotazione.this, GeolocalizationService.class);
                intent.putExtra("id", idBici);
                startService(intent);


                intent = new Intent(ConfermaPrenotazione.this, Home.class);

                startActivity(intent);
            }
        });
    }

    private void trovaElementiXML() {
        btnGiornoDa = findViewById(R.id.btnGiornoDa);
        btnOraDa = findViewById(R.id.btnOraDa);
        btnGiornoA = findViewById(R.id.btnGiornoA);
        btnOraA = findViewById(R.id.btnOraA);
        toolbar = findViewById(R.id.toolbar);
        btnConfermaPrenot = findViewById(R.id.btnConfermaPrenot);
    }

    private void inizializzaToolbar() {
        toolbar.setTitle("Dati Prenotazione");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConfermaPrenotazione.this, BiciDisponibili.class);
                intent.putExtra("id", idRastrelliera);
                startActivity(intent);
            }
        });
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        editTextCliccato.setText(sdf.format(myCalendar.getTime()));
    }

    public String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    /* Richiesta POST............. */
    private void richiestaPostPrenotazione() {
        //Istanzia la coda di richieste
        RequestQueue queue = Volley.newRequestQueue(ConfermaPrenotazione.this);

        // Stringa per fare la richiesta. Nel caso della posizione facciamo una richiesta POST all'url "http://192.168.1.122:3000/prova_posizione"
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url1,
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
                String cod = randomString(lunghCodiceGenerato);
                params.put("cod", cod);
                params.put("utente", Home.session);
                params.put("di", btnGiornoDa.getText().toString() + " " + btnOraDa.getText().toString());
                params.put("df", btnGiornoA.getText().toString() + " " + btnOraA.getText().toString());
                params.put("bici", String.valueOf(idBici));

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
}
