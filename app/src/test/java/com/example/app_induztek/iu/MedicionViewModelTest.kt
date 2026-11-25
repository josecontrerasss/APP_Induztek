package com.example.app_induztek.iu

import com.example.app_induztek.data.remote.TokenStore
import com.example.app_induztek.data.repository.InduztekRepository
import com.example.app_induztek.di.SessionManager
import com.example.app_induztek.di.UserMessages
import com.example.app_induztek.ui.viewmodel.MedicionViewModel
import io.kotest.matchers.shouldBe
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
class MedicionViewModelTest {

    private lateinit var viewModel: MedicionViewModel

    @Before
    fun preparar() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = MedicionViewModel(mockk<InduztekRepository>(), SessionManager(mockk<TokenStore>(relaxed = true)), UserMessages())
    }

    @After
    fun limpiar() {
        Dispatchers.resetMain()
    }

    @Test
    fun temperaturaSobre45GradosActivaLaAlerta() {
        viewModel.onTemperaturaChange("46")

        viewModel.formState.value.isTemperaturaAlerta shouldBe true
    }

    @Test
    fun temperaturaBajoElUmbralNoActivaLaAlerta() {
        viewModel.onTemperaturaChange("40")

        viewModel.formState.value.isTemperaturaAlerta shouldBe false
    }

    @Test
    fun humedadMayorA100EsInvalida() {
        viewModel.onHumedadChange("150")

        viewModel.formState.value.isHumedadOk shouldBe false
    }
}
