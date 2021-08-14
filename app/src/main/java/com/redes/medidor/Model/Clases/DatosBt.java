package com.redes.medidor.Model.Clases;

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
    public interface Constants{
        public static final int NEO6M_1 = 1;
        public static final int HCSR04=2;
        public static final int PITCH = 3;
    }




    private int tipo;
    private double dato;
    private long tiempo;


    /**
     * @param tipo:Definiendo que tipo de dato es, haciendo uso de la interfaz "Constants"
     * @param dato:Dato medido desde el sensor en el arduino
     */
    public DatosBt(int tipo, double dato) {
        this.tipo = tipo;
        this.dato = dato;

        //Obteniendo el tiempo actual en milisegundos
        tiempo=Calendar.getInstance().getTimeInMillis();
    }

    public int getTipo() {
        return tipo;
    }

    public double getDato() {
        return dato;
    }

    public long getTiempo() {
        return tiempo;
    }
}
