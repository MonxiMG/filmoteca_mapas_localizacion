package es.ua.eps.filmoteca

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Lectura del evento de geocercado recibido.
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent == null) {
            Log.e("GEOFENCE_TEST", "Evento de geocercado nulo")
            return
        }

        // Comprobación de errores en el evento.
        if (geofencingEvent.hasError()) {
            Log.e(
                "GEOFENCE_TEST",
                "Error en geocercado: ${geofencingEvent.errorCode}"
            )
            return
        }

        // Lectura del tipo de transición recibida.
        val transitionType = geofencingEvent.geofenceTransition

        // Solo se procesa la entrada en el geocercado.
        if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
            // Obtención de las geocercas activadas.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            if (triggeringGeofences.isNullOrEmpty()) {
                Log.d("GEOFENCE_TEST", "Entrada en geocercado sin lista de geocercas")
                return
            }

            // Se obtiene el identificador de la primera geocerca activada.
            val requestId = triggeringGeofences.first().requestId

            // Se limpia el identificador para mostrar un texto más amigable.
            val filmTitle = requestId.removePrefix("film_geofence_")

            // Mensaje que se mostrará en la notificación.
            val message = "Estás cerca del lugar de rodaje de $filmTitle"

            // Registro en Logcat para comprobar que funciona.
            Log.d("GEOFENCE_TEST", message)

            // Visualización de la notificación.
            NotificationHelper.showGeofenceNotification(
                context = context,
                title = "Lugar de rodaje cercano",
                message = message
            )
        } else {
            // Registro de otras transiciones no utilizadas en esta práctica.
            Log.d("GEOFENCE_TEST", "Transición no procesada: $transitionType")
        }
    }
}