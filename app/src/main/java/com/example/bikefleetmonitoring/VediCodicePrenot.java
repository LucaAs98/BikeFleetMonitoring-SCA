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

public class VediCodicePrenot extends AppCompatActivity {
    Toolbar toolbar;
    AppCompatButton btnCopiaCodice;
    TextView tvCodice;
    String codP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vedi_codice_prenot);
        Intent intent = getIntent();
        codP = intent.getStringExtra("codice");

        trovaElementiXML();
        inizializzaToolbar();

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
        tvCodice.setText(codP);
    }

    private void trovaElementiXML() {
        toolbar = findViewById(R.id.toolbar);
        btnCopiaCodice = findViewById(R.id.btnCopiaCodice);
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
