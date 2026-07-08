package es.ua.eps.filmoteca

data class Film(
    var title: String,
    var director: String,
    var year: Int,
    var genre: String,
    var format: String,
    var imdbUrl: String,
    var posterRes: Int,
    var notes: String,

    // Coordenada de latitud del lugar de grabación de la película.
    var latitude: Double = 0.0,

    // Coordenada de longitud del lugar de grabación de la película.
    var longitude: Double = 0.0,

    // Indica si la película tiene geocercado activado.
    // Este campo se usará en la segunda parte de la práctica.
    var geofenceEnabled: Boolean = false
) {
    // Alias para compatibilidad con adaptadores antiguos.
    val imageResId: Int
        get() = posterRes

    override fun toString(): String = title
}