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

sealed interface ResultadoRemoto<out T> {
    data class Exito<T>(val datos: T) : ResultadoRemoto<T>
    data class Error(val mensaje: String) : ResultadoRemoto<Nothing>
}