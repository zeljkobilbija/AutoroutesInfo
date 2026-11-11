package com.zeljkobilbija001.autoroutesinfo;

/** Jedan autoput na početnom ekranu (A1, A4…). */
public final class Autoput {
    public final String id;       // "A4"
    public final String ruta;     // "Torino – Trieste"
    public final String datoteka; // "autoputevi/a4.json" ili null ako podaci još ne postoje

    public Autoput(String id, String ruta, String datoteka) {
        this.id = id;
        this.ruta = ruta;
        this.datoteka = datoteka;
    }

    /** Oznaka kao na francuskoj tabli: "A6". */
    public String oznaka() {
        return id;
    }

    public boolean dostupan() {
        return datoteka != null;
    }
}
