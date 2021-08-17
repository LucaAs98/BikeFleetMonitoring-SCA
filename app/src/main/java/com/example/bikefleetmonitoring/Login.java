package com.example.bikefleetmonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;

public class Login extends AppCompatActivity {
    public static String ip;
    String ipLuca = "192.168.1.122";
    String ipMauro = "192.168.1.110";
    String ipAndrea = "192.168.1.9";

    Button btnLogin;
    TextView tvVaiARegistrazione;
    EditText etUsername, etPassword;

    String url;
    HashMap<String, String> hashMapUsers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        findXmlElements();
        ip = ipLuca;
        url = "http://" + ip + ":3000/users";

        btnLogin.setOnClickListener(new View.OnClickListener() {
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

                richiestaGetUtenti(strUsername, strPassword);
            }
        });


        tvVaiARegistrazione.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Registrazione.class);
                startActivity(intent);
            }
        });
    }

    private void richiestaGetUtenti(String strUsername, String strPassword) {
        //Istanzia la coda di richieste
        RequestQueue queue = Volley.newRequestQueue(Login.this);

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
                    checkLogin(strUsername, strPassword);
                },
                error -> {
                    // Aggiungi codice da fare se la richiesta non è andata a buon fine
                    //------------------------------------------------------------------
                });
        // Aggiungiamo la richiesta alla coda.
        queue.add(stringRequest);
    }

    private void checkLogin(String username, String password) {
        String check = hashMapUsers.get(username);
        if (check != null && check.equals(password)) {
            Toast.makeText(Login.this, "Login effettuato correttamente", Toast.LENGTH_SHORT).show();
            Home.session = username;
            vaiAHome();
        } else {
            etUsername.setError("Username/password errate");
            etPassword.setError("Username/password errate");
        }
    }

    private void findXmlElements() {
        btnLogin = findViewById(R.id.btnLogin);
        tvVaiARegistrazione = findViewById(R.id.tvVaiARegistrazione);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
    }

    private void vaiAHome() {
        Intent intent = new Intent(Login.this, Home.class);
        startActivity(intent);
    }
}