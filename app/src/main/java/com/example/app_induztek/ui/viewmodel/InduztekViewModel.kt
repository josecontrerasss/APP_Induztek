package com.example.app_induztek.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.app_induztek.data.local.entities.EquipoEntity
import com.example.app_induztek.data.local.entities.InstalacionEntity
import com.example.app_induztek.data.local.entities.MedicionEntity
import com.example.app_induztek.data.local.entities.ProtocoloEntity
import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.data.repository.InduztekRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class UserRole {
    TECNICO,
    CLIENTE
}

enum class TecnicoTab {
    INICIO,
    MEDICIONES,
    EQUIPOS,
    SINCRONIZAR,
    MAS
}

enum class ClienteTab {
    INICIO,
    PROTOCOLOS,
    DESCARGAS,
    PERFIL
}

data class TecnicoFormState(
    val tensionPrimario: String = "13200",
    val tensionSecundario: String = "420",
    val corriente: String = "256.5",
    val resistenciaAislamiento: String = "> 1000",
    val temperaturaAceite: String = "48.2",
    val observaciones: String = "Equipo operando en condiciones normales.",
    val isTensionPrimarioOk: Boolean = true,
    val isTensionSecundarioOk: Boolean = true,
    val isCorrienteOk: Boolean = true,
    val isResistenciaOk: Boolean = true,
    val isTemperaturaAlerta: Boolean = true,
    val latitud: String = "",
    val longitud: String = ""
)

data class RegistroClienteState(
    val rut: String = "",
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val contrasena: String = "",
    val mensajeError: String? = null,
    val registroExitoso: Boolean = false
)

