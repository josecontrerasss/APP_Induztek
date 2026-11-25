package com.example.app_induztek.iu

import com.example.app_induztek.data.remote.TokenStore
import com.example.app_induztek.data.remote.dto.ProtocoloDto
import com.example.app_induztek.data.remote.dto.ProtocoloSyncDto
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
}
