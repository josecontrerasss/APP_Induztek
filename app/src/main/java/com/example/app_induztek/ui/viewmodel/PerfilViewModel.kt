package com.example.app_induztek.ui.viewmodel

import com.example.app_induztek.ui.util.FiltrosEntrada
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.data.repository.InduztekRepository
import com.example.app_induztek.data.repository.ResultadoPerfil
import com.example.app_induztek.di.SessionManager
import com.example.app_induztek.di.UserMessages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.app_induztek.data.repository.ResultadoCambioClave
import com.example.app_induztek.security.ContactoValidator
import com.example.app_induztek.security.PasswordPolicy

data class PerfilEditState(
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val giro: String = "",
    val contactoNombre: String = "",
    val contactoCargo: String = "",
    val editando: Boolean = false,
    val guardando: Boolean = false,
    val nombreError: String? = null,
    val correoError: String? = null,
    val telefonoError: String? = null
)

class PerfilViewModel(
    private val repository: InduztekRepository,
    private val session: SessionManager,
    private val messages: UserMessages
) : ViewModel() {

    val perfil: StateFlow<UsuarioEntity?> = session.user

    private val _perfilEdit = MutableStateFlow(PerfilEditState())
    val perfilEdit: StateFlow<PerfilEditState> = _perfilEdit.asStateFlow()

    private val _cambioClave = MutableStateFlow(CambiarClaveUiState())
    val cambioClave: StateFlow<CambiarClaveUiState> = _cambioClave.asStateFlow()

    init {
        viewModelScope.launch {
            session.user.collect { user ->
                if (user == null) {
                    _perfilEdit.value = PerfilEditState()
                    _cambioClave.value = CambiarClaveUiState()
                }
            }
        }
    }

    fun iniciarEdicionPerfil() {
        val user = session.user.value ?: return
        _perfilEdit.value = PerfilEditState(
            nombre = user.nombre,
            correo = user.correo,
            telefono = user.telefono,
            giro = user.giro.orEmpty(),
            contactoNombre = user.contacto_nombre.orEmpty(),
            contactoCargo = user.contacto_cargo.orEmpty(),
            editando = true
        )
    }

    fun onPerfilNombreChange(value: String) {
        _perfilEdit.value = _perfilEdit.value.copy(nombre = filtrarNombre(value), nombreError = null)
    }

    private fun filtrarNombre(valor: String): String =
        if (session.role.value == UserRole.CLIENTE) FiltrosEntrada.razonSocial(valor)
        else FiltrosEntrada.soloLetras(valor, FiltrosEntrada.MAX_NOMBRE_PERSONA)

    fun onPerfilCorreoChange(value: String) {
        _perfilEdit.value = _perfilEdit.value.copy(correo = FiltrosEntrada.correo(value), correoError = null)
    }

    fun onPerfilTelefonoChange(value: String) {
        _perfilEdit.value = _perfilEdit.value.copy(telefono = FiltrosEntrada.telefono(value), telefonoError = null)
    }

    fun onPerfilGiroChange(value: String) {
        _perfilEdit.value = _perfilEdit.value.copy(giro = FiltrosEntrada.soloLetras(value, FiltrosEntrada.MAX_RAZON_SOCIAL))
    }

    fun onPerfilContactoNombreChange(value: String) {
        _perfilEdit.value = _perfilEdit.value.copy(contactoNombre = FiltrosEntrada.soloLetras(value, FiltrosEntrada.MAX_NOMBRE_PERSONA))
    }

    fun onPerfilContactoCargoChange(value: String) {
        _perfilEdit.value = _perfilEdit.value.copy(contactoCargo = FiltrosEntrada.soloLetras(value, FiltrosEntrada.MAX_TEXTO_CORTO))
    }

    fun cancelarEdicionPerfil() {
        _perfilEdit.value = PerfilEditState()
    }

    fun guardarPerfil() {
        val current = _perfilEdit.value
        if (current.guardando) return
        val user = session.user.value ?: return

        val nombre = current.nombre.trim()
        val correo = current.correo.trim()
        val telefono = current.telefono.trim()

        val nombreError = if (nombre.isBlank()) "El nombre es obligatorio." else null
        val correoError = if (ContactoValidator.correoValido(correo)) null else "Ingresa un correo válido."
        val telefonoError = if (telefono.isNotEmpty() && !ContactoValidator.telefonoValido(telefono)) "Teléfono inválido. Ejemplo: +56 9 1234 5678" else null

        if (nombreError != null || correoError != null || telefonoError != null) {
            _perfilEdit.value = current.copy(
                nombreError = nombreError,
                correoError = correoError,
                telefonoError = telefonoError
            )
            return
        }

        val actualizado = user.copy(
            nombre = nombre,
            correo = correo,
            telefono = telefono,
            giro = current.giro.trim().ifBlank { null },
            contacto_nombre = current.contactoNombre.trim().ifBlank { null },
            contacto_cargo = current.contactoCargo.trim().ifBlank { null }
        )

        _perfilEdit.value = current.copy(guardando = true, nombreError = null, correoError = null, telefonoError = null)
        viewModelScope.launch {
            when (val resultado = repository.actualizarPerfil(actualizado)) {
                is ResultadoPerfil.Exito -> {
                    session.actualizarUsuario(resultado.usuario)
                    _perfilEdit.value = PerfilEditState()
                    messages.confirmar("Perfil actualizado")
                }
                is ResultadoPerfil.CorreoDuplicado -> {
                    _perfilEdit.value = _perfilEdit.value.copy(guardando = false, correoError = "Ese correo ya está en uso.")
                }
                is ResultadoPerfil.Error -> {
                    _perfilEdit.value = _perfilEdit.value.copy(guardando = false)
                    messages.show(resultado.mensaje)
                }
            }
        }
    }

    fun onClaveActualChange(valor: String) {
        _cambioClave.value = _cambioClave.value.copy(claveActual = FiltrosEntrada.clave(valor), claveActualError = null)
    }

    fun onClaveNuevaChange(valor: String) {
        _cambioClave.value = _cambioClave.value.copy(
            claveNueva = FiltrosEntrada.clave(valor),
            requisitos = PasswordPolicy.evaluar(FiltrosEntrada.clave(valor)),
            claveNuevaError = null
        )
    }

    fun onConfirmarClaveChange(valor: String) {
        _cambioClave.value = _cambioClave.value.copy(confirmarClave = FiltrosEntrada.clave(valor), confirmarError = null)
    }

    fun guardarClave() {
        val estado = _cambioClave.value
        if (estado.guardando) return
        val usuario = session.user.value ?: return

        val claveActualError = if (estado.claveActual.isBlank()) "Ingresa tu contraseña actual" else null
        val claveNuevaError = if (estado.requisitos.valida) null else "La contraseña no cumple los requisitos"
        val confirmarError = if (estado.confirmarClave == estado.claveNueva && estado.confirmarClave.isNotEmpty()) {
            null
        } else {
            "Las contraseñas no coinciden"
        }

        if (claveActualError != null || claveNuevaError != null || confirmarError != null) {
            _cambioClave.value = estado.copy(
                claveActualError = claveActualError,
                claveNuevaError = claveNuevaError,
                confirmarError = confirmarError
            )
            return
        }

        _cambioClave.value = estado.copy(guardando = true)
        viewModelScope.launch {
            val resultado = repository.cambiarClave(usuario.id_usuario, estado.claveActual, estado.claveNueva)
            _cambioClave.value = when (resultado) {
                is ResultadoCambioClave.Exito -> {
                    messages.confirmar("Contraseña actualizada")
                    CambiarClaveUiState(exito = true)
                }
                is ResultadoCambioClave.ClaveActualIncorrecta -> _cambioClave.value.copy(
                    guardando = false,
                    claveActualError = "La contraseña actual es incorrecta"
                )
                is ResultadoCambioClave.MismaClave -> _cambioClave.value.copy(
                    guardando = false,
                    claveNuevaError = "La nueva contraseña debe ser distinta a la actual"
                )
                is ResultadoCambioClave.Error -> _cambioClave.value.copy(
                    guardando = false,
                    claveNuevaError = resultado.mensaje
                )
            }
        }
    }

    fun reiniciarCambioClave() {
        _cambioClave.value = CambiarClaveUiState()
    }
}
