package com.redes.medidor.IO;

import com.google.firebase.database.FirebaseDatabase;
import com.redes.medidor.Model.Clases.DatosBt;

import com.google.firebase.database.DatabaseReference;

public class DatosDao {

    //Referencia a la base de datos, con este objeto, se accede a la raiz de la base de datos
    private static DatabaseReference databaseReference;

    public DatosDao() {
        if(databaseReference==null){
            //permitiendo que los datos queden guardados en el dispositivo hasta que halla coneccion a internet
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);

            //Tomando la "DatabaseReference" desde la instancia del "FirebaseDatabase"
            //Osease, desde la raiz de la base
            databaseReference= FirebaseDatabase.getInstance().getReference();
        }
    }


    /**
     * Con esta funcion se planea realizar la subida de datos a la base de datos firebase
     *
     * @param datos:Recibiendo los datos que son captados por el bluetooth
     * @param MAC:Desde que dispositivo estan siendo cargados los datos
     */
    public void subirDatoVehiculo(DatosBt datos,String MAC){
        //Desde el nodo raiz, se navega hasta el nodo "Vehiculos" donde se podran guardar los datos
        DatabaseReference vehiculosRef=databaseReference.child("Datos_vehiculo");

        //"DatosVehiculos" sirve como clase que guarda los datos de "DatosBt"
        //de tal forma que puedan ser subidos a la base directamente
        DatosVehiculos datosVehiculos=new DatosVehiculos(datos);

        //Subiendo los datos a la base usando la referencia creada anteriormente
        //primero generamos una llave aleatoria, que sera el nodo de los datos que se subiran
        String key=vehiculosRef.child(MAC).push().getKey();
        //Subiendo los datos a la correspondiente key
        vehiculosRef.child(MAC).child(key).setValue(datosVehiculos).addOnCompleteListener(task -> {
            if(!task.isSuccessful()){
                //Codigo para validacion de error al subir los datos
            }
        });

        //Para hacer consultas, se creo una tabla extra por cada tipo de sensor
        //Cargando a la tabla correspondiente
        //Con esto se crea una lista de referencias a los datos dependiendo de su tipo
        vehiculosRef.child(String.valueOf(datos.getTipo())).child(key).setValue(datos.getTiempo());

        //NOTA:no es necesario revisar si existe el nodo de la direccion mac
        //     Si no existe, la api de firebase lo creara automaticamente



    }


    /**
     * Con esta clase auxiliar, que recibe un objeto tipo "DatosBt" se manejaran los datos que...
     * se subiran o bajaran desde la base
     */
    private class DatosVehiculos{
        public int tipo;
        public double dato;
        public long tiempo;

        //Para ver el uso de cada funcion, porfavor dirigirse a la clase "DatosBt"
        public DatosVehiculos(DatosBt datos) {
            this.dato=datos.getDato();
            this.tiempo=datos.getTiempo();
            this.tipo=datos.getTipo();
        }
    }
}
