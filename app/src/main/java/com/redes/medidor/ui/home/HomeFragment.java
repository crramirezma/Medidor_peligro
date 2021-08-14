package com.redes.medidor.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.redes.medidor.R;
import com.redes.medidor.databinding.FragmentHomeBinding;
import com.redes.medidor.ViewModel.DatosViewModel;

public class HomeFragment extends Fragment {

    private DatosViewModel datosViewModel;
    private FragmentHomeBinding binding;

    //Para el manejo de eventos, se usara el layout que contiene a cada elemento
    private ConstraintLayout comenzarLy;
    private ConstraintLayout detenerLy;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        datosViewModel = new ViewModelProvider(requireActivity()).get(DatosViewModel.class);
        //datosViewModel =
                //new ViewModelProvider(this).get(DatosViewModel.class);
        //Pasandole al objeto "Bluetooth" el contexto para el manejo de la base de datos


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //Iniciando las variables globales y otras
        init(root);

        //Creando los listener
        listener();

        //Lista de observadores de los livedata del viewmodel
        observers();

        return root;
    }

    private void observers() {
        datosViewModel.getEsConectado().observe(getViewLifecycleOwner(),aBoolean -> {

            Toast.makeText(getContext(), aBoolean+" falksjdhfla", Toast.LENGTH_SHORT).show();
            if(aBoolean){
                //Dejando visible los layouts ocultados al iniciar el fragment
                comenzarLy.setVisibility(View.VISIBLE);

                //detenerLy=root.findViewById(R.id.detenerLY);
                detenerLy.setVisibility(View.VISIBLE);
            }else{
                //Que hacer si no se conecto
            }
        });
    }


    //Manejo de listeners de eventos
    private void listener() {
        //Envia el dato para comenzar la transferencia
        comenzarLy.setOnClickListener(v -> datosViewModel.enviarDatoBt((byte) 1));

        //Envia el dato para detener la transferencia
        detenerLy.setOnClickListener(v -> datosViewModel.enviarDatoBt((byte)0));
    }

    //Init conecta las variables con la vista
    //e inicia las variables necesarias para la ejecucion
    private void init(View root) {
        //Se colocan invisible para que solo se puedan ver si esta disponible la coneccion

        comenzarLy=root.findViewById(R.id.comenzarLY);
        comenzarLy.setVisibility(View.INVISIBLE);

        detenerLy=root.findViewById(R.id.detenerLY);
        detenerLy.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}