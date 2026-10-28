package com.example.app_induztek.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = "roles", indices = [Index(value = ["codigo"], unique = true)])
data class RolEntity(
    @PrimaryKey(autoGenerate = true) val id_rol: Int = 0,
    val codigo: String,
    val nombre: String
)


@Entity(
    tableName = "usuarios",
    foreignKeys = [ForeignKey(entity = RolEntity::class, parentColumns = ["id_rol"], childColumns = ["id_rol"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index(value = ["rut"], unique = true), Index(value = ["correo"], unique = true), Index(value = ["id_rol"])]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id_usuario: Int = 0,
    val id_rol: Int,
    val rut: String,
    val nombre: String,
    val correo: String,
    val telefono: String = "",
    val password_hash: String = "123",
    val activo: Boolean = true,
    val giro: String? = null,
    val contacto_nombre: String? = null,
    val contacto_cargo: String? = null
)


@Entity(
    tableName = "instalaciones",
    foreignKeys = [ForeignKey(entity = UsuarioEntity::class, parentColumns = ["id_usuario"], childColumns = ["id_usuario_cliente"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index(value = ["id_usuario_cliente"])]
)
data class InstalacionEntity(
    @PrimaryKey(autoGenerate = true) val id_instalacion: Int = 0,
    val id_usuario_cliente: Int,
    val codigo: String,
    val nombre: String,
    val ubicacion: String,
    val observaciones_acceso: String = "",
    val ultima_actividad: String = "15 may. 2026"
)


@Entity(tableName = "tipos_equipo", indices = [Index(value = ["nombre"], unique = true)])
data class TipoEquipoEntity(
    @PrimaryKey(autoGenerate = true) val id_tipo_equipo: Int = 0,
    val nombre: String,
    val clasificacion: String
)


@Entity(
    tableName = "equipos",
    foreignKeys = [
        ForeignKey(entity = InstalacionEntity::class, parentColumns = ["id_instalacion"], childColumns = ["id_instalacion"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TipoEquipoEntity::class, parentColumns = ["id_tipo_equipo"], childColumns = ["id_tipo_equipo"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index(value = ["id_instalacion", "tag"], unique = true), Index(value = ["id_tipo_equipo"])]
)
data class EquipoEntity(
    @PrimaryKey(autoGenerate = true) val id_equipo: Int = 0,
    val id_instalacion: Int,
    val tag: String,
    val id_tipo_equipo: Int,
    val marca: String,
    val modelo: String,
    val numero_serie: String,
    val anio_fabricacion: Int = 2021,
    val potencia_kva: Double = 1000.0,
    val tension_primaria_kv: Double = 13.2,
    val tension_secundaria_kv: Double = 0.42,
    val corriente_nominal_a: Double = 256.5,
    val observaciones: String = ""
)


@Entity(
    tableName = "visitas",
    foreignKeys = [ForeignKey(entity = InstalacionEntity::class, parentColumns = ["id_instalacion"], childColumns = ["id_instalacion"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["numero_ot"], unique = true), Index(value = ["id_instalacion"])]
)
data class VisitaEntity(
    @PrimaryKey(autoGenerate = true) val id_visita: Int = 0,
    val numero_ot: String,
    val id_instalacion: Int,
    val fecha: String,
    val tipo_visita: String = "PRUEBAS_Y_MANT",
    val id_ingeniero_cargo: Int = 3
)


@Entity(
    tableName = "protocolos",
    foreignKeys = [
        ForeignKey(entity = VisitaEntity::class, parentColumns = ["id_visita"], childColumns = ["id_visita"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = EquipoEntity::class, parentColumns = ["id_equipo"], childColumns = ["id_equipo"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["numero"], unique = true), Index(value = ["id_equipo"])]
)
data class ProtocoloEntity(
    @PrimaryKey(autoGenerate = true) val id_protocolo: Int = 0,
    val numero: String,
    val id_visita: Int,
    val id_equipo: Int,
    val id_usuario_registra: Int,
    val fecha_hora_prueba: String,
    val temperatura_c: Double = 22.0,
    val humedad_pct: Double = 45.0,
    val descripcion_mantenimiento: String = "",
    val observaciones: String = "",
    val estado: String = "REGISTRADO_TERRENO",
    val resultado: String = "APROBADO",
    val sincronizado: Boolean = false
)


@Entity(
    tableName = "mediciones",
    foreignKeys = [ForeignKey(entity = ProtocoloEntity::class, parentColumns = ["id_protocolo"], childColumns = ["id_protocolo"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["id_protocolo"])]
)
data class MedicionEntity(
    @PrimaryKey(autoGenerate = true) val id_medicion: Int = 0,
    val id_protocolo: Int,
    val parametro_codigo: String,
    val parametro_nombre: String,
    val valor: String,
    val unidad: String,
    val estado_alerta: String = "OK"
)