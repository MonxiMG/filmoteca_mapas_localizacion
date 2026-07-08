@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package es.ua.eps.filmoteca

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.appbar.MaterialToolbar
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import java.io.File

class FilmEditActivity : AppCompatActivity() {

    private val useCompose = false

    // ---- estado para la rama XML ----
    private var currentPhotoUri: Uri? = null
    private lateinit var ivPoster: ImageView

    // Permiso de cámara
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openCamera() else
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
    }

    // Cámara → guarda foto en la URI que le pasamos
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            ivPoster.setImageURI(currentPhotoUri)
        } else {
            Toast.makeText(this, "No se tomó la foto", Toast.LENGTH_SHORT).show()
        }
    }

    // Photo Picker
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) ivPoster.setImageURI(uri)
        else Toast.makeText(this, "No se seleccionó imagen", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_TITLE   = "EXTRA_TITLE"
        const val EXTRA_DIRECTOR= "EXTRA_DIRECTOR"
        const val EXTRA_YEAR    = "EXTRA_YEAR"
        const val EXTRA_GENRE   = "EXTRA_GENRE"
        const val EXTRA_FORMAT  = "EXTRA_FORMAT"
        const val EXTRA_IMDB    = "EXTRA_IMDB"
        const val EXTRA_NOTES   = "EXTRA_NOTES"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (useCompose) {
            setContent {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("Editar película") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Volver"
                                    )
                                }
                            }
                        )
                    }
                ) { inner ->
                    Box(Modifier.padding(inner)) {
                        FilmEditScreenCompose(
                            posterRes = R.drawable.pelicula_a,
                            onTakePhoto = {
                                Toast.makeText(
                                    this@FilmEditActivity,
                                    "Tomar foto (pendiente en Compose)",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onPickImage = {
                                Toast.makeText(
                                    this@FilmEditActivity,
                                    "Seleccionar imagen (pendiente en Compose)",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onSave = { t, d, y, g, f, i, n ->
                                val data = Intent().apply {
                                    putExtra(EXTRA_TITLE, t)
                                    putExtra(EXTRA_DIRECTOR, d)
                                    putExtra(EXTRA_YEAR, y)
                                    putExtra(EXTRA_GENRE, g)
                                    putExtra(EXTRA_FORMAT, f)
                                    putExtra(EXTRA_IMDB, i)
                                    putExtra(EXTRA_NOTES, n)
                                }
                                setResult(RESULT_OK, data)
                                finish()
                            },
                            onCancel = { setResult(RESULT_CANCELED); finish() }
                        )
                    }
                }
            }
            return
        }

        // ---------------------- XML ----------------------
        setContentView(R.layout.activity_film_edit)

        findViewById<MaterialToolbar?>(R.id.toolbar)?.let {
            setSupportActionBar(it)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Editar película"
        }

        ivPoster = findViewById(R.id.ivPosterEdit)

        val etTitle    = findViewById<EditText>(R.id.etTitle)
        val etDirector = findViewById<EditText>(R.id.etDirector)
        val etYear     = findViewById<EditText>(R.id.etYear)
        val spGenre    = findViewById<Spinner>(R.id.spGenre)
        val spFormat   = findViewById<Spinner>(R.id.spFormat)
        val etImdb     = findViewById<EditText>(R.id.etImdb)
        val etNotes    = findViewById<EditText>(R.id.etNotes)

        findViewById<Button>(R.id.btnTakePhoto).setOnClickListener { checkCameraAndOpen() }
        findViewById<Button>(R.id.btnPickImage).setOnClickListener { openPicker() }

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val data = Intent().apply {
                putExtra(EXTRA_TITLE,    etTitle.text.toString())
                putExtra(EXTRA_DIRECTOR, etDirector.text.toString())
                putExtra(EXTRA_YEAR,     etYear.text.toString())
                putExtra(EXTRA_GENRE,    spGenre.selectedItem?.toString() ?: "")
                putExtra(EXTRA_FORMAT,   spFormat.selectedItem?.toString() ?: "")
                putExtra(EXTRA_IMDB,     etImdb.text.toString())
                putExtra(EXTRA_NOTES,    etNotes.text.toString())
            }
            setResult(RESULT_OK, data); finish()
        }

        findViewById<Button>(R.id.btnCancel).setOnClickListener {
            setResult(RESULT_CANCELED); finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) { finish(); true } else super.onOptionsItemSelected(item)

    // ---------- Helpers XML ----------

    private fun checkCameraAndOpen() {
        requestCameraPermission.launch(Manifest.permission.CAMERA)
    }

    private fun openCamera() {
        // Crea archivo y URI
        val imagesDir = File(getExternalFilesDir(null), "Pictures").apply { mkdirs() }
        val photoFile = File.createTempFile("photo_", ".jpg", imagesDir)

        val uri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            photoFile
        )
        currentPhotoUri = uri

        // Usar la val local para evitar el error de smart cast
        takePictureLauncher.launch(uri)
    }

    private fun openPicker() {
        // Clase correcta: PickVisualMediaRequest
        pickImageLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
}

