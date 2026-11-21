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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TecnicoViewModel(
    private val repository: InduztekRepository,
    private val session: SessionManager,
    private val messages: UserMessages
) : ViewModel() {

    val instalacionesAsignadas: StateFlow<List<InstalacionEntity>> = session
        .delUsuarioActual(emptyList()) { repository.getInstalacionesByTecnico(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val equiposAsignados: StateFlow<List<EquipoEntity>> = session
        .delUsuarioActual(emptyList()) { repository.getEquiposByTecnico(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val protocolosDelTecnico: StateFlow<List<ProtocoloEntity>> = session
        .delUsuarioActual(emptyList()) { repository.getProtocolosByTecnico(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medicionesDelTecnico: StateFlow<List<MedicionEntity>> = session
        .delUsuarioActual(emptyList()) { repository.getMedicionesByTecnico(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendientesSyncCount: StateFlow<Int> = session
        .delUsuarioActual(0) { repository.countPendientesByTecnico(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val currentUser: StateFlow<UsuarioEntity?> = session.user

    private val _busquedaHistorial = MutableStateFlow("")
    val busquedaHistorial: StateFlow<String> = _busquedaHistorial.asStateFlow()

    private val _filtroHistorial = MutableStateFlow(FiltroHistorial.TODOS)
    val filtroHistorial: StateFlow<FiltroHistorial> = _filtroHistorial.asStateFlow()

    val historialFiltrado: StateFlow<List<ProtocoloEntity>> = combine(
        protocolosDelTecnico,
        _busquedaHistorial,
        _filtroHistorial
    ) { protocolos, busqueda, filtro ->
        val texto = busqueda.trim()
        protocolos.filter { protocolo ->
            val cumpleFiltro = when (filtro) {
                FiltroHistorial.TODOS -> true
                FiltroHistorial.PENDIENTES -> !protocolo.sincronizado
                FiltroHistorial.SINCRONIZADOS -> protocolo.sincronizado
            }
            val cumpleBusqueda = texto.isEmpty() ||
                protocolo.numero.contains(texto, ignoreCase = true) ||
                protocolo.observaciones.contains(texto, ignoreCase = true)
            cumpleFiltro && cumpleBusqueda
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            session.user.collect { usuario ->
                if (usuario == null) {
                    _busquedaHistorial.value = ""
                    _filtroHistorial.value = FiltroHistorial.TODOS
                }
            }
        }
    }

    fun onBusquedaHistorialChange(valor: String) {
        _busquedaHistorial.value = valor
    }

    fun onFiltroHistorialChange(filtro: FiltroHistorial) {
        _filtroHistorial.value = filtro
    }

    fun sincronizarProtocolos() {
        val usuario = session.user.value ?: return
        viewModelScope.launch {
            repository.sincronizarProtocolos(usuario.id_usuario)
            messages.show("¡Sincronización completada con éxito!")
        }
    }
}
