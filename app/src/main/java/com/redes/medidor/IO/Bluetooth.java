package com.redes.medidor.IO;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.redes.medidor.Model.Clases.DatosBt;
import com.redes.medidor.Model.ConstantesMensajes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class Bluetooth implements ConstantesMensajes {

    //Zona de tags
    private static final String BT_TAG  = "TAG BLUETOOTH";



    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;

    //Variables para la lectura de mensajes
    //private InoutThread thread;
    private BtHandler bluetoothIn;
    ConnectedThread thread;
    boolean isThreadConnected;


    //Variables que recibiran los mensajes por parte del dispositivo
    private InputStream input;
    private OutputStream output;


    private boolean threadConnected;        //sirve para validar si el thread esta conectado
    private String address=null;//Direccion mac del dispositivo a conectar


    //datos mutables
    private MutableLiveData<ArrayList<BluetoothDevice>> dispositivos;
    private MutableLiveData<Boolean> esAdaptable;
    private MutableLiveData<Boolean> esActivado;
    private MutableLiveData<Boolean> conectadoStreams;          //Pensado para evaluar cuando uno de los Stream(input o output) no conecte
    private MutableLiveData<Boolean> errorRecibiendo;           //pensado para evaluar cuando hallan errores en la entrada de datos
    private MutableLiveData<Boolean> errorEnviando;
    private MutableLiveData<Boolean> socketCreado;
    private MutableLiveData<Boolean> esConectado;               //Pensado par aobservar si se conecto el bluetooth o no

    private MutableLiveData<DatosBt> nuevoDato;                 //Pensado para observar nuevos datos provenientes del arduino


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
                recargarListaDispositivos();
                Log.i("Mensaje",dispositivos.getValue().size()+"");
            }
        }
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
        esConectado=new MutableLiveData<>(false);

        nuevoDato=new MutableLiveData<>();


        bluetoothIn=new BtHandler();

        //bluetoothIn=new InHandler();
        isThreadConnected=false;
        //Iniciando la base de datos
        //btSQL=new BluetoothSqlLite(context);




    }

    public boolean isEnabled(){
        return btAdapter.isEnabled();
    }


    /**
     * RECARGAR LISTA DE DISPOSITIVOS:
     *
     * recargarListaDispositivos: Rellena la lista de dispositivos con los que se encuentren disponibles
     * listarDispositivos:Convierte el conjunto de dispositivos en un arraylist para presentar en la ui
     */

    public void recargarListaDispositivos(){
        ArrayList<BluetoothDevice> lista;
        if(btAdapter.isEnabled()){
            //Usando la funcion auxiliar "listarDispositivos" se consigue la lista de dispositivos disponibles
            lista=listarDispositivos();
        }else{
            //Si no esta disponible, creara una lista con ningun valor
            lista=new ArrayList<>();
        }
        //Se cargan los nuevos valores al Mutable correspondiente
        dispositivos.postValue(lista);
    }

    private ArrayList<BluetoothDevice> listarDispositivos() {
        return new ArrayList<>(btAdapter.getBondedDevices());
    }

    public void comenzarConeccion(){
        //Validando que el thread no este conectado
        Thread thread2=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BluetoothDevice device = btAdapter.getRemoteDevice(address);
                    thread = new ConnectedThread(device);
                    Log.e("creando la coneccion","1");
                    esConectado.postValue(Boolean.valueOf(true));
                    Log.e("creando la coneccion","2");
                    thread.start();
                    //esConectado.postValue(true);

                }catch(Exception e) {
                    esConectado.postValue(false);
                    Log.e("Error creando la coneccion",e.toString());
                }
            }
        });
        thread2.start();
    }
    public boolean pararConeccion(){
        try{
            if(threadConnected){
                thread.pararThread();
                threadConnected=false;
            }
            return true;
        }catch(Exception e){
            return false;
        }
    }

    //Con esta funcion se envia un dato al modulo bluetooth correspondiente
    public void enviarDato(byte b){
        //El objeto tipo thread tiene la funcion write, que se encarga del envio correspondiente
        thread.write(b);
    }

    /**
     * BLUETOOTh thread
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        boolean ejecutar;       //Al modificar la variable "ejecutar" se detiene o continua la ejecucion del hilo

        public ConnectedThread(BluetoothDevice device) {
            BluetoothSocket socket=crearSocket(device);
            mmSocket=socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            //Permitiendo la ejecucion del dato
            ejecutar=true;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(BT_TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(BT_TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {

            mmBuffer = new byte[1024];
            int numBytes = 0; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (ejecutar) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = bluetoothIn.obtainMessage(
                            MESSAGE_READ,
                            numBytes,
                            -1,
                            mmBuffer);
                    //Log.i(TAG_HANDLER,"readMsg.toString()");
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(BT_TAG, "Input stream was disconnected", e);
                    break;
                }catch (Exception e){
                    Log.d(BT_TAG, "Error", e);
                }
            }
            try {
                mmInStream.close();
                mmOutStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = bluetoothIn.obtainMessage(
                        MESSAGE_WRITE, -1, -1, mmBuffer);

                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(BT_TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        bluetoothIn.obtainMessage(MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast", "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                bluetoothIn.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(BT_TAG, "Could not close the connect socket", e);
            }
        }

        //Esta funcion crea el socket dentro del mismo thread
        public BluetoothSocket crearSocket(BluetoothDevice device){

            BluetoothSocket socket=null;
            try{
                socket=device.createRfcommSocketToServiceRecord(BTMODULEUUID);
                socket.connect();
            }catch(IOException e){
                //Validar el error
                //Puesto en el mutable para poder ser validado luego por la ui
                socketCreado.postValue(false);
                //return false;
                return null;
            }

            return socket;

        }


        public void pararThread(){
            ejecutar=false;
        }
    }

    private class BtHandler extends Handler implements ConstantesMensajes{
        Toast toast;
        Context context;
        StringBuilder recDataString;

        public BtHandler (){//Context context){
            //this.context=context;
            recDataString=new StringBuilder();
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            //recDataString=new StringBuilder();

            if(msg.what==MESSAGE_READ){       //Comprobando que el mensaje recibido es el que estabamos esperando
                //Se pasa el mensaje a un objeto de tipo string
                //String mensaje=(String)msg.obj;
                String mensaje = new String((byte[]) msg.obj, java.nio.charset.StandardCharsets.UTF_8);

                //Log.i(TAG_HANDLER,mensaje);
                recDataString.append(mensaje);
                Log.i(TAG_HANDLER+"H",mensaje);
                //Usando el INDICE_FINAL para saber cuando termina el mensaje
                int finalMensaje=recDataString.indexOf(Character.toString(INDICE_FINAL));
                //Si el mensaje no es de tamaño uno, leer el mensaje
                if(finalMensaje>0){
                    //Sacando el String correspondiente al mensaje
                    mensaje=recDataString.substring(0,finalMensaje+1);
                    //Se valida si la cadena empieza como se quiere que empiece
                    if(mensaje.charAt(0)==INDICE_INICIAl){
                        //Que hacer con el mensaje
                        //toast.makeText(context,mensaje,Toast.LENGTH_SHORT).show();
                        Log.i(TAG_HANDLER,mensaje);

                        //usando la funcion auxiliar "datosFromString" para convertir el mensaje en un objeto tipo "DatosBt"
                        DatosBt dato=datosFromString(mensaje);
                        //Si el objeto no es nulo, se puede enviar al mutable correspondiente
                        if(dato!=null){
                            nuevoDato.postValue(dato);
                        }
                    }

                }
                //Limpiando el Stringbuilder del mensaje que tiene
                recDataString.delete(0,recDataString.length());

            }
        }

        /**
         *
         * @param msg:mensaje leido desde el bluetooth
         * @return objeto de tipo "DatosBt" a partir del mensaje
         */
        private DatosBt datosFromString(String msg) {
            //Variables para el retorno
            int tipo;
            double dato;

            //str se usara para jugar con las posiciones dentro del string
            StringBuilder str=new StringBuilder();
            str.append(msg);

            //calculando los indices para poder luego separar los datos
            int inicio=str.indexOf(Character.toString(INDICE_INICIAl));
            int fin=str.indexOf(Character.toString(INDICE_FINAL));
            int medio=str.indexOf(":");

            try {
                //tomando el dato que se encuentra entre "[" y ":", el cual corresponde al tipo
                tipo=Integer.parseInt(str.substring(inicio+1,medio));
                //tomando el dato que se encuentra entre ":" y "]", el cual corresponde al dato
                dato=Double.parseDouble(str.substring(medio+1,fin-1));
            }catch (Exception e){
                Log.i(TAG_HANDLER,e.toString());
                return null;
            }

            //Si la linea anterior no tuvo error, ya se podra crear el nuevo obejto sin problema
            return new DatosBt(tipo,dato);
        }
    }

    /**
     * GETTER Y SETTER
     */

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

    public LiveData<DatosBt> getNuevoDato() {
        return nuevoDato;
    }

    public LiveData<Boolean> getEsConectado() {
        return esConectado;
    }
}
