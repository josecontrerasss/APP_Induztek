package com.example.app_induztek.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_induztek.data.remote.dto.AsignacionDto
import com.example.app_induztek.data.remote.dto.ProtocoloSyncDto
import com.example.app_induztek.data.remote.dto.TecnicoCrearDto
import com.example.app_induztek.data.remote.dto.UsuarioDto
import com.example.app_induztek.data.repository.InduztekRepository
import com.example.app_induztek.data.repository.ResultadoRemoto
import com.example.app_induztek.di.SessionManager
import com.example.app_induztek.di.UserMessages
import com.example.app_induztek.security.ContactoValidator
import com.example.app_induztek.security.RutValidator
import com.example.app_induztek.ui.util.FiltrosEntrada
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SupervisorUiState(
    val clientes: List<UsuarioDto> = emptyList(),
    val tecnicos: List<UsuarioDto> = emptyList(),
    val asignaciones: List<AsignacionDto> = emptyList(),
    val pendientes: List<ProtocoloSyncDto> = emptyList(),
    val idClienteSeleccionado: Int? = null,
    val idTecnicoSeleccionado: Int? = null,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null
)

data class CrearTecnicoUiState(
    val rut: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val telefono: String = "",
    val correo: String = "",
    val rutError: String? = null,
    val nombreError: String? = null,
    val apellidoError: String? = null,
    val telefonoError: String? = null,
    val correoError: String? = null,
    val intentoEnviar: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val tecnicoCreado: String? = null,
    val claveTemporal: String? = null
) {
    val tieneErrores: Boolean
        get() = listOf(rutError, nombreError, apellidoError, telefonoError, correoError).any { it != null }
}

class SupervisorViewModel(
    private val repository: InduztekRepository,
    private val session: SessionManager,
    private val messages: UserMessages
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupervisorUiState())
    val uiState: StateFlow<SupervisorUiState> = _uiState.asStateFlow()

    val nombreSupervisor: String get() = session.user.value?.nombre.orEmpty()

    private val _crearTecnico = MutableStateFlow(CrearTecnicoUiState())
    val crearTecnicoState: StateFlow<CrearTecnicoUiState> = _crearTecnico.asStateFlow()

    fun onTecnicoRutChange(valor: String) = actualizarFormulario { it.copy(rut = FiltrosEntrada.rut(valor)) }

    fun onTecnicoNombreChange(valor: String) = actualizarFormulario { it.copy(nombre = FiltrosEntrada.palabra(valor, 30)) }

    fun onTecnicoApellidoChange(valor: String) = actualizarFormulario { it.copy(apellido = FiltrosEntrada.palabra(valor, 30)) }

    fun onTecnicoTelefonoChange(valor: String) = actualizarFormulario { it.copy(telefono = FiltrosEntrada.telefono(valor)) }

    fun onTecnicoCorreoChange(valor: String) = actualizarFormulario { it.copy(correo = FiltrosEntrada.correo(valor)) }

    fun crearTecnico() {
        if (_crearTecnico.value.guardando) return
        val estado = validarTecnico(_crearTecnico.value.copy(intentoEnviar = true, error = null))
        if (estado.tieneErrores) {
            _crearTecnico.value = estado
            return
        }
        viewModelScope.launch {
            _crearTecnico.value = estado.copy(guardando = true)
            val datos = TecnicoCrearDto(
                rut = RutValidator.formatear(estado.rut),
                nombre = estado.nombre.trim(),
                apellido = estado.apellido.trim(),
                telefono = estado.telefono.trim(),
                correo = estado.correo.trim().lowercase()
            )
            when (val resultado = repository.crearTecnico(datos)) {
                is ResultadoRemoto.Exito -> {
                    _crearTecnico.value = CrearTecnicoUiState(
                        tecnicoCreado = resultado.datos.tecnico.nombre,
                        claveTemporal = resultado.datos.claveTemporal
                    )
                    _uiState.value = _uiState.value.copy(
                        tecnicos = (_uiState.value.tecnicos + resultado.datos.tecnico).sortedBy { it.nombre }
                    )
                }
                is ResultadoRemoto.Error -> _crearTecnico.value = estado.copy(guardando = false, error = resultado.mensaje)
            }
        }
    }

    fun cerrarClaveTemporal() {
        _crearTecnico.value = _crearTecnico.value.copy(tecnicoCreado = null, claveTemporal = null)
    }

    private fun actualizarFormulario(cambio: (CrearTecnicoUiState) -> CrearTecnicoUiState) {
        val nuevo = cambio(_crearTecnico.value).copy(error = null)
        _crearTecnico.value = if (nuevo.intentoEnviar) validarTecnico(nuevo) else nuevo
    }

    private fun validarTecnico(estado: CrearTecnicoUiState): CrearTecnicoUiState = estado.copy(
        rutError = if (RutValidator.esValido(estado.rut)) null else "RUT inválido",
        nombreError = if (ContactoValidator.nombreValido(estado.nombre)) null else "Solo letras, entre 2 y 30",
        apellidoError = if (ContactoValidator.nombreValido(estado.apellido)) null else "Solo letras, entre 2 y 30",
        telefonoError = if (ContactoValidator.telefonoValido(estado.telefono)) null else "Teléfono inválido. Ejemplo: +56 9 1234 5678",
        correoError = if (ContactoValidator.correoValido(estado.correo)) null else "Correo electrónico inválido"
    )

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)
            val clientes = repository.usuariosPorRol(InduztekRepository.ROL_CLIENTE)
            val tecnicos = repository.usuariosPorRol(InduztekRepository.ROL_TECNICO)
            val asignaciones = repository.asignaciones()
            val pendientes = repository.protocolosPendientes()
            val error = listOf(clientes, tecnicos, asignaciones, pendientes).filterIsInstance<ResultadoRemoto.Error>().firstOrNull()
            _uiState.value = _uiState.value.copy(
                clientes = (clientes as? ResultadoRemoto.Exito)?.datos ?: _uiState.value.clientes,
                tecnicos = (tecnicos as? ResultadoRemoto.Exito)?.datos ?: _uiState.value.tecnicos,
                asignaciones = (asignaciones as? ResultadoRemoto.Exito)?.datos ?: _uiState.value.asignaciones,
                pendientes = (pendientes as? ResultadoRemoto.Exito)?.datos ?: _uiState.value.pendientes,
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

    fun revisarProtocolo(idProtocolo: Int, aprobar: Boolean) {
        viewModelScope.launch {
            when (val resultado = repository.revisarProtocolo(idProtocolo, aprobar)) {
                is ResultadoRemoto.Exito -> {
                    _uiState.value = _uiState.value.copy(
                        pendientes = _uiState.value.pendientes.filterNot { it.protocolo.idProtocolo == idProtocolo }
                    )
                    messages.confirmar(if (aprobar) "Protocolo aprobado\n${resultado.datos.numero}" else "Protocolo rechazado\n${resultado.datos.numero}")
                }
                is ResultadoRemoto.Error -> _uiState.value = _uiState.value.copy(error = resultado.mensaje)
            }
        }
    }

    fun nombreEmpresa(idCliente: Int?): String =
        _uiState.value.clientes.firstOrNull { it.idUsuario == idCliente }?.nombre ?: "Empresa #${idCliente ?: "-"}"

    fun nombreTecnico(idTecnico: Int): String =
        _uiState.value.tecnicos.firstOrNull { it.idUsuario == idTecnico }?.nombre ?: "Técnico #$idTecnico"

    fun cerrarSesion() {
        session.logout()
    }
}
