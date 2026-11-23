package com.example.app_induztek.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_induztek.data.local.entities.InstalacionEntity
import com.example.app_induztek.data.local.entities.TipoEquipoEntity
import com.example.app_induztek.data.remote.dto.AsignacionDto
import com.example.app_induztek.data.remote.dto.EquipoCrearDto
import com.example.app_induztek.data.remote.dto.InstalacionCrearDto
import com.example.app_induztek.data.repository.InduztekRepository
import com.example.app_induztek.data.repository.ResultadoRemoto
import com.example.app_induztek.di.SessionManager
import com.example.app_induztek.di.UserMessages
import com.example.app_induztek.ui.util.FiltrosEntrada
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CampoInstalacion { CODIGO, NOMBRE, UBICACION, OBSERVACIONES }

enum class CampoEquipo { TAG, MARCA, MODELO, SERIE, ANIO, POTENCIA, TENSION_PRIMARIA, TENSION_SECUNDARIA, CORRIENTE, OBSERVACIONES }

data class InstalacionFormState(
    val visible: Boolean = false,
    val empresas: List<AsignacionDto> = emptyList(),
    val cargandoEmpresas: Boolean = false,
    val idCliente: Int? = null,
    val valores: Map<CampoInstalacion, String> = emptyMap(),
    val errores: Map<CampoInstalacion, String> = emptyMap(),
    val errorGeneral: String? = null,
    val guardando: Boolean = false
)

data class EquipoFormState(
    val visible: Boolean = false,
    val idInstalacion: Int? = null,
    val idTipoEquipo: Int? = null,
    val valores: Map<CampoEquipo, String> = emptyMap(),
    val errores: Map<CampoEquipo, String> = emptyMap(),
    val errorGeneral: String? = null,
    val guardando: Boolean = false
)

