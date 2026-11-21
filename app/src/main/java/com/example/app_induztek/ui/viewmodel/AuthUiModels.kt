package com.example.app_induztek.ui.viewmodel

import com.example.app_induztek.security.PasswordPolicy

data class RegistroUiState(
    val rut: String = "",
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val requisitos: PasswordPolicy.Resultado = PasswordPolicy.evaluar(""),
    val rutError: String? = null,
    val correoError: String? = null,
    val telefonoError: String? = null,
    val confirmarError: String? = null,
    val intentoEnviar: Boolean = false,
    val enviando: Boolean = false,
    val mensajeError: String? = null,
    val registroExitoso: Boolean = false
)

data class CambiarClaveUiState(
    val claveActual: String = "",
    val claveNueva: String = "",
    val confirmarClave: String = "",
    val requisitos: PasswordPolicy.Resultado = PasswordPolicy.evaluar(""),
    val claveActualError: String? = null,
    val claveNuevaError: String? = null,
    val confirmarError: String? = null,
    val guardando: Boolean = false,
    val exito: Boolean = false
)

data class RecuperarClaveUiState(
    val correo: String = "",
    val correoError: String? = null,
    val enviando: Boolean = false,
    val mensaje: String? = null,
    val exito: Boolean = false
)
