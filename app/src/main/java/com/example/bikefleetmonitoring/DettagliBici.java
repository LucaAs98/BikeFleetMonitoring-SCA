package com.example.bikefleetmonitoring;

public class DettagliBici {
    private String nomeBici;

    public DettagliBici(String nomeBici) {
        this.nomeBici = nomeBici;
    }

    public String getName() {
        return nomeBici;
    }

    public void setName(String nomeBici) {
        this.nomeBici = nomeBici;
    }
}
