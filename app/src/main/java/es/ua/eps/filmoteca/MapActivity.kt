package es.ua.eps.filmoteca

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_DIRECTOR = "EXTRA_DIRECTOR"
        const val EXTRA_YEAR = "EXTRA_YEAR"
        const val EXTRA_LATITUDE = "EXTRA_LATITUDE"
        const val EXTRA_LONGITUDE = "EXTRA_LONGITUDE"
    }

    private var filmTitle: String = ""
    private var filmDirector: String = ""
    private var filmYear: String = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Carga del layout que contiene el SupportMapFragment.
        setContentView(R.layout.activity_maps)

        // Lectura de los datos recibidos desde FilmDataActivity.
        filmTitle = intent.getStringExtra(EXTRA_TITLE) ?: "Película"
        filmDirector = intent.getStringExtra(EXTRA_DIRECTOR) ?: "Director desconocido"
        filmYear = intent.getStringExtra(EXTRA_YEAR) ?: "Sin año"
        latitude = intent.getDoubleExtra(EXTRA_LATITUDE, 0.0)
        longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0)

        // Registro en Logcat para comprobar que los datos llegan correctamente.
        Log.d("MAP_TEST", "Título: $filmTitle")
        Log.d("MAP_TEST", "Director: $filmDirector")
        Log.d("MAP_TEST", "Año: $filmYear")
        Log.d("MAP_TEST", "Latitud: $latitude")
        Log.d("MAP_TEST", "Longitud: $longitude")

        // Obtención del fragmento de mapa definido en activity_maps.xml.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        // Solicitud del mapa de forma asíncrona.
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Creación de la posición de la película.
        val filmLocation = LatLng(latitude, longitude)

        // Configuración básica del tipo de mapa.
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        // Activación de controles básicos del mapa.
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true

        // Creación del marcador.
        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(filmLocation)
                .title(filmTitle)
                .snippet("$filmDirector - $filmYear")
        )

        // Movimiento de la cámara hacia la posición de la película.
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(filmLocation, 14f)
        )

        // Apertura automática de la ventana de información del marcador.
        marker?.showInfoWindow()

        // Registro para confirmar que el mapa ha sido preparado.
        Log.d("MAP_TEST", "Mapa preparado y marcador añadido")
    }
}