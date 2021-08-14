package com.redes.medidor.ModelosML;

public class Predictor {
    public static int prediction(double [] function){
        int[]datos={0,0,0,0};
        int v=0;
        for(int i=0; i <3;i++){
            for(int j=i+1;j<4;j++){
                if(function[v++]>=0)
                    datos[i]++;
                else
                    datos[j]++;
            }
        }
        return mayor(datos);
    }
    private static int mayor(int[] numeros){
        int pos=0;
        int dato=numeros[0];
        for(int i=1;i<numeros.length;i++){
            if(dato<numeros[i]){
                dato=numeros[i];
                pos=i;
            }

        }
        return pos;
    }
}
