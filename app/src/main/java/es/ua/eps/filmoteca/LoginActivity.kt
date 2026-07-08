package es.ua.eps.filmoteca

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Carga de la pantalla de login.
        setContentView(R.layout.activity_login)

        // Inicialización de Firebase Authentication.
        auth = FirebaseAuth.getInstance()

        // Configuración del inicio de sesión con Google.
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Creación del cliente de Google Sign In.
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // Referencia al botón de inicio de sesión.
        val btnGoogleSignIn = findViewById<Button>(R.id.btnGoogleSignIn)

        // Lanzamiento de la pantalla de selección de cuenta Google.
        btnGoogleSignIn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onStart() {
        super.onStart()

        // Comprobación de usuario ya autenticado.
        if (auth.currentUser != null) {
            openFilmListActivity()
        }
    }

    @Deprecated("Uso mantenido para una implementación sencilla de la práctica.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Comprobación del resultado del inicio de sesión con Google.
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                // Obtención de la cuenta Google seleccionada.
                val account = task.getResult(ApiException::class.java)

                // Autenticación en Firebase con el token de Google.
                firebaseAuthWithGoogle(account.idToken)

            } catch (e: ApiException) {
                Toast.makeText(
                    this,
                    "Error al iniciar sesión con Google",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        // Validación del token recibido desde Google.
        if (idToken == null) {
            Toast.makeText(
                this,
                "No se ha recibido el token de Google",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Creación de la credencial de Firebase a partir del token de Google.
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        // Inicio de sesión en Firebase con la credencial de Google.
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    openFilmListActivity()
                } else {
                    Toast.makeText(
                        this,
                        "No se ha podido autenticar en Firebase",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun openFilmListActivity() {
        // Apertura de la pantalla principal de la Filmoteca.
        val intent = Intent(this, FilmListActivity::class.java)
        startActivity(intent)

        // Cierre de LoginActivity para evitar volver al login con el botón Atrás.
        finish()
    }
}