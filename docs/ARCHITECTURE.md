# Estructura de DocuPyme

La aplicación Android usa Kotlin, Compose y Room. Se conserva el identificador
`com.aistudio.docupyme.qzvkm` y la base de datos existente para no romper instalaciones.

## Responsabilidades

- `MainActivity.kt`: punto de entrada y tema.
- `ui/DocuPymeApp.kt`: composición de pantallas y conexión de eventos.
- `ui/DocuPymeViewModel.kt`: estado observable y coordinación de operaciones.
- `ui/Screen.kt`: destinos de navegación.
- `ui/UploadUiState.kt`: estado del formulario de carga.
- `ui/screens/`: un archivo por pantalla; los auxiliares exclusivos permanecen junto a su pantalla.
- `ui/components/`: componentes compartidos de navegación.
- `ui/theme/`: colores, tipografía y tema.
- `data/local/`: entidades, DAO, base de datos y datos de demostración.
- `data/repository/`: acceso a datos y operaciones documentales.
- `data/ai/`: clasificación y conversación, con alternativa local sin API key.
- `platform/DocumentNotifier.kt`: canales, permisos y envío de notificaciones Android.

La UI observa flujos del ViewModel; este coordina repositorio y servicios. El acceso
a las APIs de notificaciones queda aislado de los estados de pantalla. Las pruebas
de `DocumentNotifierTest` verifican el comportamiento con permisos concedidos y denegados.

## Compilación reproducible

Requiere JDK 21, Android SDK Platform 36.1 y las herramientas de compilación del SDK.
Define `ANDROID_HOME` o `sdk.dir` en un archivo local `local.properties`.

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
```

En Windows usa `gradlew.bat`. El wrapper descarga Gradle 9.3.1. La APK de prueba
se genera en `app/build/outputs/apk/debug/app-debug.apk`; Android genera la clave
debug local automáticamente. No requiere Firebase ni una clave de Gemini para el modo local.

La tarea de GitHub Actions ejecuta pruebas, lint y compilación, y publica la APK
como artefacto de cada ejecución correcta en `main` o en un pull request.

Para producción se necesita una clave propia, con `KEYSTORE_PATH`, `STORE_PASSWORD`
y `KEY_PASSWORD` en el entorno y alias `upload`. Las claves y `.env` no se versionan.

## Alcance actual

Se trata de una aplicación con datos de demostración. Algunas acciones, como
descarga, exportación PDF/CSV y sello de aprobación, muestran mensajes o registran
auditoría, pero no generan ni modifican archivos reales. La selección de usuario
simula roles y no sustituye una autenticación. Estas capacidades requieren una
implementación adicional antes de utilizarse con documentos empresariales reales.
