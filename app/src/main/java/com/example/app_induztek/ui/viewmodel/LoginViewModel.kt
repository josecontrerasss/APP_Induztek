package com.example.app_induztek.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.data.repository.InduztekRepository
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
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun onLoginPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun iniciarSesion() {
        if (_uiState.value.isLoggingIn) return
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Por favor ingresa correo y contraseña")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoggingIn = true, error = null)
            val usuario = repository.autenticar(email, password)
            if (usuario != null) {
                val rolCodigo = repository.getRolDeUsuario(usuario.id_usuario)
                val rol = if (rolCodigo == "CLIENTE") UserRole.CLIENTE else UserRole.TECNICO
                session.login(usuario, rol)
                _uiState.value = _uiState.value.copy(error = null, isLoggingIn = false)
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
}