class InduztekViewModel(
    private val repository: InduztekRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<UsuarioEntity?>(null)
    val currentUser: StateFlow<UsuarioEntity?> = _currentUser.asStateFlow()

    private val _loginEmail = MutableStateFlow("")
    val loginEmail: StateFlow<String> = _loginEmail.asStateFlow()

    private val _loginPassword = MutableStateFlow("")
    val loginPassword: StateFlow<String> = _loginPassword.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> = _isLoggingIn.asStateFlow()

    private val _currentRole = MutableStateFlow(UserRole.TECNICO)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _tecnicoTab = MutableStateFlow(TecnicoTab.MEDICIONES)
    val tecnicoTab: StateFlow<TecnicoTab> = _tecnicoTab.asStateFlow()

    private val _clienteTab = MutableStateFlow(ClienteTab.INICIO)
    val clienteTab: StateFlow<ClienteTab> = _clienteTab.asStateFlow()

    val clienteIdActivo = 10

    val todasLasInstalaciones: StateFlow<List<InstalacionEntity>> = repository.todasLasInstalaciones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todosLosEquipos: StateFlow<List<EquipoEntity>> = repository.todosLosEquipos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _clienteSearchQuery = MutableStateFlow("")
    val clienteSearchQuery: StateFlow<String> = _clienteSearchQuery.asStateFlow()

    val instalacionesCliente: StateFlow<List<InstalacionEntity>> = combine(
        repository.getInstalacionesByCliente(clienteIdActivo),
        _clienteSearchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else list.filter {
            it.nombre.contains(query, ignoreCase = true) ||
                    it.codigo.contains(query, ignoreCase = true) ||
                    it.ubicacion.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val protocolosCliente: StateFlow<List<ProtocoloEntity>> = repository.getProtocolosCliente(clienteIdActivo)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todosLosProtocolos: StateFlow<List<ProtocoloEntity>> = repository.todosLosProtocolos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todasLasMediciones: StateFlow<List<MedicionEntity>> = repository.todasLasMediciones
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendientesSyncCount: StateFlow<Int> = repository.countPendientesSincronizar
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _formState = MutableStateFlow(TecnicoFormState())
    val formState: StateFlow<TecnicoFormState> = _formState.asStateFlow()

    private val _selectedInstalacionId = MutableStateFlow<Int?>(1)
    val selectedInstalacionId: StateFlow<Int?> = _selectedInstalacionId.asStateFlow()

    private val _selectedEquipoId = MutableStateFlow<Int?>(1)
    val selectedEquipoId: StateFlow<Int?> = _selectedEquipoId.asStateFlow()

    private val _selectedProtocoloDetalle = MutableStateFlow<ProtocoloEntity?>(null)
    val selectedProtocoloDetalle: StateFlow<ProtocoloEntity?> = _selectedProtocoloDetalle.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _registroState = MutableStateFlow(RegistroClienteState())
    val registroState: StateFlow<RegistroClienteState> = _registroState.asStateFlow()

    fun onLoginEmailChange(email: String) {
        _loginEmail.value = email
        _loginError.value = null
    }

    fun onLoginPasswordChange(password: String) {
        _loginPassword.value = password
        _loginError.value = null
    }

    fun iniciarSesion() {
        val email = _loginEmail.value.trim()
        val password = _loginPassword.value.trim()
        if (email.isBlank() || password.isBlank()) {
            _loginError.value = "Por favor ingresa correo y contraseña"
            return
        }

        viewModelScope.launch {
            _isLoggingIn.value = true
            val user = repository.autenticar(email, password)
            if (user != null) {
                val rolCodigo = repository.getRolDeUsuario(user.id_usuario)
                _currentUser.value = user
                _currentRole.value = if (rolCodigo == "CLIENTE") UserRole.CLIENTE else UserRole.TECNICO
                _loginError.value = null
            } else {
                _loginError.value = "Credenciales incorrectas o usuario inactivo en SQLite."
            }
            _isLoggingIn.value = false
        }
    }

    fun cerrarSesion() {
        _currentUser.value = null
        _loginPassword.value = ""
        _loginError.value = null
    }

    fun setTecnicoTab(tab: TecnicoTab) {
        _tecnicoTab.value = tab
    }

    fun setClienteTab(tab: ClienteTab) {
        _clienteTab.value = tab
    }

    fun selectInstalacion(id: Int) {
        _selectedInstalacionId.value = id
        viewModelScope.launch {
            val equiposInstalacion = repository.getEquiposByInstalacion(id).firstOrNull()
            if (!equiposInstalacion.isNullOrEmpty()) {
                _selectedEquipoId.value = equiposInstalacion.first().id_equipo
            }
        }
    }

    fun selectEquipo(id: Int) {
        _selectedEquipoId.value = id
    }

    fun onTensionPrimarioChange(valor: String) {
        _formState.value = _formState.value.copy(tensionPrimario = valor, isTensionPrimarioOk = valor.isNotBlank())
    }

    fun onTensionSecundarioChange(valor: String) {
        _formState.value = _formState.value.copy(tensionSecundario = valor, isTensionSecundarioOk = valor.isNotBlank())
    }

    fun onCorrienteChange(valor: String) {
        _formState.value = _formState.value.copy(corriente = valor, isCorrienteOk = valor.isNotBlank())
    }

    fun onResistenciaChange(valor: String) {
        _formState.value = _formState.value.copy(resistenciaAislamiento = valor, isResistenciaOk = valor.isNotBlank())
    }

    fun onTemperaturaChange(valor: String) {
        val temp = valor.toDoubleOrNull() ?: 0.0
        _formState.value = _formState.value.copy(temperaturaAceite = valor, isTemperaturaAlerta = temp > 45.0)
    }

    fun onObservacionesChange(valor: String) {
        if (valor.length <= 200) {
            _formState.value = _formState.value.copy(observaciones = valor)
        }
    }

    fun onUbicacionCapturada(lat: String, lon: String) {
        _formState.value = _formState.value.copy(latitud = lat, longitud = lon)
    }

    fun guardarMedicion() {
        viewModelScope.launch {
            val equipoId = _selectedEquipoId.value ?: 1
            val randomSuffix = Random.nextInt(100, 999)
            val numeroProtocolo = "PT-2026-0$randomSuffix"
            val now = SimpleDateFormat("dd MMM. yyyy, HH:mm", Locale("es", "CL")).format(Date())
            val tempVal = _formState.value.temperaturaAceite.toDoubleOrNull() ?: 25.0

            val obsConUbicacion = if (_formState.value.latitud.isNotBlank()) {
                "${_formState.value.observaciones} [GPS: ${_formState.value.latitud}, ${_formState.value.longitud}]"
            } else {
                _formState.value.observaciones
            }

            val nuevoProtocolo = ProtocoloEntity(
                numero = numeroProtocolo,
                id_visita = 1,
                id_equipo = equipoId,
                id_usuario_registra = _currentUser.value?.id_usuario ?: 1,
                fecha_hora_prueba = now,
                temperatura_c = tempVal,
                humedad_pct = 44.0,
                descripcion_mantenimiento = "Mediciones eléctricas de control en terreno.",
                observaciones = obsConUbicacion,
                estado = "REGISTRADO_TERRENO",
                resultado = if (_formState.value.isTemperaturaAlerta) "OBSERVADO" else "APROBADO",
                sincronizado = false
            )

            val mediciones = listOf(
                MedicionEntity(0, 0, "V_PRIM", "Tensión Primario", _formState.value.tensionPrimario, "V", if (_formState.value.isTensionPrimarioOk) "OK" else "ALERTA"),
                MedicionEntity(0, 0, "V_SEC", "Tensión Secundario", _formState.value.tensionSecundario, "V", if (_formState.value.isTensionSecundarioOk) "OK" else "ALERTA"),
                MedicionEntity(0, 0, "I_NOM", "Corriente", _formState.value.corriente, "A", if (_formState.value.isCorrienteOk) "OK" else "ALERTA"),
                MedicionEntity(0, 0, "R_AISL", "Resistencia Aislamiento", _formState.value.resistenciaAislamiento, "MΩ", if (_formState.value.isResistenciaOk) "OK" else "ALERTA"),
                MedicionEntity(0, 0, "T_ACEITE", "Temperatura Aceite", _formState.value.temperaturaAceite, "°C", if (_formState.value.isTemperaturaAlerta) "ALERTA" else "OK")
            )

            repository.guardarMedicionEnTerreno(nuevoProtocolo, mediciones)
            _userMessage.value = "¡Medición guardada en SQLite local! (Protocolo: $numeroProtocolo)"
        }
    }

    fun sincronizarProtocolos() {
        viewModelScope.launch {
            repository.sincronizarProtocolos()
            _userMessage.value = "¡Sincronización completada con éxito!"
        }
    }

    fun simularDescargaCertificado(protocolo: ProtocoloEntity) {
        _userMessage.value = "Descargando certificado oficial ${protocolo.numero}.pdf..."
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

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun onRegistroCampoCambia(
        rut: String = _registroState.value.rut,
        nombre: String = _registroState.value.nombre,
        correo: String = _registroState.value.correo,
        telefono: String = _registroState.value.telefono,
        contrasena: String = _registroState.value.contrasena
    ) {
        _registroState.value = _registroState.value.copy(
            rut = rut, nombre = nombre, correo = correo, telefono = telefono,
            contrasena = contrasena, mensajeError = null
        )
    }

    fun registrarCliente() {
        val state = _registroState.value
        if (state.rut.isBlank() || state.nombre.isBlank() || state.correo.isBlank() ||
            state.contrasena.isBlank()) {
            _registroState.value = state.copy(mensajeError = "RUT, Nombre, Correo y Contraseña son obligatorios")
            return
        }
        viewModelScope.launch {
            try {
                val nuevoCliente = UsuarioEntity(
                    id_rol = 4,
                    rut = state.rut.trim(),
                    nombre = state.nombre.trim(),
                    correo = state.correo.trim(),
                    password_hash = state.contrasena.trim(),
                    telefono = state.telefono.trim(),
                    activo = true
                )
                repository.registrarCliente(nuevoCliente)
                _registroState.value = state.copy(registroExitoso = true, mensajeError = null)
            } catch (e: Exception) {
                _registroState.value = state.copy(mensajeError = "Error: El RUT o Correo ya están registrados")
            }
        }
    }

    fun limpiarRegistro() {
        _registroState.value = RegistroClienteState()
    }
}

class InduztekViewModelFactory(
    private val repository: InduztekRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InduztekViewModel::class.java)) {
            return InduztekViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}