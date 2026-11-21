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
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    @POST("auth/registro")
    suspend fun registro(@Body request: RegistroRequestDto): Response<UsuarioDto>

    @POST("auth/recuperar-clave")
    suspend fun recuperarClave(@Body request: RecuperarClaveRequestDto): Response<Unit>

    @PUT("usuarios/{id}")
    suspend fun actualizarPerfil(@Path("id") id: Int, @Body request: PerfilUpdateDto): Response<UsuarioDto>

    @GET("clientes/{id}/instalaciones")
    suspend fun instalacionesCliente(@Path("id") id: Int): Response<List<InstalacionDto>>

    @GET("clientes/{id}/protocolos")
    suspend fun protocolosCliente(@Path("id") id: Int): Response<List<ProtocoloDto>>

    @GET("instalaciones/{id}/equipos")
    suspend fun equiposInstalacion(@Path("id") id: Int): Response<List<EquipoDto>>

    @POST("protocolos/sincronizar")
    suspend fun sincronizarProtocolos(@Body lista: List<ProtocoloSyncDto>): Response<SyncResponseDto>
}
