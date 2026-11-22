package com.example.app_induztek.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.app_induztek.data.local.AppDatabase
import com.example.app_induztek.data.local.entities.EquipoEntity
import com.example.app_induztek.data.local.entities.InstalacionEntity
import com.example.app_induztek.data.local.entities.MedicionEntity
import com.example.app_induztek.data.local.entities.ProtocoloEntity
import com.example.app_induztek.data.local.entities.TipoEquipoEntity
import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.data.remote.dto.AsignacionCrearDto
import com.example.app_induztek.data.remote.dto.AsignacionDto
import com.example.app_induztek.data.remote.dto.EquipoCrearDto
import com.example.app_induztek.data.remote.dto.EquipoDto
import com.example.app_induztek.data.remote.dto.InstalacionCrearDto
import com.example.app_induztek.data.remote.dto.InstalacionDto
import com.example.app_induztek.data.remote.ApiResult
import com.example.app_induztek.data.remote.RemoteDataSource
import com.example.app_induztek.data.remote.TokenStore
import com.example.app_induztek.data.remote.dto.PerfilUpdateDto
import com.example.app_induztek.data.remote.dto.ProtocoloDto
import com.example.app_induztek.data.remote.dto.ProtocoloSyncDto
import com.example.app_induztek.data.remote.dto.RegistroRequestDto
import com.example.app_induztek.data.remote.dto.UsuarioDto
import com.example.app_induztek.data.remote.toDto
import com.example.app_induztek.data.remote.toEntity
import com.example.app_induztek.security.PasswordHasher
import com.example.app_induztek.security.PasswordPolicy
import com.example.app_induztek.security.RutValidator
import kotlinx.coroutines.flow.Flow

