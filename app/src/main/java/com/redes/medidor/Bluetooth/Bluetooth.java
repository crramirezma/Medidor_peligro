//Clase inspirada en el trabajo hecho por  jose-jhr
//repositorio: https://github.com/jose-jhr/-bluetoothjhr.git

package com.redes.medidor.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.redes.medidor.baseDatos.BluetoothSqlLite;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

public class Bluetooth {
    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;

    //Variables para la lectura de mensajes
    private InoutThread thread;
    private Handler bluetoothIn;
    private static final int HANDLER_STATE=0;
    private StringBuilder recibirDatos;


    //Variables que recibiran los mensajes por parte del dispositivo
    private InputStream input;
    private OutputStream output;


    private boolean threadConnected;        //sirve para validar si el thread esta conectado
    private String address=null;//Direccion mac del dispositivo a conectar
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //datos mutables
    private MutableLiveData<ArrayList<BluetoothDevice>> dispositivos;
    private MutableLiveData<Boolean> esAdaptable;
    private MutableLiveData<Boolean> esActivado;
    private MutableLiveData<Boolean> conectadoStreams;          //Pensado para evaluar cuando uno de los Stream(input o output) no conecte
    private MutableLiveData<Boolean> errorRecibiendo;           //pensado para evaluar cuando hallan errores en la entrada de datos
    private MutableLiveData<Boolean> errorEnviando;



    public Bluetooth(){
        //inicializando las variables
        inicializar();

        btAdapter=BluetoothAdapter.getDefaultAdapter();
        //Comprobando que no sea nulo
        //Si es nulo, significa que el dispositivo no aguanta bluetooth
        if(btAdapter==null){
            esAdaptable.postValue(Boolean.FALSE);
        }else{
            //luego revisar si esta activado
            if(!btAdapter.isEnabled()){
                //si no lo esta, pedir que se lance
                //esto se hara desde la ui
                esActivado.postValue(Boolean.FALSE);
            }else{
                //Lista de dispositivos

                ArrayList<BluetoothDevice> lista=listarDispositivos();

                dispositivos.postValue(lista);
                Log.i("Mensaje",dispositivos.getValue().size()+"");
            }
        }
    }

    public MutableLiveData<ArrayList<BluetoothDevice>> getDispositivos() {
        return dispositivos;
    }

    public MutableLiveData<Boolean> getEsAdaptable() {
        return esAdaptable;
    }

    public MutableLiveData<Boolean> getEsActivado() {
        return esActivado;
    }

    public void setAddress(String address){
        this.address=address;
    }

    private void inicializar() {
        dispositivos=new MutableLiveData<>();
        ArrayList<BluetoothDevice> d=new ArrayList<>();
        dispositivos.setValue(d);

        esActivado=new MutableLiveData<>();
        esAdaptable=new MutableLiveData<>();

        conectadoStreams=new MutableLiveData<>();
        errorRecibiendo=new MutableLiveData<>();
        errorEnviando=new MutableLiveData<>();


        bluetoothIn=new InHandler();
        //Iniciando la base de datos
        //btSQL=new BluetoothSqlLite(context);
    }

    public boolean conectarBt(){
        boolean conecto=false;


        return false;
    }

    //Convierte el conjunto de dispositivos en un arraylist para presentar en la ui
    private ArrayList<BluetoothDevice> listarDispositivos() {
        return new ArrayList<>(btAdapter.getBondedDevices());
    }


    //Creando el socket bluetooth con el uuid creado anteriormente
    private BluetoothSocket crearSocket(BluetoothDevice device) throws IOException{
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }


    public boolean comenzarListenerDatos(){
        //final Handler handler=new Handler();
        final String delimiter="]";

        //Obtener la direccion mac alojada en la SharedPreferences


        //Creando el socket usando la funcion "crearSocket"
        if(!address.equals("0")){       //primero se valida que la variable address tenga un valor de mac confiable

            try {
                //Obteniendo el dispositivo usando el address
                BluetoothDevice d = btAdapter.getRemoteDevice(address);
                //creando el socket a partir de el
                btSocket = crearSocket(d);
                //conectandolo
                //Toca comprobar que no se generen errores de coneccion con  el socket
                try{
                    btSocket.connect();
                }catch(IOException e){
                    //si se generan, toca desconectarlo
                    while (btSocket.isConnected()){
                        try{
                            btSocket.close();
                        }catch (IOException e2){
                            //
                        }
                    }
                    //dado que no se conecto el socket, tampoco se podra conectar el thread
                    return false;
                }
                int i=0;
                //Creando el thread para la ejecucion de tareas asincronas de comunicacion con el programa
                thread = new InoutThread(btSocket);
            }catch (Exception e){
                conectadoStreams.postValue(false);
                //dado esto, no se creo el thread correctamente
                return false;
            }
            //Probando que el modulo reciba el dato
            thread.writeData("Hola mundo");
            return true;
        }
        //Si la ejecucion llega hasta este punto, es que no se ejecuto el bloque if anterior
        //por lo que no se creo el Thread
        return false;

    }



    //handler para conectar con el hilo principa;
    public class InHandler extends Handler{


        @Override
        public void handleMessage(@NonNull Message msg) {
            //que hacer con el mensaje
            Log.i("Mensaje desde el thread", (String)msg.obj);

        }
    }



    //Thread de recepcion y envio de datos
    private class InoutThread extends Thread{
        private InputStream entradaDatos;
        private OutputStream salidaDatos;

        public InoutThread(BluetoothSocket socket) {

            //Iniciando los valores de las variables de entrada y salida de datos
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try{
                tmpIn= socket.getInputStream();
                tmpOut= socket.getOutputStream();
            }catch (IOException e){
                conectadoStreams.postValue(false);
            }

            entradaDatos=tmpIn;
            salidaDatos=tmpOut;
        }

        @Override
        public void run() {
            byte[] buffer= new byte[256];
            int cantBytes;

            while(true){
                try{
                    //Se le carga a la variable "buffer" lo que le llegue del bluetooth
                    //y se le crga a "cantBytes" el tamaño en bytes del mensaje
                    cantBytes=entradaDatos.read(buffer);
                    //luego se traduce a String, texto
                    String mensaje=new String(buffer,0,cantBytes);

                    //zona para el handler
                    Message message=bluetoothIn.obtainMessage();
                    message.obj=mensaje;
                    bluetoothIn.sendMessage(message);
                    //completar
                }catch (IOException e){
                    errorRecibiendo.postValue(false);
                    break;
                }
            }

        }

        //Metodo para enviar datos al Bluetooth
        public void writeData(String msg){
            //Primero toca convertir el mensaje tipo String en bytes
            byte[] buffer=msg.getBytes();

            //Validando errores de envio de datos
            try{
                salidaDatos.write(buffer);
            } catch (IOException e) {
                //Cargando al mutablelivedata el valor falso, esto se hace para poder ser evaluado luego por la interfaz
                errorEnviando.postValue(false);
            }
        }
    }


}
