package com.example.app_induztek

import com.example.app_induztek.ui.screens.supervisor.SupervisorScreen
import com.example.app_induztek.ui.viewmodel.SupervisorViewModel
import com.example.app_induztek.ui.viewmodel.RegistroTerrenoViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.ui.components.*
import com.example.app_induztek.ui.screens.LoginScreen
import com.example.app_induztek.ui.screens.RecuperarClaveScreen
import com.example.app_induztek.ui.screens.RegistroClienteScreen
import com.example.app_induztek.ui.screens.cliente.*
import com.example.app_induztek.ui.screens.tecnico.*
import com.example.app_induztek.ui.theme.MiRegistroTheme
import com.example.app_induztek.ui.viewmodel.ClienteTab
import com.example.app_induztek.ui.viewmodel.AppViewModelProvider
import com.example.app_induztek.ui.viewmodel.LoginViewModel
import com.example.app_induztek.ui.viewmodel.RegistroViewModel
import com.example.app_induztek.ui.viewmodel.ClienteViewModel
import com.example.app_induztek.ui.viewmodel.MedicionViewModel
import com.example.app_induztek.ui.viewmodel.ShellViewModel
import com.example.app_induztek.ui.viewmodel.TecnicoViewModel
import com.example.app_induztek.ui.viewmodel.TecnicoTab
import com.example.app_induztek.ui.viewmodel.UserRole
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            MiRegistroTheme {
                MainNavigationRoot()
            }
        }
    }
}

enum class AuthDestino { LOGIN, REGISTRO, RECUPERAR }

@Composable
fun MainNavigationRoot(
    loginViewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory),
    registroViewModel: RegistroViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val currentUser by loginViewModel.currentUser.collectAsStateWithLifecycle()
    val rolActual by loginViewModel.currentRole.collectAsStateWithLifecycle()
    var destino by remember { mutableStateOf(AuthDestino.LOGIN) }

    AnimatedContent(
        targetState = currentUser to destino,
        transitionSpec = {
            val haciaAdelante = targetState.second.ordinal >= initialState.second.ordinal
            val direccion = if (haciaAdelante) 1 else -1
            (fadeIn(tween(250)) + slideInHorizontally(tween(300)) { ancho -> direccion * ancho / 4 }) togetherWith
                (fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { ancho -> -direccion * ancho / 4 })
        },
        label = "AuthFlow"
    ) { (user, pantalla) ->
        when {
            user != null && rolActual == UserRole.SUPERVISOR -> SupervisorRaiz()
            user != null -> InduztekApp(user = user)
            pantalla == AuthDestino.REGISTRO -> {
                BackHandler { destino = AuthDestino.LOGIN }
                RegistroClienteScreen(
                    viewModel = registroViewModel,
                    onVolver = { destino = AuthDestino.LOGIN }
                )
            }
            pantalla == AuthDestino.RECUPERAR -> {
                BackHandler {
                    loginViewModel.reiniciarRecuperacion()
                    destino = AuthDestino.LOGIN
                }
                RecuperarClaveScreen(
                    viewModel = loginViewModel,
                    onVolver = { destino = AuthDestino.LOGIN }
                )
            }
            else -> LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = { destino = AuthDestino.LOGIN },
                onRegistroClick = { destino = AuthDestino.REGISTRO },
                onRecuperarClick = { destino = AuthDestino.RECUPERAR }
            )
        }
    }
}

@Composable
fun SupervisorRaiz(
    supervisorViewModel: SupervisorViewModel = viewModel(factory = AppViewModelProvider.Factory),
    shellViewModel: ShellViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by supervisorViewModel.uiState.collectAsStateWithLifecycle()
    val confirmacion by shellViewModel.confirmacion.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { supervisorViewModel.cargar() }

    Box(modifier = Modifier.fillMaxSize()) {
        SupervisorScreen(
            nombre = supervisorViewModel.nombreSupervisor,
            estado = estado,
            onSeleccionarCliente = supervisorViewModel::seleccionarCliente,
            onSeleccionarTecnico = supervisorViewModel::seleccionarTecnico,
            onAsignar = supervisorViewModel::asignar,
            onEliminar = supervisorViewModel::eliminar,
            onRecargar = supervisorViewModel::cargar,
            onCerrarSesion = supervisorViewModel::cerrarSesion,
            onAprobarProtocolo = { supervisorViewModel.revisarProtocolo(it, aprobar = true) },
            onRechazarProtocolo = { supervisorViewModel.revisarProtocolo(it, aprobar = false) },
            nombreEmpresa = supervisorViewModel::nombreEmpresa,
            nombreTecnico = supervisorViewModel::nombreTecnico
        )
        ConfirmacionAnimada(
            mensaje = confirmacion,
            onTerminar = { shellViewModel.limpiarConfirmacion() }
        )
    }
}

