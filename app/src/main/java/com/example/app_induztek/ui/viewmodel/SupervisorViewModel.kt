package com.example.app_induztek.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.app_induztek.data.remote.dto.UsuarioDto
import com.example.app_induztek.data.repository.InduztekRepository
import com.example.app_induztek.di.SessionManager
import com.example.app_induztek.di.UserMessages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SupervisorUiState(
    val clientes: List<UsuarioDto> = emptyList(),
    val tecnicos: List<UsuarioDto> = emptyList(),
    val idClienteSeleccionado: Int? = null,
    val idTecnicoSeleccionado: Int? = null,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null
)

class SupervisorViewModel(
    private val repository: InduztekRepository,
    private val session: SessionManager,
    private val messages: UserMessages
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupervisorUiState())
    val uiState: StateFlow<SupervisorUiState> = _uiState.asStateFlow()

    val nombreSupervisor: String get() = session.user.value?.nombre.orEmpty()

    fun seleccionarCliente(id: Int) {
        _uiState.value = _uiState.value.copy(idClienteSeleccionado = id, error = null)
    }

    fun seleccionarTecnico(id: Int) {
        _uiState.value = _uiState.value.copy(idTecnicoSeleccionado = id, error = null)
    }

    fun cerrarSesion() {
        session.logout()
    }
}