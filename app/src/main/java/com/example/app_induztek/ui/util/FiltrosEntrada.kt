package com.example.app_induztek.ui.util

object FiltrosEntrada {

    const val MAX_CORREO = 100
    const val MAX_CLAVE = 30
    const val MAX_RAZON_SOCIAL = 100
    const val MAX_NOMBRE_PERSONA = 60
    const val MAX_TEXTO_CORTO = 60
    const val MAX_TELEFONO = 15
    const val MAX_RUT = 12
    const val MAX_CODIGO = 30

    private val SIGNOS_RAZON_SOCIAL = setOf(' ', '.', ',', '&', '-')
    private val SIGNOS_CODIGO = setOf('-', '_', ' ', '.', '/')

    fun soloDigitos(valor: String, maximo: Int): String = valor.filter { it.isDigit() }.take(maximo)

    fun decimal(valor: String, maxEnteros: Int, maxDecimales: Int = 2): String {
        val limpio = valor.replace(',', '.').filter { it.isDigit() || it == '.' }
        val partes = limpio.split('.')
        val enteros = partes[0].take(maxEnteros)
        if (partes.size == 1) return enteros
        return enteros + "." + partes.drop(1).joinToString("").take(maxDecimales)
    }

    fun soloLetras(valor: String, maximo: Int): String =
        valor.filter { it.isLetter() || it == ' ' }.take(maximo)

    fun razonSocial(valor: String): String =
        valor.filter { it.isLetterOrDigit() || it in SIGNOS_RAZON_SOCIAL }.take(MAX_RAZON_SOCIAL)

    fun codigo(valor: String, maximo: Int = MAX_CODIGO): String =
        valor.filter { it.isLetterOrDigit() || it in SIGNOS_CODIGO }.take(maximo)

    fun textoLibre(valor: String, maximo: Int): String = valor.take(maximo)

    fun rut(valor: String): String =
        valor.uppercase().filter { it.isDigit() || it == 'K' || it == '.' || it == '-' }.take(MAX_RUT)

    fun telefono(valor: String): String =
        valor.filterIndexed { i, c -> c.isDigit() || c == ' ' || (c == '+' && i == 0) }.take(MAX_TELEFONO)

    fun correo(valor: String): String = valor.filter { !it.isWhitespace() }.take(MAX_CORREO)

    fun clave(valor: String): String = valor.filter { !it.isWhitespace() }.take(MAX_CLAVE)
}
