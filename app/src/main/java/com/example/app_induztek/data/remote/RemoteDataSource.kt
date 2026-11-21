package com.example.app_induztek.data.remote

import com.example.app_induztek.data.remote.dto.AuthResponseDto
import com.example.app_induztek.data.remote.dto.EquipoDto
import com.example.app_induztek.data.remote.dto.InstalacionDto
import com.example.app_induztek.data.remote.dto.LoginRequestDto
import com.example.app_induztek.data.remote.dto.PerfilUpdateDto
import com.example.app_induztek.data.remote.dto.ProtocoloDto
import com.example.app_induztek.data.remote.dto.ProtocoloSyncDto
import com.example.app_induztek.data.remote.dto.RecuperarClaveRequestDto
import com.example.app_induztek.data.remote.dto.RegistroRequestDto
import com.example.app_induztek.data.remote.dto.SyncResponseDto
import com.example.app_induztek.data.remote.dto.UsuarioDto
import java.io.IOException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import retrofit2.Response

class RemoteDataSource(private val api: ApiService) {

    suspend fun login(correo: String, password: String): ApiResult<AuthResponseDto> =
        safeCall { api.login(LoginRequestDto(correo, password)) }

    suspend fun registro(request: RegistroRequestDto): ApiResult<UsuarioDto> =
        safeCall { api.registro(request) }

    suspend fun recuperarClave(correo: String): ApiResult<Unit> =
        safeCall(respaldo = Unit) { api.recuperarClave(RecuperarClaveRequestDto(correo)) }

    suspend fun actualizarPerfil(id: Int, dto: PerfilUpdateDto): ApiResult<UsuarioDto> =
        safeCall { api.actualizarPerfil(id, dto) }

    suspend fun instalacionesCliente(id: Int): ApiResult<List<InstalacionDto>> =
        safeCall { api.instalacionesCliente(id) }

    suspend fun protocolosCliente(id: Int): ApiResult<List<ProtocoloDto>> =
        safeCall { api.protocolosCliente(id) }

    suspend fun equiposInstalacion(id: Int): ApiResult<List<EquipoDto>> =
        safeCall { api.equiposInstalacion(id) }

    suspend fun sincronizarProtocolos(lista: List<ProtocoloSyncDto>): ApiResult<SyncResponseDto> =
        safeCall { api.sincronizarProtocolos(lista) }

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
        if (!cuerpo.isNullOrBlank()) return cuerpo
        val mensaje = respuesta.message()
        return if (mensaje.isBlank()) "Error HTTP ${respuesta.code()}" else mensaje
    }
}
