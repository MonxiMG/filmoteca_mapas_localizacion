package es.ua.eps.filmoteca

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button as M3Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri

import com.google.firebase.auth.FirebaseAuth

// Enumeración para cambiar entre interfaces
enum class Mode { Layouts, Compose }

class AboutActivity : AppCompatActivity() {

    // Modo Layouts o Compose
    private val mode = Mode.Layouts
    // private val mode = Mode.Compose

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initUI()
    }

    private fun initUI() {
        when (mode) {
            Mode.Layouts -> initLayouts()
            Mode.Compose -> initCompose()
        }
    }

    /** -------- Funciones de acción (reutilizadas en ambos modos) -------- **/
    private fun openWebsite() {
        val url = "https://www.ua.es".toUri()
        val intent = Intent(Intent.ACTION_VIEW, url).addCategory(Intent.CATEGORY_BROWSABLE)
        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "No se pudo abrir un navegador", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendSupportEmail() {
        val mailUri = "mailto:monsem.garcia@ua.es".toUri()
        val intent = Intent(Intent.ACTION_SENDTO, mailUri).apply {
            putExtra(Intent.EXTRA_SUBJECT, "Consulta de soporte - Filmoteca")
            putExtra(Intent.EXTRA_TEXT, "Hola, tengo una duda sobre la app Filmoteca.")
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "No hay aplicación de correo disponible", Toast.LENGTH_SHORT).show()
        }
    }

    /** --- Versión XML tradicional --- **/
    private fun initLayouts() {
        setContentView(R.layout.activity_about)

        // Toolbar como AppBar + flecha HOME
        findViewById<MaterialToolbar?>(R.id.toolbar)?.let {
            setSupportActionBar(it)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        val tvAuthor = findViewById<TextView>(R.id.tvAuthor)
        tvAuthor.text = getString(R.string.about_author, "Monse Muñoz")

        // Referencia al usuario autenticado en Firebase.
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Referencia al TextView de datos del usuario.
        val tvUser = findViewById<TextView>(R.id.tvUser)

        // Preparación de los datos del usuario conectado.
        val userName = currentUser?.displayName ?: "Usuario no disponible"
        val userEmail = currentUser?.email ?: "Email no disponible"

        // Visualización del usuario conectado.
        tvUser.text = "Usuario conectado:\n$userName\n$userEmail"

        findViewById<Button>(R.id.btnWeb).setOnClickListener { openWebsite() }
        findViewById<Button>(R.id.btnSupport).setOnClickListener { sendSupportEmail() }
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }

    /** --- Versión Compose --- **/
    private fun initCompose() {
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                AboutScreen(
                    authorName = "Monse Muñoz",
                    onWeb = { openWebsite() },
                    onSupport = { sendSupportEmail() },
                    onBack = { finish() }
                )
            }
        }
    }

    // Gestiona la flecha HOME del AppBar (modo Layouts)
    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            finish(); true
        } else super.onOptionsItemSelected(item)
}

/** --- UI en Compose --- **/
@Composable
fun AboutScreen(
    authorName: String,
    onWeb: () -> Unit,
    onSupport: () -> Unit,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.rosa_azul),
                contentDescription = stringResource(R.string.about_image_cd),
                modifier = Modifier
                    .size(160.dp)
                    .padding(bottom = 24.dp)
            )

            Text(
                text = stringResource(R.string.about_author, authorName),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            M3Button(
                onClick = onWeb,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) { Text(text = stringResource(R.string.btnWeb)) }

            M3Button(
                onClick = onSupport,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) { Text(text = stringResource(R.string.btnSupport)) }

            M3Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) { Text(text = stringResource(R.string.btnBack)) }
        }
    }
}
