package es.ua.eps.filmoteca

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

object GeofenceHelper {

    // Radio de geocercado pedido por la práctica.
    private const val GEOFENCE_RADIUS_METERS = 500f

    // Duración de la geocerca. NEVER_EXPIRE indica que no caduca automáticamente.
    private const val GEOFENCE_EXPIRATION = Geofence.NEVER_EXPIRE

    fun addGeofence(
        context: Context,
        film: Film,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Comprobación de coordenadas válidas.
        if (film.latitude == 0.0 && film.longitude == 0.0) {
            onError("La película no tiene coordenadas válidas")
            return
        }

        // Comprobación del permiso de localización precisa.
        val hasFineLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation) {
            onError("Falta el permiso de localización")
            return
        }

        // Cliente de geocercas de Google Play Services.
        val geofencingClient = LocationServices.getGeofencingClient(context)

        // Identificador único de la geocerca.
        val requestId = getGeofenceRequestId(film)

        // Creación de la geocerca.
        val geofence = Geofence.Builder()
            .setRequestId(requestId)
            .setCircularRegion(
                film.latitude,
                film.longitude,
                GEOFENCE_RADIUS_METERS
            )
            .setExpirationDuration(GEOFENCE_EXPIRATION)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        // Petición de geocercado.
        // INITIAL_TRIGGER_ENTER permite avisar si el usuario ya está dentro del perímetro.
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        // Alta de la geocerca.
        geofencingClient.addGeofences(
            geofencingRequest,
            getGeofencePendingIntent(context)
        ).addOnSuccessListener {
            // Se marca la película como geocercada en memoria.
            film.geofenceEnabled = true

            Log.d("GEOFENCE_TEST", "Geocercado añadido: ${film.title}")
            onSuccess()
        }.addOnFailureListener { exception ->
            val message = exception.message ?: "Error desconocido al añadir geocercado"

            Log.e("GEOFENCE_TEST", "Error al añadir geocercado: $message")
            onError(message)
        }
    }

    fun removeGeofence(
        context: Context,
        film: Film,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Cliente de geocercas de Google Play Services.
        val geofencingClient = LocationServices.getGeofencingClient(context)

        // Identificador de la geocerca que se desea eliminar.
        val requestId = getGeofenceRequestId(film)

        // Eliminación de la geocerca por identificador.
        geofencingClient.removeGeofences(listOf(requestId))
            .addOnSuccessListener {
                // Se marca la película como no geocercada en memoria.
                film.geofenceEnabled = false

                Log.d("GEOFENCE_TEST", "Geocercado eliminado: ${film.title}")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                val message = exception.message ?: "Error desconocido al eliminar geocercado"

                Log.e("GEOFENCE_TEST", "Error al eliminar geocercado: $message")
                onError(message)
            }
    }

    private fun getGeofenceRequestId(film: Film): String {
        // Identificador usado para reconocer la geocerca en el BroadcastReceiver.
        return "film_geofence_${film.title}"
    }

    private fun getGeofencePendingIntent(context: Context): PendingIntent {
        // Intent que se enviará cuando se active la geocerca.
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)

        // Flags necesarios para Android 12 o superior.
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        // PendingIntent usado por el sistema de geocercas.
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            flags
        )
    }
}