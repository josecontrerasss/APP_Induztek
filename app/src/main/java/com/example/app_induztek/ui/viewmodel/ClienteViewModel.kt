package com.example.app_induztek.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_induztek.data.local.entities.EquipoEntity
import com.example.app_induztek.data.local.entities.InstalacionEntity
import com.example.app_induztek.data.local.entities.MedicionEntity
import com.example.app_induztek.data.local.entities.ProtocoloEntity
import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.data.repository.InduztekRepository
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

    private val _selectedProtocoloDetalle = MutableStateFlow<ProtocoloEntity?>(null)
    val selectedProtocoloDetalle: StateFlow<ProtocoloEntity?> = _selectedProtocoloDetalle.asStateFlow()

    private val _detalleProtocolo = MutableStateFlow<CertificadoData?>(null)
    val detalleProtocolo: StateFlow<CertificadoData?> = _detalleProtocolo.asStateFlow()

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

    init {
        viewModelScope.launch {
            session.user.collect { user ->
                if (user == null) {
                    _instalacionSeleccionada.value = null
                    _selectedProtocoloDetalle.value = null
                    _detalleProtocolo.value = null
                    _certificadoPendiente.value = null
                }
            }
        }
    }

    fun setClienteSearchQuery(query: String) {
        _clienteSearchQuery.value = query
    }

    fun verDetalleProtocolo(protocolo: ProtocoloEntity) {
        _selectedProtocoloDetalle.value = protocolo
        _detalleProtocolo.value = null
        viewModelScope.launch {
            _detalleProtocolo.value = cargarDatosProtocolo(protocolo)
        }
    }

    fun cerrarDetalleProtocolo() {
        _selectedProtocoloDetalle.value = null
        _detalleProtocolo.value = null
    }

    private suspend fun cargarDatosProtocolo(protocolo: ProtocoloEntity): CertificadoData {
        val equipo = repository.getEquipoById(protocolo.id_equipo).first()
        val instalacion = equipo?.let { repository.getInstalacionById(it.id_instalacion).first() }
        return CertificadoData(
            protocolo = protocolo,
            equipo = equipo,
            instalacion = instalacion,
            mediciones = repository.getMedicionesByProtocolo(protocolo.id_protocolo).first(),
            cliente = session.user.value
        )
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
            _certificadoPendiente.value = cargarDatosProtocolo(protocolo)
        }
    }

    fun certificadoEntregado() {
        _certificadoPendiente.value = null
    }

    fun certificadoFallido() {
        messages.show("No se pudo generar el certificado.")
        _certificadoPendiente.value = null
    }

    companion object {
        private const val MAX_INSTALACIONES_INICIALES = 3
    }
}
