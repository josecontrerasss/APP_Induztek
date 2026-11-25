package com.example.app_induztek.security

object ContactoValidator {

    private val FORMATO_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

    fun correoValido(correo: String): Boolean {
        val limpio = correo.trim()
        if (!FORMATO_CORREO.matches(limpio)) return false
        val usuario = limpio.substringBefore('@')
        val dominio = limpio.substringAfter('@').substringBeforeLast('.')
        return usuario.any { it.isLetter() } && dominio.any { it.isLetter() }
    }

    fun nombreValido(nombre: String): Boolean {
        val limpio = nombre.trim()
        return limpio.length in 2..30 && limpio.all { it.isLetter() }
    }

    fun telefonoValido(telefono: String): Boolean {
        val digitos = telefono.filter { it.isDigit() }
        val numero = if (digitos.length == 11 && digitos.startsWith("56")) digitos.drop(2) else digitos
        if (numero.length != 9) return false
        if (numero.first() !in '2'..'9') return false
        return numero.any { it != numero.first() }
    }
}
