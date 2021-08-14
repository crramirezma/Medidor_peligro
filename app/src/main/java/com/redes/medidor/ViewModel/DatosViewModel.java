package com.redes.medidor.ViewModel;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.redes.medidor.IO.Bluetooth;
import com.redes.medidor.Model.Clases.DatosBt;
import com.redes.medidor.IO.DatosDao;

import java.util.ArrayList;

public class DatosViewModel extends ViewModel {
    //ZONA DEL BLUETOOTH
    private Bluetooth miBt;

    private MutableLiveData<ArrayList<BluetoothDevice>> dispositivos;
    private MutableLiveData<Boolean> esAdaptable;
    private MutableLiveData<Boolean> esActivado;

    private LiveData<Boolean> esConectado;

    //private boolean bdCreada;         //Con esta variable se pretende evitar que se cree nuevamente el objeto de tipo Bluetooth
    //BluetoothSqlLite bdBt;            //Variable de la base de datos


    /**
     *ZONA DE DECLARACION PARA LA BASE DE DATOS
     */

    //Variable encargada de administrar la subida de datos a firebase
    private DatosDao DAO;

    //LiveData con el que se observaran los datos nuevos que lleguen desde el arduino
    private LiveData<DatosBt> nuevoDato;

    /**
     * ZONA DE MANEJO DE PYTHON
     */

    public DatosViewModel(){
        //El context lo usa el bluetooth para el manejo de la base de datos interna
        miBt=new Bluetooth();

        //Instanciando os valores con variables existentes en la variable de tipo "Bluetooth"
        dispositivos=miBt.getDispositivos();
        esAdaptable= miBt.getEsAdaptable();
        esActivado= miBt.getEsActivado();

        esConectado=miBt.getEsConectado();

        DAO=new DatosDao();
        nuevoDato= miBt.getNuevoDato();
    }



    /**
     * ZONA DE MANEJO DEL BLUETOOTH
     */
    public boolean bluetoothDisponible(){
        return miBt.isEnabled();
    }

    public void modificarMac(String MAC){
        //Enviandole al objeto bluetooth el valor del MAC que se encuentra en las shared preferences
        miBt.setAddress(MAC);
    }



    //Con recargarDatos se llama a la funcion correspondiente en el objeto Bluetooth...
    //...para que recargue los datos de sus dispositivos
    public void recargarDatos(){
        miBt.recargarListaDispositivos();
    }

    public void comenzarConeccion(){
        miBt.comenzarConeccion();
    }

    public boolean detenerTransferencia(){
        return miBt.pararConeccion();
    }

    public void enviarDatoBt(byte b){
        miBt.enviarDato(b);
    }


    /**
     * ZONA DAO(persistencia de datos)
     */
    public void subirDatos(String MAC,DatosBt datos){
        DAO.subirDatoVehiculo(datos, MAC);
    }

    /**
     * ZONA DE GETTER Y SETTER
     */
    public MutableLiveData<Boolean> getEsAdaptable() {
        return esAdaptable;
    }

    public MutableLiveData<Boolean> getEsActivado() {
        return esActivado;
    }

    public MutableLiveData<ArrayList<BluetoothDevice>> getDispositivos() {
        Log.i("Mensaje",dispositivos.getValue().size()+"     ");
        return dispositivos;
    }

    public LiveData<DatosBt> getNuevoDato() {
        return nuevoDato;
    }

    public LiveData<Boolean> getEsConectado() {
        return esConectado;
    }
}