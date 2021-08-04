package com.example.bikefleetmonitoring;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

public class Login extends AppCompatActivity {
    Button btnLogin;
    TextView tvVaiARegistrazione;
    EditText etEmail, etPassword;
    FirebaseAuth fAuth;
    boolean controlloLogin = false;         //Quando è "false" vogliamo debuggare all'interno senza inserire email e password

    String url = "http://192.168.1.8:3000/users";
    AsyncTask<Void, Void, Void> mTask;
    HashMap<String, String> hashMapUsers;
    Intent intent;

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

                login(strEmail, strPassword);
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


    public static HashMap<String, String> findUsers(String url) throws IOException, JSONException {

        BufferedReader inputStream = null;

        URL jsonUrl = new URL(url);
        URLConnection dc = jsonUrl.openConnection();

        dc.setConnectTimeout(5000);
        dc.setReadTimeout(5000);

        inputStream = new BufferedReader(new InputStreamReader(
                dc.getInputStream()));

        // read the JSON results into a string
        String jsonS = inputStream.readLine();
        JSONArray arr = new JSONArray(jsonS);

        HashMap<String, String> users = new HashMap<String, String>();

        for (int i = 0; i < arr.length(); i++) {
            String username = arr.getJSONObject(i).getString("username");
            String password = arr.getJSONObject(i).getString("password");
            users.put(username, password);
        }

        return users;
    }


    public void login(String username, String password) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    hashMapUsers = findUsers(url);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                String check = hashMapUsers.get(username);
                if (check != null && check.equals(password)  ) {
                    Toast.makeText(Login.this, "Login effettuato correttamente", Toast.LENGTH_SHORT).show();
                    Home.session = username;
                    vaiAHome();

                } else {
                    etEmail.setError("Username/password errate");
                    etPassword.setError("Username/password errate");

                }


            }
        }.execute();

    }
}