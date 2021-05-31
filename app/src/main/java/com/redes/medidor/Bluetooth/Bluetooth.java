//Clase inspirada en el trabajo hecho por  jose-jhr
//repositorio: https://github.com/jose-jhr/-bluetoothjhr.git

package com.redes.medidor.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.redes.medidor.ConstantesMensajes;
import com.redes.medidor.baseDatos.BluetoothSqlLite;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

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


    /**
     * CREACION DE HILOS PARA LA LECTURA DE DATOS
     */
    /*
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
            thread.writeData("Hola mundo como tan");
            return true;
        }
        //Si la ejecucion llega hasta este punto, es que no se ejecuto el bloque if anterior
        //por lo que no se creo el Thread
        return false;

    }*/

    public boolean comenzarListenerDatos(){
        //Validando que el thread no este conectado
        if(!threadConnected){
            try {
                BluetoothDevice device = btAdapter.getRemoteDevice(address);
                thread = new ConnectedThread(device);
                thread.start();
            }catch(Exception e){
                return false;
            }
            return true;
        }
        return true;
    }
    public boolean pararListenerDatos(){
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

    /*
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
*/

    /**
     * SEGUNDO INTENTO BLUETOOTh thread
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
                    Log.i(TAG_HANDLER,readMsg.toString());
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(BT_TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
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
            if(msg.what==MESSAGE_READ){       //Comprobando que el mensaje recibido es el que estabamos esperando
                //Se pasa el mensaje a un objeto de tipo string
                //String mensaje=(String)msg.obj;
                String mensaje = new String((byte[]) msg.obj, java.nio.charset.StandardCharsets.UTF_8);
                //toast.makeText(context,mensaje,Toast.LENGTH_SHORT).show();
                //Log.i(TAG_HANDLER,mensaje);
                recDataString.append(mensaje);
                //Usando el INDICE_FINAL para saber cuando termina el mensaje
                int finalMensaje=recDataString.indexOf(Character.toString(INDICE_FINAL));
                //Si el mensaje no es de tamaño uno, leer el mensaje
                if(finalMensaje>0){
                    //Sacando el String correspondiente al mensaje
                    mensaje=recDataString.substring(0,finalMensaje);
                    //Se valida si la cadena empieza como se quiere que empiece
                    if(mensaje.charAt(0)==INDICE_INICIAl){
                        //Que hacer con el mensaje
                        //toast.makeText(context,mensaje,Toast.LENGTH_SHORT).show();
                        Log.i(TAG_HANDLER,mensaje);
                    }

                }
                //Limpiando el Stringbuilder del mensaje que tiene
                recDataString.delete(0,recDataString.length());

            }
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
}
