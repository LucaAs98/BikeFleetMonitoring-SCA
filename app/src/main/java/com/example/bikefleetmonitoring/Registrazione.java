package com.example.bikefleetmonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Registrazione extends AppCompatActivity {
    Button btnRegistrazione;
    TextView tvVaiALogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrazione);

        findXmlElements();

        btnRegistrazione.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Registrazione.this, MappaRastrelliere.class);
                startActivity(intent);
            }
        });

        tvVaiALogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Registrazione.this, Login.class);
                startActivity(intent);
            }
        });
    }

    private void findXmlElements() {
        btnRegistrazione = findViewById(R.id.btnRegistrazione);
        tvVaiALogin = findViewById(R.id.tvVaiALogin);
    }
}