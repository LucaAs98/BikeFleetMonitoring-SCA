package com.example.bikefleetmonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

public class Home extends AppCompatActivity {
    AppCompatButton btnPrenotaBici, btnVediPrenotazione, btnLogout;
    TextView tvMessaggioIniziale, tvVediCodPrenot;
    Toolbar toolbar;
    FirebaseAuth fAuth;
    boolean prenotato = false;

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
                Intent intent = new Intent(Home.this, MappaRastrelliere.class);
                startActivity(intent);
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
}
