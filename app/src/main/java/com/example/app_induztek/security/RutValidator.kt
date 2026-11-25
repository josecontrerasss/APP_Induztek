package com.example.app_induztek.security

object RutValidator {

    fun esValido(rut: String): Boolean {
        val limpio = limpiar(rut)
        if (limpio.length < 2) return false
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.last()
        if (cuerpo.any { !it.isDigit() }) return false
        if (cuerpo.length !in 7..8) return false
        if (cuerpo.all { it == cuerpo.first() }) return false
        if (!dv.isDigit() && dv != 'K') return false
        return calcularDv(cuerpo) == dv
    }

    fun formatear(rut: String): String {
        val limpio = limpiar(rut)
        if (limpio.length < 2) return rut.trim()
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.last()
        if (cuerpo.any { !it.isDigit() }) return rut.trim()
        val conPuntos = cuerpo.reversed().chunked(3).joinToString(".").reversed()
        return "$conPuntos-$dv"
    }

    private fun limpiar(rut: String): String =
        rut.filter { it.isLetterOrDigit() }.uppercase()

    private fun calcularDv(cuerpo: String): Char {
        var suma = 0
        var factor = 2
        for (i in cuerpo.length - 1 downTo 0) {
            suma += (cuerpo[i] - '0') * factor
            factor = if (factor == 7) 2 else factor + 1
        }
        val resto = 11 - (suma % 11)
        return when (resto) {
            11 -> '0'
            10 -> 'K'
            else -> resto.toString()[0]
        }
    }
}
