package com.redes.medidor.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.redes.medidor.Model.ConstantesMensajes;
import com.redes.medidor.R;
import com.redes.medidor.databinding.ActivityMainBinding;
import com.redes.medidor.ViewModel.DatosViewModel;


public class MainActivity extends AppCompatActivity{
    //Constante para el control del bluetooth encendido
    private static final int REQUEST_ENABLE_BT = 1;

    StringBuilder recDataString;


    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private DatosViewModel datosViewModel;

    private Handler datosBTHandler;


    //observador del estado del bluetooth
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Iniciando el viewModel
        //mainViewModel=new ViewModelProvider(this).get(MainViewModel.class);

        super.onCreate(savedInstanceState);

        //Creando y enlazando el viewModel
        datosViewModel =new ViewModelProvider(this).get(DatosViewModel.class);

        //definiendo por primera vez el valor del MAC
        //Si ya se ha usado la aplicacion, este valor estara en las "SharedPreferences"
        datosViewModel.modificarMac(getApplicationContext().getSharedPreferences(String.valueOf(R.string.modulo_bluetooth),Context.MODE_PRIVATE).getString(String.valueOf(R.string.mac_bluetooth),"0"));

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        //Iniciando el BroadCaster
        //Usando la funcion auxiliar, se crea el broadcast
        broadcastReceiver=crearBroadCast();
        //Luego de crear el Broadcast, lo ultimo que falta es suscribirlo para que cuando se ejecute cierta accion
        //El receiver la pueda procesar
        //Para ello, la funcion "registrarEventosBluetooth" se encarga del trabajo
        registrarEventosBluetooth();

        datosBTHandler=new BtHandler();



        //Iniciando el StringBuilder para la recepcion de mensajes desde el hilo
        recDataString=new StringBuilder();

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(opcionesBluetoothDialog);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        //Creando los observadores
        observadores();

        //Funcion que crea y gestiona los observers de los viewModel
        //iniciarObservers();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQUEST_ENABLE_BT){
            if(resultCode==Activity.RESULT_OK){

            }else{
                Toast.makeText(getApplicationContext(),"La aplicacion requiere de Bluetooth para poder funcionar",Toast.LENGTH_LONG).show();
            }
            //Validar que se halla iniciado el bluetooth
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Eliminando el thread creado antes

        //Eliminando el Broadcast receiver de ejecucion

