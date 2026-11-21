package com.example.app_induztek.security

object PasswordPolicy {

    const val LONGITUD_MINIMA = 8

    data class Resultado(
        val longitudOk: Boolean,
        val tieneNumero: Boolean,
        val tieneEspecial: Boolean
    ) {
        val valida: Boolean
            get() = longitudOk && tieneNumero && tieneEspecial
    }

    fun evaluar(password: String): Resultado {
        val longitudOk = password.length >= LONGITUD_MINIMA
        val tieneNumero = password.any { it.isDigit() }
        val tieneEspecial = password.any { !it.isLetter() && !it.isDigit() && !it.isWhitespace() }
        return Resultado(longitudOk, tieneNumero, tieneEspecial)
    }
}
