package com.example.bikefleetmonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Home extends AppCompatActivity {
    Button bookButton;
    Button viewBooksButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        findXmlElements();

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
                Intent intent = new Intent(Home.this, ViewBooks.class);
                startActivity(intent);
            }
        });
    }

    private void findXmlElements() {
        bookButton = findViewById(R.id.book);
        viewBooksButton = findViewById(R.id.viewBooks);
    }

}
