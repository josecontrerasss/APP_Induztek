package com.example.app_induztek.ui.viewmodel

enum class UserRole {
    TECNICO,
    CLIENTE,
    SUPERVISOR
}

enum class TecnicoTab {
    INICIO,
    MEDICIONES,
    EQUIPOS,
    SINCRONIZAR,
    MAS
}

enum class FiltroHistorial {
    TODOS,
    PENDIENTES,
    SINCRONIZADOS
}

enum class ClienteTab {
    INICIO,
    PROTOCOLOS,
    DESCARGAS,
    PERFIL
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val error: String? = null,
    val isLoggingIn: Boolean = false
)

data class TecnicoFormState(
    val tensionPrimario: String = "",
    val tensionSecundario: String = "",
    val corriente: String = "",
    val resistenciaAislamiento: String = ">",
    val temperaturaAceite: String = "",
    val observaciones: String = "Equipo operando en condiciones normales.",
    val isTensionPrimarioOk: Boolean = true,
    val isTensionSecundarioOk: Boolean = true,
    val isCorrienteOk: Boolean = true,
    val isResistenciaOk: Boolean = true,
    val isTemperaturaAlerta: Boolean = true,
    val latitud: String = "",
    val longitud: String = "",
    val fotoPath: String? = null,
    val intentoGuardar: Boolean = false,
    val guardando: Boolean = false
)