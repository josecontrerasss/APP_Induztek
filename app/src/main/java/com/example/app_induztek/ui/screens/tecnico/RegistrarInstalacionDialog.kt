package com.example.app_induztek.ui.screens.tecnico

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import com.example.app_induztek.ui.components.BotonPrincipalInduztek
import com.example.app_induztek.ui.components.CampoTextoInduztek
import com.example.app_induztek.ui.components.MensajeError
import com.example.app_induztek.ui.viewmodel.CampoInstalacion
import com.example.app_induztek.ui.viewmodel.InstalacionFormState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegistrarInstalacionDialog(
    estado: InstalacionFormState,
    onSeleccionarEmpresa: (Int) -> Unit,
    onCambio: (CampoInstalacion, String) -> Unit,
    onGuardar: () -> Unit,
    onCerrar: () -> Unit
) {
    if (!estado.visible) return

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Registrar instalación") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Empresa", style = MaterialTheme.typography.labelLarge)
                if (estado.cargandoEmpresas) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        estado.empresas.forEach { empresa ->
                            FilterChip(
                                selected = empresa.idCliente == estado.idCliente,
                                onClick = { onSeleccionarEmpresa(empresa.idCliente) },
                                label = { Text(empresa.nombreCliente) }
                            )
                        }
                    }
                }
                CampoTextoInduztek(
                    valor = estado.valores[CampoInstalacion.CODIGO].orEmpty(),
                    onValorCambia = { onCambio(CampoInstalacion.CODIGO, it) },
                    etiqueta = "Código (ej: SE-MX-01)",
                    icono = Icons.Default.Tag,
                    error = estado.errores[CampoInstalacion.CODIGO]
                )
                CampoTextoInduztek(
                    valor = estado.valores[CampoInstalacion.NOMBRE].orEmpty(),
                    onValorCambia = { onCambio(CampoInstalacion.NOMBRE, it) },
                    etiqueta = "Nombre de la instalación",
                    icono = Icons.Default.Factory,
                    error = estado.errores[CampoInstalacion.NOMBRE]
                )
                CampoTextoInduztek(
                    valor = estado.valores[CampoInstalacion.UBICACION].orEmpty(),
                    onValorCambia = { onCambio(CampoInstalacion.UBICACION, it) },
                    etiqueta = "Ubicación",
                    icono = Icons.Default.Place,
                    error = estado.errores[CampoInstalacion.UBICACION]
                )
                CampoTextoInduztek(
                    valor = estado.valores[CampoInstalacion.OBSERVACIONES].orEmpty(),
                    onValorCambia = { onCambio(CampoInstalacion.OBSERVACIONES, it) },
                    etiqueta = "Observaciones de acceso (opcional)",
                    icono = Icons.Default.Info,
                    error = estado.errores[CampoInstalacion.OBSERVACIONES]
                )
                estado.errorGeneral?.let { MensajeError(it) }
            }
        },
        confirmButton = {
            Box(modifier = Modifier.width(140.dp)) {
                BotonPrincipalInduztek(
                    texto = "Guardar",
                    onClick = onGuardar,
                    cargando = estado.guardando
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) {
                Text("Cancelar")
            }
        }
    )
}
