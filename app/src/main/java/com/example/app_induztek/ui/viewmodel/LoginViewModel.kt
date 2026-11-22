package com.example.app_induztek.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.data.repository.InduztekRepository
import com.example.app_induztek.data.repository.ResultadoRecuperacion
import com.example.app_induztek.di.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: InduztekRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<UsuarioEntity?> = session.user

    fun onLoginEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, emailError = null, error = null)
    }

    fun onLoginPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, passwordError = null, error = null)
    }

    fun iniciarSesion() {
        if (_uiState.value.isLoggingIn) return
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val emailError = if (email.isBlank()) "Ingresa tu correo" else null
        val passwordError = if (password.isBlank()) "Ingresa tu contraseña" else null
        if (emailError != null || passwordError != null) {
            _uiState.value = _uiState.value.copy(emailError = emailError, passwordError = passwordError, error = null)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoggingIn = true, error = null)
            val usuario = repository.autenticar(email, password)
            if (usuario != null) {
                val rolCodigo = repository.getRolDeUsuario(usuario.id_usuario)
                val rol = when (rolCodigo) {
                    InduztekRepository.ROL_CLIENTE -> UserRole.CLIENTE
                    InduztekRepository.ROL_SUPERVISOR -> UserRole.SUPERVISOR
                    else -> UserRole.TECNICO
                }
                session.login(usuario, rol)
                _uiState.value = _uiState.value.copy(password = "", error = null, isLoggingIn = false)
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "Correo o contraseña incorrectos.",
                    isLoggingIn = false
                )
            }
        }
    }

    fun cerrarSesion() {
        session.logout()
        _uiState.value = _uiState.value.copy(password = "", error = null)
    }

    private val _recuperarState = MutableStateFlow(RecuperarClaveUiState())
    val recuperarState: StateFlow<RecuperarClaveUiState> = _recuperarState.asStateFlow()

    fun onRecuperarCorreoChange(valor: String) {
        _recuperarState.value = _recuperarState.value.copy(
            correo = valor,
            correoError = null,
            mensaje = null,
            exito = false
        )
    }

    fun enviarRecuperacion() {
        if (_recuperarState.value.enviando) return
        val correo = _recuperarState.value.correo.trim()
        if (correo.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            _recuperarState.value = _recuperarState.value.copy(
                correoError = "Ingresa un correo electrónico válido",
                mensaje = null,
                exito = false
            )
            return
        }
        viewModelScope.launch {
            _recuperarState.value = _recuperarState.value.copy(enviando = true, correoError = null, mensaje = null)
            _recuperarState.value = when (val resultado = repository.solicitarRecuperacionClave(correo)) {
                is ResultadoRecuperacion.Enviado ->
                    _recuperarState.value.copy(enviando = false, exito = true, mensaje = MENSAJE_ENVIADO)
                is ResultadoRecuperacion.ServicioNoDisponible ->
                    _recuperarState.value.copy(enviando = false, exito = false, mensaje = MENSAJE_SERVICIO_NO_DISPONIBLE)
                is ResultadoRecuperacion.Error ->
                    _recuperarState.value.copy(enviando = false, exito = false, mensaje = resultado.mensaje)
            }
        }
    }

    fun reiniciarRecuperacion() {
        _recuperarState.value = RecuperarClaveUiState()
    }

    companion object {
        const val MENSAJE_ENVIADO = "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña."
        const val MENSAJE_SERVICIO_NO_DISPONIBLE = "El servicio de recuperación aún no está disponible. Contacta a soporte Induztek."
    }
}