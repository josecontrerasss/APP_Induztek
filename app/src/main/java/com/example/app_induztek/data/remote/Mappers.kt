package com.example.app_induztek.data.remote

import com.example.app_induztek.data.local.entities.EquipoEntity
import com.example.app_induztek.data.local.entities.InstalacionEntity
import com.example.app_induztek.data.local.entities.MedicionEntity
import com.example.app_induztek.data.local.entities.ProtocoloEntity
import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.data.remote.dto.EquipoDto
import com.example.app_induztek.data.remote.dto.InstalacionDto
import com.example.app_induztek.data.remote.dto.MedicionDto
import com.example.app_induztek.data.remote.dto.ProtocoloDto
import com.example.app_induztek.data.remote.dto.UsuarioDto

fun UsuarioEntity.toDto(): UsuarioDto = UsuarioDto(
    idUsuario = id_usuario,
    idRol = id_rol,
    rut = rut,
    nombre = nombre,
    correo = correo,
    telefono = telefono,
    activo = activo,
    giro = giro,
    contactoNombre = contacto_nombre,
    contactoCargo = contacto_cargo
)

fun UsuarioDto.toEntity(hash: String = "", salt: String = ""): UsuarioEntity = UsuarioEntity(
    id_usuario = idUsuario,
    id_rol = idRol,
    rut = rut,
    nombre = nombre,
    correo = correo,
    telefono = telefono,
    password_hash = hash,
    password_salt = salt,
    activo = activo,
    giro = giro,
    contacto_nombre = contactoNombre,
    contacto_cargo = contactoCargo
)

fun InstalacionEntity.toDto(): InstalacionDto = InstalacionDto(
    idInstalacion = id_instalacion,
    idUsuarioCliente = id_usuario_cliente,
    codigo = codigo,
    nombre = nombre,
    ubicacion = ubicacion,
    observacionesAcceso = observaciones_acceso,
    ultimaActividad = ultima_actividad
)

fun InstalacionDto.toEntity(): InstalacionEntity = InstalacionEntity(
    id_instalacion = idInstalacion,
    id_usuario_cliente = idUsuarioCliente,
    codigo = codigo,
    nombre = nombre,
    ubicacion = ubicacion,
    observaciones_acceso = observacionesAcceso,
    ultima_actividad = ultimaActividad
)

fun EquipoEntity.toDto(): EquipoDto = EquipoDto(
    idEquipo = id_equipo,
    idInstalacion = id_instalacion,
    tag = tag,
    idTipoEquipo = id_tipo_equipo,
    marca = marca,
    modelo = modelo,
    numeroSerie = numero_serie,
    anioFabricacion = anio_fabricacion,
    potenciaKva = potencia_kva,
    tensionPrimariaKv = tension_primaria_kv,
    tensionSecundariaKv = tension_secundaria_kv,
    corrienteNominalA = corriente_nominal_a,
    observaciones = observaciones
)

fun EquipoDto.toEntity(): EquipoEntity = EquipoEntity(
    id_equipo = idEquipo,
    id_instalacion = idInstalacion,
    tag = tag,
    id_tipo_equipo = idTipoEquipo,
    marca = marca,
    modelo = modelo,
    numero_serie = numeroSerie,
    anio_fabricacion = anioFabricacion,
    potencia_kva = potenciaKva,
    tension_primaria_kv = tensionPrimariaKv,
    tension_secundaria_kv = tensionSecundariaKv,
    corriente_nominal_a = corrienteNominalA,
    observaciones = observaciones
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

fun ProtocoloDto.toEntity(): ProtocoloEntity = ProtocoloEntity(
    id_protocolo = idProtocolo,
    numero = numero,
    id_visita = idVisita,
    id_equipo = idEquipo,
    id_usuario_registra = idUsuarioRegistra,
    fecha_hora_prueba = fechaHoraPrueba,
    temperatura_c = temperaturaC,
    humedad_pct = humedadPct,
    descripcion_mantenimiento = descripcionMantenimiento,
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

fun MedicionDto.toEntity(): MedicionEntity = MedicionEntity(
    id_medicion = idMedicion,
    id_protocolo = idProtocolo,
    parametro_codigo = parametroCodigo,
    parametro_nombre = parametroNombre,
    valor = valor,
    unidad = unidad,
    estado_alerta = estadoAlerta
)