/* ------------------ UI COMPOSE ------------------ */

@Composable
fun FilmEditScreenCompose(
    posterRes: Int,
    onTakePhoto: () -> Unit,
    onPickImage: () -> Unit,
    onSave: (title:String, director:String, year:String, genre:String, format:String, imdb:String, notes:String) -> Unit,
    onCancel: () -> Unit
) {
    val blackYellow = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.Yellow)

    var title by rememberSaveable { mutableStateOf("") }
    var director by rememberSaveable { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf("") }
    var imdb by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }

    val genres = listOf("Acción", "Drama", "Comedia", "Terror", "Sci-Fi")
    val formats = listOf("DVD", "Blu-ray", "Online")
    var genreExpanded by remember { mutableStateOf(false) }
    var formatExpanded by remember { mutableStateOf(false) }
    var genreSelected by rememberSaveable { mutableStateOf(genres.first()) }
    var formatSelected by rememberSaveable { mutableStateOf(formats.first()) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(posterRes),
                contentDescription = "Cartel",
                modifier = Modifier.width(96.dp).height(140.dp),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onTakePhoto, colors = blackYellow, modifier = Modifier.fillMaxWidth()) {
                    Text("Tomar una fotografía")
                }
                Button(onClick = onPickImage, colors = blackYellow, modifier = Modifier.fillMaxWidth()) {
                    Text("Seleccionar una imagen")
                }
            }
        }

        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título de la película") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = director, onValueChange = { director = it }, label = { Text("Director") }, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(
            value = year,
            onValueChange = { new -> year = new.filter { it.isDigit() }.take(4) },
            label = { Text("Año de estreno") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Género
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = genreSelected, onValueChange = {}, readOnly = true,
                label = { Text("Género") },
                trailingIcon = { Text(if (genreExpanded) "▲" else "▼") },
                modifier = Modifier.fillMaxWidth().clickable { genreExpanded = !genreExpanded }
            )
            DropdownMenu(expanded = genreExpanded, onDismissRequest = { genreExpanded = false }, modifier = Modifier.fillMaxWidth()) {
                genres.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { genreSelected = option; genreExpanded = false })
                }
            }
        }

        // Formato
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = formatSelected, onValueChange = {}, readOnly = true,
                label = { Text("Formato") },
                trailingIcon = { Text(if (formatExpanded) "▲" else "▼") },
                modifier = Modifier.fillMaxWidth().clickable { formatExpanded = !formatExpanded }
            )
            DropdownMenu(expanded = formatExpanded, onDismissRequest = { formatExpanded = false }, modifier = Modifier.fillMaxWidth()) {
                formats.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { formatSelected = option; formatExpanded = false })
                }
            }
        }

        OutlinedTextField(value = imdb, onValueChange = { imdb = it }, label = { Text("Enlace a IMDB") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notas del usuario...") }, modifier = Modifier.fillMaxWidth(), minLines = 4)

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { onSave(title, director, year, genreSelected, formatSelected, imdb, notes) },
                colors = blackYellow, modifier = Modifier.weight(1f)
            ) { Text("Guardar") }
            Button(onClick = onCancel, colors = blackYellow, modifier = Modifier.weight(1f)) { Text("Cancelar") }
        }
    }
}
