package com.redes.medidor.Bluetooth;

import java.util.Calendar;

public class DatosBt {
    /**
     * Constantes de los tipos de datos que llegan
     *
     * NEO6M_1=Medidor gps, en este caso para la velocidad
     * HCSR04= medidor ultrasonido de distancia
     * PITCH=dato del giroscopio correspondiente al pitch
     * FUTUROS DATOS AQUI
     *
     *
     */
    public static final int NEO6M_1 = 1;
    public static final int HCSR04=2;
    public static final int PITCH = 3;




    private double tipo;
    private double dato;
    private long tiempo;

    public DatosBt(double tipo, double dato) {
        this.tipo = tipo;
        this.dato = dato;

        //Obteniendo el tiempo actual en milisegundos
        tiempo=Calendar.getInstance().getTimeInMillis();

    }
}
