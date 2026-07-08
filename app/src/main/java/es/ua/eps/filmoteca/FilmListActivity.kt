package es.ua.eps.filmoteca

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

class FilmListActivity : AppCompatActivity() {

    private val useCompose = false

    private var filmAdapter: FilmAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showFcmToken()

        if (useCompose) {
            setContent {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FilmListScreen(
                        onSeeA = {
                            startActivity(
                                Intent(this, FilmDataActivity::class.java)
                                    .putExtra(FilmDataActivity.EXTRA_FILM_TITLE, "Película A")
                            )
                        },
                        onSeeB = {
                            startActivity(
                                Intent(this, FilmDataActivity::class.java)
                                    .putExtra(FilmDataActivity.EXTRA_FILM_TITLE, "Película B")
                            )
                        },
                        onAbout = {
                            startActivity(Intent(this, AboutActivity::class.java))
                        },
                        onExit = {
                            finishAffinity()
                        }
                    )
                }
            }
        } else {
            setContentView(R.layout.activity_film_list)

            // Configuración de la Toolbar como AppBar.
            findViewById<MaterialToolbar?>(R.id.toolbar)?.let { tb ->
                setSupportActionBar(tb)

                // Ocultación del título en la AppBar.
                supportActionBar?.setDisplayShowTitleEnabled(false)
                supportActionBar?.title = null
                tb.title = ""

                // Carga del menú en la Toolbar.
                tb.menu.clear()
                tb.inflateMenu(R.menu.menu_film_list)

                // Gestión de las opciones del menú de la Toolbar.
                tb.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_add -> {
                            addDefaultFilm()
                            true
                        }

                        R.id.action_about -> {
                            startActivity(Intent(this, AboutActivity::class.java))
                            true
                        }

                        R.id.action_sign_out -> {
                            closeSession()
                            true
                        }

                        R.id.action_disconnect -> {
                            disconnectAccount()
                            true
                        }

                        else -> false
                    }
                }
            }

            // Configuración del listado de películas.
            val listView = findViewById<ListView>(R.id.lvFilms)
            filmAdapter = FilmAdapter(this, FilmDataSource.films)
            listView.adapter = filmAdapter

            // Apertura del detalle de la película seleccionada.
            listView.setOnItemClickListener { _, _, position, _ ->
                startActivity(
                    Intent(this, FilmDataActivity::class.java)
                        .putExtra("EXTRA_FILM_INDEX", position)
                )
            }

            // Activación del borrado múltiple de películas.
            filmAdapter?.let { adapter ->
                activarBorradoMultiple(listView, adapter)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Registro del aviso de cambios en las películas.
        FilmDataSource.onFilmsChanged = {
            runOnUiThread {
                // Actualización visual del listado.
                filmAdapter?.notifyDataSetChanged()

                Toast.makeText(
                    this,
                    "Listado actualizado por FCM",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Refresco del listado al volver a la pantalla.
        filmAdapter?.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()

        // Eliminación del aviso de cambios al salir de la pantalla.
        FilmDataSource.onFilmsChanged = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_film_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                addDefaultFilm()
                true
            }

            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }

            R.id.action_sign_out -> {
                closeSession()
                true
            }

            R.id.action_disconnect -> {
                disconnectAccount()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addDefaultFilm() {
        val nueva = Film(
            title = "Nueva película",
            director = "Desconocido",
            year = 2025,
            genre = "Drama",
            format = "Blu-ray",
            imdbUrl = "https://www.imdb.com/",
            posterRes = R.drawable.ic_launcher_foreground,
            notes = ""
        )

        FilmDataSource.add(nueva)
    }

    private fun activarBorradoMultiple(listView: ListView, adapter: FilmAdapter) {
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL

        listView.setMultiChoiceModeListener(object : android.widget.AbsListView.MultiChoiceModeListener {
            private val seleccionadas = mutableSetOf<Int>()

            override fun onCreateActionMode(mode: android.view.ActionMode, menu: Menu): Boolean {
                // Carga del menú contextual de borrado.
                mode.menuInflater.inflate(R.menu.menu_context_delete, menu)

                // Limpieza de selecciones anteriores.
                seleccionadas.clear()

                return true
            }

            override fun onPrepareActionMode(mode: android.view.ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onItemCheckedStateChanged(
                mode: android.view.ActionMode,
                position: Int,
                id: Long,
                checked: Boolean
            ) {
                // Actualización del conjunto de películas seleccionadas.
                if (checked) {
                    seleccionadas.add(position)
                } else {
                    seleccionadas.remove(position)
                }

                // Actualización del título del modo contextual.
                mode.title = "${seleccionadas.size} seleccionada(s)"
            }

            override fun onActionItemClicked(
                mode: android.view.ActionMode,
                item: MenuItem
            ): Boolean {
                return if (item.itemId == R.id.action_delete) {
                    // Borrado de las películas seleccionadas desde el final para evitar errores de índice.
                    val indices = seleccionadas.toList().sortedDescending()

                    for (i in indices) {
                        FilmDataSource.removeAt(i)
                    }

                    // Actualización del listado después del borrado.
                    adapter.notifyDataSetChanged()

                    // Cierre del modo contextual.
                    mode.finish()

                    true
                } else {
                    false
                }
            }

            override fun onDestroyActionMode(mode: android.view.ActionMode) {
                // Limpieza de la selección al cerrar el modo contextual.
                seleccionadas.clear()
            }
        })
    }

    private fun showFcmToken() {
        // Solicitud del token actual de Firebase Cloud Messaging.
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->

                // Comprobación de error al obtener el token.
                if (!task.isSuccessful) {
                    Log.w("FCM_TOKEN", "No se pudo obtener el token FCM", task.exception)
                    return@addOnCompleteListener
                }

                // Obtención del token actual.
                val token = task.result

                // Registro del token en Logcat.
                Log.d("FCM_TOKEN", "Token actual: $token")

                // Aviso visual para confirmar que se ha solicitado el token.
                Toast.makeText(
                    this,
                    "Token FCM mostrado en Logcat",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun closeSession() {
        // Configuración del cliente de Google Sign In.
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Creación del cliente de Google Sign In.
        val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // Cierre de sesión en Firebase.
        FirebaseAuth.getInstance().signOut()

        // Cierre de sesión en Google.
        googleSignInClient.signOut().addOnCompleteListener {
            openLoginActivity()
        }
    }

    private fun disconnectAccount() {
        // Configuración del cliente de Google Sign In.
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Creación del cliente de Google Sign In.
        val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // Cierre de sesión en Firebase.
        FirebaseAuth.getInstance().signOut()

        // Revocación del acceso de la cuenta Google a la aplicación.
        googleSignInClient.revokeAccess().addOnCompleteListener {
            openLoginActivity()
        }
    }

    private fun openLoginActivity() {
        // Creación del intent para volver a la pantalla de login.
        val intent = Intent(this, LoginActivity::class.java)

        // Limpieza de la pila de pantallas para evitar volver atrás a la Filmoteca.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Apertura de la pantalla de login.
        startActivity(intent)

        // Cierre de la pantalla actual.
        finish()
    }
}

/* ----------------- Compose UI sin cambios ----------------- */

@Composable
fun FilmListScreen(
    onSeeA: () -> Unit,
    onSeeB: () -> Unit,
    onAbout: () -> Unit,
    onExit: () -> Unit
) {
    val blackYellow = ButtonDefaults.buttonColors(
        containerColor = Color.Black,
        contentColor = Color.Yellow
    )

    Box(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Button(
            onClick = onAbout,
            colors = blackYellow,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Text("Acerca de")
        }

        Column(
            Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Listado de películas",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onSeeA,
                colors = blackYellow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text("Ver película A")
            }

            Button(
                onClick = onSeeB,
                colors = blackYellow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver película B")
            }
        }

        Button(
            onClick = onExit,
            colors = blackYellow,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Text("Salir de la aplicación")
        }
    }
}