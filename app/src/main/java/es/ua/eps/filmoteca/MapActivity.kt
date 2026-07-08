package es.ua.eps.filmoteca

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MapActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_DIRECTOR = "EXTRA_DIRECTOR"
        const val EXTRA_YEAR = "EXTRA_YEAR"
        const val EXTRA_LATITUDE = "EXTRA_LATITUDE"
        const val EXTRA_LONGITUDE = "EXTRA_LONGITUDE"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lectura de los datos recibidos desde FilmDataActivity.
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Película"
        val director = intent.getStringExtra(EXTRA_DIRECTOR) ?: "Director desconocido"
        val year = intent.getStringExtra(EXTRA_YEAR) ?: "Sin año"
        val latitude = intent.getDoubleExtra(EXTRA_LATITUDE, 0.0)
        val longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0)

        // Creación del WebView por código.
        val webView = WebView(this)

        // Activación de JavaScript para que Leaflet pueda mostrar el mapa.
        webView.settings.javaScriptEnabled = true

        // Activación de almacenamiento DOM para mejorar la carga del mapa.
        webView.settings.domStorageEnabled = true

        // Apertura del contenido dentro del propio WebView.
        webView.webViewClient = WebViewClient()

        // Visualización del WebView en pantalla.
        setContentView(webView)

        // Carga del mapa HTML.
        webView.loadDataWithBaseURL(
            "https://carto.com/",
            createMapHtml(
                title = title,
                director = director,
                year = year,
                latitude = latitude,
                longitude = longitude
            ),
            "text/html",
            "UTF-8",
            null
        )
    }

    private fun createMapHtml(
        title: String,
        director: String,
        year: String,
        latitude: Double,
        longitude: Double
    ): String {
        // Escapado sencillo para evitar errores si el texto contiene comillas simples.
        val safeTitle = title.replace("'", "\\'")
        val safeDirector = director.replace("'", "\\'")
        val safeYear = year.replace("'", "\\'")

        // HTML con Leaflet y fondo de mapa de CARTO.
        // Se evita usar directamente tile.openstreetmap.org para que no salga "Access blocked".
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                
                <link
                    rel="stylesheet"
                    href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                
                <script
                    src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js">
                </script>
                
                <style>
                    html, body {
                        height: 100%;
                        margin: 0;
                        padding: 0;
                    }
                    
                    #map {
                        width: 100%;
                        height: 100%;
                    }
                </style>
            </head>
            
            <body>
                <div id="map"></div>
                
                <script>
                    // Creación del mapa centrado en las coordenadas de la película.
                    var map = L.map('map').setView([$latitude, $longitude], 14);
                    
                    // Capa base de CARTO para evitar el bloqueo de las teselas directas de OpenStreetMap.
                    L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png', {
                        maxZoom: 19,
                        attribution: '&copy; OpenStreetMap contributors &copy; CARTO'
                    }).addTo(map);
                    
                    // Creación del marcador de la película.
                    var marker = L.marker([$latitude, $longitude]).addTo(map);
                    
                    // Ventana de información del marcador.
                    marker.bindPopup(
                        '<b>$safeTitle</b><br>$safeDirector - $safeYear'
                    ).openPopup();
                </script>
            </body>
            </html>
        """.trimIndent()
    }
}