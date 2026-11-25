package com.example.app_induztek.data.remote.dto

data class LoginRequestDto(
    val correo: String,
    val password: String
)

data class AuthResponseDto(
    val token: String,
    val rol: String,
    val usuario: UsuarioDto
)

data class RegistroRequestDto(
    val rut: String,
    val nombre: String,
    val correo: String,
    val telefono: String?,
    val password: String,
    val giro: String? = null,
    val contactoNombre: String? = null,
    val contactoCargo: String? = null
)

data class RecuperarClaveRequestDto(
    val correo: String
)

data class RestablecerClaveRequestDto(
    val correo: String,
    val codigo: String,
    val claveNueva: String
)

data class CambiarClaveRequestDto(
    val claveActual: String,
    val claveNueva: String
)

data class UsuarioDto(
    val idUsuario: Int,
    val idRol: Int,
    val rut: String,
    val nombre: String,
    val correo: String,
    val telefono: String?,
    val activo: Boolean?,
    val giro: String?,
    val contactoNombre: String?,
    val contactoCargo: String?
)

data class PerfilUpdateDto(
    val nombre: String,
    val correo: String,
    val telefono: String?,
    val giro: String?,
    val contactoNombre: String?,
    val contactoCargo: String?
)

data class InstalacionDto(
    val idInstalacion: Int,
    val idUsuarioCliente: Int,
    val codigo: String,
    val nombre: String,
    val ubicacion: String,
    val observacionesAcceso: String?,
    val ultimaActividad: String?
)

data class TipoEquipoDto(
    val id: Int,
    val nombre: String,
    val clasificacion: String
)

data class EquipoDto(
    val idEquipo: Int,
    val idInstalacion: Int,
    val tag: String,
    val idTipoEquipo: Int,
    val marca: String,
    val modelo: String,
    val numeroSerie: String,
    val anioFabricacion: Int?,
    val potenciaKva: Double?,
    val tensionPrimariaKv: Double?,
    val tensionSecundariaKv: Double?,
    val corrienteNominalA: Double?,
    val observaciones: String?
)

data class VisitaDto(
    val idVisita: Int,
    val numeroOt: String,
    val idInstalacion: Int,
    val fecha: String,
    val tipoVisita: String,
    val idTecnico: Int?
)

data class ProtocoloDto(
    val idProtocolo: Int,
    val numero: String,
    val idVisita: Int,
    val idEquipo: Int,
    val idUsuarioRegistra: Int,
    val fechaHoraPrueba: String,
    val temperaturaC: Double?,
    val humedadPct: Double?,
    val descripcionMantenimiento: String?,
    val observaciones: String?,
    val estado: String?,
    val resultado: String,
    val sincronizado: Boolean?,
    val fotoPath: String?,
    val idCliente: Int? = null
)

data class MedicionDto(
    val idMedicion: Int,
    val idProtocolo: Int,
    val parametroCodigo: String,
    val parametroNombre: String,
    val valor: String,
    val unidad: String,
    val estadoAlerta: String?
)

data class ProtocoloSyncDto(
    val protocolo: ProtocoloDto,
    val mediciones: List<MedicionDto>?
)

data class SyncResponseDto(
    val sincronizados: Int,
    val aceptados: List<String>?,
    val rechazados: List<String>?
)

data class AsignacionDto(
    val idAsignacion: Int,
    val idCliente: Int,
    val nombreCliente: String,
    val idTecnico: Int,
    val nombreTecnico: String,
    val fechaAsignacion: String?
)

data class TecnicoCrearDto(
    val rut: String,
    val nombre: String,
    val apellido: String,
    val telefono: String,
    val correo: String
)

data class TecnicoCreadoDto(
    val tecnico: UsuarioDto,
    val claveTemporal: String
)

data class AsignacionCrearDto(
    val idCliente: Int,
    val nombreCliente: String,
    val idTecnico: Int,
    val nombreTecnico: String
)

data class InstalacionCrearDto(
    val idCliente: Int,
    val codigo: String,
    val nombre: String,
    val ubicacion: String,
    val observacionesAcceso: String?
)

data class EquipoCrearDto(
    val tag: String,
    val idTipoEquipo: Int,
    val marca: String,
    val modelo: String,
    val numeroSerie: String,
    val anioFabricacion: Int?,
    val potenciaKva: Double?,
    val tensionPrimariaKv: Double?,
    val tensionSecundariaKv: Double?,
    val corrienteNominalA: Double?,
    val observaciones: String?
)
