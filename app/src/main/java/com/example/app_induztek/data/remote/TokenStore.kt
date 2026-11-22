package com.example.app_induztek.data.remote

import android.content.Context

class TokenStore(context: Context) {

    private val preferencias = context.getSharedPreferences("sesion_induztek", Context.MODE_PRIVATE)

    @Volatile
    var token: String? = null
        private set

    fun guardar(idUsuario: Int, nuevoToken: String) {
        token = nuevoToken
        preferencias.edit().putString(clave(idUsuario), nuevoToken).apply()
    }

    fun restaurar(idUsuario: Int) {
        token = preferencias.getString(clave(idUsuario), null)
    }

    fun borrar(idUsuario: Int?) {
        token = null
        if (idUsuario != null) preferencias.edit().remove(clave(idUsuario)).apply()
    }

    private fun clave(idUsuario: Int) = "token_$idUsuario"
}
