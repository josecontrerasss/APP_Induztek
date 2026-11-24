# APP_Induztek

App Android (Kotlin, Jetpack Compose, Material 3) para registrar protocolos de pruebas eléctricas en terreno. El técnico mide y sincroniza, el supervisor revisa y aprueba, y el cliente consulta y descarga sus certificados en PDF.

## Roles
| Rol | Qué hace en la app |
|---|---|
| Técnico | Registra mediciones con validación, GPS y foto; funciona sin internet y sincroniza después. Registra instalaciones y equipos en terreno para las empresas que tiene asignadas. |
| Supervisor | Asigna técnicos a empresas y aprueba o rechaza los protocolos que llegan desde terreno. |
| Cliente | Ve sus instalaciones, equipos, protocolos y técnico asignado; descarga el certificado PDF de los protocolos aprobados; edita su perfil y su contraseña. |

## Arquitectura
- MVVM con un ViewModel por funcionalidad, creados con `AppViewModelProvider`.
- Inyección manual: `InduztekApplication` crea `AppContainer` (base de datos, repositorio, sesión y mensajes).
- Room 2.8 con un DAO por entidad y migraciones escritas (versión 7, esquemas exportados en `app/schemas`). Contraseñas locales con PBKDF2 y sal por usuario.
- Repositorio híbrido: primero consulta el backend y guarda una copia en Room. Si no hay conexión, el login y los datos funcionan con lo guardado en el teléfono.
- Retrofit + OkHttp + Gson con token JWT, guardado por usuario.
- Navegación según el rol: pestañas para técnico y cliente, pantalla propia para el supervisor.

## Backend
Los microservicios están en el repositorio [microservicios_induztek](https://github.com/josecontrerasss/microservicios_induztek) (Spring Boot, MySQL y Docker). Se levantan con:

```
docker compose up --build -d
```

La app se conecta al API Gateway en el puerto 8080. La URL se configura en `local.properties`:

```
API_BASE_URL=http://10.0.2.2:8080/api/
```


| Método | Ruta | Uso |
|---|---|---|
| POST | auth/login · auth/registro | Inicio de sesión y registro de cliente |
| POST | auth/recuperar-clave · auth/restablecer-clave | Código de 6 dígitos por correo y nueva contraseña |
| PUT | usuarios/{id} · usuarios/{id}/clave | Editar perfil y cambiar contraseña |
| GET | clientes/{id}/instalaciones · visitas · protocolos · tecnicos | Datos del cliente |
| GET | tecnicos/{id}/instalaciones · equipos · visitas · protocolos · empresas | Datos del técnico |
| POST | instalaciones · instalaciones/{id}/equipos | Registro en terreno |
| POST | protocolos/sincronizar | Envía los protocolos pendientes con sus mediciones |
| GET/POST/DELETE | usuarios · asignaciones | Supervisor: técnicos, empresas y asignaciones |
| GET/PUT | protocolos/pendientes · protocolos/{id}/aprobar · protocolos/{id}/rechazar | Supervisor: revisión de protocolos |

## Compilar
1. Levantar el backend (ver arriba) y crear `local.properties` con `API_BASE_URL` y, si se quiere ver el mapa, `MAPS_API_KEY=tu_key`.
2. Abrir en Android Studio y ejecutar, o generar el APK con Docker:

```
MAPS_API_KEY=tu_key docker compose run --build android-build
```

El APK queda en la carpeta `salida/`.

## Usuarios de prueba
| Rol | Correo | Contraseña |
|---|---|---|
| Técnico | tecnico@induztek.cl | Tecnico123! |
| Cliente | cliente@mineranorte.cl | Cliente123! |
| Supervisor | supervisor@induztek.cl | Supervisor123! |
