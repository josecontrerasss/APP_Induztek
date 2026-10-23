package com.example.app_induztek.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_induztek.R
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
                title = { Text(stringResource(R.string.titulo_registro)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_volver))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                text = stringResource(R.string.subtitulo_registro),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.desc_registro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            CampoTextoInduztek(
                valor = state.rut,
                onValorCambia = { viewModel.onRegistroCampoCambia(rut = it) },
                etiqueta = stringResource(R.string.campo_rut),
                icono = Icons.Default.Badge
            )

            CampoTextoInduztek(
                valor = state.nombre,
                onValorCambia = { viewModel.onRegistroCampoCambia(nombre = it) },
                etiqueta = stringResource(R.string.campo_razon_social),
                icono = Icons.Default.Business
            )

            CampoTextoInduztek(
                valor = state.correo,
                onValorCambia = { viewModel.onRegistroCampoCambia(correo = it) },
                etiqueta = stringResource(R.string.campo_correo),
                icono = Icons.Default.Email,
                tipoTeclado = KeyboardType.Email
            )

            CampoTextoInduztek(
                valor = state.telefono,
                onValorCambia = { viewModel.onRegistroCampoCambia(telefono = it) },
                etiqueta = stringResource(R.string.campo_telefono),
                icono = Icons.Default.Phone,
                tipoTeclado = KeyboardType.Phone
            )

            CampoContrasenaInduztek(
                valor = state.contrasena,
                onValorCambia = { viewModel.onRegistroCampoCambia(contrasena = it) },
                etiqueta = stringResource(R.string.campo_contrasena),
                icono = Icons.Default.Lock
            )

            if (state.mensajeError != null) {
                Text(
                    text = state.mensajeError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            BotonPrincipalInduztek(
                texto = stringResource(R.string.btn_registrarme),
                onClick = { viewModel.registrarCliente() }
            )
        }
    }
}