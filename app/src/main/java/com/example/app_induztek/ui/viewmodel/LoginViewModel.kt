package com.example.app_induztek.ui.viewmodel

import com.example.app_induztek.ui.util.FiltrosEntrada
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.data.repository.InduztekRepository
import com.example.app_induztek.di.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Patterns
import com.example.app_induztek.data.repository.ResultadoLogin
import com.example.app_induztek.data.repository.ResultadoRecuperacion
import com.example.app_induztek.data.repository.ResultadoRestablecer
import com.example.app_induztek.di.UserMessages
import com.example.app_induztek.security.PasswordPolicy

class LoginViewModel(
    private val repository: InduztekRepository,
    private val session: SessionManager,
    private val messages: UserMessages
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val currentUser: StateFlow<UsuarioEntity?> = session.user

    val currentRole: StateFlow<UserRole> = session.role

    fun onLoginEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = FiltrosEntrada.correo(email), emailError = null, error = null)
    }

    fun onLoginPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = FiltrosEntrada.clave(password), passwordError = null, error = null)
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
            val error = when (val resultado = repository.autenticar(email, password)) {
                is ResultadoLogin.Exito -> {
                    val rol = when (resultado.rol) {
                        InduztekRepository.ROL_CLIENTE -> UserRole.CLIENTE
                        InduztekRepository.ROL_SUPERVISOR -> UserRole.SUPERVISOR
                        else -> UserRole.TECNICO
                    }
                    session.login(resultado.usuario, rol)
                    if (resultado.sinConexion) messages.show(MENSAJE_MODO_SIN_CONEXION)
                    null
                }
                is ResultadoLogin.CredencialesInvalidas -> "Correo o contraseña incorrectos."
                is ResultadoLogin.SinConexion -> InduztekRepository.MENSAJE_SIN_CONEXION
                is ResultadoLogin.Error -> resultado.mensaje
            }
            _uiState.value = if (error == null) {
                _uiState.value.copy(password = "", error = null, isLoggingIn = false)
            } else {
                _uiState.value.copy(error = error, isLoggingIn = false)
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
            correo = FiltrosEntrada.correo(valor),
            correoError = null,
            mensaje = null,
            exito = false
        )
    }

    fun onRecuperarCodigoChange(valor: String) {
        _recuperarState.value = _recuperarState.value.copy(codigo = valor.filter { it.isDigit() }.take(6), codigoError = null)
    }

    fun onRecuperarClaveNuevaChange(valor: String) {
        _recuperarState.value = _recuperarState.value.copy(
            claveNueva = FiltrosEntrada.clave(valor),
            requisitos = PasswordPolicy.evaluar(FiltrosEntrada.clave(valor)),
            claveNuevaError = null
        )
    }

    fun onRecuperarConfirmarChange(valor: String) {
        _recuperarState.value = _recuperarState.value.copy(confirmarClave = FiltrosEntrada.clave(valor), confirmarError = null)
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

    fun restablecerClave() {
        val estado = _recuperarState.value
        if (estado.enviando) return
        val codigoError = if (estado.codigo.length == 6) null else "El código tiene 6 dígitos"
        val claveNuevaError = if (estado.requisitos.valida) null else "La contraseña no cumple los requisitos"
        val confirmarError = if (estado.confirmarClave.isNotEmpty() && estado.confirmarClave == estado.claveNueva) null
            else "Las contraseñas no coinciden"
        if (codigoError != null || claveNuevaError != null || confirmarError != null) {
            _recuperarState.value = estado.copy(
                codigoError = codigoError,
                claveNuevaError = claveNuevaError,
                confirmarError = confirmarError
            )
            return
        }
        viewModelScope.launch {
            _recuperarState.value = estado.copy(enviando = true, mensaje = null)
            _recuperarState.value = when (val resultado = repository.restablecerClave(estado.correo, estado.codigo, estado.claveNueva)) {
                is ResultadoRestablecer.Exito -> {
                    messages.confirmar("Contraseña restablecida")
                    _recuperarState.value.copy(enviando = false, restablecido = true)
                }
                is ResultadoRestablecer.CodigoInvalido ->
                    _recuperarState.value.copy(enviando = false, codigoError = resultado.mensaje)
                is ResultadoRestablecer.ServicioNoDisponible ->
                    _recuperarState.value.copy(enviando = false, codigoError = MENSAJE_SERVICIO_NO_DISPONIBLE)
            }
        }
    }

    fun reiniciarRecuperacion() {
        _recuperarState.value = RecuperarClaveUiState()
    }

    companion object {
        const val MENSAJE_ENVIADO = "Si el correo está registrado, te enviamos un código de 6 dígitos. Escríbelo abajo junto a tu nueva contraseña."
        const val MENSAJE_SERVICIO_NO_DISPONIBLE = "No pudimos conectar con el servicio de recuperación. Revisa tu internet e inténtalo más tarde."
        const val MENSAJE_MODO_SIN_CONEXION = "Sin conexión: estás usando los datos guardados en el teléfono."
    }
}
