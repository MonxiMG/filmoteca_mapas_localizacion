package es.ua.eps.filmoteca

data class Film(
    var title: String,
    var director: String,
    var year: Int,
    var genre: String,
    var format: String,
    var imdbUrl: String,
    var posterRes: Int,
    var notes: String
) {
    // Alias para compatibilidad con adapters antiguos
    val imageResId: Int
        get() = posterRes

    override fun toString(): String = title
}
