package com.example.app_induztek.di

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserMessages {

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _confirmacion = MutableStateFlow<String?>(null)
    val confirmacion: StateFlow<String?> = _confirmacion.asStateFlow()

    fun show(text: String) {
        _message.value = text
    }

    fun clear() {
        _message.value = null
    }

    fun confirmar(text: String) {
        _confirmacion.value = text
    }

    fun limpiarConfirmacion() {
        _confirmacion.value = null
    }
}
