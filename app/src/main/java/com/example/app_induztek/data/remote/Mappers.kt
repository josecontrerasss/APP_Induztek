package com.example.app_induztek.data.remote

import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.data.local.entities.InstalacionEntity
import com.example.app_induztek.data.local.entities.TipoEquipoEntity
import com.example.app_induztek.data.local.entities.EquipoEntity
import com.example.app_induztek.data.local.entities.VisitaEntity
import com.example.app_induztek.data.local.entities.ProtocoloEntity
import com.example.app_induztek.data.local.entities.MedicionEntity
import com.example.app_induztek.data.remote.dto.UsuarioDto
import com.example.app_induztek.data.remote.dto.InstalacionDto
import com.example.app_induztek.data.remote.dto.TipoEquipoDto
import com.example.app_induztek.data.remote.dto.EquipoDto
import com.example.app_induztek.data.remote.dto.VisitaDto
import com.example.app_induztek.data.remote.dto.ProtocoloDto
import com.example.app_induztek.data.remote.dto.MedicionDto

fun UsuarioDto.toEntity(hash: String, salt: String): UsuarioEntity = UsuarioEntity(
    id_usuario = idUsuario,
    id_rol = idRol,
    rut = rut,
    nombre = nombre,
    correo = correo,
    telefono = telefono.orEmpty(),
    password_hash = hash,
    password_salt = salt,
    activo = activo ?: true,
    giro = giro,
    contacto_nombre = contactoNombre,
    contacto_cargo = contactoCargo
)

fun InstalacionDto.toEntity(): InstalacionEntity = InstalacionEntity(
    id_instalacion = idInstalacion,
    id_usuario_cliente = idUsuarioCliente,
    codigo = codigo,
    nombre = nombre,
    ubicacion = ubicacion,
    observaciones_acceso = observacionesAcceso.orEmpty(),
    ultima_actividad = ultimaActividad.orEmpty()
)

fun TipoEquipoDto.toEntity(): TipoEquipoEntity = TipoEquipoEntity(
    id_tipo_equipo = id,
    nombre = nombre,
    clasificacion = clasificacion
)

fun EquipoDto.toEntity(): EquipoEntity = EquipoEntity(
    id_equipo = idEquipo,
    id_instalacion = idInstalacion,
    tag = tag,
    id_tipo_equipo = idTipoEquipo,
    marca = marca,
    modelo = modelo,
    numero_serie = numeroSerie,
    anio_fabricacion = anioFabricacion ?: 0,
    potencia_kva = potenciaKva ?: 0.0,
    tension_primaria_kv = tensionPrimariaKv ?: 0.0,
    tension_secundaria_kv = tensionSecundariaKv ?: 0.0,
    corriente_nominal_a = corrienteNominalA ?: 0.0,
    observaciones = observaciones.orEmpty()
)

fun VisitaDto.toEntity(): VisitaEntity = VisitaEntity(
    id_visita = idVisita,
    numero_ot = numeroOt,
    id_instalacion = idInstalacion,
    fecha = fecha,
    tipo_visita = tipoVisita,
    id_tecnico = idTecnico
)

fun ProtocoloDto.toEntity(idLocal: Int): ProtocoloEntity = ProtocoloEntity(
    id_protocolo = idLocal,
    numero = numero,
    id_visita = idVisita,
    id_equipo = idEquipo,
    id_usuario_registra = idUsuarioRegistra,
    fecha_hora_prueba = fechaHoraPrueba,
    temperatura_c = temperaturaC ?: 0.0,
    humedad_pct = humedadPct ?: 0.0,
    descripcion_mantenimiento = descripcionMantenimiento.orEmpty(),
    observaciones = observaciones.orEmpty(),
    estado = estado ?: "REGISTRADO_TERRENO",
    resultado = resultado,
    sincronizado = true,
    fotoPath = fotoPath
)

fun MedicionDto.toEntity(idProtocoloLocal: Int): MedicionEntity = MedicionEntity(
    id_medicion = 0,
    id_protocolo = idProtocoloLocal,
    parametro_codigo = parametroCodigo,
    parametro_nombre = parametroNombre,
    valor = valor,
    unidad = unidad,
    estado_alerta = estadoAlerta ?: "OK"
)

fun ProtocoloEntity.toDto(): ProtocoloDto = ProtocoloDto(
    idProtocolo = id_protocolo,
    numero = numero,
    idVisita = id_visita,
    idEquipo = id_equipo,
    idUsuarioRegistra = id_usuario_registra,
    fechaHoraPrueba = fecha_hora_prueba,
    temperaturaC = temperatura_c,
    humedadPct = humedad_pct,
    descripcionMantenimiento = descripcion_mantenimiento,
    observaciones = observaciones,
    estado = estado,
    resultado = resultado,
    sincronizado = sincronizado,
    fotoPath = fotoPath
)

fun MedicionEntity.toDto(): MedicionDto = MedicionDto(
    idMedicion = id_medicion,
    idProtocolo = id_protocolo,
    parametroCodigo = parametro_codigo,
    parametroNombre = parametro_nombre,
    valor = valor,
    unidad = unidad,
    estadoAlerta = estado_alerta
)
