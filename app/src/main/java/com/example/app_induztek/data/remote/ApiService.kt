package com.example.app_induztek.data.remote

import com.example.app_induztek.data.remote.dto.AuthResponseDto
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
import retrofit2.Response
import com.example.app_induztek.data.remote.dto.AsignacionCrearDto
import com.example.app_induztek.data.remote.dto.AsignacionDto
import com.example.app_induztek.data.remote.dto.EquipoCrearDto
import com.example.app_induztek.data.remote.dto.InstalacionCrearDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Query
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

    @POST("auth/restablecer-clave")
    suspend fun restablecerClave(@Body request: RestablecerClaveRequestDto): Response<Unit>

    @PUT("usuarios/{id}")
    suspend fun actualizarPerfil(@Path("id") id: Int, @Body request: PerfilUpdateDto): Response<UsuarioDto>

    @PUT("usuarios/{id}/clave")
    suspend fun cambiarClave(@Path("id") id: Int, @Body request: CambiarClaveRequestDto): Response<Unit>

    @GET("tipos-equipo")
    suspend fun tiposEquipo(): Response<List<TipoEquipoDto>>

    @GET("clientes/{id}/instalaciones")
    suspend fun instalacionesCliente(@Path("id") id: Int): Response<List<InstalacionDto>>

    @GET("clientes/{id}/visitas")
    suspend fun visitasCliente(@Path("id") id: Int): Response<List<VisitaDto>>

    @GET("clientes/{id}/protocolos")
    suspend fun protocolosCliente(@Path("id") id: Int): Response<List<ProtocoloDto>>

    @GET("instalaciones/{id}/equipos")
    suspend fun equiposInstalacion(@Path("id") id: Int): Response<List<EquipoDto>>

    @GET("tecnicos/{id}/instalaciones")
    suspend fun instalacionesTecnico(@Path("id") id: Int): Response<List<InstalacionDto>>

    @GET("tecnicos/{id}/equipos")
    suspend fun equiposTecnico(@Path("id") id: Int): Response<List<EquipoDto>>

    @GET("tecnicos/{id}/visitas")
    suspend fun visitasTecnico(@Path("id") id: Int): Response<List<VisitaDto>>

    @GET("tecnicos/{id}/protocolos")
    suspend fun protocolosTecnico(@Path("id") id: Int): Response<List<ProtocoloDto>>

    @GET("protocolos/{id}")
    suspend fun detalleProtocolo(@Path("id") id: Int): Response<ProtocoloSyncDto>

    @GET("usuarios")
    suspend fun usuariosPorRol(@Query("rol") rol: String): Response<List<UsuarioDto>>

    @GET("asignaciones")
    suspend fun asignaciones(): Response<List<AsignacionDto>>

    @POST("asignaciones")
    suspend fun crearAsignacion(@Body request: AsignacionCrearDto): Response<AsignacionDto>

    @DELETE("asignaciones/{id}")
    suspend fun eliminarAsignacion(@Path("id") id: Int): Response<Unit>

    @GET("tecnicos/{id}/empresas")
    suspend fun empresasTecnico(@Path("id") id: Int): Response<List<AsignacionDto>>

    @POST("instalaciones")
    suspend fun crearInstalacion(@Body request: InstalacionCrearDto): Response<InstalacionDto>

    @POST("instalaciones/{id}/equipos")
    suspend fun crearEquipo(@Path("id") id: Int, @Body request: EquipoCrearDto): Response<EquipoDto>

    @GET("clientes/{id}/tecnicos")
    suspend fun tecnicosCliente(@Path("id") id: Int): Response<List<AsignacionDto>>

    @GET("protocolos/pendientes")
    suspend fun protocolosPendientes(): Response<List<ProtocoloSyncDto>>

    @PUT("protocolos/{id}/aprobar")
    suspend fun aprobarProtocolo(@Path("id") id: Int): Response<ProtocoloDto>

    @PUT("protocolos/{id}/rechazar")
    suspend fun rechazarProtocolo(@Path("id") id: Int): Response<ProtocoloDto>

    @POST("protocolos/sincronizar")
    suspend fun sincronizarProtocolos(@Body lista: List<ProtocoloSyncDto>): Response<SyncResponseDto>
}