class RegistroTerrenoViewModel(
    private val repository: InduztekRepository,
    private val session: SessionManager,
    private val messages: UserMessages
) : ViewModel() {

    private val _instalacionForm = MutableStateFlow(InstalacionFormState())
    val instalacionForm: StateFlow<InstalacionFormState> = _instalacionForm.asStateFlow()

    private val _equipoForm = MutableStateFlow(EquipoFormState())
    val equipoForm: StateFlow<EquipoFormState> = _equipoForm.asStateFlow()

    val instalaciones: StateFlow<List<InstalacionEntity>> =
        session.delUsuarioActual(emptyList()) { repository.getInstalacionesByTecnico(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tiposEquipo: StateFlow<List<TipoEquipoEntity>> =
        repository.getTiposEquipo().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun abrirRegistroInstalacion() {
        val usuario = session.user.value ?: return
        _instalacionForm.value = InstalacionFormState(visible = true, cargandoEmpresas = true)
        viewModelScope.launch {
            _instalacionForm.value = when (val resultado = repository.empresasAsignadas(usuario.id_usuario)) {
                is ResultadoRemoto.Exito -> _instalacionForm.value.copy(
                    cargandoEmpresas = false,
                    empresas = resultado.datos,
                    idCliente = resultado.datos.singleOrNull()?.idCliente,
                    errorGeneral = if (resultado.datos.isEmpty()) "El supervisor aún no te asigna ninguna empresa." else null
                )
                is ResultadoRemoto.Error -> _instalacionForm.value.copy(cargandoEmpresas = false, errorGeneral = resultado.mensaje)
            }
        }
    }

    fun cerrarRegistroInstalacion() {
        _instalacionForm.value = InstalacionFormState()
    }

    fun seleccionarEmpresa(idCliente: Int) {
        _instalacionForm.value = _instalacionForm.value.copy(idCliente = idCliente, errorGeneral = null)
    }

    fun onInstalacionChange(campo: CampoInstalacion, valor: String) {
        val filtrado = when (campo) {
            CampoInstalacion.CODIGO -> FiltrosEntrada.codigo(valor.uppercase())
            CampoInstalacion.NOMBRE -> FiltrosEntrada.razonSocial(valor)
            CampoInstalacion.UBICACION -> FiltrosEntrada.textoLibre(valor, 120)
            CampoInstalacion.OBSERVACIONES -> FiltrosEntrada.textoLibre(valor, 200)
        }
        val estado = _instalacionForm.value
        _instalacionForm.value = estado.copy(valores = estado.valores + (campo to filtrado), errores = estado.errores - campo)
    }

    fun guardarInstalacion() {
        val estado = _instalacionForm.value
        val usuario = session.user.value ?: return
        if (estado.guardando) return
        val obligatorios = listOf(CampoInstalacion.CODIGO, CampoInstalacion.NOMBRE, CampoInstalacion.UBICACION)
        val errores = obligatorios.filter { estado.valores[it].isNullOrBlank() }.associateWith { "Campo obligatorio" }
        val idCliente = estado.idCliente
        if (errores.isNotEmpty() || idCliente == null) {
            _instalacionForm.value = estado.copy(
                errores = errores,
                errorGeneral = if (idCliente == null) "Elige la empresa" else null
            )
            return
        }
        val solicitud = InstalacionCrearDto(
            idCliente = idCliente,
            codigo = estado.valores[CampoInstalacion.CODIGO].orEmpty().trim(),
            nombre = estado.valores[CampoInstalacion.NOMBRE].orEmpty().trim(),
            ubicacion = estado.valores[CampoInstalacion.UBICACION].orEmpty().trim(),
            observacionesAcceso = estado.valores[CampoInstalacion.OBSERVACIONES]?.trim()?.ifBlank { null }
        )
        viewModelScope.launch {
            _instalacionForm.value = estado.copy(guardando = true, errorGeneral = null)
            when (val resultado = repository.registrarInstalacion(usuario.id_usuario, solicitud)) {
                is ResultadoRemoto.Exito -> {
                    _instalacionForm.value = InstalacionFormState()
                    messages.confirmar("Instalación registrada\n${resultado.datos.codigo}")
                }
                is ResultadoRemoto.Error ->
                    _instalacionForm.value = _instalacionForm.value.copy(guardando = false, errorGeneral = resultado.mensaje)
            }
        }
    }

    fun abrirRegistroEquipo() {
        _equipoForm.value = EquipoFormState(visible = true, idInstalacion = instalaciones.value.singleOrNull()?.id_instalacion)
    }

    fun cerrarRegistroEquipo() {
        _equipoForm.value = EquipoFormState()
    }

    fun seleccionarInstalacion(id: Int) {
        _equipoForm.value = _equipoForm.value.copy(idInstalacion = id, errorGeneral = null)
    }

    fun seleccionarTipoEquipo(id: Int) {
        _equipoForm.value = _equipoForm.value.copy(idTipoEquipo = id, errorGeneral = null)
    }

    fun onEquipoChange(campo: CampoEquipo, valor: String) {
        val filtrado = when (campo) {
            CampoEquipo.TAG -> FiltrosEntrada.codigo(valor, 40)
            CampoEquipo.MARCA -> FiltrosEntrada.razonSocial(valor).take(40)
            CampoEquipo.MODELO -> FiltrosEntrada.codigo(valor, 40)
            CampoEquipo.SERIE -> FiltrosEntrada.codigo(valor, 40)
            CampoEquipo.ANIO -> FiltrosEntrada.soloDigitos(valor, 4)
            CampoEquipo.POTENCIA -> FiltrosEntrada.decimal(valor, 5)
            CampoEquipo.TENSION_PRIMARIA -> FiltrosEntrada.decimal(valor, 3)
            CampoEquipo.TENSION_SECUNDARIA -> FiltrosEntrada.decimal(valor, 3)
            CampoEquipo.CORRIENTE -> FiltrosEntrada.decimal(valor, 4)
            CampoEquipo.OBSERVACIONES -> FiltrosEntrada.textoLibre(valor, 200)
        }
        val estado = _equipoForm.value
        _equipoForm.value = estado.copy(valores = estado.valores + (campo to filtrado), errores = estado.errores - campo)
    }

    fun guardarEquipo() {
        val estado = _equipoForm.value
        if (estado.guardando) return
        val obligatorios = listOf(CampoEquipo.TAG, CampoEquipo.MARCA, CampoEquipo.MODELO, CampoEquipo.SERIE)
        val errores = obligatorios.filter { estado.valores[it].isNullOrBlank() }.associateWith { "Campo obligatorio" }.toMutableMap()
        val anio = estado.valores[CampoEquipo.ANIO]?.toIntOrNull()
        if (anio != null && anio !in 1950..2100) errores[CampoEquipo.ANIO] = "Año entre 1950 y 2100"
        val idInstalacion = estado.idInstalacion
        val idTipo = estado.idTipoEquipo
        if (errores.isNotEmpty() || idInstalacion == null || idTipo == null) {
            _equipoForm.value = estado.copy(
                errores = errores,
                errorGeneral = when {
                    idInstalacion == null -> "Elige la instalación"
                    idTipo == null -> "Elige el tipo de equipo"
                    else -> null
                }
            )
            return
        }
        fun numero(campo: CampoEquipo) = estado.valores[campo]?.toDoubleOrNull()
        val solicitud = EquipoCrearDto(
            tag = estado.valores[CampoEquipo.TAG].orEmpty().trim(),
            idTipoEquipo = idTipo,
            marca = estado.valores[CampoEquipo.MARCA].orEmpty().trim(),
            modelo = estado.valores[CampoEquipo.MODELO].orEmpty().trim(),
            numeroSerie = estado.valores[CampoEquipo.SERIE].orEmpty().trim(),
            anioFabricacion = anio,
            potenciaKva = numero(CampoEquipo.POTENCIA),
            tensionPrimariaKv = numero(CampoEquipo.TENSION_PRIMARIA),
            tensionSecundariaKv = numero(CampoEquipo.TENSION_SECUNDARIA),
            corrienteNominalA = numero(CampoEquipo.CORRIENTE),
            observaciones = estado.valores[CampoEquipo.OBSERVACIONES]?.trim()?.ifBlank { null }
        )
        viewModelScope.launch {
            _equipoForm.value = estado.copy(guardando = true, errorGeneral = null)
            when (val resultado = repository.registrarEquipo(idInstalacion, solicitud)) {
                is ResultadoRemoto.Exito -> {
                    _equipoForm.value = EquipoFormState()
                    messages.confirmar("Equipo registrado\n${resultado.datos.tag}")
                }
                is ResultadoRemoto.Error ->
                    _equipoForm.value = _equipoForm.value.copy(guardando = false, errorGeneral = resultado.mensaje)
            }
        }
    }
}
