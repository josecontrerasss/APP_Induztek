package com.example.app_induztek.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_induztek.data.remote.dto.AsignacionDto
import com.example.app_induztek.data.remote.dto.UsuarioDto
import com.example.app_induztek.data.repository.InduztekRepository
import com.example.app_induztek.data.repository.ResultadoRemoto
import com.example.app_induztek.di.SessionManager
import com.example.app_induztek.di.UserMessages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SupervisorUiState(
    val clientes: List<UsuarioDto> = emptyList(),
    val tecnicos: List<UsuarioDto> = emptyList(),
    val asignaciones: List<AsignacionDto> = emptyList(),
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

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)
            val clientes = repository.usuariosPorRol(InduztekRepository.ROL_CLIENTE)
            val tecnicos = repository.usuariosPorRol(InduztekRepository.ROL_TECNICO)
            val asignaciones = repository.asignaciones()
            val error = listOf(clientes, tecnicos, asignaciones).filterIsInstance<ResultadoRemoto.Error>().firstOrNull()
            _uiState.value = _uiState.value.copy(
                clientes = (clientes as? ResultadoRemoto.Exito)?.datos ?: _uiState.value.clientes,
                tecnicos = (tecnicos as? ResultadoRemoto.Exito)?.datos ?: _uiState.value.tecnicos,
                asignaciones = (asignaciones as? ResultadoRemoto.Exito)?.datos ?: _uiState.value.asignaciones,
                cargando = false,
                error = error?.mensaje
            )
        }
    }

    fun seleccionarCliente(id: Int) {
        _uiState.value = _uiState.value.copy(idClienteSeleccionado = id, error = null)
    }

    fun seleccionarTecnico(id: Int) {
        _uiState.value = _uiState.value.copy(idTecnicoSeleccionado = id, error = null)
    }

    fun asignar() {
        val estado = _uiState.value
        if (estado.guardando) return
        val cliente = estado.clientes.firstOrNull { it.idUsuario == estado.idClienteSeleccionado }
        val tecnico = estado.tecnicos.firstOrNull { it.idUsuario == estado.idTecnicoSeleccionado }
        if (cliente == null || tecnico == null) {
            _uiState.value = estado.copy(error = "Elige una empresa y un técnico")
            return
        }
        viewModelScope.launch {
            _uiState.value = estado.copy(guardando = true, error = null)
            when (val resultado = repository.crearAsignacion(cliente, tecnico)) {
                is ResultadoRemoto.Exito -> {
                    _uiState.value = _uiState.value.copy(
                        guardando = false,
                        asignaciones = listOf(resultado.datos) + _uiState.value.asignaciones,
                        idClienteSeleccionado = null,
                        idTecnicoSeleccionado = null
                    )
                    messages.confirmar("Técnico asignado\n${tecnico.nombre} → ${cliente.nombre}")
                }
                is ResultadoRemoto.Error -> _uiState.value = _uiState.value.copy(guardando = false, error = resultado.mensaje)
            }
        }
    }

    fun eliminar(idAsignacion: Int) {
        viewModelScope.launch {
            when (val resultado = repository.eliminarAsignacion(idAsignacion)) {
                is ResultadoRemoto.Exito -> {
                    _uiState.value = _uiState.value.copy(
                        asignaciones = _uiState.value.asignaciones.filterNot { it.idAsignacion == idAsignacion }
                    )
                    messages.confirmar("Asignación eliminada")
                }
                is ResultadoRemoto.Error -> _uiState.value = _uiState.value.copy(error = resultado.mensaje)
            }
        }
    }

    fun cerrarSesion() {
        session.logout()
    }
}
