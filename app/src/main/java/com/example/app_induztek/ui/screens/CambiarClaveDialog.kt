package com.example.app_induztek.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_induztek.ui.viewmodel.AppViewModelProvider
import com.example.app_induztek.ui.viewmodel.PerfilViewModel
import com.example.app_induztek.ui.components.CampoContrasenaInduztek
import com.example.app_induztek.ui.components.RequisitoContrasena

@Composable
fun CambiarClaveDialog(
    onDismiss: () -> Unit,
    viewModel: PerfilViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.cambioClave.collectAsStateWithLifecycle()

    val cerrar: () -> Unit = {
        viewModel.reiniciarCambioClave()
        onDismiss()
    }

    LaunchedEffect(state.exito) {
        if (state.exito) {
            viewModel.reiniciarCambioClave()
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (!state.guardando) {
                cerrar()
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null
            )
        },
        title = {
            Text(text = "Cambiar contraseña")
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CampoContrasenaInduztek(
                    valor = state.claveActual,
                    onValorCambia = viewModel::onClaveActualChange,
                    etiqueta = "Contraseña actual",
                    error = state.claveActualError,
                    imeAction = ImeAction.Next,
                    habilitado = !state.guardando
                )
                CampoContrasenaInduztek(
                    valor = state.claveNueva,
                    onValorCambia = viewModel::onClaveNuevaChange,
                    etiqueta = "Nueva contraseña",
                    error = state.claveNuevaError,
                    imeAction = ImeAction.Next,
                    habilitado = !state.guardando
                )
                CampoContrasenaInduztek(
                    valor = state.confirmarClave,
                    onValorCambia = viewModel::onConfirmarClaveChange,
                    etiqueta = "Repetir nueva contraseña",
                    error = state.confirmarError,
                    imeAction = ImeAction.Done,
                    habilitado = !state.guardando
                )
                RequisitoContrasena(
                    cumple = state.requisitos.longitudOk,
                    texto = "Mínimo 8 caracteres"
                )
                RequisitoContrasena(
                    cumple = state.requisitos.tieneMayuscula,
                    texto = "Al menos 1 mayúscula"
                )
                RequisitoContrasena(
                    cumple = state.requisitos.tieneNumero,
                    texto = "Al menos 1 número"
                )
                RequisitoContrasena(
                    cumple = state.requisitos.tieneEspecial,
                    texto = "Al menos 1 carácter especial"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.guardarClave() },
                enabled = !state.guardando
            ) {
                if (state.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = cerrar,
                enabled = !state.guardando
            ) {
                Text(text = "Cancelar")
            }
        }
    )
}