@Composable
fun InduztekApp(
    user: UsuarioEntity,
    shellViewModel: ShellViewModel = viewModel(factory = AppViewModelProvider.Factory),
    medicionViewModel: MedicionViewModel = viewModel(factory = AppViewModelProvider.Factory),
    tecnicoViewModel: TecnicoViewModel = viewModel(factory = AppViewModelProvider.Factory),
    registroTerrenoViewModel: RegistroTerrenoViewModel = viewModel(factory = AppViewModelProvider.Factory),
    clienteViewModel: ClienteViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val currentRole by shellViewModel.currentRole.collectAsStateWithLifecycle()
    val tecnicoTab by shellViewModel.tecnicoTab.collectAsStateWithLifecycle()
    val clienteTab by shellViewModel.clienteTab.collectAsStateWithLifecycle()
    val pendientesSyncCount by tecnicoViewModel.pendientesSyncCount.collectAsStateWithLifecycle()
    val selectedProtocoloDetalle by clienteViewModel.selectedProtocoloDetalle.collectAsStateWithLifecycle()
    val userMessage by shellViewModel.userMessage.collectAsStateWithLifecycle()
    val confirmacion by shellViewModel.confirmacion.collectAsStateWithLifecycle()

    CertificadoEffect(viewModel = clienteViewModel)

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            shellViewModel.clearUserMessage()
        }
    }

    BackHandler(enabled = (currentRole == UserRole.TECNICO && tecnicoTab != TecnicoTab.MEDICIONES) || (currentRole == UserRole.CLIENTE && clienteTab != ClienteTab.INICIO)) {
        if (currentRole == UserRole.TECNICO) {
            shellViewModel.setTecnicoTab(TecnicoTab.MEDICIONES)
        } else {
            shellViewModel.setClienteTab(ClienteTab.INICIO)
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
        UserRole.SUPERVISOR -> "Supervisor"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBarHeader(
                currentRole = currentRole,
                userName = user.nombre,
                title = topBarTitle,
                pendientesSyncCount = pendientesSyncCount,
                onSyncClick = { tecnicoViewModel.sincronizarProtocolos() },
                onNotificationsClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("No hay alertas críticas pendientes.")
                    }
                },
                onLogoutClick = { shellViewModel.cerrarSesion() }
            )
        },
        bottomBar = {
            when (currentRole) {
                UserRole.TECNICO -> {
                    TecnicoBottomNav(
                        selectedTab = tecnicoTab,
                        pendientesSyncCount = pendientesSyncCount,
                        onTabSelected = { shellViewModel.setTecnicoTab(it) }
                    )
                }
                UserRole.CLIENTE -> {
                    ClienteBottomNav(
                        selectedTab = clienteTab,
                        onTabSelected = { shellViewModel.setClienteTab(it) }
                    )
                }
                UserRole.SUPERVISOR -> Unit
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = Triple(currentRole, tecnicoTab, clienteTab),
                transitionSpec = {
                    val desde = if (initialState.first == UserRole.TECNICO) initialState.second.ordinal else initialState.third.ordinal
                    val hacia = if (targetState.first == UserRole.TECNICO) targetState.second.ordinal else targetState.third.ordinal
                    val direccion = if (hacia >= desde) 1 else -1
                    (fadeIn(tween(220)) + slideInHorizontally(tween(260)) { ancho -> direccion * ancho / 6 }) togetherWith
                        fadeOut(tween(160))
                },
                label = "Pestanas"
            ) { (rol, tabTecnico, tabCliente) ->
                when (rol) {
                    UserRole.TECNICO -> {
                        when (tabTecnico) {
                            TecnicoTab.INICIO -> TecnicoInicioScreen(
                                viewModel = tecnicoViewModel,
                                registroViewModel = registroTerrenoViewModel,
                                onNavigateToMediciones = { shellViewModel.setTecnicoTab(TecnicoTab.MEDICIONES) }
                            )
                            TecnicoTab.MEDICIONES -> TecnicoMedicionScreen(
                                viewModel = medicionViewModel,
                                pendientes = pendientesSyncCount,
                                onSincronizar = { tecnicoViewModel.sincronizarProtocolos() },
                                onLogout = { shellViewModel.cerrarSesion() }
                            )
                            TecnicoTab.EQUIPOS -> TecnicoEquiposScreen(viewModel = tecnicoViewModel, registroViewModel = registroTerrenoViewModel)
                            TecnicoTab.SINCRONIZAR -> TecnicoSincronizacionScreen(viewModel = tecnicoViewModel)
                            TecnicoTab.MAS -> TecnicoMasScreen(viewModel = tecnicoViewModel)
                        }
                    }
                    UserRole.CLIENTE -> {
                        when (tabCliente) {
                            ClienteTab.INICIO -> ClienteInstalacionesScreen(viewModel = clienteViewModel)
                            ClienteTab.PROTOCOLOS -> ClienteProtocolosScreen(viewModel = clienteViewModel)
                            ClienteTab.DESCARGAS -> ClienteDescargasScreen(viewModel = clienteViewModel)
                            ClienteTab.PERFIL -> ClientePerfilScreen(viewModel = clienteViewModel)
                        }
                    }
                    UserRole.SUPERVISOR -> Unit
                }
            }

            selectedProtocoloDetalle?.let { proto ->
                ProtocoloDetalleDialog(
                    protocolo = proto,
                    viewModel = clienteViewModel,
                    onDismiss = { clienteViewModel.cerrarDetalleProtocolo() }
                )
            }

            ConfirmacionAnimada(
                mensaje = confirmacion,
                onTerminar = { shellViewModel.limpiarConfirmacion() }
            )
        }
    }
}