        this.unregisterReceiver(broadcastReceiver);
    }
    /**
     * ESTADO DEL BLUETOOTH
     *
     * crearBroadCast=crea el BroadCast para la recepcion del estado del bluetooth
     * registrarEventosBluetooth(): Subscribe el broadcast a los eventos para que los pueda recibir y procesar
     */
    BroadcastReceiver crearBroadCast(){
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Se recupera desde el intent el tipo de accion que se esta leyendo
                final String action = intent.getAction();
                // Filtramos por la accion. Nos interesa detectar BluetoothAdapter.ACTION_STATE_CHANGED
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    //Ahora toca filtrar que tipo de accion esta realizando el bluetooth
                    //Para ello, primero le pedimos el dato al intent
                    final int estado = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);

                    switch (estado){
                        case BluetoothAdapter.STATE_OFF:
                            Toast.makeText(getApplicationContext(),"Porfavor inicia el Bluetooth",Toast.LENGTH_SHORT).show();
                            datosViewModel.recargarDatos();
                            break;
                        case BluetoothAdapter.STATE_ON:
                            //codigo para cuando se activa el bluetooth
                            datosViewModel.recargarDatos();
                            break;
                        default:

                            break;
                    }
                }
            }
        };
    }

    public void registrarEventosBluetooth(){
        IntentFilter filtro = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(broadcastReceiver, filtro);
    }


    /**
     * ZONA DE RECEPCION DE SOLICITUDES DE LA INTERFAZ PARA EL MODULO BLUETOOTH
     * Correponde de:
     *      -listener
     *      -Dialog
     *      -
     */

    /*
    Listener del boton flotante de la pantalla main
    este boton desplegara un alertdialog con distintas opciones de mensajes que se le podran enviar al
        modulo bluetooth con el que se encuentre conectado el dispositivo\
    */
    View.OnClickListener opcionesBluetoothDialog= v -> {
        //Validando que el Bluetooth se encuentre disponible
        if(datosViewModel.bluetoothDisponible()){
            AlertDialog alert= lanzarDialog();
            alert.show();
        }else{
            Toast.makeText(getApplicationContext(),"El bluetooth no se encuentra disponible", Toast.LENGTH_SHORT).show();
        }

    };

    /*
        En esta funcion se devolvera un AlertDialog con los comandos para el bluetooth
    */
    private AlertDialog lanzarDialog() {
        //Recibiendo los datos del preference para luego conectar el bluetooth
        SharedPreferences preferences=getSharedPreferences(String.valueOf(R.string.modulo_bluetooth), Context.MODE_PRIVATE);
        String nombre=preferences.getString(String.valueOf(R.string.nombre_bluetooth),"No hay modulo disponible");
        String Mac=preferences.getString(String.valueOf(R.string.mac_bluetooth),"0");

        String[] datos={"Comenzar coneccion"};
        //Lista de opciones para el manejo del bluetooth

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder
                .setItems(datos, (dialog, which) -> {
                    switch (which){
                        case 0:

                            datosViewModel.comenzarConeccion();
                            //Toast.makeText(getApplicationContext(),(v?"Se ha conectado":"No se ha conectado"),Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.enviar_comando, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(Mac.equals("0")){
                            Toast.makeText(getApplicationContext(),"No ha escogido un dispositivo para el trabajo",Toast.LENGTH_LONG).show();
                        }else{

                        }
                    }
                })
                .setNegativeButton(R.string.cancelar_dialogo, (dialog, which) -> Toast.makeText(getApplicationContext(),R.string.dialogo_cancelado,Toast.LENGTH_SHORT).show())
                .setTitle(preferences.getString(String.valueOf(R.string.nombre_bluetooth),"No hay modulo disponible"));

        return builder.create();
    }


    /**
     * En esta funcion se encuentran programados todos los observaDOREs
     */
    private void observadores() {


        //Validando si el valor de activado es modificado
        datosViewModel.getEsActivado().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(!aBoolean){
                    //Peticion para que inicie el bluetooth
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

                }else{
                    Toast.makeText(getApplicationContext(),"El dispositivo tiene el Bluetooth activado",Toast.LENGTH_LONG).show();
                }
                //Validar si el bluetooth esta activado
            }
        });

        //Evaluando si el dispositivo acepta bluetooth
        datosViewModel.getEsAdaptable().observe(this, aBoolean -> {
            //evalua si el dispositivo es apto para bluetooth o no
            if(!aBoolean){
                Toast toast = Toast.makeText(getApplicationContext(),"El dispositivo no es apto para bluetooth",Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }

        });


        //Evaluando nuevos datos provenientes del arduino
        datosViewModel.getNuevoDato().observe(this,datosBt -> {
            //Evaluando el dato para ver si se puede enviar a la base de datos
            if(datosBt!=null){
                //Obteniendo el valor de la mac a partir de las preferencias compartidas
                SharedPreferences preferences=getSharedPreferences(String.valueOf(R.string.modulo_bluetooth), Context.MODE_PRIVATE);
                String MAC=preferences.getString(String.valueOf(R.string.mac_bluetooth),"0");

                //TODO implementar el algoritmo de SVC
                datosViewModel.subirDatos(MAC,datosBt);
            }
        });


    }
    //Creando una subclase para el handler
    private class BtHandler extends Handler implements ConstantesMensajes {
        Toast toast;
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what==MESSAGE_WRITE){       //Comprobando que el mensaje recibido es el que estabamos esperando
                //Se pasa el mensaje a un objeto de tipo string
                String mensaje=(String)msg.obj;
                toast.makeText(getApplicationContext(),mensaje,Toast.LENGTH_SHORT).show();
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
                        toast.makeText(getApplicationContext(),mensaje,Toast.LENGTH_SHORT).show();
                    }

                }
                //Limpiando el Stringbuilder del mensaje que tiene
                recDataString.delete(0,recDataString.length());

            }
        }
    }


}