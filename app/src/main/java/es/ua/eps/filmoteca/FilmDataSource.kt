package es.ua.eps.filmoteca

object FilmDataSource {

    // Lista mutable de películas.
    val films: MutableList<Film> = mutableListOf(
        Film(
            title = "The Matrix",
            director = "Lana & Lilly Wachowski",
            year = 1999,
            genre = "Sci-Fi",
            format = "Blu-ray",
            imdbUrl = "https://www.imdb.com/title/tt0133093/",
            posterRes = R.drawable.ic_launcher_foreground,
            notes = "Clásico de ciencia ficción",

            // Coordenadas aproximadas de una localización de rodaje.
            latitude = 37.8114,
            longitude = -122.4777,

            // Geocercado desactivado inicialmente.
            geofenceEnabled = false
        ),
        Film(
            title = "Inception",
            director = "Christopher Nolan",
            year = 2010,
            genre = "Sci-Fi",
            format = "Digital",
            imdbUrl = "https://www.imdb.com/title/tt1375666/",
            posterRes = R.drawable.ic_launcher_foreground,
            notes = "",

            // Coordenadas aproximadas de una localización de rodaje.
            latitude = 48.8738,
            longitude = 2.2950,

            // Geocercado desactivado inicialmente.
            geofenceEnabled = false
        ),
        Film(
            title = "Spirited Away",
            director = "Hayao Miyazaki",
            year = 2001,
            genre = "Animación",
            format = "DVD",
            imdbUrl = "https://www.imdb.com/title/tt0245429/",
            posterRes = R.drawable.ic_launcher_foreground,
            notes = "Studio Ghibli",

            // Coordenadas aproximadas de una localización relacionada.
            latitude = 35.7148,
            longitude = 139.7967,

            // Geocercado desactivado inicialmente.
            geofenceEnabled = false
        )
    )

    // Listener para avisar a la pantalla del listado cuando cambien las películas.
    var onFilmsChanged: (() -> Unit)? = null

    private fun notifyFilmsChanged() {
        // Aviso de cambio en el listado de películas.
        onFilmsChanged?.invoke()
    }

    fun add(film: Film) {
        // Alta manual de una película.
        films.add(film)

        // Notificación a la interfaz.
        notifyFilmsChanged()
    }

    fun removeAt(index: Int) {
        // Eliminación de una película por posición si la posición es válida.
        if (index in films.indices) {
            films.removeAt(index)

            // Notificación a la interfaz.
            notifyFilmsChanged()
        }
    }

    fun clearAll() {
        // Eliminación de todas las películas.
        films.clear()

        // Notificación a la interfaz.
        notifyFilmsChanged()
    }

    fun addOrUpdateFilm(film: Film): String {
        // Búsqueda de una película con el mismo título.
        val index = films.indexOfFirst {
            it.title.equals(film.title, ignoreCase = true)
        }

        return if (index >= 0) {
            // Actualización de la película existente.
            films[index] = film

            // Notificación a la interfaz.
            notifyFilmsChanged()

            "Película actualizada: ${film.title}"
        } else {
            // Alta de una nueva película.
            films.add(film)

            // Notificación a la interfaz.
            notifyFilmsChanged()

            "Película añadida: ${film.title}"
        }
    }

    fun deleteFilmByTitle(title: String): String {
        // Búsqueda de una película con el mismo título.
        val index = films.indexOfFirst {
            it.title.equals(title, ignoreCase = true)
        }

        return if (index >= 0) {
            // Eliminación de la película existente.
            films.removeAt(index)

            // Notificación a la interfaz.
            notifyFilmsChanged()

            "Película eliminada: $title"
        } else {
            // Resultado sin cambios cuando la película no existe.
            "Película no encontrada: $title"
        }
    }

    fun processRemoteData(data: Map<String, String>): String {
        // Lectura de la operación recibida en el mensaje FCM.
        val operation = data["operacion"]
            ?: data["operation"]
            ?: data["tipo"]
            ?: ""

        // Lectura del título de la película.
        val title = data["title"]
            ?: data["titulo"]
            ?: ""

        if (title.isBlank()) {
            return "Mensaje FCM sin título de película"
        }

        return when (operation.lowercase()) {
            "alta", "add" -> {
                // Creación de la película a partir de los datos recibidos.
                val film = Film(
                    title = title,
                    director = data["director"] ?: "Desconocido",
                    year = data["year"]?.toIntOrNull()
                        ?: data["anio"]?.toIntOrNull()
                        ?: 0,
                    genre = data["genre"] ?: data["genero"] ?: "Sin género",
                    format = data["format"] ?: data["formato"] ?: "Digital",
                    imdbUrl = data["imdbUrl"] ?: data["imdb"] ?: "https://www.imdb.com/",
                    posterRes = R.drawable.ic_launcher_foreground,
                    notes = data["notes"] ?: data["notas"] ?: "",

                    // Lectura de latitud recibida por FCM.
                    latitude = data["latitude"]?.toDoubleOrNull()
                        ?: data["latitud"]?.toDoubleOrNull()
                        ?: 0.0,

                    // Lectura de longitud recibida por FCM.
                    longitude = data["longitude"]?.toDoubleOrNull()
                        ?: data["longitud"]?.toDoubleOrNull()
                        ?: 0.0,

                    // Lectura del estado del geocercado recibido por FCM.
                    geofenceEnabled = data["geofenceEnabled"]?.toBooleanStrictOrNull()
                        ?: data["geocercado"]?.toBooleanStrictOrNull()
                        ?: false
                )

                addOrUpdateFilm(film)
            }

            "baja", "delete", "remove" -> {
                // Baja de película a partir del título recibido.
                deleteFilmByTitle(title)
            }

            else -> {
                // Operación no reconocida.
                "Operación FCM no reconocida: $operation"
            }
        }
    }
}