class InduztekRepository(
    private val database: AppDatabase,
    private val remote: RemoteDataSource,
    private val tokenStore: TokenStore
) {

    private val rolDao = database.rolDao()
    private val usuarioDao = database.usuarioDao()
    private val tipoEquipoDao = database.tipoEquipoDao()
    private val instalacionDao = database.instalacionDao()
    private val equipoDao = database.equipoDao()
    private val visitaDao = database.visitaDao()
    private val protocoloDao = database.protocoloDao()
    private val medicionDao = database.medicionDao()

    suspend fun autenticar(correo: String, password: String): ResultadoLogin {
        val correoNormalizado = correo.trim().lowercase()
        return when (val resultado = remote.login(correoNormalizado, password)) {
            is ApiResult.Success -> {
                val usuario = guardarUsuarioRemoto(resultado.data.usuario, password)
                tokenStore.guardar(usuario.id_usuario, resultado.data.token)
                descargarDatos(usuario.id_usuario, resultado.data.rol)
                ResultadoLogin.Exito(usuario, resultado.data.rol, sinConexion = false)
            }
            is ApiResult.HttpError -> when {
                resultado.code == 401 -> ResultadoLogin.CredencialesInvalidas
                resultado.code >= 500 -> autenticarSinConexion(correoNormalizado, password)
                else -> ResultadoLogin.Error(resultado.mensaje)
            }
            is ApiResult.NetworkError -> autenticarSinConexion(correoNormalizado, password)
        }
    }

    private suspend fun autenticarSinConexion(correo: String, password: String): ResultadoLogin {
        val usuario = usuarioDao.getUsuarioPorCorreo(correo)
        if (usuario == null || !usuario.activo) return ResultadoLogin.SinConexion
        if (!PasswordHasher.verify(password, usuario.password_salt, usuario.password_hash)) {
            return ResultadoLogin.CredencialesInvalidas
        }
        tokenStore.restaurar(usuario.id_usuario)
        val rol = usuarioDao.getRolCodigoByUsuario(usuario.id_usuario) ?: ROL_TECNICO
        return ResultadoLogin.Exito(usuario, rol, sinConexion = true)
    }

    suspend fun getRolId(codigo: String): Int? = rolDao.getRolIdPorCodigo(codigo)

    suspend fun registrarCliente(cliente: UsuarioEntity, passwordPlano: String): ResultadoRegistro {
        if (!PasswordPolicy.evaluar(passwordPlano).valida) {
            return ResultadoRegistro.Error("La contraseña no cumple los requisitos.")
        }
        if (!RutValidator.esValido(cliente.rut)) {
            return ResultadoRegistro.Error("RUT inválido.")
        }
        val solicitud = RegistroRequestDto(
            rut = RutValidator.formatear(cliente.rut),
            nombre = cliente.nombre,
            correo = cliente.correo.trim().lowercase().replace(" ", ""),
            telefono = cliente.telefono.ifBlank { null },
            password = passwordPlano
        )
        return when (val resultado = remote.registro(solicitud)) {
            is ApiResult.Success -> {
                val usuario = guardarUsuarioRemoto(resultado.data, passwordPlano)
                ResultadoRegistro.Exito(usuario.id_usuario.toLong())
            }
            is ApiResult.HttpError -> when {
                resultado.code == 409 -> ResultadoRegistro.RutOCorreoDuplicado
                resultado.code >= 500 -> ResultadoRegistro.Error(MENSAJE_SIN_CONEXION)
                else -> ResultadoRegistro.Error(resultado.mensaje)
            }
            is ApiResult.NetworkError -> ResultadoRegistro.Error(MENSAJE_SIN_CONEXION)
        }
    }

    suspend fun solicitarRecuperacionClave(correo: String): ResultadoRecuperacion {
        return when (val resultado = remote.recuperarClave(correo.trim().lowercase())) {
            is ApiResult.Success<*> -> ResultadoRecuperacion.Enviado
            is ApiResult.HttpError -> when {
                resultado.code >= 500 -> ResultadoRecuperacion.ServicioNoDisponible
                else -> ResultadoRecuperacion.Error(resultado.mensaje)
            }
            is ApiResult.NetworkError -> ResultadoRecuperacion.ServicioNoDisponible
        }
    }

    suspend fun restablecerClave(correo: String, codigo: String, claveNueva: String): ResultadoRestablecer {
        val correoNormalizado = correo.trim().lowercase()
        return when (val resultado = remote.restablecerClave(correoNormalizado, codigo.trim(), claveNueva)) {
            is ApiResult.Success -> {
                usuarioDao.getUsuarioPorCorreo(correoNormalizado)?.let { actualizarClaveLocal(it, claveNueva) }
                ResultadoRestablecer.Exito
            }
            is ApiResult.HttpError ->
                if (resultado.code >= 500) ResultadoRestablecer.ServicioNoDisponible
                else ResultadoRestablecer.CodigoInvalido(resultado.mensaje)
            is ApiResult.NetworkError -> ResultadoRestablecer.ServicioNoDisponible
        }
    }

    suspend fun getUsuario(id: Int): UsuarioEntity? = usuarioDao.getUsuarioPorId(id)

    suspend fun actualizarPerfil(usuario: UsuarioEntity): ResultadoPerfil {
        val actual = usuarioDao.getUsuarioPorId(usuario.id_usuario)
            ?: return ResultadoPerfil.Error("Usuario no encontrado.")
        val solicitud = PerfilUpdateDto(
            nombre = usuario.nombre,
            correo = usuario.correo.trim().lowercase(),
            telefono = usuario.telefono.ifBlank { null },
            giro = usuario.giro,
            contactoNombre = usuario.contacto_nombre,
            contactoCargo = usuario.contacto_cargo
        )
        return when (val resultado = remote.actualizarPerfil(actual.id_usuario, solicitud)) {
            is ApiResult.Success -> ResultadoPerfil.Exito(guardarUsuarioRemoto(resultado.data, null, actual))
            is ApiResult.HttpError -> when {
                resultado.code == 409 -> ResultadoPerfil.CorreoDuplicado
                resultado.code == 401 -> ResultadoPerfil.Error(MENSAJE_SESION_EXPIRADA)
                resultado.code >= 500 -> ResultadoPerfil.Error(MENSAJE_SIN_CONEXION)
                else -> ResultadoPerfil.Error(resultado.mensaje)
            }
            is ApiResult.NetworkError -> ResultadoPerfil.Error(MENSAJE_SIN_CONEXION)
        }
    }

    suspend fun cambiarClave(idUsuario: Int, claveActual: String, claveNueva: String): ResultadoCambioClave {
        val usuario = usuarioDao.getUsuarioPorId(idUsuario)
            ?: return ResultadoCambioClave.Error("Usuario no encontrado.")
        if (claveActual == claveNueva) {
            return ResultadoCambioClave.MismaClave
        }
        if (!PasswordPolicy.evaluar(claveNueva).valida) {
            return ResultadoCambioClave.Error("La nueva contraseña no cumple los requisitos.")
        }
        return when (val resultado = remote.cambiarClave(idUsuario, claveActual, claveNueva)) {
            is ApiResult.Success -> {
                actualizarClaveLocal(usuario, claveNueva)
                ResultadoCambioClave.Exito
            }
            is ApiResult.HttpError -> when {
                resultado.code == 400 && resultado.mensaje.contains("actual") -> ResultadoCambioClave.ClaveActualIncorrecta
                resultado.code == 401 -> ResultadoCambioClave.Error(MENSAJE_SESION_EXPIRADA)
                resultado.code >= 500 -> ResultadoCambioClave.Error(MENSAJE_SIN_CONEXION)
                else -> ResultadoCambioClave.Error(resultado.mensaje)
            }
            is ApiResult.NetworkError -> ResultadoCambioClave.Error(MENSAJE_SIN_CONEXION)
        }
    }

    fun getInstalacionById(id: Int): Flow<InstalacionEntity?> = instalacionDao.getInstalacionById(id)

    fun getInstalacionesByTecnico(tecnicoId: Int): Flow<List<InstalacionEntity>> =
        instalacionDao.getInstalacionesByTecnico(tecnicoId)

    fun getEquiposByTecnico(tecnicoId: Int): Flow<List<EquipoEntity>> =
        equipoDao.getEquiposByTecnico(tecnicoId)

    fun getProtocolosByTecnico(tecnicoId: Int): Flow<List<ProtocoloEntity>> =
        protocoloDao.getProtocolosByTecnico(tecnicoId)

    fun getMedicionesByTecnico(tecnicoId: Int): Flow<List<MedicionEntity>> =
        medicionDao.getMedicionesByTecnico(tecnicoId)

    fun countPendientesByTecnico(tecnicoId: Int): Flow<Int> =
        protocoloDao.countPendientesByTecnico(tecnicoId)

    fun getInstalacionesByCliente(clienteId: Int): Flow<List<InstalacionEntity>> {
        return instalacionDao.getInstalacionesByCliente(clienteId)
    }

    fun getEquipoById(id: Int): Flow<EquipoEntity?> = equipoDao.getEquipoById(id)

    fun getEquiposByInstalacion(instalacionId: Int): Flow<List<EquipoEntity>> {
        return equipoDao.getEquiposByInstalacion(instalacionId)
    }

    fun getProtocolosCliente(clienteId: Int): Flow<List<ProtocoloEntity>> {
        return protocoloDao.getProtocolosByCliente(clienteId)
    }

    fun getMedicionesByProtocolo(protocoloId: Int): Flow<List<MedicionEntity>> {
        return medicionDao.getMedicionesByProtocolo(protocoloId)
    }

    suspend fun guardarMedicionEnTerreno(
        protocolo: ProtocoloEntity,
        mediciones: List<MedicionEntity>
    ): Long = protocoloDao.guardarProtocoloConMediciones(protocolo, mediciones)

    suspend fun sincronizarProtocolos(tecnicoId: Int): ResultadoSync {
        val pendientes = protocoloDao.getPendientesByTecnico(tecnicoId)
        if (pendientes.isEmpty()) return ResultadoSync.NadaPendiente
        if (tokenStore.token == null) return ResultadoSync.SesionExpirada
        val lote = pendientes.map { protocolo ->
            ProtocoloSyncDto(
                protocolo = protocolo.toDto(),
                mediciones = medicionDao.getMedicionesDeProtocolo(protocolo.id_protocolo).map { it.toDto() }
            )
        }
        return when (val resultado = remote.sincronizarProtocolos(lote)) {
            is ApiResult.Success -> {
                val aceptados = resultado.data.aceptados.orEmpty()
                if (aceptados.isNotEmpty()) protocoloDao.marcarSincronizados(aceptados)
                ResultadoSync.Exito(aceptados.size, resultado.data.rechazados.orEmpty())
            }
            is ApiResult.HttpError -> when {
                resultado.code == 401 -> ResultadoSync.SesionExpirada
                resultado.code >= 500 -> ResultadoSync.SinConexion
                else -> ResultadoSync.Error(resultado.mensaje)
            }
            is ApiResult.NetworkError -> ResultadoSync.SinConexion
        }
    }

    suspend fun getUltimaVisitaIdDeTecnico(instalacionId: Int, tecnicoId: Int): Int? =
        visitaDao.getUltimaVisitaIdDeTecnico(instalacionId, tecnicoId)

    fun getTiposEquipo(): Flow<List<TipoEquipoEntity>> = tipoEquipoDao.getTiposEquipo()

    suspend fun usuariosPorRol(rol: String): ResultadoRemoto<List<UsuarioDto>> = remote.usuariosPorRol(rol).comoResultado()

    suspend fun asignaciones(): ResultadoRemoto<List<AsignacionDto>> = remote.asignaciones().comoResultado()

    suspend fun crearAsignacion(cliente: UsuarioDto, tecnico: UsuarioDto): ResultadoRemoto<AsignacionDto> =
        remote.crearAsignacion(
            AsignacionCrearDto(cliente.idUsuario, cliente.nombre, tecnico.idUsuario, tecnico.nombre)
        ).comoResultado()

    suspend fun eliminarAsignacion(id: Int): ResultadoRemoto<Unit> = remote.eliminarAsignacion(id).comoResultado()

    suspend fun empresasAsignadas(idTecnico: Int): ResultadoRemoto<List<AsignacionDto>> =
        remote.empresasTecnico(idTecnico).comoResultado()

    suspend fun registrarInstalacion(idTecnico: Int, solicitud: InstalacionCrearDto): ResultadoRemoto<InstalacionDto> {
        val resultado = remote.crearInstalacion(solicitud).comoResultado()
        if (resultado is ResultadoRemoto.Exito) actualizarDatosTecnico(idTecnico)
        return resultado
    }

    suspend fun registrarEquipo(idInstalacion: Int, solicitud: EquipoCrearDto): ResultadoRemoto<EquipoDto> {
        val resultado = remote.crearEquipo(idInstalacion, solicitud).comoResultado()
        if (resultado is ResultadoRemoto.Exito) equipoDao.upsertEquipos(listOf(resultado.datos.toEntity()))
        return resultado
    }

    suspend fun actualizarDatosTecnico(idTecnico: Int) {
        if (tokenStore.token == null) return
        try {
            remote.tiposEquipo().datos()?.let { tipos -> tipoEquipoDao.upsertTiposEquipo(tipos.map { it.toEntity() }) }
            descargarDatosTecnico(idTecnico)
        } catch (e: SQLiteConstraintException) {
            return
        }
    }

    private fun <T> ApiResult<T>.comoResultado(): ResultadoRemoto<T> = when (this) {
        is ApiResult.Success -> ResultadoRemoto.Exito(data)
        is ApiResult.HttpError -> ResultadoRemoto.Error(
            when {
                code == 401 -> MENSAJE_SESION_EXPIRADA
                code >= 500 -> MENSAJE_SIN_CONEXION
                else -> mensaje
            }
        )
        is ApiResult.NetworkError -> ResultadoRemoto.Error(MENSAJE_SIN_CONEXION)
    }

    private suspend fun descargarDatos(idUsuario: Int, rol: String) {
        try {
            remote.tiposEquipo().datos()?.let { tipos -> tipoEquipoDao.upsertTiposEquipo(tipos.map { it.toEntity() }) }
            when (rol) {
                ROL_CLIENTE -> descargarDatosCliente(idUsuario)
                ROL_TECNICO -> descargarDatosTecnico(idUsuario)
            }
        } catch (e: SQLiteConstraintException) {
            return
        }
    }

    private suspend fun descargarDatosCliente(idCliente: Int) {
        val instalaciones = remote.instalacionesCliente(idCliente).datos() ?: return
        instalacionDao.upsertInstalaciones(instalaciones.map { it.toEntity() })
        instalaciones.forEach { instalacion ->
            remote.equiposInstalacion(instalacion.idInstalacion).datos()
                ?.let { equipos -> equipoDao.upsertEquipos(equipos.map { it.toEntity() }) }
        }
        remote.visitasCliente(idCliente).datos()?.let { visitas ->
            visitaDao.upsertVisitas(visitas.map { visita ->
                val tecnicoLocal = visita.idTecnico?.takeIf { usuarioDao.getUsuarioPorId(it) != null }
                visita.toEntity().copy(id_tecnico = tecnicoLocal)
            })
        }
        remote.protocolosCliente(idCliente).datos()?.let { guardarProtocolosRemotos(it) }
    }

    private suspend fun descargarDatosTecnico(idTecnico: Int) {
        val instalaciones = remote.instalacionesTecnico(idTecnico).datos() ?: return
        asegurarClientesLocales(instalaciones.map { it.idUsuarioCliente }.distinct())
        instalacionDao.upsertInstalaciones(instalaciones.map { it.toEntity() })
        remote.equiposTecnico(idTecnico).datos()?.let { equipos -> equipoDao.upsertEquipos(equipos.map { it.toEntity() }) }
        remote.visitasTecnico(idTecnico).datos()?.let { visitas -> visitaDao.upsertVisitas(visitas.map { it.toEntity() }) }
        remote.protocolosTecnico(idTecnico).datos()?.let { guardarProtocolosRemotos(it) }
    }

    private suspend fun guardarProtocolosRemotos(protocolos: List<ProtocoloDto>) {
        protocolos.forEach { protocolo ->
            val local = protocoloDao.getPorNumero(protocolo.numero)
            if (local != null) {
                if (!local.sincronizado) protocoloDao.updateProtocolo(local.copy(sincronizado = true))
                return@forEach
            }
            val detalle = remote.detalleProtocolo(protocolo.idProtocolo).datos() ?: return@forEach
            try {
                protocoloDao.guardarProtocoloConMediciones(
                    protocolo.toEntity(idLocal = 0),
                    detalle.mediciones.orEmpty().map { it.toEntity(idProtocoloLocal = 0) }
                )
            } catch (e: SQLiteConstraintException) {
                return@forEach
            }
        }
    }

    private suspend fun asegurarClientesLocales(idsCliente: List<Int>) {
        val rolCliente = rolDao.getRolIdPorCodigo(ROL_CLIENTE) ?: return
        idsCliente.filter { usuarioDao.getUsuarioPorId(it) == null }.forEach { id ->
            usuarioDao.upsertUsuario(
                UsuarioEntity(
                    id_usuario = id,
                    id_rol = rolCliente,
                    rut = "SIN-RUT-$id",
                    nombre = "Cliente $id",
                    correo = "cliente$id@sin-datos.local",
                    activo = false
                )
            )
        }
    }

    private suspend fun guardarUsuarioRemoto(
        dto: UsuarioDto,
        password: String?,
        anterior: UsuarioEntity? = null
    ): UsuarioEntity {
        val salt = if (password != null) PasswordHasher.newSalt() else anterior?.password_salt.orEmpty()
        val hash = if (password != null) PasswordHasher.hash(password, salt) else anterior?.password_hash.orEmpty()
        val usuario = dto.toEntity(hash, salt)
        try {
            usuarioDao.borrarOtrosConCorreo(usuario.correo, usuario.id_usuario)
        } catch (e: SQLiteConstraintException) {
            return usuario
        }
        usuarioDao.upsertUsuario(usuario)
        return usuario
    }

    private suspend fun actualizarClaveLocal(usuario: UsuarioEntity, claveNueva: String) {
        val salt = PasswordHasher.newSalt()
        usuarioDao.updateUsuario(usuario.copy(password_hash = PasswordHasher.hash(claveNueva, salt), password_salt = salt))
    }

    private fun <T> ApiResult<T>.datos(): T? = (this as? ApiResult.Success)?.data

    companion object {
        const val ROL_CLIENTE = "CLIENTE"
        const val ROL_TECNICO = "TECNICO"
        const val ROL_SUPERVISOR = "SUPERVISOR"
        const val MENSAJE_SIN_CONEXION = "No hay conexión con el servidor. Revisa tu internet e inténtalo de nuevo."
        const val MENSAJE_SESION_EXPIRADA = "Tu sesión expiró. Cierra sesión y vuelve a entrar con internet."
    }
}
