package com.redes.medidor.ui.estadisticas;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.redes.medidor.R;

public class EstadisticasFragment extends Fragment {

    private EstadisticasViewModel mViewModel;

    public static EstadisticasFragment newInstance() {
        return new EstadisticasFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.estadisticas_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(EstadisticasViewModel.class);
        // TODO: Use the ViewModel
    }

}