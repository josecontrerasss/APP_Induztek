package com.example.app_induztek.ui.viewmodel

import com.example.app_induztek.ui.util.FiltrosEntrada
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.data.repository.InduztekRepository
import com.example.app_induztek.data.repository.ResultadoRegistro
import com.example.app_induztek.security.ContactoValidator
import com.example.app_induztek.security.PasswordPolicy
import com.example.app_induztek.security.RutValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegistroViewModel(
    private val repository: InduztekRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun onRutChange(valor: String) {
        _uiState.value = calcularErrores(_uiState.value.copy(rut = FiltrosEntrada.rut(valor), mensajeError = null))
    }

    fun onNombreChange(valor: String) {
        _uiState.value = calcularErrores(_uiState.value.copy(nombre = FiltrosEntrada.razonSocial(valor), mensajeError = null))
    }

    fun onCorreoChange(valor: String) {
        _uiState.value = calcularErrores(_uiState.value.copy(correo = FiltrosEntrada.correo(valor), mensajeError = null))
    }

    fun onTelefonoChange(valor: String) {
        _uiState.value = calcularErrores(_uiState.value.copy(telefono = FiltrosEntrada.telefono(valor), mensajeError = null))
    }

    fun onContrasenaChange(valor: String) {
        _uiState.value = calcularErrores(_uiState.value.copy(contrasena = FiltrosEntrada.clave(valor), mensajeError = null))
    }

    fun onConfirmarContrasenaChange(valor: String) {
        _uiState.value = calcularErrores(_uiState.value.copy(confirmarContrasena = FiltrosEntrada.clave(valor), mensajeError = null))
    }

    fun registrarCliente() {
        if (_uiState.value.enviando) return
        val estado = calcularErrores(_uiState.value).copy(intentoEnviar = true)
        if (estado.rutError != null || estado.nombreError != null || estado.correoError != null ||
            estado.telefonoError != null || estado.contrasenaError != null || estado.confirmarError != null) {
            _uiState.value = estado.copy(mensajeError = null)
            return
        }
        viewModelScope.launch {
            _uiState.value = estado.copy(enviando = true, mensajeError = null)
            val rolId = repository.getRolId("CLIENTE")
            if (rolId == null) {
                _uiState.value = estado.copy(enviando = false, mensajeError = "No existe el rol CLIENTE")
                return@launch
            }
            val nuevoCliente = UsuarioEntity(
                id_rol = rolId,
                rut = estado.rut.trim(),
                nombre = estado.nombre.trim(),
                correo = estado.correo.trim(),
                telefono = estado.telefono.trim()
            )
            when (val resultado = repository.registrarCliente(nuevoCliente, estado.contrasena)) {
                is ResultadoRegistro.Exito -> {
                    _uiState.value = estado.copy(
                        enviando = false,
                        registroExitoso = true,
                        mensajeError = null,
                        contrasena = "",
                        confirmarContrasena = "",
                        requisitos = PasswordPolicy.evaluar(""),
                        confirmarError = null
                    )
                }
                is ResultadoRegistro.RutOCorreoDuplicado -> {
                    _uiState.value = estado.copy(
                        enviando = false,
                        mensajeError = "El RUT o el correo ya están registrados."
                    )
                }
                is ResultadoRegistro.Error -> {
                    _uiState.value = estado.copy(enviando = false, mensajeError = resultado.mensaje)
                }
            }
        }
    }

    fun limpiarRegistro() {
        _uiState.value = RegistroUiState()
    }

    private fun calcularErrores(estado: RegistroUiState): RegistroUiState {
        return estado.copy(
            rutError = if (RutValidator.esValido(estado.rut.trim())) null else "RUT inválido",
            nombreError = if (estado.nombre.isBlank()) "El nombre o razón social es obligatorio" else null,
            correoError = if (ContactoValidator.correoValido(estado.correo)) null else "Correo electrónico inválido",
            telefonoError = validarTelefono(estado.telefono),
            contrasenaError = if (PasswordPolicy.evaluar(estado.contrasena).valida) null else "La contraseña no cumple los requisitos de seguridad",
            confirmarError = if (estado.confirmarContrasena.isNotEmpty() && estado.confirmarContrasena == estado.contrasena) null else "Las contraseñas no coinciden",
            requisitos = PasswordPolicy.evaluar(estado.contrasena)
        )
    }

    private fun validarTelefono(telefono: String): String? {
        if (telefono.isBlank()) return null
        return if (ContactoValidator.telefonoValido(telefono)) null else "Teléfono inválido. Ejemplo: +56 9 1234 5678"
    }
}
