package com.redes.medidor.ui.vehiculo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.redes.medidor.Bluetooth.Bluetooth;
import com.redes.medidor.R;
import com.redes.medidor.baseDatos.BluetoothSqlLite;

import java.util.ArrayList;

public class VehiculosViewModel extends ViewModel {
    private Bluetooth miBt;

    private MutableLiveData<ArrayList<BluetoothDevice>> dispositivos;
    private MutableLiveData<Boolean> esAdaptable;
    private MutableLiveData<Boolean> esActivado;

    private boolean bdCreada;         //Con esta variable se pretende evitar que se cree nuevamente el objeto de tipo Bluetooth
    BluetoothSqlLite bdBt;            //Variable de la base de datos

    public VehiculosViewModel(){
        //El context lo usa el bluetooth para el manejo de la base de datos interna
        miBt=new Bluetooth();

        //Instanciando os valores con variables existentes en la variable de tipo "Bluetooth"
        dispositivos=miBt.getDispositivos();
        esAdaptable= miBt.getEsAdaptable();
        esActivado= miBt.getEsActivado();


        //


        //
        bdCreada=false;

    }

    public boolean bluetoothDisponible(){
        return miBt.isEnabled();
    }

    public boolean activarBluetooth(Context context){

        //Solo se ejecuta una vez, para evitar perdida de datos y perdida de rendimiento
        if(!bdCreada){


            //creando la base de datos


            bdCreada=true;
        }

        //Enviandole al objeto bluetooth el valor del MAC que se encuentra en las shared preferences
        miBt.setAddress(context.getSharedPreferences(String.valueOf(R.string.modulo_bluetooth),Context.MODE_PRIVATE).getString(String.valueOf(R.string.mac_bluetooth),"0"));
        //Comenzar la transferencia de datos
        return true;
    }

    /**
     * Con recargarDatos se llama a la funcion correspondiente en el objeto Bluetooth para que recargue los datos de sus dispositivos
     */
    public void recargarDatos(){
        miBt.recargarListaDispositivos();

    }

    public boolean comenzarTransferencia(){
        return miBt.comenzarListenerDatos();
    }

    public boolean detenerTransferencia(){
        return miBt.pararListenerDatos();
    }

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
}