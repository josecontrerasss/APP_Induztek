package com.example.app_induztek.data.repository

import com.example.app_induztek.data.local.entities.UsuarioEntity

sealed interface ResultadoPerfil {
    data class Exito(val usuario: UsuarioEntity) : ResultadoPerfil
    data object CorreoDuplicado : ResultadoPerfil
    data class Error(val mensaje: String) : ResultadoPerfil
}

sealed interface ResultadoRecuperacion {
    data object Enviado : ResultadoRecuperacion
    data object ServicioNoDisponible : ResultadoRecuperacion
    data class Error(val mensaje: String) : ResultadoRecuperacion
}

sealed interface ResultadoCambioClave {
    data object Exito : ResultadoCambioClave
    data object ClaveActualIncorrecta : ResultadoCambioClave
    data object MismaClave : ResultadoCambioClave
    data class Error(val mensaje: String) : ResultadoCambioClave
}

sealed interface ResultadoRegistro {
    data class Exito(val idUsuario: Long) : ResultadoRegistro
    data object RutOCorreoDuplicado : ResultadoRegistro
    data class Error(val mensaje: String) : ResultadoRegistro
}

sealed interface ResultadoLogin {
    data class Exito(val usuario: UsuarioEntity, val rol: String, val sinConexion: Boolean) : ResultadoLogin
    data object CredencialesInvalidas : ResultadoLogin
    data object SinConexion : ResultadoLogin
    data class Error(val mensaje: String) : ResultadoLogin
}

sealed interface ResultadoRestablecer {
    data object Exito : ResultadoRestablecer
    data class CodigoInvalido(val mensaje: String) : ResultadoRestablecer
    data object ServicioNoDisponible : ResultadoRestablecer
}

sealed interface ResultadoSync {
    data class Exito(val sincronizados: Int, val rechazados: List<String>) : ResultadoSync
    data object NadaPendiente : ResultadoSync
    data object SinConexion : ResultadoSync
    data object SesionExpirada : ResultadoSync
    data class Error(val mensaje: String) : ResultadoSync
}

sealed interface ResultadoRemoto<out T> {
    data class Exito<T>(val datos: T) : ResultadoRemoto<T>
    data class Error(val mensaje: String) : ResultadoRemoto<Nothing>
}
