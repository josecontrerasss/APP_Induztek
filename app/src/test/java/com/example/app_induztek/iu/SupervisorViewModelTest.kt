package com.example.app_induztek.iu

import com.example.app_induztek.data.remote.TokenStore
import com.example.app_induztek.data.remote.dto.ProtocoloDto
import com.example.app_induztek.data.remote.dto.ProtocoloSyncDto
import com.example.app_induztek.data.remote.dto.TecnicoCrearDto
import com.example.app_induztek.data.remote.dto.TecnicoCreadoDto
import com.example.app_induztek.data.remote.dto.UsuarioDto
import com.example.app_induztek.data.repository.InduztekRepository
import com.example.app_induztek.data.repository.ResultadoRemoto
import com.example.app_induztek.di.SessionManager
import com.example.app_induztek.di.UserMessages
import com.example.app_induztek.ui.viewmodel.SupervisorViewModel
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SupervisorViewModelTest {

    private lateinit var repository: InduztekRepository
    private lateinit var viewModel: SupervisorViewModel

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = mockk()
        viewModel = SupervisorViewModel(repository, SessionManager(mockk<TokenStore>(relaxed = true)), UserMessages())
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    private fun protocolo(id: Int, numero: String) = ProtocoloDto(
        idProtocolo = id,
        numero = numero,
        idVisita = 1,
        idEquipo = 1,
        idUsuarioRegistra = 1,
        fechaHoraPrueba = "2026-11-24 10:00",
        temperaturaC = 30.0,
        humedadPct = 50.0,
        descripcionMantenimiento = null,
        observaciones = null,
        estado = "PENDIENTE_REVISION",
        resultado = "APROBADO",
        sincronizado = true,
        fotoPath = null
    )

    @Test
    fun asignarSinElegirEmpresaNiTecnicoMuestraErrorYNoLlamaAlServidor() {
        viewModel.asignar()

        viewModel.uiState.value.error shouldBe "Elige una empresa y un técnico"
        coVerify(exactly = 0) { repository.crearAsignacion(any(), any()) }
    }

    @Test
    fun alAprobarUnProtocoloDesapareceDeLosPendientes() {
        val pendientes = listOf(
            ProtocoloSyncDto(protocolo(1, "PRT-001"), null),
            ProtocoloSyncDto(protocolo(2, "PRT-002"), null)
        )
        coEvery { repository.usuariosPorRol(any()) } returns ResultadoRemoto.Exito(emptyList())
        coEvery { repository.asignaciones() } returns ResultadoRemoto.Exito(emptyList())
        coEvery { repository.protocolosPendientes() } returns ResultadoRemoto.Exito(pendientes)
        coEvery { repository.revisarProtocolo(1, true) } returns ResultadoRemoto.Exito(protocolo(1, "PRT-001"))
        viewModel.cargar()

        viewModel.revisarProtocolo(1, aprobar = true)

        viewModel.uiState.value.pendientes.map { it.protocolo.numero } shouldBe listOf("PRT-002")
    }

    @Test
    fun crearTecnicoConDatosInvalidosMuestraErroresYNoLlamaAlServidor() {
        viewModel.onTecnicoRutChange("11.111.111-1")
        viewModel.onTecnicoNombreChange("P")
        viewModel.onTecnicoApellidoChange("Soto")
        viewModel.onTecnicoTelefonoChange("1111111")
        viewModel.onTecnicoCorreoChange("1111@asdas.cl")

        viewModel.crearTecnico()

        val estado = viewModel.crearTecnicoState.value
        estado.rutError shouldBe "RUT inválido"
        estado.nombreError shouldBe "Solo letras, entre 2 y 30"
        estado.apellidoError shouldBe null
        estado.telefonoError shouldBe "Teléfono inválido. Ejemplo: +56 9 1234 5678"
        estado.correoError shouldBe "Correo electrónico inválido"
        coVerify(exactly = 0) { repository.crearTecnico(any()) }
    }

    @Test
    fun crearTecnicoValidoEnviaRutFormateadoYMuestraLaClaveTemporal() {
        val creado = UsuarioDto(20, 1, "15.234.567-4", "Pedro Soto", "pedro.soto@induztek.cl", "+56 9 8765 4321", true, null, null, null)
        coEvery {
            repository.crearTecnico(TecnicoCrearDto("15.234.567-4", "Pedro", "Soto", "+56 9 8765 4321", "pedro.soto@induztek.cl"))
        } returns ResultadoRemoto.Exito(TecnicoCreadoDto(creado, "Tec123456!"))
        viewModel.onTecnicoRutChange("152345674")
        viewModel.onTecnicoNombreChange("Pedro")
        viewModel.onTecnicoApellidoChange("Soto")
        viewModel.onTecnicoTelefonoChange("+56 9 8765 4321")
        viewModel.onTecnicoCorreoChange("Pedro.Soto@Induztek.cl")

        viewModel.crearTecnico()

        viewModel.crearTecnicoState.value.claveTemporal shouldBe "Tec123456!"
        viewModel.crearTecnicoState.value.tecnicoCreado shouldBe "Pedro Soto"
        viewModel.uiState.value.tecnicos.map { it.nombre } shouldBe listOf("Pedro Soto")
    }
}
