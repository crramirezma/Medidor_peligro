package com.redes.medidor.ui.Agradecimientos;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.redes.medidor.R;

import java.util.ArrayList;

public class AgradecimientosFragment extends Fragment {


    private ArrayList<String[]> agradecimiento;

    public static AgradecimientosFragment newInstance() {
        return new AgradecimientosFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.agradecimientos_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        agradecimiento=llenarArray();


        // TODO: Use the ViewModel
    }

    private ArrayList<String[]> llenarArray() {
        return null;
    }

    //Agregar a mano los agradecimientos aqui(creditos)


}