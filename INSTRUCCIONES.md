# IPTV App — Instrucciones de compilación

## Requisitos
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 17
- Android SDK 35 (compileSdk / targetSdk)
- Dispositivo/emulador con Android 7.0+ (minSdk 24)

## Cómo abrir el proyecto
1. Abre **Android Studio**
2. `File → Open` → selecciona la carpeta `IPTVApp`
3. Gradle sincronizará automáticamente las dependencias (~2–5 min la primera vez)
4. Verifica que `local.properties` tenga la ruta correcta a tu Android SDK

## Compilar y ejecutar
```
./gradlew assembleDebug      # APK debug en app/build/outputs/apk/debug/
./gradlew assembleRelease    # APK release (requiere keystore)
```
O pulsa el botón ▶ Run en Android Studio.

## Uso de la app
1. Al abrirla verás la pantalla de inicio vacía
2. Toca **+** (esquina superior derecha) o ve a **Ajustes**
3. Pega la URL de tu lista M3U (ej: `http://miservidor.com/lista.m3u`)
4. Los canales se cargarán y clasificarán automáticamente

## Formatos soportados
| Formato | Soporte |
|---------|---------|
| HLS (.m3u8) | ✅ Nativo via Media3 |
| DASH (.mpd) | ✅ Nativo via Media3 |
| MP4 | ✅ Nativo via Media3 |
| RTMP | ⚠️ Requiere lib adicional (ver nota) |

> **RTMP**: ExoPlayer/Media3 no soporta RTMP de forma nativa. Para streams RTMP
> agrega `com.github.pedroSG94.RootEncoder:library:2.3.4` a `app/build.gradle.kts`
> y ajusta `PlayerScreen.kt` para usar ese reproductor en vez de ExoPlayer.

## Arquitectura
```
MVVM + Clean Architecture
├── data/        ← modelos, Room, parser M3U, repositorio
├── di/          ← módulos Hilt
└── ui/
    ├── theme/   ← colores, tipografía, tema oscuro
    ├── navigation/
    ├── screens/ ← Home · Player · Favorites · Search · Settings
    └── components/ ← ChannelCard · CategoryTabs · EpgGuide
```
