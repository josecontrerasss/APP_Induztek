package com.example.app_induztek.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.data.repository.InduztekRepository
import com.example.app_induztek.di.SessionManager
import com.example.app_induztek.di.UserMessages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShellViewModel(
    private val repository: InduztekRepository,
    private val session: SessionManager,
    private val messages: UserMessages
) : ViewModel() {

    private val _tecnicoTab = MutableStateFlow(TecnicoTab.MEDICIONES)
    val tecnicoTab: StateFlow<TecnicoTab> = _tecnicoTab.asStateFlow()

    private val _clienteTab = MutableStateFlow(ClienteTab.INICIO)
    val clienteTab: StateFlow<ClienteTab> = _clienteTab.asStateFlow()

    val currentRole: StateFlow<UserRole> = session.role

    val currentUser: StateFlow<UsuarioEntity?> = session.user

    val userMessage: StateFlow<String?> = messages.message

    val pendientesSyncCount: StateFlow<Int> = repository.countPendientesSincronizar
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setTecnicoTab(tab: TecnicoTab) {
        _tecnicoTab.value = tab
    }

    fun setClienteTab(tab: ClienteTab) {
        _clienteTab.value = tab
    }

    fun sincronizarProtocolos() {
        viewModelScope.launch {
            repository.sincronizarProtocolos()
            messages.show("¡Sincronización completada con éxito!")
        }
    }

    fun cerrarSesion() {
        session.logout()
    }

    fun clearUserMessage() {
        messages.clear()
    }
}
