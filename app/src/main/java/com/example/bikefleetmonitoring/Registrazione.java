package com.example.bikefleetmonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Registrazione extends AppCompatActivity {

    Button registerButton;
    TextView goToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrazione);

        findXmlElements();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Registrazione.this, MainActivity.class);
                startActivity(intent);
            }
        });

        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Registrazione.this, Login.class);
                startActivity(intent);
            }
        });
    }

    private void findXmlElements() {
        registerButton = findViewById(R.id.registerButton);
        goToLogin = findViewById(R.id.goToLogin);
    }
}