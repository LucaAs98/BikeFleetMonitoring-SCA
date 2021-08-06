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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class Registrazione extends AppCompatActivity {
    Button btnRegistrazione;
    TextView tvVaiALogin;
    EditText etUsername, etPassword;
    String url = "http://" + Login.ip + ":3000/users";
    String url1 = "http://" + Login.ip + "/registrazione";
    HashMap<String, String> hashMapUsers = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registrazione);

        findXmlElements();

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

                richiestaGetUtenti(strUsername, strPassword);
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

    private void richiestaPostRegistrazione(String strUsername, String strPassword) {
        //Istanzia la coda di richieste
        RequestQueue queue = Volley.newRequestQueue(Registrazione.this);

        // Stringa per fare la richiesta. Nel caso della posizione facciamo una richiesta POST all'url "http://192.168.1.122:3000/prova_posizione"
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url1,
                response -> {
                    // Aggiungi codice da fare quando arriva la risposta dalla richiesta

                },
                error -> {
                    // Aggiungi codice da fare se la richiesta non è andata a buon fine
                    //------------------------------------------------------------------
                }) {
            //Utile ad inserire i parametri alla richiesta. Messi nel body
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("username", strUsername);
                params.put("password", strPassword);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        // Aggiungiamo la richiesta alla coda.
        queue.add(stringRequest);
    }

    private boolean checkRegistrazione(String username) {
        String check = hashMapUsers.get(username);
        if (check == null) {
            return true;
        } else {
            etUsername.setError("Username già esistente");
            return false;
        }
    }

    private void richiestaGetUtenti(String strUsername, String strPassword) {
        //Istanzia la coda di richieste
        RequestQueue queue = Volley.newRequestQueue(Registrazione.this);

        // Stringa per fare la richiesta. Nel caso della posizione facciamo una richiesta POST all'url "http://192.168.1.122:3000/prova_posizione"
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    // Aggiungi codice da fare quando arriva la risposta dalla richiesta
                    try {
                        JSONArray arr = new JSONArray(response);
                        for (int i = 0; i < arr.length(); i++) {
                            String username = arr.getJSONObject(i).getString("username");
                            String password = arr.getJSONObject(i).getString("password");
                            hashMapUsers.put(username, password);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (checkRegistrazione(strUsername)) {
                        richiestaPostRegistrazione(strUsername, strPassword);
                        Home.session = strUsername;
                        vaiAHome();
                    } else {
                        Toast.makeText(Registrazione.this, "Utente già esistente", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Aggiungi codice da fare se la richiesta non è andata a buon fine
                    //------------------------------------------------------------------
                });
        // Aggiungiamo la richiesta alla coda.
        queue.add(stringRequest);
    }


    private void vaiAHome() {
        Intent intent = new Intent(Registrazione.this, Home.class);
        startActivity(intent);
    }
}