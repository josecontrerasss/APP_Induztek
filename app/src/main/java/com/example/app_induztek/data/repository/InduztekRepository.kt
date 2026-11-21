package com.example.app_induztek.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.app_induztek.data.local.AppDatabase
import com.example.app_induztek.data.local.entities.EquipoEntity
import com.example.app_induztek.data.local.entities.InstalacionEntity
import com.example.app_induztek.data.local.entities.MedicionEntity
import com.example.app_induztek.data.local.entities.ProtocoloEntity
import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.data.remote.ApiResult
import com.example.app_induztek.data.remote.RemoteDataSource
import com.example.app_induztek.security.PasswordHasher
import com.example.app_induztek.security.PasswordPolicy
import com.example.app_induztek.security.RutValidator
import kotlinx.coroutines.flow.Flow

class InduztekRepository(
    private val database: AppDatabase,
    private val remote: RemoteDataSource
) {

    private val rolDao = database.rolDao()
    private val usuarioDao = database.usuarioDao()
    private val instalacionDao = database.instalacionDao()
    private val equipoDao = database.equipoDao()
    private val visitaDao = database.visitaDao()
    private val protocoloDao = database.protocoloDao()
    private val medicionDao = database.medicionDao()

    suspend fun autenticar(correo: String, password: String): UsuarioEntity? {
        val usuario = usuarioDao.getUsuarioPorCorreo(correo) ?: return null
        if (!usuario.activo) return null
        return if (PasswordHasher.verify(password, usuario.password_salt, usuario.password_hash)) usuario else null
    }

    suspend fun getRolDeUsuario(usuarioId: Int): String? = usuarioDao.getRolCodigoByUsuario(usuarioId)

    suspend fun getRolId(codigo: String): Int? = rolDao.getRolIdPorCodigo(codigo)

    suspend fun registrarCliente(cliente: UsuarioEntity, passwordPlano: String): ResultadoRegistro {
        if (!PasswordPolicy.evaluar(passwordPlano).valida) {
            return ResultadoRegistro.Error("La contraseña no cumple los requisitos.")
        }
        if (!RutValidator.esValido(cliente.rut)) {
            return ResultadoRegistro.Error("RUT inválido.")
        }
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash(passwordPlano, salt)
        val nuevo = cliente.copy(
            rut = RutValidator.formatear(cliente.rut),
            correo = cliente.correo.trim().lowercase().replace(" ", ""),
            password_hash = hash,
            password_salt = salt
        )
        return try {
            val id = usuarioDao.insertUsuario(nuevo)
            ResultadoRegistro.Exito(id)
        } catch (e: SQLiteConstraintException) {
            ResultadoRegistro.RutOCorreoDuplicado
        }
    }

    suspend fun solicitarRecuperacionClave(correo: String): ResultadoRecuperacion {
        return when (val resultado = remote.recuperarClave(correo.trim().lowercase())) {
            is ApiResult.Success<*> -> ResultadoRecuperacion.Enviado
            is ApiResult.HttpError -> when {
                resultado.code == 404 -> ResultadoRecuperacion.Enviado
                resultado.code >= 500 -> ResultadoRecuperacion.ServicioNoDisponible
                else -> ResultadoRecuperacion.Error(resultado.mensaje)
            }
            is ApiResult.NetworkError -> ResultadoRecuperacion.ServicioNoDisponible
        }
    }

    suspend fun getUsuario(id: Int): UsuarioEntity? = usuarioDao.getUsuarioPorId(id)

    suspend fun actualizarPerfil(usuario: UsuarioEntity): ResultadoPerfil {
        val actual = usuarioDao.getUsuarioPorId(usuario.id_usuario)
            ?: return ResultadoPerfil.Error("Usuario no encontrado.")
        val correo = usuario.correo.trim().lowercase()
        if (usuarioDao.contarCorreoEnOtros(correo, actual.id_usuario) > 0) {
            return ResultadoPerfil.CorreoDuplicado
        }
        val copia = actual.copy(
            nombre = usuario.nombre,
            correo = correo,
            telefono = usuario.telefono,
            giro = usuario.giro,
            contacto_nombre = usuario.contacto_nombre,
            contacto_cargo = usuario.contacto_cargo
        )
        return try {
            usuarioDao.updateUsuario(copia)
            ResultadoPerfil.Exito(copia)
        } catch (e: SQLiteConstraintException) {
            ResultadoPerfil.CorreoDuplicado
        }
    }

    suspend fun cambiarClave(idUsuario: Int, claveActual: String, claveNueva: String): ResultadoCambioClave {
        val usuario = usuarioDao.getUsuarioPorId(idUsuario)
            ?: return ResultadoCambioClave.Error("Usuario no encontrado.")
        if (!PasswordHasher.verify(claveActual, usuario.password_salt, usuario.password_hash)) {
            return ResultadoCambioClave.ClaveActualIncorrecta
        }
        if (claveActual == claveNueva) {
            return ResultadoCambioClave.MismaClave
        }
        if (!PasswordPolicy.evaluar(claveNueva).valida) {
            return ResultadoCambioClave.Error("La nueva contraseña no cumple los requisitos.")
        }
        val salt = PasswordHasher.newSalt()
        usuarioDao.updateUsuario(
            usuario.copy(password_hash = PasswordHasher.hash(claveNueva, salt), password_salt = salt)
        )
        return ResultadoCambioClave.Exito
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

    suspend fun sincronizarProtocolos(tecnicoId: Int): Int = protocoloDao.sincronizarByTecnico(tecnicoId)

    suspend fun getUltimaVisitaIdDeTecnico(instalacionId: Int, tecnicoId: Int): Int? =
        visitaDao.getUltimaVisitaIdDeTecnico(instalacionId, tecnicoId)
}
