package es.ua.eps.filmoteca

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {

    private const val CHANNEL_ID = "filmoteca_geofences"
    private const val CHANNEL_NAME = "Geocercas de películas"

    fun createNotificationChannel(context: Context) {
        // Creación del canal de notificaciones en Android 8 o superior.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos al entrar cerca del lugar de rodaje de una película"
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showGeofenceNotification(
        context: Context,
        title: String,
        message: String
    ) {
        // Creación del canal antes de mostrar la notificación.
        createNotificationChannel(context)

        // Comprobación del permiso de notificaciones en Android 13 o superior.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                // Si no hay permiso, no se muestra la notificación.
                return
            }
        }

        // Creación de la notificación.
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Identificador sencillo para la notificación.
        val notificationId = System.currentTimeMillis().toInt()

        // Visualización de la notificación.
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}