package com.example.bikefleetmonitoring;

public class DettagliBici {
    private String nomeBici;
    private  int idBici;

    public DettagliBici(String nomeBici, int idBici) {
        this.nomeBici = nomeBici;
        this.idBici = idBici;
    }

    public String getName() {
        return nomeBici;
    }


    public void setName(String nomeBici) {
        this.nomeBici = nomeBici;
    }

    public int getId() {
        return idBici;
    }
}
