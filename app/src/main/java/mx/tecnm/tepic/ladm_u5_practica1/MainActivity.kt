package mx.tecnm.tepic.ladm_u5_practica1

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var baseRemota = FirebaseFirestore.getInstance()
    var posicion = ArrayList<Data>()
    var siPermiso = 98
    lateinit var locacion : LocationManager
    var pos1 : Location = Location("")
    var pos2 : Location = Location("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),siPermiso)
        } else {
            locacion = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            var oyente = Oyente(this)
            locacion.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, oyente)
        }

        baseRemota.collection("tecnologico")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if(firebaseFirestoreException != null){
                    lista.setText("ERROR: "+firebaseFirestoreException.message)
                    return@addSnapshotListener
                }

                var resultado = ""
                posicion.clear()
                for(document in querySnapshot!!){
                    var data = Data()
                    data.nombre = document.getString("nombre").toString()
                    data.posicion1 = document.getGeoPoint("posicion1")!!
                    data.posicion2 = document.getGeoPoint("posicion2")!!

                    resultado += data.toString()+"\n\n"
                    posicion.add(data)
                }

                lista.setText(resultado)
            }

        boton.setOnClickListener {
            if(busqueda.text.toString() == ""){
                ubicacion.setText("")
            }
            baseRemota.collection("tecnologico")
                .whereEqualTo("nombre", busqueda.getText().toString())
                .addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                    if(firebaseFirestoreException != null){
                        ubicacion.setText("ERROR, NO HAY CONEXIÓN CON LA BD")
                        return@addSnapshotListener
                    }

                    for(document in  querySnapshot!!){
                        pos1.longitude = document.getGeoPoint("posicion1")!!.longitude
                        pos1.latitude = document.getGeoPoint("posicion1")!!.latitude

                        pos2.longitude = document.getGeoPoint("posicion2")!!.longitude
                        pos2.latitude = document.getGeoPoint("posicion2")!!.latitude
                    }

                    var r = "(${(pos1.latitude)}, ${pos1.longitude}),(${pos2.latitude}, ${pos2.longitude})"
                    ubicacion.setText(r)
                }
        }

    }
}

class Oyente(puntero:MainActivity) : LocationListener {
    var p = puntero
    override fun onLocationChanged(location: Location) {
        p.coords.setText("COORDENADAS:\n${location.latitude}, ${location.longitude}")
        p.estas.setText("USTED ESTA DE PASEO POR EL TECNOLOGICO DE TEPIC")
        var geoPosicionGPS = GeoPoint(location.latitude, location.longitude)

        for (item in p.posicion) {
            if (item.estoyEn(geoPosicionGPS)) {
                p.estas.setText("USTED ESTA EN:\n${item.nombre}")
            }
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String) {
    }

    override fun onProviderDisabled(provider: String) {
    }
}