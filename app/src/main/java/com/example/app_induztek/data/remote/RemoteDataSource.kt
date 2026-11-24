package com.example.app_induztek.data.remote

import com.example.app_induztek.data.remote.dto.AsignacionCrearDto
import com.example.app_induztek.data.remote.dto.AsignacionDto
import com.example.app_induztek.data.remote.dto.AuthResponseDto
import com.example.app_induztek.data.remote.dto.EquipoCrearDto
import com.example.app_induztek.data.remote.dto.InstalacionCrearDto
import com.example.app_induztek.data.remote.dto.CambiarClaveRequestDto
import com.example.app_induztek.data.remote.dto.EquipoDto
import com.example.app_induztek.data.remote.dto.InstalacionDto
import com.example.app_induztek.data.remote.dto.LoginRequestDto
import com.example.app_induztek.data.remote.dto.PerfilUpdateDto
import com.example.app_induztek.data.remote.dto.ProtocoloDto
import com.example.app_induztek.data.remote.dto.ProtocoloSyncDto
import com.example.app_induztek.data.remote.dto.RecuperarClaveRequestDto
import com.example.app_induztek.data.remote.dto.RegistroRequestDto
import com.example.app_induztek.data.remote.dto.RestablecerClaveRequestDto
import com.example.app_induztek.data.remote.dto.SyncResponseDto
import com.example.app_induztek.data.remote.dto.TipoEquipoDto
import com.example.app_induztek.data.remote.dto.UsuarioDto
import com.example.app_induztek.data.remote.dto.VisitaDto
import java.io.IOException
import kotlinx.coroutines.CancellationException
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response

class RemoteDataSource(private val api: ApiService) {

    suspend fun login(correo: String, password: String): ApiResult<AuthResponseDto> =
        safeCall { api.login(LoginRequestDto(correo, password)) }

    suspend fun registro(request: RegistroRequestDto): ApiResult<UsuarioDto> =
        safeCall { api.registro(request) }

    suspend fun recuperarClave(correo: String): ApiResult<Unit> =
        safeCall(respaldo = Unit) { api.recuperarClave(RecuperarClaveRequestDto(correo)) }

    suspend fun restablecerClave(correo: String, codigo: String, claveNueva: String): ApiResult<Unit> =
        safeCall(respaldo = Unit) { api.restablecerClave(RestablecerClaveRequestDto(correo, codigo, claveNueva)) }

    suspend fun actualizarPerfil(id: Int, dto: PerfilUpdateDto): ApiResult<UsuarioDto> =
        safeCall { api.actualizarPerfil(id, dto) }

    suspend fun cambiarClave(id: Int, claveActual: String, claveNueva: String): ApiResult<Unit> =
        safeCall(respaldo = Unit) { api.cambiarClave(id, CambiarClaveRequestDto(claveActual, claveNueva)) }

    suspend fun tiposEquipo(): ApiResult<List<TipoEquipoDto>> = safeCall { api.tiposEquipo() }

    suspend fun instalacionesCliente(id: Int): ApiResult<List<InstalacionDto>> =
        safeCall { api.instalacionesCliente(id) }

    suspend fun visitasCliente(id: Int): ApiResult<List<VisitaDto>> = safeCall { api.visitasCliente(id) }

    suspend fun protocolosCliente(id: Int): ApiResult<List<ProtocoloDto>> =
        safeCall { api.protocolosCliente(id) }

    suspend fun equiposInstalacion(id: Int): ApiResult<List<EquipoDto>> =
        safeCall { api.equiposInstalacion(id) }

    suspend fun instalacionesTecnico(id: Int): ApiResult<List<InstalacionDto>> =
        safeCall { api.instalacionesTecnico(id) }

    suspend fun equiposTecnico(id: Int): ApiResult<List<EquipoDto>> = safeCall { api.equiposTecnico(id) }

