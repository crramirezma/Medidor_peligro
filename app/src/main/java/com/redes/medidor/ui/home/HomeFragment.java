package com.redes.medidor.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.redes.medidor.R;
import com.redes.medidor.databinding.FragmentHomeBinding;
import com.redes.medidor.ui.vehiculo.VehiculosViewModel;

public class HomeFragment extends Fragment {

    private VehiculosViewModel vehiculosViewModel;
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        vehiculosViewModel =
                new ViewModelProvider(this).get(VehiculosViewModel.class);
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