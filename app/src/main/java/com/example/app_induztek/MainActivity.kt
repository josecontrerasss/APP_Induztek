package com.example.app_induztek

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_induztek.data.local.AppDatabase
import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.data.repository.InduztekRepository
import com.example.app_induztek.ui.components.*
import com.example.app_induztek.ui.screens.LoginScreen
import com.example.app_induztek.ui.screens.RegistroClienteScreen
import com.example.app_induztek.ui.screens.cliente.*
import com.example.app_induztek.ui.screens.tecnico.*
import com.example.app_induztek.ui.theme.MiRegistroTheme
import com.example.app_induztek.ui.viewmodel.ClienteTab
import com.example.app_induztek.ui.viewmodel.InduztekViewModel
import com.example.app_induztek.ui.viewmodel.InduztekViewModelFactory
import com.example.app_induztek.ui.viewmodel.TecnicoTab
import com.example.app_induztek.ui.viewmodel.UserRole
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(this, lifecycleScope)
        val repository = InduztekRepository(database.induztekDao())
        val viewModelFactory = InduztekViewModelFactory(repository)

        setContent {

            MiRegistroTheme {
                val viewModel: InduztekViewModel = viewModel(factory = viewModelFactory)
                MainNavigationRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainNavigationRoot(viewModel: InduztekViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var mostrandoRegistro by remember { mutableStateOf(false) }

    Crossfade(targetState = Pair(currentUser, mostrandoRegistro), label = "AuthFlow") { (user, enRegistro) ->
        when {
            user == null && enRegistro -> {
                BackHandler { mostrandoRegistro = false }
                RegistroClienteScreen(
                    viewModel = viewModel,
                    onVolver = { mostrandoRegistro = false }
                )
            }
            user == null -> {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {  },
                    onNavigateToRegistro = { mostrandoRegistro = true }
                )
            }
            else -> {
                InduztekApp(viewModel = viewModel, user = user)
            }
        }
    }
}

@Composable
fun InduztekApp(viewModel: InduztekViewModel, user: UsuarioEntity) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val tecnicoTab by viewModel.tecnicoTab.collectAsStateWithLifecycle()
    val clienteTab by viewModel.clienteTab.collectAsStateWithLifecycle()
    val pendientesSyncCount by viewModel.pendientesSyncCount.collectAsStateWithLifecycle()
    val selectedProtocoloDetalle by viewModel.selectedProtocoloDetalle.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearUserMessage()
        }
    }

    BackHandler(enabled = (currentRole == UserRole.TECNICO && tecnicoTab != TecnicoTab.MEDICIONES) || (currentRole == UserRole.CLIENTE && clienteTab != ClienteTab.INICIO)) {
        if (currentRole == UserRole.TECNICO) {
            viewModel.setTecnicoTab(TecnicoTab.MEDICIONES)
        } else {
            viewModel.setClienteTab(ClienteTab.INICIO)
        }
    }

    val topBarTitle = when (currentRole) {
        UserRole.TECNICO -> when (tecnicoTab) {
            TecnicoTab.INICIO -> "Inicio Técnico"
            TecnicoTab.MEDICIONES -> "Nueva Medición"
            TecnicoTab.EQUIPOS -> "Equipos en Planta"
            TecnicoTab.SINCRONIZAR -> "Sincronización"
            TecnicoTab.MAS -> "Más Información"
        }
        UserRole.CLIENTE -> when (clienteTab) {
            ClienteTab.INICIO -> "Mis Instalaciones"
            ClienteTab.PROTOCOLOS -> "Protocolos Oficiales"
            ClienteTab.DESCARGAS -> "Descargas PDF"
            ClienteTab.PERFIL -> "Perfil Empresa"
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBarHeader(
                currentRole = currentRole,
                userName = user.nombre,
                title = topBarTitle,
                pendientesSyncCount = pendientesSyncCount,
                onSyncClick = { viewModel.sincronizarProtocolos() },
                onNotificationsClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("No hay alertas críticas pendientes.")
                    }
                },
                onLogoutClick = { viewModel.cerrarSesion() }
            )
        },
        bottomBar = {
            when (currentRole) {
                UserRole.TECNICO -> {
                    TecnicoBottomNav(
                        selectedTab = tecnicoTab,
                        pendientesSyncCount = pendientesSyncCount,
                        onTabSelected = { viewModel.setTecnicoTab(it) }
                    )
                }
                UserRole.CLIENTE -> {
                    ClienteBottomNav(
                        selectedTab = clienteTab,
                        onTabSelected = { viewModel.setClienteTab(it) }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRole) {
                UserRole.TECNICO -> {
                    when (tecnicoTab) {
                        TecnicoTab.INICIO -> TecnicoInicioScreen(
                            viewModel = viewModel,
                            onNavigateToMediciones = { viewModel.setTecnicoTab(TecnicoTab.MEDICIONES) }
                        )
                        TecnicoTab.MEDICIONES -> TecnicoMedicionScreen(
                            viewModel = viewModel,
                            onLogout = { viewModel.cerrarSesion() }
                        )
                        TecnicoTab.EQUIPOS -> TecnicoEquiposScreen(viewModel = viewModel)
                        TecnicoTab.SINCRONIZAR -> TecnicoSincronizacionScreen(viewModel = viewModel)
                        TecnicoTab.MAS -> TecnicoMasScreen(viewModel = viewModel)
                    }
                }
                UserRole.CLIENTE -> {
                    when (clienteTab) {
                        ClienteTab.INICIO -> ClienteInstalacionesScreen(viewModel = viewModel)
                        ClienteTab.PROTOCOLOS -> ClienteProtocolosScreen(viewModel = viewModel)
                        ClienteTab.DESCARGAS -> ClienteDescargasScreen(viewModel = viewModel)
                        ClienteTab.PERFIL -> ClientePerfilScreen(viewModel = viewModel)
                    }
                }
            }

            selectedProtocoloDetalle?.let { proto ->
                ProtocoloDetalleDialog(
                    protocolo = proto,
                    viewModel = viewModel,
                    onDismiss = { viewModel.cerrarDetalleProtocolo() }
                )
            }
        }
    }
}