package com.example.app_induztek.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.app_induztek.data.local.dao.InduztekDao
import com.example.app_induztek.data.local.entities.EquipoEntity
import com.example.app_induztek.data.local.entities.InstalacionEntity
import com.example.app_induztek.data.local.entities.MedicionEntity
import com.example.app_induztek.data.local.entities.ProtocoloEntity
import com.example.app_induztek.data.local.entities.RolEntity
import com.example.app_induztek.data.local.entities.TipoEquipoEntity
import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.data.local.entities.VisitaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        RolEntity::class,
        UsuarioEntity::class,
        InstalacionEntity::class,
        TipoEquipoEntity::class,
        EquipoEntity::class,
        VisitaEntity::class,
        ProtocoloEntity::class,
        MedicionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun induztekDao(): InduztekDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "induztek_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.induztekDao())
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val dao = database.induztekDao()
                    if (dao.countRoles() == 0) {
                        populateInitialData(dao)
                    }
                }
            }
        }

        private suspend fun populateInitialData(dao: InduztekDao) {

            val roles = listOf(
                RolEntity(id_rol = 1, codigo = "TECNICO", nombre = "P-01 Técnico de terreno"),
                RolEntity(id_rol = 2, codigo = "INGENIERO", nombre = "P-02 Ingeniero de pruebas"),
                RolEntity(id_rol = 3, codigo = "JEFE_TECNICO", nombre = "P-03 Jefe técnico"),
                RolEntity(id_rol = 4, codigo = "CLIENTE", nombre = "P-04 Cliente: cuenta empresa")
            )
            dao.insertRoles(roles)


            val usuarios = listOf(

                UsuarioEntity(
                    id_usuario = 1,
                    id_rol = 1,
                    rut = "16.782.910-4",
                    nombre = "Juan Pérez",
                    correo = "tecnico@induztek.cl",
                    password_hash = "tecnico123",
                    telefono = "+56 9 8765 4321",
                    activo = true
                ),

                UsuarioEntity(
                    id_usuario = 10,
                    id_rol = 4,
                    rut = "76.543.210-K",
                    nombre = "Minera del Norte SpA",
                    correo = "cliente@mineranorte.cl",
                    password_hash = "cliente123",
                    telefono = "+56 2 2345 6789",
                    giro = "Minería y Concentrado de Cobre",
                    contacto_nombre = "Carlos Mendoza",
                    contacto_cargo = "Jefe Mantenimiento Eléctrico",
                    activo = true
                )
            )
            dao.insertUsuarios(usuarios)


            val tipos = listOf(
                TipoEquipoEntity(id_tipo_equipo = 1, nombre = "Transformador", clasificacion = "PRIMARIO"),
                TipoEquipoEntity(id_tipo_equipo = 2, nombre = "Interruptor", clasificacion = "PRIMARIO"),
                TipoEquipoEntity(id_tipo_equipo = 3, nombre = "Celda MT", clasificacion = "PRIMARIO")
            )
            dao.insertTiposEquipo(tipos)


            val instalaciones = listOf(
                InstalacionEntity(
                    id_instalacion = 1,
                    id_usuario_cliente = 10,
                    codigo = "SE-IND-102",
                    nombre = "Planta Norte",
                    ubicacion = "Km 45 Ruta 5 Norte, Lampa",
                    observaciones_acceso = "Inducción básica de seguridad requerida al ingreso",
                    ultima_actividad = "15 may. 2026"
                ),
                InstalacionEntity(
                    id_instalacion = 2,
                    id_usuario_cliente = 10,
                    codigo = "SE-MDN-01",
                    nombre = "Planta Concentradora",
                    ubicacion = "Patio 110 kV, Edificio Concentradora",
                    observaciones_acceso = "EPP completo y permiso de trabajo en altura",
                    ultima_actividad = "15 may. 2026"
                ),
                InstalacionEntity(
                    id_instalacion = 3,
                    id_usuario_cliente = 10,
                    codigo = "SE-MDN-02",
                    nombre = "Planta Chancado",
                    ubicacion = "Sector Chancado Primario",
                    observaciones_acceso = "Coordinar con operador de sala de control",
                    ultima_actividad = "10 may. 2026"
                )
            )
            dao.insertInstalaciones(instalaciones)


            val equipos = listOf(
                EquipoEntity(
                    id_equipo = 1,
                    id_instalacion = 1,
                    tag = "Transformador T-01",
                    id_tipo_equipo = 1,
                    marca = "ABB",
                    modelo = "Resibloc 1000",
                    numero_serie = "ABB-TR-9982",
                    potencia_kva = 1000.0,
                    tension_primaria_kv = 13.2,
                    tension_secundaria_kv = 0.42,
                    corriente_nominal_a = 256.5
                ),
                EquipoEntity(
                    id_equipo = 2,
                    id_instalacion = 1,
                    tag = "Interruptor INT-01",
                    id_tipo_equipo = 2,
                    marca = "Schneider",
                    modelo = "Evolis 24kV",
                    numero_serie = "SCH-SW-4412",
                    potencia_kva = 0.0,
                    tension_primaria_kv = 24.0,
                    tension_secundaria_kv = 0.0,
                    corriente_nominal_a = 630.0
                ),
                EquipoEntity(
                    id_equipo = 3,
                    id_instalacion = 2,
                    tag = "Transformador T-02",
                    id_tipo_equipo = 1,
                    marca = "Siemens",
                    modelo = "Geafol 1500",
                    numero_serie = "SIE-TR-1123",
                    potencia_kva = 1500.0,
                    tension_primaria_kv = 23.0,
                    tension_secundaria_kv = 0.40,
                    corriente_nominal_a = 380.0
                )
            )
            dao.insertEquipos(equipos)

            val visitas = listOf(
                VisitaEntity(
                    id_visita = 1,
                    numero_ot = "OT-2026-1187",
                    id_instalacion = 1,
                    fecha = "15-05-2026",
                    tipo_visita = "PRUEBAS_Y_MANT"
                ),
                VisitaEntity(
                    id_visita = 2,
                    numero_ot = "OT-2026-1140",
                    id_instalacion = 2,
                    fecha = "10-05-2026",
                    tipo_visita = "SOLO_PRUEBAS"
                )
            )
            dao.insertVisitas(visitas)


            val protocolos = listOf(
                ProtocoloEntity(
                    id_protocolo = 1,
                    numero = "PT-2025-0008",
                    id_visita = 2,
                    id_equipo = 3,
                    id_usuario_registra = 1,
                    fecha_hora_prueba = "15 may. 2025, 09:15",
                    temperatura_c = 21.5,
                    humedad_pct = 42.0,
                    descripcion_mantenimiento = "Pruebas de aislación y relación de transformación.",
                    observaciones = "Equipo operando en condiciones normales. Parámetros conformes.",
                    estado = "FORMALIZADO",
                    resultado = "APROBADO",
                    sincronizado = true
                ),
                ProtocoloEntity(
                    id_protocolo = 2,
                    numero = "PT-2025-0007",
                    id_visita = 1,
                    id_equipo = 2,
                    id_usuario_registra = 1,
                    fecha_hora_prueba = "10 may. 2025, 14:30",
                    temperatura_c = 24.0,
                    humedad_pct = 40.0,
                    descripcion_mantenimiento = "Verificación de resistencia de contactos.",
                    observaciones = "Contactos dentro de norma IEEE.",
                    estado = "FORMALIZADO",
                    resultado = "APROBADO",
                    sincronizado = true
                )
            )
            dao.insertProtocolos(protocolos)


            val mediciones = listOf(
                MedicionEntity(
                    id_medicion = 1,
                    id_protocolo = 1,
                    parametro_codigo = "V_PRIM",
                    parametro_nombre = "Tensión Primario",
                    valor = "23000",
                    unidad = "V",
                    estado_alerta = "OK"
                ),
                MedicionEntity(
                    id_medicion = 2,
                    id_protocolo = 1,
                    parametro_codigo = "V_SEC",
                    parametro_nombre = "Tensión Secundario",
                    valor = "400",
                    unidad = "V",
                    estado_alerta = "OK"
                ),
                MedicionEntity(
                    id_medicion = 3,
                    id_protocolo = 1,
                    parametro_codigo = "I_NOM",
                    parametro_nombre = "Corriente",
                    valor = "378.2",
                    unidad = "A",
                    estado_alerta = "OK"
                ),
                MedicionEntity(
                    id_medicion = 4,
                    id_protocolo = 1,
                    parametro_codigo = "R_AISL",
                    parametro_nombre = "Resistencia Aislamiento",
                    valor = "> 1000",
                    unidad = "MΩ",
                    estado_alerta = "OK"
                ),
                MedicionEntity(
                    id_medicion = 5,
                    id_protocolo = 1,
                    parametro_codigo = "T_ACEITE",
                    parametro_nombre = "Temperatura Aceite",
                    valor = "41.5",
                    unidad = "°C",
                    estado_alerta = "OK"
                )
            )
            dao.insertMediciones(mediciones)
        }
    }
}