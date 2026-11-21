package com.example.app_induztek.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_induztek.data.local.entities.EquipoEntity
import com.example.app_induztek.data.local.entities.InstalacionEntity
import com.example.app_induztek.data.local.entities.MedicionEntity
import com.example.app_induztek.data.local.entities.ProtocoloEntity
import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.data.repository.InduztekRepository
import com.example.app_induztek.data.repository.ResultadoPerfil
import com.example.app_induztek.di.SessionManager
import com.example.app_induztek.di.UserMessages
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CertificadoData(
    val protocolo: ProtocoloEntity,
    val equipo: EquipoEntity?,
    val instalacion: InstalacionEntity?,
    val mediciones: List<MedicionEntity>,
    val cliente: UsuarioEntity?
)

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

@OptIn(ExperimentalCoroutinesApi::class)
class ClienteViewModel(
    private val repository: InduztekRepository,
    private val session: SessionManager,
    private val messages: UserMessages
) : ViewModel() {

    private val _clienteSearchQuery = MutableStateFlow("")
    val clienteSearchQuery: StateFlow<String> = _clienteSearchQuery.asStateFlow()

    val perfil: StateFlow<UsuarioEntity?> = session.user
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val instalacionesDelCliente: StateFlow<List<InstalacionEntity>> = session.user
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getInstalacionesByCliente(user.id_usuario)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalInstalacionesCliente: StateFlow<Int> = instalacionesDelCliente
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val instalacionesCliente: StateFlow<List<InstalacionEntity>> = combine(
        instalacionesDelCliente,
        _clienteSearchQuery
    ) { list, query ->
        val q = query.trim()
        if (q.isBlank()) {
            list.take(MAX_INSTALACIONES_INICIALES)
        } else {
            list.filter {
                it.nombre.contains(q, ignoreCase = true) ||
                        it.codigo.contains(q, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val protocolosCliente: StateFlow<List<ProtocoloEntity>> = session.user
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getProtocolosCliente(user.id_usuario)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todasLasInstalaciones: StateFlow<List<InstalacionEntity>> = repository.todasLasInstalaciones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todosLosEquipos: StateFlow<List<EquipoEntity>> = repository.todosLosEquipos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todasLasMediciones: StateFlow<List<MedicionEntity>> = repository.todasLasMediciones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProtocoloDetalle = MutableStateFlow<ProtocoloEntity?>(null)
    val selectedProtocoloDetalle: StateFlow<ProtocoloEntity?> = _selectedProtocoloDetalle.asStateFlow()

    private val _instalacionSeleccionada = MutableStateFlow<InstalacionEntity?>(null)
    val instalacionSeleccionada: StateFlow<InstalacionEntity?> = _instalacionSeleccionada.asStateFlow()

    val equiposInstalacionSeleccionada: StateFlow<List<EquipoEntity>> = _instalacionSeleccionada
        .flatMapLatest { inst ->
            if (inst == null) flowOf(emptyList())
            else repository.getEquiposByInstalacion(inst.id_instalacion)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val protocolosInstalacionSeleccionada: StateFlow<List<ProtocoloEntity>> = combine(
        protocolosCliente,
        equiposInstalacionSeleccionada
    ) { protocolos, equipos ->
        val ids = equipos.map { it.id_equipo }.toSet()
        protocolos.filter { it.id_equipo in ids }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _certificadoPendiente = MutableStateFlow<CertificadoData?>(null)
    val certificadoPendiente: StateFlow<CertificadoData?> = _certificadoPendiente.asStateFlow()

    private val _perfilEdit = MutableStateFlow(PerfilEditState())
    val perfilEdit: StateFlow<PerfilEditState> = _perfilEdit.asStateFlow()

    init {
        viewModelScope.launch {
            session.user.collect { user ->
                if (user == null) {
                    _instalacionSeleccionada.value = null
                    _selectedProtocoloDetalle.value = null
                    _certificadoPendiente.value = null
                    _perfilEdit.value = PerfilEditState()
                }
            }
        }
    }

    fun setClienteSearchQuery(query: String) {
        _clienteSearchQuery.value = query
    }

    fun verDetalleProtocolo(protocolo: ProtocoloEntity) {
        _selectedProtocoloDetalle.value = protocolo
    }

    fun cerrarDetalleProtocolo() {
        _selectedProtocoloDetalle.value = null
    }

    fun seleccionarInstalacion(i: InstalacionEntity) {
        _instalacionSeleccionada.value = i
    }

    fun cerrarInstalacion() {
        _instalacionSeleccionada.value = null
    }

    fun descargarCertificado(protocolo: ProtocoloEntity) {
        if (protocolo.estado != "FORMALIZADO") {
            messages.show("Protocolo en revisión: el certificado estará disponible cuando sea formalizado.")
            return
        }
        viewModelScope.launch {
            val mediciones = repository.getMedicionesByProtocolo(protocolo.id_protocolo).first()
            val equipo = todosLosEquipos.value.find { it.id_equipo == protocolo.id_equipo }
            val instalacion = todasLasInstalaciones.value.find { it.id_instalacion == equipo?.id_instalacion }
            _certificadoPendiente.value = CertificadoData(
                protocolo = protocolo,
                equipo = equipo,
                instalacion = instalacion,
                mediciones = mediciones,
                cliente = session.user.value
            )
        }
    }

    fun certificadoEntregado() {
        _certificadoPendiente.value = null
    }

    fun certificadoFallido() {
        messages.show("No se pudo generar el certificado.")
        _certificadoPendiente.value = null
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
        _perfilEdit.value = _perfilEdit.value.copy(nombre = value, nombreError = null)
    }

    fun onPerfilCorreoChange(value: String) {
        _perfilEdit.value = _perfilEdit.value.copy(correo = value, correoError = null)
    }

    fun onPerfilTelefonoChange(value: String) {
        _perfilEdit.value = _perfilEdit.value.copy(telefono = value, telefonoError = null)
    }

    fun onPerfilGiroChange(value: String) {
        _perfilEdit.value = _perfilEdit.value.copy(giro = value)
    }

    fun onPerfilContactoNombreChange(value: String) {
        _perfilEdit.value = _perfilEdit.value.copy(contactoNombre = value)
    }

    fun onPerfilContactoCargoChange(value: String) {
        _perfilEdit.value = _perfilEdit.value.copy(contactoCargo = value)
    }

    fun cancelarEdicionPerfil() {
        _perfilEdit.value = PerfilEditState()
    }

    fun guardarPerfil() {
        val current = _perfilEdit.value
        if (current.guardando) return

        val user = session.user.value ?: return

        val nombreTrim = current.nombre.trim()
        val correoTrim = current.correo.trim()
        val telefonoTrim = current.telefono.trim()

        val nombreError = if (nombreTrim.isBlank()) "El nombre es obligatorio." else null
        val correoError = if (correoTrim.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(correoTrim).matches()) {
            "Ingresa un correo válido."
        } else null
        val digitos = telefonoTrim.filter { it.isDigit() }
        val telefonoError = if (telefonoTrim.isNotEmpty() && (digitos.length < 8 || digitos.length > 12)) {
            "El teléfono debe tener entre 8 y 12 dígitos."
        } else null

        if (nombreError != null || correoError != null || telefonoError != null) {
            _perfilEdit.value = current.copy(
                nombreError = nombreError,
                correoError = correoError,
                telefonoError = telefonoError
            )
            return
        }

        val actualizado = user.copy(
            nombre = nombreTrim,
            correo = correoTrim,
            telefono = telefonoTrim,
            giro = current.giro.trim().ifBlank { null },
            contacto_nombre = current.contactoNombre.trim().ifBlank { null },
            contacto_cargo = current.contactoCargo.trim().ifBlank { null }
        )

        _perfilEdit.value = current.copy(
            guardando = true,
            nombreError = null,
            correoError = null,
            telefonoError = null
        )

        viewModelScope.launch {
            when (val resultado = repository.actualizarPerfil(actualizado)) {
                is ResultadoPerfil.Exito -> {
                    session.actualizarUsuario(resultado.usuario)
                    _perfilEdit.value = PerfilEditState()
                    messages.show("Perfil actualizado.")
                }
                is ResultadoPerfil.CorreoDuplicado -> {
                    _perfilEdit.value = _perfilEdit.value.copy(
                        guardando = false,
                        correoError = "Ese correo ya está en uso."
                    )
                }
                is ResultadoPerfil.Error -> {
                    _perfilEdit.value = _perfilEdit.value.copy(guardando = false)
                    messages.show(resultado.mensaje)
                }
            }
        }
    }

    companion object {
        private const val MAX_INSTALACIONES_INICIALES = 3
    }
}
