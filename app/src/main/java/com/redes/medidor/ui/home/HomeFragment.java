package com.redes.medidor.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.redes.medidor.databinding.FragmentHomeBinding;
import com.redes.medidor.ViewModel.DatosViewModel;

public class HomeFragment extends Fragment {

    private DatosViewModel datosViewModel;
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        datosViewModel =
                new ViewModelProvider(this).get(DatosViewModel.class);
        //Pasandole al objeto "Bluetooth" el contexto para el manejo de la base de datos


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
            
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}