# Filmoteca – Práctica 5

## Entorno
- Android Studio: Narwhal 3 Feature Drop (2025.1.3)
- Gradle wrapper: 8.13
- Kotlin: (indica la versión que aparece en el build)
- compileSdk/targetSdk: 36
- minSdk: 24
- Emulador: Pixel 7a – API 36

## Qué se ha hecho
- Actividad `AboutActivity` con dos modos:
  - XML (ConstraintLayout) + listeners.
  - Jetpack Compose (`AboutScreen`) con la misma UI.
- Internacionalización (ES/EN) mediante `values/strings.xml` y `values-en/strings.xml`.
- Imagen del autor (`drawable/rosa_azul.png`).
- Botones: web (ua.es), soporte por email, volver.

## Cómo ejecutar
1. Abrir el proyecto en Android Studio Narwhal 3.
2. Verificar Gradle 8.13 (wrapper).
3. Seleccionar AVD API 36.
4. Run app.

## Problemas encontrados
- Problemas inicialmente en los layout-> Para probar y realizar el resto de apartados pues lo he realizado todo en la pantalla inicial. No me arrancaba la pantalla de AboutActivity.
- Recordar dónde está el código y el lenguaje.
-Problemas de actualización de imports
-Problemas de API con la cámara y selección de imagen
-No está perfecta la visualización.

## Cómo se resolvieron
- Visualizando foros y lo indicado en la web de android.com

## Evidencias
- Vídeo demostrativo: `video_demostracion.mp4` resolución baja para menor tamaño.
