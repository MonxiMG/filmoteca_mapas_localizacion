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
            notes = "Clásico de ciencia ficción"
        ),
        Film(
            title = "Inception",
            director = "Christopher Nolan",
            year = 2010,
            genre = "Sci-Fi",
            format = "Digital",
            imdbUrl = "https://www.imdb.com/title/tt1375666/",
            posterRes = R.drawable.ic_launcher_foreground,
            notes = ""
        ),
        Film(
            title = "Spirited Away",
            director = "Hayao Miyazaki",
            year = 2001,
            genre = "Animación",
            format = "DVD",
            imdbUrl = "https://www.imdb.com/title/tt0245429/",
            posterRes = R.drawable.ic_launcher_foreground,
            notes = "Studio Ghibli"
        )
    )

    // Listener para avisar a la pantalla del listado cuando cambien las películas.
    var onFilmsChanged: (() -> Unit)? = null

    private fun notifyFilmsChanged() {
        // Aviso de cambio en el listado de películas.
        onFilmsChanged?.invoke()
    }

    fun add(film: Film) {
        films.add(film)
        notifyFilmsChanged()
    }

    fun removeAt(index: Int) {
        if (index in films.indices) {
            films.removeAt(index)
            notifyFilmsChanged()
        }
    }

    fun clearAll() {
        films.clear()
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
            notifyFilmsChanged()
            "Película actualizada: ${film.title}"
        } else {
            // Alta de una nueva película.
            films.add(film)
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
                    notes = data["notes"] ?: data["notas"] ?: ""
                )

                addOrUpdateFilm(film)
            }

            "baja", "delete", "remove" -> {
                deleteFilmByTitle(title)
            }

            else -> {
                "Operación FCM no reconocida: $operation"
            }
        }
    }
}