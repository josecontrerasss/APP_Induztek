package com.example.app_induztek.ui.viewmodel

import com.example.app_induztek.ui.util.FiltrosEntrada
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_induztek.data.local.entities.EquipoEntity
import com.example.app_induztek.data.local.entities.InstalacionEntity
import com.example.app_induztek.data.local.entities.MedicionEntity
import com.example.app_induztek.data.local.entities.ProtocoloEntity
import com.example.app_induztek.data.repository.InduztekRepository
import com.example.app_induztek.di.SessionManager
import com.example.app_induztek.di.UserMessages
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedicionViewModel(
    private val repository: InduztekRepository,
    private val session: SessionManager,
    private val messages: UserMessages
) : ViewModel() {

    private val UMBRAL_TEMPERATURA = 45.0

    private val _formState = MutableStateFlow(TecnicoFormState())
    val formState: StateFlow<TecnicoFormState> = _formState.asStateFlow()

    private val _selectedInstalacionId = MutableStateFlow<Int?>(null)
    val selectedInstalacionId: StateFlow<Int?> = _selectedInstalacionId.asStateFlow()

    private val _selectedEquipoId = MutableStateFlow<Int?>(null)
    val selectedEquipoId: StateFlow<Int?> = _selectedEquipoId.asStateFlow()

    val instalacionesAsignadas: StateFlow<List<InstalacionEntity>> = session
        .delUsuarioActual(emptyList()) { repository.getInstalacionesByTecnico(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val equiposAsignados: StateFlow<List<EquipoEntity>> = session
        .delUsuarioActual(emptyList()) { repository.getEquiposByTecnico(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            session.user.collectLatest { usuario ->
                if (usuario == null) {
                    val fotoPendiente = _formState.value.fotoPath
                    if (!fotoPendiente.isNullOrEmpty()) {
                        runCatching { File(fotoPendiente).delete() }
                    }
                    _formState.value = TecnicoFormState()
                    _selectedInstalacionId.value = null
                    _selectedEquipoId.value = null
                } else if (_selectedInstalacionId.value == null && _selectedEquipoId.value == null) {
                    val instalaciones = repository.getInstalacionesByTecnico(usuario.id_usuario).first()
                    val primera = instalaciones.firstOrNull()
                    if (primera != null) {
                        _selectedInstalacionId.value = primera.id_instalacion
                        val equipos = repository.getEquiposByInstalacion(primera.id_instalacion).firstOrNull()
                        _selectedEquipoId.value = equipos?.firstOrNull()?.id_equipo
                    }
                }
            }
        }
    }

    private fun parsearNumero(valor: String): Double? =
        valor.trim().replace(',', '.').toDoubleOrNull()

    private fun validarNumeroOpcional(valor: String, intentoGuardar: Boolean): Boolean {
        if (valor.isBlank()) return !intentoGuardar
        return parsearNumero(valor) != null
    }

    private fun validarResistenciaOpcional(valor: String, intentoGuardar: Boolean): Boolean {
        val v = valor.trim()
        if (v.isEmpty() || v == ">" || v == "<") return !intentoGuardar
        val sinPrefijo = when {
            v.startsWith(">") -> v.substring(1).trim()
            v.startsWith("<") -> v.substring(1).trim()
            else -> v
        }
        if (sinPrefijo.isEmpty()) return !intentoGuardar
        val n = parsearNumero(sinPrefijo) ?: return false
        return n > 0.0
    }

    private fun validarHumedadOpcional(valor: String, intentoGuardar: Boolean): Boolean {
        if (valor.isBlank()) return !intentoGuardar
        val n = parsearNumero(valor) ?: return false
        return n in 0.0..100.0
    }

    private fun recomputarEstados(estado: TecnicoFormState): TecnicoFormState {
        val intento = estado.intentoGuardar
        val tempNum = parsearNumero(estado.temperaturaAceite)
        return estado.copy(
            isTensionPrimarioOk = validarNumeroOpcional(estado.tensionPrimario, intento),
            isTensionSecundarioOk = validarNumeroOpcional(estado.tensionSecundario, intento),
            isCorrienteOk = validarNumeroOpcional(estado.corriente, intento),
            isResistenciaOk = validarResistenciaOpcional(estado.resistenciaAislamiento, intento),
            isTemperaturaOk = validarNumeroOpcional(estado.temperaturaAceite, intento),
            isHumedadOk = validarHumedadOpcional(estado.humedad, intento),
            isTemperaturaAlerta = tempNum != null && tempNum > UMBRAL_TEMPERATURA
        )
    }

    fun selectInstalacion(id: Int) {
        _selectedInstalacionId.value = id
        viewModelScope.launch {
            val equipos = repository.getEquiposByInstalacion(id).firstOrNull()
            _selectedEquipoId.value = equipos?.firstOrNull()?.id_equipo
        }
    }

    fun selectEquipo(id: Int) {
        _selectedEquipoId.value = id
    }

    fun onTensionPrimarioChange(valor: String) {
        _formState.value = recomputarEstados(_formState.value.copy(tensionPrimario = FiltrosEntrada.decimal(valor, 6)))
    }

    fun onTensionSecundarioChange(valor: String) {
        _formState.value = recomputarEstados(_formState.value.copy(tensionSecundario = FiltrosEntrada.decimal(valor, 4)))
    }

    fun onCorrienteChange(valor: String) {
        _formState.value = recomputarEstados(_formState.value.copy(corriente = FiltrosEntrada.decimal(valor, 4)))
    }

    fun onResistenciaChange(valor: String) {
        _formState.value = recomputarEstados(_formState.value.copy(resistenciaAislamiento = FiltrosEntrada.decimal(valor, 5)))
    }

    fun onTemperaturaChange(valor: String) {
        _formState.value = recomputarEstados(_formState.value.copy(temperaturaAceite = FiltrosEntrada.decimal(valor, 3)))
    }

    fun onHumedadChange(valor: String) {
        _formState.value = recomputarEstados(_formState.value.copy(humedad = FiltrosEntrada.decimal(valor, 3)))
    }

    fun onObservacionesChange(valor: String) {
        if (valor.length <= 200) {
            _formState.value = _formState.value.copy(observaciones = valor)
        }
    }

    fun onUbicacionCapturada(lat: String, lon: String) {
        _formState.value = _formState.value.copy(latitud = lat, longitud = lon)
    }

    fun onFotoGuardada(path: String) {
        val anterior = _formState.value.fotoPath
        if (!anterior.isNullOrEmpty() && anterior != path) {
            runCatching { File(anterior).delete() }
        }
        _formState.value = _formState.value.copy(fotoPath = path)
        messages.show("Foto guardada como evidencia.")
    }

    fun onFotoEliminada() {
        val actual = _formState.value.fotoPath
        if (!actual.isNullOrEmpty()) {
            runCatching { File(actual).delete() }
        }
        _formState.value = _formState.value.copy(fotoPath = null)
        messages.show("Foto eliminada.")
    }

    fun onFotoError() {
        messages.show("No se pudo guardar la foto.")
    }

    fun guardarMedicion() {
        if (_formState.value.guardando) return
        viewModelScope.launch {
            _formState.value = recomputarEstados(
                _formState.value.copy(intentoGuardar = true, guardando = true)
            )
            var exito = false
            try {
                val estado = _formState.value
                if (!estado.isTensionPrimarioOk || !estado.isTensionSecundarioOk ||
                    !estado.isCorrienteOk || !estado.isResistenciaOk ||
                    !estado.isTemperaturaOk || !estado.isHumedadOk
                ) {
                    messages.show("Error: Complete todos los campos con valores válidos.")
                    return@launch
                }

                val instalacionId = _selectedInstalacionId.value
                if (instalacionId == null) {
                    messages.show("Error: Debe seleccionar una instalación antes de guardar.")
                    return@launch
                }

                val equipoId = _selectedEquipoId.value
                if (equipoId == null) {
                    messages.show("Error: Debe seleccionar un equipo antes de guardar.")
                    return@launch
                }

                val usuario = session.user.value
                if (usuario == null) {
                    messages.show("Error: No hay una sesión activa para registrar la medición.")
                    return@launch
                }

                val visitaId = repository.getUltimaVisitaIdDeTecnico(instalacionId, usuario.id_usuario)
                if (visitaId == null) {
                    messages.show("No hay una OT registrada para esta instalación.")
                    return@launch
                }

                val numeroProtocolo = "PT-${LocalDate.now().year}-${System.currentTimeMillis()}"
                val now = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd MMM. yyyy, HH:mm", Locale.forLanguageTag("es-CL"))
                )
                val tempVal = parsearNumero(estado.temperaturaAceite) ?: 25.0
                val humedadVal = parsearNumero(estado.humedad) ?: 0.0

                val obsConUbicacion = if (estado.latitud.isNotBlank()) {
                    "${estado.observaciones} [GPS: ${estado.latitud}, ${estado.longitud}]"
                } else {
                    estado.observaciones
                }

                val nuevoProtocolo = ProtocoloEntity(
                    id_protocolo = 0,
                    numero = numeroProtocolo,
                    id_visita = visitaId,
                    id_equipo = equipoId,
                    id_usuario_registra = usuario.id_usuario,
                    fecha_hora_prueba = now,
                    temperatura_c = tempVal,
                    humedad_pct = humedadVal,
                    descripcion_mantenimiento = "Mediciones eléctricas de control en terreno.",
                    observaciones = obsConUbicacion,
                    estado = "REGISTRADO_TERRENO",
                    resultado = if (estado.isTemperaturaAlerta) "OBSERVADO" else "APROBADO",
                    sincronizado = false,
                    fotoPath = estado.fotoPath ?: ""
                )

                val mediciones = listOf(
                    MedicionEntity(0, 0, "V_PRIM", "Tensión Primario", estado.tensionPrimario, "V", if (estado.isTensionPrimarioOk) "OK" else "ALERTA"),
                    MedicionEntity(0, 0, "V_SEC", "Tensión Secundario", estado.tensionSecundario, "V", if (estado.isTensionSecundarioOk) "OK" else "ALERTA"),
                    MedicionEntity(0, 0, "I_NOM", "Corriente", estado.corriente, "A", if (estado.isCorrienteOk) "OK" else "ALERTA"),
                    MedicionEntity(0, 0, "R_AISL", "Resistencia Aislamiento", estado.resistenciaAislamiento, "MΩ", if (estado.isResistenciaOk) "OK" else "ALERTA"),
                    MedicionEntity(0, 0, "T_ACEITE", "Temperatura Aceite", estado.temperaturaAceite, "°C", if (estado.isTemperaturaAlerta) "ALERTA" else "OK"),
                    MedicionEntity(0, 0, "H_REL", "Humedad Relativa", estado.humedad, "%", if (estado.isHumedadOk) "OK" else "ALERTA")
                )

                repository.guardarMedicionEnTerreno(nuevoProtocolo, mediciones)
                messages.confirmar("Medición guardada\nProtocolo $numeroProtocolo")
                exito = true
            } catch (e: Exception) {
                messages.show("No se pudo guardar la medición. Intente nuevamente.")
            } finally {
                if (exito) {
                    _formState.value = TecnicoFormState()
                } else {
                    _formState.value = _formState.value.copy(guardando = false)
                }
            }
        }
    }
}
