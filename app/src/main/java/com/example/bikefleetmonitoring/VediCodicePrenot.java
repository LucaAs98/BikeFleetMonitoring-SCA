package com.example.bikefleetmonitoring;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

public class VediCodicePrenot extends AppCompatActivity {
    Toolbar toolbar;
    AppCompatButton btnCopiaCodice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vedi_codice_prenot);

        trovaElementiXML();
        inizializzaToolbar();

        btnCopiaCodice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                CharSequence testoToast = "Codice copiato negli appunti!";
                int durataToast = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, testoToast, durataToast);
                toast.show();
            }
        });

    }

    private void trovaElementiXML() {
        toolbar = findViewById(R.id.toolbar);
        btnCopiaCodice = findViewById(R.id.btnCopiaCodice);
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
