package com.example.app_induztek.ui.screens.tecnico

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.app_induztek.data.local.entities.InstalacionEntity
import com.example.app_induztek.data.local.entities.TipoEquipoEntity
import com.example.app_induztek.ui.components.BotonPrincipalInduztek
import com.example.app_induztek.ui.components.CampoTextoInduztek
import com.example.app_induztek.ui.components.MensajeError
import com.example.app_induztek.ui.viewmodel.CampoEquipo
import com.example.app_induztek.ui.viewmodel.EquipoFormState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegistrarEquipoDialog(
    estado: EquipoFormState,
    instalaciones: List<InstalacionEntity>,
    tipos: List<TipoEquipoEntity>,
    onSeleccionarInstalacion: (Int) -> Unit,
    onSeleccionarTipo: (Int) -> Unit,
    onCambio: (CampoEquipo, String) -> Unit,
    onGuardar: () -> Unit,
    onCerrar: () -> Unit
) {
    if (!estado.visible) return

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Registrar equipo") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Instalación", style = MaterialTheme.typography.labelLarge)
                if (instalaciones.isEmpty()) {
                    Text(
                        "No tienes instalaciones asignadas",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        instalaciones.forEach { instalacion ->
                            FilterChip(
                                selected = instalacion.id_instalacion == estado.idInstalacion,
                                onClick = { onSeleccionarInstalacion(instalacion.id_instalacion) },
                                label = { Text(instalacion.codigo) }
                            )
                        }
                    }
                }
                Text("Tipo de equipo", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tipos.forEach { tipo ->
                        FilterChip(
                            selected = tipo.id_tipo_equipo == estado.idTipoEquipo,
                            onClick = { onSeleccionarTipo(tipo.id_tipo_equipo) },
                            label = { Text(tipo.nombre) }
                        )
                    }
                }
                CampoTextoInduztek(
                    valor = estado.valores[CampoEquipo.TAG].orEmpty(),
                    onValorCambia = { onCambio(CampoEquipo.TAG, it) },
                    etiqueta = "TAG (ej: Transformador T-03)",
                    icono = Icons.Default.Tag,
                    error = estado.errores[CampoEquipo.TAG]
                )
                CampoTextoInduztek(
                    valor = estado.valores[CampoEquipo.MARCA].orEmpty(),
                    onValorCambia = { onCambio(CampoEquipo.MARCA, it) },
                    etiqueta = "Marca",
                    icono = Icons.Default.Business,
                    error = estado.errores[CampoEquipo.MARCA]
                )
                CampoTextoInduztek(
                    valor = estado.valores[CampoEquipo.MODELO].orEmpty(),
                    onValorCambia = { onCambio(CampoEquipo.MODELO, it) },
                    etiqueta = "Modelo",
                    icono = Icons.Default.Settings,
                    error = estado.errores[CampoEquipo.MODELO]
                )
                CampoTextoInduztek(
                    valor = estado.valores[CampoEquipo.SERIE].orEmpty(),
                    onValorCambia = { onCambio(CampoEquipo.SERIE, it) },
                    etiqueta = "N° de serie",
                    icono = Icons.Default.Numbers,
                    error = estado.errores[CampoEquipo.SERIE]
                )
                CampoTextoInduztek(
                    valor = estado.valores[CampoEquipo.ANIO].orEmpty(),
                    onValorCambia = { onCambio(CampoEquipo.ANIO, it) },
                    etiqueta = "Año de fabricación",
                    icono = Icons.Default.CalendarMonth,
                    tipoTeclado = KeyboardType.Number,
                    error = estado.errores[CampoEquipo.ANIO]
                )
                CampoTextoInduztek(
                    valor = estado.valores[CampoEquipo.POTENCIA].orEmpty(),
                    onValorCambia = { onCambio(CampoEquipo.POTENCIA, it) },
                    etiqueta = "Potencia (kVA)",
                    icono = Icons.Default.Bolt,
                    tipoTeclado = KeyboardType.Decimal,
                    error = estado.errores[CampoEquipo.POTENCIA]
                )
                CampoTextoInduztek(
                    valor = estado.valores[CampoEquipo.TENSION_PRIMARIA].orEmpty(),
                    onValorCambia = { onCambio(CampoEquipo.TENSION_PRIMARIA, it) },
                    etiqueta = "Tensión primaria (kV)",
                    icono = Icons.Default.ElectricBolt,
                    tipoTeclado = KeyboardType.Decimal,
                    error = estado.errores[CampoEquipo.TENSION_PRIMARIA]
                )
                CampoTextoInduztek(
                    valor = estado.valores[CampoEquipo.TENSION_SECUNDARIA].orEmpty(),
                    onValorCambia = { onCambio(CampoEquipo.TENSION_SECUNDARIA, it) },
                    etiqueta = "Tensión secundaria (kV)",
                    icono = Icons.Default.ElectricBolt,
                    tipoTeclado = KeyboardType.Decimal,
                    error = estado.errores[CampoEquipo.TENSION_SECUNDARIA]
                )
                CampoTextoInduztek(
                    valor = estado.valores[CampoEquipo.CORRIENTE].orEmpty(),
                    onValorCambia = { onCambio(CampoEquipo.CORRIENTE, it) },
                    etiqueta = "Corriente nominal (A)",
                    icono = Icons.Default.Speed,
                    tipoTeclado = KeyboardType.Decimal,
                    error = estado.errores[CampoEquipo.CORRIENTE]
                )
                CampoTextoInduztek(
                    valor = estado.valores[CampoEquipo.OBSERVACIONES].orEmpty(),
                    onValorCambia = { onCambio(CampoEquipo.OBSERVACIONES, it) },
                    etiqueta = "Observaciones (opcional)",
                    icono = Icons.Default.Info,
                    error = estado.errores[CampoEquipo.OBSERVACIONES]
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
