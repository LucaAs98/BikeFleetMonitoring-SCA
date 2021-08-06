package com.example.bikefleetmonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Registrazione extends AppCompatActivity {
    Button btnRegistrazione;
    TextView tvVaiALogin;
    EditText etUsername, etPassword;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrazione);

        findXmlElements();

        fAuth = FirebaseAuth.getInstance();

        btnRegistrazione.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strUsername = etUsername.getText().toString();
                String strPassword = etPassword.getText().toString();

                if (TextUtils.isEmpty(strUsername)) {
                    etUsername.setError("Il campo 'Username' è richiesto.");
                    return;
                }

                if (TextUtils.isEmpty(strPassword)) {
                    etPassword.setError("Il campo 'Password' è richiesto.");
                    return;
                }

                if (strPassword.length() < 6) {
                    etPassword.setError("La password deve essere di almeno sei caratteri.");
                    return;
                }

                /*fAuth.createUserWithUsernameAndPassword(strUsername, strPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Registrazione.this, "Utente creato", Toast.LENGTH_SHORT).show();
                            vaiAHome();
                        } else {
                            Toast.makeText(Registrazione.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });*/
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
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
    }

    private void vaiAHome() {
        Intent intent = new Intent(Registrazione.this, Home.class);
        startActivity(intent);
    }
}