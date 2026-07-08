# Filmoteca - Mapas y Localización

Práctica de **Servicios de Plataformas Móviles** realizada sobre la aplicación Android **Filmoteca**.

En esta versión se ha añadido soporte para:

- Google Maps SDK for Android.
- Coordenadas de localización en las películas.
- Pantalla de mapa con marcador.
- Geocercas de 500 metros.
- Notificaciones al estar cerca del lugar de rodaje.
- Pruebas de ubicación mediante el emulador de Android.

---

## 1. Descripción del proyecto

La aplicación Filmoteca permite consultar un listado de películas y abrir la pantalla de detalle de cada una.

En esta práctica se ha ampliado la aplicación para que cada película tenga asociadas unas coordenadas de localización, formadas por:

```text
latitude
longitude
```

Desde la pantalla de detalle se puede pulsar el botón **Ver mapa** para abrir una pantalla con Google Maps y mostrar un marcador en la ubicación de la película.

Además, desde la pantalla de edición se han añadido dos botones:

```text
Añadir geocercado
Eliminar geocercado
```

El geocercado permite detectar si el usuario está cerca del lugar de rodaje de la película y mostrar una notificación.

---

## 2. Tecnologías utilizadas

- Android Studio
- Kotlin
- XML
- Jetpack Compose
- Google Maps SDK for Android
- Google Play Services Location
- Geofencing API
- Firebase Authentication
- Firebase Cloud Messaging
- Git y GitHub



## 3. Clave de Google Maps

Por seguridad, la clave de Google Maps **no se incluye en el repositorio**.

Para probar la carga visual del mapa, se debe añadir una clave válida en el archivo `local.properties`:

```properties
MAPS_API_KEY=CLAVE_DE_GOOGLE_MAPS
```

El archivo `local.properties` no se sube a GitHub porque puede contener datos privados del entorno local.

En el `AndroidManifest.xml` se utiliza el placeholder:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${mapsApiKey}" />
```

De esta manera, la clave se inyecta desde Gradle sin escribirla directamente en el Manifest.

---

## 4. Coordenadas de las películas

Se ha modificado el modelo `Film.kt` para añadir:

```kotlin
var latitude: Double = 0.0
var longitude: Double = 0.0
var geofenceEnabled: Boolean = false
```

En `FilmDataSource.kt` se han añadido coordenadas a las películas iniciales.

Ejemplo de coordenadas usadas:

```text
The Matrix
Latitud: 37.8114
Longitud: -122.4777

Inception
Latitud: 48.8738
Longitud: 2.2950

Spirited Away
Latitud: 35.7148
Longitud: 139.7967
```

---


## 5. Geocercas

Se ha implementado la parte de geocercado con los siguientes archivos:

```text
GeofenceHelper.kt
GeofenceBroadcastReceiver.kt
NotificationHelper.kt
```

### 5.1 GeofenceHelper.kt

Gestiona la creación y eliminación de geocercas.

La geocerca se crea con un radio de:

```text
500 metros
```

También se configura para avisar al entrar o si el usuario ya se encuentra dentro:

```kotlin
.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
```

### 5.2 GeofenceBroadcastReceiver.kt

Recibe el evento cuando se activa una geocerca.

Cuando el usuario entra dentro de la zona, se genera el mensaje:

```text
Estás cerca del lugar de rodaje de The Matrix
```

### 5.3 NotificationHelper.kt

Crea el canal de notificación y muestra una notificación cuando se activa el geocercado.

---

## 6. Prueba con el emulador

La parte de localización se ha probado usando el emulador de Android.

La prueba principal se ha realizado con la película:

```text
The Matrix
```

Coordenadas configuradas para la película:

```text
Latitud: 37.811400
Longitud: -122.477700
```

### Pasos realizados

1. Se ha abierto la aplicación en el emulador.
2. Se ha entrado en la película **The Matrix**.
3. Se ha pulsado **Editar**.
4. Se ha abierto la ventana del emulador:

```text
Extended Controls > Location
```

5. Se ha introducido la ubicación:

```text
37.811400, -122.477700
```

6. Se ha pulsado **Set Location**.
7. En la app se ha pulsado **Añadir geocercado**.
8. La app ha comprobado que la ubicación actual estaba dentro del radio de 500 metros.
9. Se ha mostrado la notificación indicando que el usuario está cerca del lugar de rodaje.

---

## 7. Estado final

La práctica queda implementada con:

- Mapa mediante Google Maps SDK.
- Marcador por película.
- Coordenadas en el modelo de datos.
- Geocercas de 500 metros.
- Botones para añadir y eliminar geocercado.
- Notificación al estar cerca del lugar de rodaje.
- Prueba realizada con el emulador usando las coordenadas de **The Matrix**.

La clave de Google Maps no se incluye en GitHub por seguridad.

## 8. Evidencias
Video demostrativo 
- mapas_localizacion.mp4
