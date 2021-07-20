package com.example.bikefleetmonitoring;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    Button btnLogin;
    TextView tvVaiARegistrazione;
    EditText etEmail, etPassword;
    FirebaseAuth fAuth;
    boolean controlloLogin = false;         //Quando è "false" vogliamo debuggare all'interno senza inserire email e password

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        findXmlElements();

        fAuth = FirebaseAuth.getInstance();

        if (fAuth.getCurrentUser() != null) {
            vaiAHome();
            finish();
        }

        if (controlloLogin) {
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String strEmail = etEmail.getText().toString();
                    String strPassword = etPassword.getText().toString();

                    if (TextUtils.isEmpty(strEmail)) {
                        etEmail.setError("Il campo 'Email' è richiesto.");
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

                    fAuth.signInWithEmailAndPassword(strEmail, strPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Login.this, "Login effettuato correttamente", Toast.LENGTH_SHORT).show();
                                vaiAHome();
                            } else {
                                Toast.makeText(Login.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        } else {
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vaiAHome();
                }
            });
        }


        tvVaiARegistrazione.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Registrazione.class);
                startActivity(intent);
            }
        });
    }

    private void findXmlElements() {
        btnLogin = findViewById(R.id.btnLogin);
        tvVaiARegistrazione = findViewById(R.id.tvVaiARegistrazione);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
    }

    private void vaiAHome() {
        Intent intent = new Intent(Login.this, Home.class);
        startActivity(intent);
    }
}