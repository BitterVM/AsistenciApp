package com.example.AsistenciApp;

public class Evento {
    private String idEvento;
    private String nombreEvento;

    public Evento(String idEvento, String nombreEvento) {
        this.idEvento = idEvento;
        this.nombreEvento = nombreEvento;
    }

    public String getIdEvento() {
        return idEvento;
    }

    public String getNombreEvento() {
        return nombreEvento;
    }

    // Sobrescribe toString para que devuelva el nombre del evento
    @Override
    public String toString() {
        return nombreEvento;
    }
}
