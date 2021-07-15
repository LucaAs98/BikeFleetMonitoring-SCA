package com.example.bikefleetmonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class Home extends AppCompatActivity {
    Button bookButton;
    Button viewBooksButton;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        findXmlElements();
        initializeToolbar();

        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, MappaRastrelliere.class);
                startActivity(intent);
            }
        });

        viewBooksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home.this, ViewBikes.class);
                startActivity(intent);
            }
        });
    }

    private void findXmlElements() {
        bookButton = findViewById(R.id.book);
        viewBooksButton = findViewById(R.id.viewBooks);
        toolbar = findViewById(R.id.toolbar);
    }

    private void initializeToolbar() {
        toolbar.setTitle("Home");
    }
}
