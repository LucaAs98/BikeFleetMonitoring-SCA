package com.example.bikefleetmonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

public class Home extends AppCompatActivity {
    AppCompatButton btnPrenotaBici;
    AppCompatButton btnVediPrenotazione;
    TextView tvMessaggioIniziale;
    TextView tvVediCodPrenot;
    Toolbar toolbar;
    boolean prenotato = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        trovaElementiXML();
        inizializzaToolbar();

        if (prenotato) {
            inizializzaPrenotato();
        } else {
            inizializzaNonPrenotato();
        }
    }

    private void trovaElementiXML() {
        btnPrenotaBici = findViewById(R.id.btnPrenotaBici);
        btnVediPrenotazione = findViewById(R.id.btnVediPrenotazione);
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
        tvMessaggioIniziale.setText("Ciao $UTENTE !\nStai già noleggiando una bici, vuoi annullare la tua prenotazione?");
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
        btnVediPrenotazione.setVisibility(View.GONE);
        tvMessaggioIniziale.setText("Ciao $UTENTE !\nInizia subito a noleggiare una bici vicino a te!");
    }
}