    suspend fun visitasTecnico(id: Int): ApiResult<List<VisitaDto>> = safeCall { api.visitasTecnico(id) }

    suspend fun protocolosTecnico(id: Int): ApiResult<List<ProtocoloDto>> =
        safeCall { api.protocolosTecnico(id) }

    suspend fun detalleProtocolo(id: Int): ApiResult<ProtocoloSyncDto> = safeCall { api.detalleProtocolo(id) }

    suspend fun sincronizarProtocolos(lista: List<ProtocoloSyncDto>): ApiResult<SyncResponseDto> =
        safeCall { api.sincronizarProtocolos(lista) }

    suspend fun usuariosPorRol(rol: String): ApiResult<List<UsuarioDto>> = safeCall { api.usuariosPorRol(rol) }

    suspend fun asignaciones(): ApiResult<List<AsignacionDto>> = safeCall { api.asignaciones() }

    suspend fun crearAsignacion(request: AsignacionCrearDto): ApiResult<AsignacionDto> =
        safeCall { api.crearAsignacion(request) }

    suspend fun eliminarAsignacion(id: Int): ApiResult<Unit> = safeCall(respaldo = Unit) { api.eliminarAsignacion(id) }

    suspend fun empresasTecnico(id: Int): ApiResult<List<AsignacionDto>> = safeCall { api.empresasTecnico(id) }

    suspend fun crearInstalacion(request: InstalacionCrearDto): ApiResult<InstalacionDto> =
        safeCall { api.crearInstalacion(request) }

    suspend fun crearEquipo(idInstalacion: Int, request: EquipoCrearDto): ApiResult<EquipoDto> =
        safeCall { api.crearEquipo(idInstalacion, request) }

    suspend fun tecnicosCliente(id: Int): ApiResult<List<AsignacionDto>> = safeCall { api.tecnicosCliente(id) }

    suspend fun protocolosPendientes(): ApiResult<List<ProtocoloSyncDto>> = safeCall { api.protocolosPendientes() }

    suspend fun revisarProtocolo(id: Int, aprobar: Boolean): ApiResult<ProtocoloDto> =
        safeCall { if (aprobar) api.aprobarProtocolo(id) else api.rechazarProtocolo(id) }

    private suspend fun <T : Any> safeCall(
        respaldo: T? = null,
        call: suspend () -> Response<T>
    ): ApiResult<T> {
        return try {
            val respuesta = call()
            if (respuesta.isSuccessful) {
                val cuerpo = respuesta.body()
                when {
                    cuerpo != null -> ApiResult.Success(cuerpo)
                    respaldo != null -> ApiResult.Success(respaldo)
                    else -> ApiResult.HttpError(respuesta.code(), "Respuesta vacía del servidor.")
                }
            } else {
                ApiResult.HttpError(respuesta.code(), mensajeDeError(respuesta))
            }
        } catch (e: IOException) {
            ApiResult.NetworkError(e)
        } catch (e: HttpException) {
            ApiResult.HttpError(e.code(), e.message() ?: "Error HTTP ${e.code()}")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ApiResult.NetworkError(e)
        }
    }

    private fun mensajeDeError(respuesta: Response<*>): String {
        val cuerpo = try {
            respuesta.errorBody()?.string()
        } catch (e: Exception) {
            null
        }
        if (!cuerpo.isNullOrBlank()) {
            val json = try {
                JSONObject(cuerpo)
            } catch (e: Exception) {
                null
            }
            val errores = json?.optJSONObject("errores")
            if (errores != null && errores.length() > 0) {
                return errores.keys().asSequence().map { errores.optString(it) }.joinToString("\n")
            }
            val mensaje = json?.optString("mensaje").orEmpty()
            if (mensaje.isNotBlank()) return mensaje
        }
        val mensaje = respuesta.message()
        return if (mensaje.isBlank()) "Error HTTP ${respuesta.code()}" else mensaje
    }
}
