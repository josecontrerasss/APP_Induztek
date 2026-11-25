package com.example.app_induztek.iu

import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.data.remote.TokenStore
import com.example.app_induztek.data.repository.InduztekRepository
import com.example.app_induztek.data.repository.ResultadoLogin
import com.example.app_induztek.di.SessionManager
import com.example.app_induztek.di.UserMessages
import com.example.app_induztek.ui.viewmodel.LoginViewModel
import com.example.app_induztek.ui.viewmodel.UserRole
import io.kotest.matchers.nulls.shouldBeNull
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
class LoginViewModelTest {

    private lateinit var repository: InduztekRepository
    private lateinit var session: SessionManager
    private lateinit var viewModel: LoginViewModel

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = mockk()
        session = SessionManager(mockk<TokenStore>(relaxed = true))
        viewModel = LoginViewModel(repository, session, UserMessages())
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    @Test
    fun conCamposVaciosMuestraErroresYNoConsultaAlServidor() {
        viewModel.iniciarSesion()

        viewModel.uiState.value.emailError shouldBe "Ingresa tu correo"
        viewModel.uiState.value.passwordError shouldBe "Ingresa tu contraseña"
        coVerify(exactly = 0) { repository.autenticar(any(), any()) }
    }

    @Test
    fun conCredencialesCorrectasAbreSesionConElRolDelServidor() {
        val usuario = UsuarioEntity(id_usuario = 10, id_rol = 2, rut = "76.123.456-7", nombre = "Minera del Norte SpA", correo = "cliente@mineranorte.cl")
        coEvery { repository.autenticar("cliente@mineranorte.cl", "Cliente123!") } returns
            ResultadoLogin.Exito(usuario, InduztekRepository.ROL_CLIENTE, sinConexion = false)
        viewModel.onLoginEmailChange("cliente@mineranorte.cl")
        viewModel.onLoginPasswordChange("Cliente123!")

        viewModel.iniciarSesion()

        session.user.value?.id_usuario shouldBe 10
        session.user.value?.correo shouldBe "cliente@mineranorte.cl"
        session.role.value shouldBe UserRole.CLIENTE
        viewModel.uiState.value.error.shouldBeNull()
    }

    @Test
    fun conCredencialesInvalidasMuestraErrorYNoAbreSesion() {
        coEvery { repository.autenticar(any(), any()) } returns ResultadoLogin.CredencialesInvalidas
        viewModel.onLoginEmailChange("falso@induztek.cl")
        viewModel.onLoginPasswordChange("Incorrecta1!")

        viewModel.iniciarSesion()

        viewModel.uiState.value.error shouldBe "Correo o contraseña incorrectos."
        session.user.value.shouldBeNull()
    }

    @Test
    fun sinConexionConElServidorMuestraMensajeYTerminaLaCarga() {
        coEvery { repository.autenticar(any(), any()) } returns ResultadoLogin.SinConexion
        viewModel.onLoginEmailChange("supervisor@induztek.cl")
        viewModel.onLoginPasswordChange("Supervisor123!")

        viewModel.iniciarSesion()

        viewModel.uiState.value.error shouldBe "No hay conexión con el servidor. Revisa tu internet e inténtalo de nuevo."
        viewModel.uiState.value.isLoggingIn shouldBe false
        session.user.value.shouldBeNull()
    }
}
