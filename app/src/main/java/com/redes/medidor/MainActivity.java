package com.redes.medidor;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.redes.medidor.ViewModel.MainViewModel;
import com.redes.medidor.databinding.ActivityMainBinding;
import com.redes.medidor.ui.vehiculo.VehiculosViewModel;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private VehiculosViewModel vehiculosViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Iniciando el viewModel
        //mainViewModel=new ViewModelProvider(this).get(MainViewModel.class);

        super.onCreate(savedInstanceState);

        //Creando y enlazando el viewModel
        vehiculosViewModel=new ViewModelProvider(this).get(VehiculosViewModel.class);
        vehiculosViewModel.activarBluetooth(getApplicationContext());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

   /* private void iniciarObservers() {
        mainViewModel.getDispositivos().observe(this,bluetoothDevices -> {

        });
    }*/

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
        AlertDialog alert= lanzarDialog();
        alert.show();
    };

    /*
        En esta funcion se devolvera un AlertDialog con los comandos para el bluetooth
    */
    private AlertDialog lanzarDialog() {
        //Recibiendo los datos del preference para luego conectar el bluetooth
        SharedPreferences preferences=getSharedPreferences(String.valueOf(R.string.modulo_bluetooth), Context.MODE_PRIVATE);
        String nombre=preferences.getString(String.valueOf(R.string.nombre_bluetooth),"No hay modulo disponible");
        String Mac=preferences.getString(String.valueOf(R.string.mac_bluetooth),"0");

        //Lista de opciones para el manejo del bluetooth

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder
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
}