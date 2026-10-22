package com.example.app_induztek.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_induztek.ui.components.BotonPrincipalInduztek
import com.example.app_induztek.ui.components.CampoContrasenaInduztek
import com.example.app_induztek.ui.components.CampoTextoInduztek
import com.example.app_induztek.ui.viewmodel.InduztekViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroClienteScreen(
    viewModel: InduztekViewModel,
    onVolver: () -> Unit
) {
    val state by viewModel.registroState.collectAsStateWithLifecycle()

    LaunchedEffect(state.registroExitoso) {
        if (state.registroExitoso) {
            viewModel.limpiarRegistro()
            onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Cliente") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Crea tu cuenta de empresa",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            CampoTextoInduztek(
                valor = state.rut,
                onValorCambia = { viewModel.onRegistroCampoCambia(rut = it) },
                etiqueta = "RUT Empresa",
                icono = Icons.Default.Badge
            )

            CampoTextoInduztek(
                valor = state.nombre,
                onValorCambia = { viewModel.onRegistroCampoCambia(nombre = it) },
                etiqueta = "Razón Social / Nombre",
                icono = Icons.Default.Business
            )

            CampoTextoInduztek(
                valor = state.correo,
                onValorCambia = { viewModel.onRegistroCampoCambia(correo = it) },
                etiqueta = "Correo Electrónico",
                icono = Icons.Default.Email,
                tipoTeclado = KeyboardType.Email
            )

            CampoTextoInduztek(
                valor = state.telefono,
                onValorCambia = { viewModel.onRegistroCampoCambia(telefono = it) },
                etiqueta = "Teléfono de Contacto",
                icono = Icons.Default.Phone,
                tipoTeclado = KeyboardType.Phone
            )

            CampoContrasenaInduztek(
                valor = state.contrasena,
                onValorCambia = { viewModel.onRegistroCampoCambia(contrasena = it) },
                etiqueta = "Contraseña",
                icono = Icons.Default.Lock
            )

            if (state.mensajeError != null) {
                Text(
                    text = state.mensajeError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            BotonPrincipalInduztek(
                texto = "REGISTRARME",
                onClick = { viewModel.registrarCliente() }
            )
        }
    }
}