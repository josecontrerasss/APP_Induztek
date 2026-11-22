# APP_Induztek

App Android (Kotlin, Jetpack Compose, Material 3) para registrar mediciones eléctricas en terreno y consultar protocolos por parte del cliente.

## Arquitectura
- MVVM con un ViewModel por área: Auth, Shell, Medicion, Tecnico y Cliente, creados con `AppViewModelProvider`.
- Inyección manual: `InduztekApplication` crea `AppContainer` (base de datos, repositorio, sesión y mensajes).
- Room 2.8 con migraciones escritas (versión 5, esquema exportado en `app/schemas`). Contraseñas con PBKDF2 y sal por usuario.
- Navegación por pestañas según el rol (técnico o cliente).

## Backend (pendiente)
La capa remota está lista en `data/remote` (Retrofit + OkHttp + Gson) para una API REST en Spring Boot. La URL se configura en `local.properties` con `API_BASE_URL=http://10.0.2.2:8080/api/` (por defecto apunta al localhost del PC desde el emulador).

| Método | Ruta | Uso |
|---|---|---|
| POST | auth/login | Inicio de sesión, devuelve token y usuario |
| POST | auth/registro | Registro de cliente |
| POST | auth/recuperar-clave | Envía el correo de recuperación (202 sin cuerpo) |
| PUT | usuarios/{id} | Actualiza el perfil |
| GET | clientes/{id}/instalaciones | Instalaciones del cliente |
| GET | clientes/{id}/protocolos | Protocolos del cliente |
| GET | instalaciones/{id}/equipos | Equipos de una instalación |
| POST | protocolos/sincronizar | Sube protocolos y mediciones pendientes |

## Funciones
- Técnico: formulario de mediciones con validación numérica, GPS, foto, humedad, equipos por instalación, sincronización local.
- Registro con repetir contraseña, mínimo 8 caracteres, 1 número y 1 carácter especial, y validación de RUT.
- Recuperación de contraseña conectada a la API (muestra aviso mientras el backend no exista).
- Cliente: sus centrales (3 visibles, el resto por búsqueda), edición de perfil, con detalle de equipos y protocolos, historial, certificado PDF y perfil de empresa.

## Compilar
1. Crear `local.properties` con `MAPS_API_KEY=tu_key`.
2. Abrir en Android Studio y ejecutar, o usar Docker:

```
MAPS_API_KEY=tu_key docker compose run --build android-build
```

El APK queda en la carpeta `salida/`.

## Usuarios de prueba
- tecnico@induztek.cl / tecnico123
- cliente@mineranorte.cl / cliente123
