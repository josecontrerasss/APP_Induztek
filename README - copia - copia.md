# APP_Induztek
APP_Induztek es una aplicación de Android diseñada para la gestión de operaciones y la recopilación de datos en campo. Su propósito es facilitar el trabajo de técnicos, permitiéndoles gestionar tareas, sincronizar información y registrar mediciones de manera eficiente.

## Características Principales
- **Gestión de Operaciones:** Flujos de trabajo dedicados para el seguimiento de actividades técnicas.
- **Persistencia de Datos:** Uso de Room (SQLite) para asegurar el almacenamiento local y persistente de datos.
- **Arquitectura:** Adopta el patrón MVVM (Model-View-ViewModel) para garantizar una clara separación de responsabilidades.
- **Interfaz de Usuario:** Desarrollada con Jetpack Compose y Material 3 para ofrecer una interfaz moderna y responsiva.

##  Tecnologías
La aplicación utiliza las siguientes tecnologías clave:
- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Arquitectura:** MVVM
- **Persistencia:** Room Database
- **Navegación:** Jetpack Navigation

##  Estructura del Proyecto
La aplicación sigue una arquitectura en capas (Clean Architecture ligera):
- **`:app` (Módulo Principal):**
    - `ui/screens`: Contiene los composables y la interfaz de usuario (ej. `TecnicoMainScreen`).
    - `ui/viewmodel`: Gestionan la lógica de negocio y el estado de las pantallas (ej. `InduztekViewModel`).
    - `data/local`: Implementan la capa de datos, incluyendo entidades, el DAO y la configuración de la base de datos.
    - `MainActivity.kt`: Punto de entrada de la aplicación.


