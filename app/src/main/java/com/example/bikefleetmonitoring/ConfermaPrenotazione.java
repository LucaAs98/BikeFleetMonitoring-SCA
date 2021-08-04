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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

public class ConfermaPrenotazione extends AppCompatActivity {
    Toolbar toolbar;
    final Calendar myCalendar = Calendar.getInstance();
    EditText btnGiornoDa;
    EditText btnOraDa;
    EditText btnGiornoA;
    EditText btnOraA;
    EditText editTextCliccato;
    AppCompatButton btnConfermaPrenot;
    String url1 = "http://192.168.1.8:3000/prenota";
    int idBici;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conferma_prenotazione);
        Intent intent = getIntent();
        idBici = intent.getIntExtra("idBici", 0);

        trovaElementiXML();
        inizializzaToolbar();


        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
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
    }

    private void trovaElementiXML() {
        btnGiornoDa = findViewById(R.id.btnGiornoDa);
        btnOraDa = findViewById(R.id.btnOraDa);
        btnGiornoA = findViewById(R.id.btnGiornoA);
        btnOraA = findViewById(R.id.btnOraA);
        toolbar = findViewById(R.id.toolbar);
        btnConfermaPrenot = findViewById(R.id.btnConfermaPrenot);
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

                insert();


            }
        });
    }

    private void inizializzaToolbar() {
        toolbar.setTitle("Dati Prenotazione");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConfermaPrenotazione.this, BiciDisponibili.class);
                startActivity(intent);
            }
        });
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        editTextCliccato.setText(sdf.format(myCalendar.getTime()));
    }

    private void insert(){
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    executeQuery();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                Intent intent = new Intent(ConfermaPrenotazione.this, Home.class);
                startActivity(intent);
            }
        }.execute();
    }

    private void executeQuery() throws IOException {
        URL url = new URL(url1);
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("POST"); // PUT is another valid option
        http.setDoOutput(true);

        //Generazione del codice generico da fare!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        Map<String, String> arguments = new HashMap<>();
        String[] nomiVariabili = new String[]{"cod", "utente", "di", "df"};
        String[] valori = new String[]{"cod"+idBici, Home.session,
                btnGiornoDa.getText().toString() + " " + btnOraDa.getText().toString(),
                btnGiornoA.getText().toString() + " " + btnOraA.getText().toString()};

        StringJoiner sj = new StringJoiner("&");
        for (int i = 0; i < 4; i++){
            sj.add(URLEncoder.encode(nomiVariabili[i], "UTF-8") + "="
                    + URLEncoder.encode(valori[i], "UTF-8"));
        }

        sj.add(URLEncoder.encode("bici", "UTF-8") + "="
                + URLEncoder.encode(String.valueOf(idBici), "UTF-8"));


        byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
        int length = out.length;

        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        http.connect();

        //da vedere se queste cose sono da tenere !!!!
        try(OutputStream os = http.getOutputStream()) {
            os.write(out);
        }
        try {
            BufferedReader inputStream = null;
            inputStream = new BufferedReader(new InputStreamReader(http.getInputStream()));

        } finally {
            http.disconnect();
        }
    }
}
