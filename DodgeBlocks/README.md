# Dodge Blocks (Android / Jetpack Compose)

Juego Android completo listo para abrir en **Android Studio** y ejecutar en emulador/dispositivo.

## Requisitos
- Android Studio reciente (AGP **8.2.x** compatible).
- SDK **34** instalado.
- `minSdk 24`, `target/compileSdk 34`.
- Java **17** (Android Studio ya lo gestiona por defecto).

## Ejecutar (Android Studio)
1. **Open** → selecciona la carpeta `DodgeBlocks/`.
2. Espera a que termine **Gradle Sync**.
3. Selecciona un emulador o dispositivo.
4. **Run ▶ app**.

> No usa red, no requiere claves, no necesita permisos extra (solo `VIBRATE`).

## Controles
- **En partida**: mueve el jugador (círculo) arrastrando el dedo (**drag**).
- **Pausa**: botón de pausa (arriba a la derecha).
- **Back**:
  - Si estás jugando → pausa.
  - Si estás en pausa o Game Over → vuelve al menú.

## Pantallas
### 1) Menú
- Título del juego.
- **High-score**.
- **Top-5** de puntuaciones persistente.
- Botones: **Jugar**, **Ajustes**.

### 2) Juego
- Canvas con jugador (círculo) y bloques (cuadrados) cayendo desde arriba.
- Score = **tiempo sobrevivido en segundos redondeados**.
- Pausa manual + **pausa automática** al ir la app a background.
- Overlay de **Game Over** con **Reintentar** y **Menú**.
- **Edge-to-edge inmersivo** (system bars ocultas; reaparecen temporalmente con swipe).
- Pantalla encendida durante la partida (`FLAG_KEEP_SCREEN_ON`).

### 3) Ajustes
Persistentes vía **DataStore Preferences**:
- `soundEnabled` (bool): sonido SOLO con efectos del sistema (`playSoundEffect`).
- `vibrationEnabled` (bool): haptics al colisionar (`LocalHapticFeedback`).
- `difficulty` (EASY/NORMAL/HARD): intervalo de spawn, tamaños, velocidades.
- `skin` (CLASSIC/NEON/PASTEL): paletas jugador/bloques/fondo.
- Toggle opcional **FPS overlay** (no persistente).

## Features implementadas
- Movimiento por drag.
- Spawn de bloques con velocidad aleatoria.
- **Colisión círculo vs rectángulo** correcta (closest-point + radio).
- Dificultad progresiva: cada ~30s sube multiplicador de velocidad y aumenta ligeramente el spawn rate.
- Leaderboard persistente: **Top-5** ordenado desc y **High-score**.
- Haptics al colisionar (si está activado).
- Sonido con efectos del sistema (sin `raw/`, sin binarios).
- Portrait lock + edge-to-edge inmersivo + keep-screen-on + pausa por lifecycle.

## Estructura del proyecto
- `app/src/main/java/com/example/dodgeblocks/MainActivity.kt`  
  UI Compose + navegación simple (Menu/Game/Settings) + loop y overlays.
- `app/src/main/java/com/example/dodgeblocks/game/GameLogic.kt`  
  Estado del mundo, loop, spawn, progresión de dificultad y colisiones.
- `app/src/main/java/com/example/dodgeblocks/data/Prefs.kt`  
  DataStore Preferences + leaderboard Top-5 (JSON) + high-score.
- `app/src/main/java/com/example/dodgeblocks/data/PrefsCompose.kt`  
  Helpers Compose (`rememberPrefs`, `rememberPrefsState`).
- `app/src/main/java/com/example/dodgeblocks/ui/Theme.kt`  
  Tema Material3.
- `app/src/main/res/drawable/ic_launcher.xml`  
  Icono vectorial simple (sin mipmap).
- `app/src/main/AndroidManifest.xml`  
  Solo permiso `VIBRATE`, orientación portrait.

## Notas de compatibilidad
- Sin dependencias de red.
- Sin recursos binarios (sonido usa efectos del sistema).
- Permisos mínimos: **solo** `VIBRATE`.
