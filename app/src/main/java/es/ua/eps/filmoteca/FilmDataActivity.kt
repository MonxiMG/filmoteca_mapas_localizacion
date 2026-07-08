@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package es.ua.eps.filmoteca

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class FilmDataActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FILM_TITLE = "EXTRA_FILM_TITLE"
        const val EXTRA_FILM_INDEX = "EXTRA_FILM_INDEX"
    }

    private val useCompose = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtención de la película real enviada desde el listado.
        val filmInfo = getFilmInfoFromIntent()

        if (useCompose) {
            setContent {
                FilmDataScaffold(
                    title = filmInfo.title,
                    onBack = { finish() }
                ) {
                    FilmDataScreenCompose(
                        info = filmInfo,
                        onOpenImdb = { openUrl(filmInfo.imdbUrl) },
                        onEdit = {
                            startActivity(Intent(this, FilmEditActivity::class.java))
                        },
                        onBackToMain = {
                            val intent = Intent(this, FilmListActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            startActivity(intent)
                        }
                    )
                }
            }
        } else {
            setContentView(R.layout.activity_film_data)
        }
    }

    private fun getFilmInfoFromIntent(): FilmInfo {
        // Lectura del índice enviado desde FilmListActivity.
        val filmIndex = intent.getIntExtra(EXTRA_FILM_INDEX, -1)

        // Búsqueda de la película por índice.
        if (filmIndex in FilmDataSource.films.indices) {
            val film = FilmDataSource.films[filmIndex]
            return film.toFilmInfo()
        }

        // Compatibilidad por si alguna pantalla antigua envía el título.
        val filmTitle = intent.getStringExtra(EXTRA_FILM_TITLE)

        if (!filmTitle.isNullOrBlank()) {
            val film = FilmDataSource.films.firstOrNull {
                it.title.equals(filmTitle, ignoreCase = true)
            }

            if (film != null) {
                return film.toFilmInfo()
            }
        }

        // Película por defecto si no se recibe información válida.
        return FilmInfo(
            posterRes = R.drawable.ic_launcher_foreground,
            title = "Película desconocida",
            director = "Desconocido",
            year = "Sin año",
            genre = "Sin género",
            format = "Sin formato",
            imdbUrl = "https://www.imdb.com/",
            notes = ""
        )
    }

    private fun Film.toFilmInfo(): FilmInfo {
        // Conversión del modelo Film al modelo visual FilmInfo.
        return FilmInfo(
            posterRes = posterRes,
            title = title,
            director = director,
            year = year.toString(),
            genre = genre,
            format = format,
            imdbUrl = imdbUrl,
            notes = notes
        )
    }

    private fun openUrl(url: String) {
        // Apertura de la URL de IMDb en el navegador.
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}

data class FilmInfo(
    val posterRes: Int,
    val title: String,
    val director: String,
    val year: String,
    val genre: String,
    val format: String,
    val imdbUrl: String,
    val notes: String
)

/* ---------------- Scaffold con App Bar ---------------- */

@Composable
private fun FilmDataScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            content()
        }
    }
}

/* ---------------- Pantalla Compose ---------------- */

@Composable
fun FilmDataScreenCompose(
    info: FilmInfo,
    onOpenImdb: () -> Unit,
    onEdit: () -> Unit,
    onBackToMain: () -> Unit
) {
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = Color.Black,
        contentColor = Color.Yellow
    )

    var notes by rememberSaveable { mutableStateOf(info.notes) }
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cartel de la película.
        Image(
            painter = painterResource(id = info.posterRes),
            contentDescription = "Cartel de ${info.title}",
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Título de la película.
        Text(
            text = info.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tarjeta de datos.
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                LabeledValue(label = "Director", value = info.director)
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                LabeledValue(label = "Año", value = info.year)
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                LabeledValue(label = "Género", value = info.genre)
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                LabeledValue(label = "Formato", value = info.format)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notas de la película.
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.hint_notes)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Botón IMDb.
        Button(
            onClick = onOpenImdb,
            colors = buttonColors,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Text(text = stringResource(R.string.action_imdb))
        }

        // Botón editar.
        Button(
            onClick = onEdit,
            colors = buttonColors,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Text(text = stringResource(R.string.action_edit))
        }

        // Botón volver.
        Button(
            onClick = onBackToMain,
            colors = buttonColors,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.action_back_to_main))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}