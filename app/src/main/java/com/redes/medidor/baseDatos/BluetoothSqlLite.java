package com.redes.medidor.baseDatos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class BluetoothSqlLite extends SQLiteOpenHelper {
    /**
     * Constantes para la base
     */
    private static final int DB_VERSION = 1;
    private static final String DB_NAME ="vehiculo.db" ;
    private static final String TABLE_DATOS = "t_datos";


    public BluetoothSqlLite(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Creando la tabla para subir los datos
        db.execSQL("CREATE TABLE "+ TABLE_DATOS +"("+
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tipo INTEGER NOT NULL," +
                "dato REAL NOT NULL," +
                "fecha INTEGER NOT NULL" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATOS);
        onCreate(db);
    }
}
