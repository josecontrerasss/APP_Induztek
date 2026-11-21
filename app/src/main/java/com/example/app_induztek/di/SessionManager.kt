package com.example.app_induztek.di

import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.ui.viewmodel.UserRole
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class SessionManager {

    private val _user = MutableStateFlow<UsuarioEntity?>(null)
    val user: StateFlow<UsuarioEntity?> = _user.asStateFlow()

    private val _role = MutableStateFlow(UserRole.TECNICO)
    val role: StateFlow<UserRole> = _role.asStateFlow()

    fun login(usuario: UsuarioEntity, role: UserRole) {
        _user.value = usuario
        _role.value = role
    }

    fun logout() {
        _user.value = null
        _role.value = UserRole.TECNICO
    }

    fun actualizarUsuario(usuario: UsuarioEntity) {
        val actual = _user.value ?: return
        if (actual.id_usuario == usuario.id_usuario) {
            _user.value = usuario
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T> delUsuarioActual(sinSesion: T, consulta: (Int) -> Flow<T>): Flow<T> =
        user.flatMapLatest { usuario ->
            if (usuario == null) flowOf(sinSesion) else consulta(usuario.id_usuario)
        }
}
