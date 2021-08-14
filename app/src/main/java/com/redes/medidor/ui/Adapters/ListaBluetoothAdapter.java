package com.redes.medidor.ui.Adapters;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.redes.medidor.R;
import com.redes.medidor.ViewModel.DatosViewModel;

import org.jetbrains.annotations.NotNull;

public class ListaBluetoothAdapter extends RecyclerView.Adapter<ListaBluetoothAdapter.ListaBluetoothHolder> {
    DatosViewModel viewModel;

    FragmentActivity fragment;


    public ListaBluetoothAdapter(FragmentActivity fragment, DatosViewModel viewModel) {
        this.fragment=fragment;

        this.viewModel=viewModel;

    }

    @NonNull
    @NotNull
    @Override
    public ListaBluetoothHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.lista_bluetooth_adapter,parent,false);
        return new ListaBluetoothHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ListaBluetoothHolder holder, int position) {
        holder.asignarDatos(position);
        holder.layout.setAnimation(AnimationUtils.loadAnimation(fragment,R.anim.transition_animation));
    }

    @Override
    public int getItemCount() {
        if(viewModel.getDispositivos().getValue()==null){
            Log.i("Mensaje","la lista de dispositivos es 0");
            return 0;
        }

        return viewModel.getDispositivos().getValue().size();
    }



    /**
     * HOLDER
     * se encarga de crearle al adapter un objeto por cada item dentro de la lista de datos que se le pasan al adapter
    */
    public class ListaBluetoothHolder extends RecyclerView.ViewHolder {
        private int posicion;

        //Declaracion de variables
        private TextView nombreTxt;
        private TextView macTxt;

        //Layout sera usado para la animacion
        ConstraintLayout layout;

        private ImageButton btBoton;

        public ListaBluetoothHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            //conectando las variables con su respectiva vista
            iniciar(itemView);
            //Iniciando los listeners correspondientes
            listeners();
        }

        private void iniciar(View itemView) {
            nombreTxt=itemView.findViewById(R.id.nombreTxt);
            macTxt=itemView.findViewById(R.id.macTxt);

            btBoton=itemView.findViewById(R.id.btBoton);

            layout=itemView.findViewById(R.id.layout_main);


        }

        private void listeners() {

            //Boton de opciones del dispositivo
            btBoton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog opciones=lanzarAlertDialog();


                    opciones.show();
                }
            });
        }

        private AlertDialog lanzarAlertDialog() {
            //Variable que cargara con el valor que se escoja en el alert
            int item=0;
            //Lista de datos elegibles para el alertdialog
            String [] items={"Conectar al mando principal","No tener en cuenta","Otras"};
            AlertDialog.Builder alert=new AlertDialog.Builder(fragment)
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case 0:
                                    //subiendo los datos escogidos a la opcion por defecto
                                    subirSharedPreferences();
                                    break;
                                case 1:
                                    //Implementacion futura
                                    break;
                                case 2:
                                    //otras implementaciones
                                    break;
                                default:
                                    dialog.dismiss();
                                    break;

                            }
                        }
                        //cargar los datos a las preferencias compartidas
                        private void subirSharedPreferences() {
                            //Iniciando el objeto de SharedPreferences
                            SharedPreferences preferences=fragment.getSharedPreferences(String.valueOf(R.string.modulo_bluetooth), Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor= preferences.edit();

                            //Datos para guardar
                            String nombre=nombreTxt.getText().toString();
                            String macAdress=macTxt.getText().toString();

                            //Pasandolos a las preferencias
                            editor.putString(String.valueOf(R.string.nombre_bluetooth),nombre);
                            editor.putString(String.valueOf(R.string.mac_bluetooth),macAdress);

                            //Subiendolos a las preferencias
                            if(editor.commit()){
                                //Zona para validar que se hallan subido los datos a las preferencias

                                //Modificando el valor del MAc en el viewModel
                                viewModel.modificarMac(macAdress);
                            }


                        }
                    })
                    /*.setSingleChoiceItems(items, 1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case 0:

                                    //subirSharedPreferences();
                                break;
                                case 1:
                                    //Implementacion futura
                                break;
                                case 2:
                                    //otras implementaciones
                                break;
                                default:
                                    dialog.dismiss();
                                break;

                            }
                        }
                        //cargar los datos a las preferencias compartidas
                        private void subirSharedPreferences() {
                            //Iniciando el objeto de SharedPreferences
                            SharedPreferences preferences=context.getSharedPreferences("Modulo", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor= preferences.edit();

                            //Datos para guardar
                            String nombre=nombreTxt.getText().toString();
                            String macAdress=macTxt.getText().toString();

                            //Pasandolos a las preferencias
                            editor.putString("Nombre",nombre);
                            editor.putString("Mac",macAdress);

                            //Subiendolos a las preferencias
                            editor.apply();
                        }
                    })*/
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Validar si hay opciones disponibles
                        }
                    })
                    .setTitle("Que hacer con el dispositivo "+nombreTxt.getText().toString());
            return alert.create();

        }

        public void asignarDatos(int position) {
            BluetoothDevice d=viewModel.getDispositivos().getValue().get(position);
            nombreTxt.setText(d.getName());
            macTxt.setText(d.getAddress());

            //Viendo si el dispositivo se encuentra emparejado
            if(d.getBondState()==BluetoothDevice.BOND_BONDED){
                //Colocando el boton en color rojo para denotar que no esta conectado
                //btBoton.getBackground().setColorFilter(Color.parseColor("#00ff00"), PorterDuff.Mode.DARKEN);
                btBoton.setBackgroundColor(Color.RED);
            }


        }
    }
}
