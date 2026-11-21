package com.example.app_induztek.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_induztek.data.repository.InduztekRepository
import com.example.app_induztek.data.repository.ResultadoRecuperacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecuperarClaveViewModel(
    private val repository: InduztekRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecuperarClaveUiState())
    val uiState: StateFlow<RecuperarClaveUiState> = _uiState.asStateFlow()

    fun onCorreoChange(valor: String) {
        _uiState.value = _uiState.value.copy(
            correo = valor,
            correoError = null,
            mensaje = null,
            exito = false
        )
    }

    fun enviar() {
        if (_uiState.value.enviando) return
        val correo = _uiState.value.correo.trim()
        if (correo.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            _uiState.value = _uiState.value.copy(
                correoError = "Ingresa un correo electrónico válido",
                mensaje = null,
                exito = false
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(enviando = true, correoError = null, mensaje = null)
            when (val resultado = repository.solicitarRecuperacionClave(correo)) {
                is ResultadoRecuperacion.Enviado -> {
                    _uiState.value = _uiState.value.copy(
                        enviando = false,
                        exito = true,
                        mensaje = MENSAJE_ENVIADO
                    )
                }
                is ResultadoRecuperacion.ServicioNoDisponible -> {
                    _uiState.value = _uiState.value.copy(
                        enviando = false,
                        exito = false,
                        mensaje = MENSAJE_SERVICIO_NO_DISPONIBLE
                    )
                }
                is ResultadoRecuperacion.Error -> {
                    _uiState.value = _uiState.value.copy(
                        enviando = false,
                        exito = false,
                        mensaje = resultado.mensaje
                    )
                }
            }
        }
    }

    fun reiniciar() {
        _uiState.value = RecuperarClaveUiState()
    }

    companion object {
        const val MENSAJE_ENVIADO = "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña."
        const val MENSAJE_SERVICIO_NO_DISPONIBLE = "El servicio de recuperación aún no está disponible. Contacta a soporte Induztek."
    }
}
