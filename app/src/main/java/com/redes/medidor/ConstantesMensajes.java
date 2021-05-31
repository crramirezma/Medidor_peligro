package com.redes.medidor;

import java.util.UUID;

public interface ConstantesMensajes {
    public static final int MESSAGE_READ = 0;
    public static final int MESSAGE_WRITE = 1;
    public static final int MESSAGE_TOAST = 2;

    public static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //Indices sobre el inicio y el final de un mensaje enviado por el arduino
    public static final char INDICE_INICIAl='[';
    public static final char INDICE_FINAL=']';


    //TAGS
    public static final String TAG_HANDLER  = "MENSAJE POR BLUETOOTH";

}
