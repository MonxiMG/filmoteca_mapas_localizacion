package es.ua.eps.filmoteca

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Registro del token FCM del dispositivo.
        Log.d("FCM_TOKEN", "Token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Registro de recepción de mensaje FCM.
        Log.d("FCM_MESSAGE", "Mensaje recibido")

        // Lectura de los datos recibidos en el mensaje.
        val data = message.data

        // Registro de los datos recibidos.
        Log.d("FCM_MESSAGE", "Datos: $data")

        // Procesamiento de alta o baja de películas.
        val result = FilmDataSource.processRemoteData(data)

        // Registro del resultado de la operación.
        Log.d("FCM_MESSAGE", result)
    }
}