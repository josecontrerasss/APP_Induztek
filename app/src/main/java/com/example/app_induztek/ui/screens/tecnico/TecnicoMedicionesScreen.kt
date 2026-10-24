package com.example.app_induztek.ui.screens.tecnico

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_induztek.ui.components.BotonPrincipalInduztek
import com.example.app_induztek.ui.components.TopBarHeader
import com.example.app_induztek.ui.viewmodel.InduztekViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TecnicoMedicionScreen(
    viewModel: InduztekViewModel,
    onLogout: () -> Unit
) {
    // Agregamos la observación del currentUser para pasárselo al TopBarHeader
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val form by viewModel.formState.collectAsStateWithLifecycle()
    val instalaciones by viewModel.todasLasInstalaciones.collectAsStateWithLifecycle()
    val equipos by viewModel.todosLosEquipos.collectAsStateWithLifecycle()
    val pendientes by viewModel.pendientesSyncCount.collectAsStateWithLifecycle()
    val userMsg by viewModel.userMessage.collectAsStateWithLifecycle()
    val selInstalacion by viewModel.selectedInstalacionId.collectAsStateWithLifecycle()
    val selEquipo by viewModel.selectedEquipoId.collectAsStateWithLifecycle()

    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(userMsg) {
        userMsg?.let { snackbarHost.showSnackbar(it); viewModel.clearUserMessage() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            // Reemplazamos el TopAppBar genérico por el componente de tu compañero
            TopBarHeader(
                nombreTecnico = currentUser?.nombre ?: "Técnico",
                onNotificacionesClick = { /* Acción para la campanita */ }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (pendientes > 0) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (pendientes > 0) "$pendientes mediciones por sincronizar" else "SQLite local sincronizado ✔",
                        color = if (pendientes > 0) Color(0xFFE65100) else Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    if (pendientes > 0) {
                        TextButton(onClick = { viewModel.sincronizarProtocolos() }) {
                            Text("Sincronizar ahora")
                        }
                    }
                }
            }


            Text("Subestación y Equipo:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                instalaciones.forEach { inst ->
                    FilterChip(
                        selected = selInstalacion == inst.id_instalacion,
                        onClick = { viewModel.selectInstalacion(inst.id_instalacion) },
                        label = { Text(inst.codigo) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                equipos.filter { it.id_instalacion == selInstalacion }.forEach { eq ->
                    FilterChip(
                        selected = selEquipo == eq.id_equipo,
                        onClick = { viewModel.selectEquipo(eq.id_equipo) },
                        label = { Text(eq.tag) }
                    )
                }
            }


            Text("Mediciones:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            CampoMedicion("Tensión Primario", form.tensionPrimario, "V") { viewModel.onTensionPrimarioChange(it) }
            CampoMedicion("Tensión Secundario", form.tensionSecundario, "V") { viewModel.onTensionSecundarioChange(it) }
            CampoMedicion("Corriente Medida", form.corriente, "A") { viewModel.onCorrienteChange(it) }
            CampoMedicion("Resistencia Aislamiento", form.resistenciaAislamiento, "MΩ") { viewModel.onResistenciaChange(it) }
            CampoMedicion("Temperatura Aceite", form.temperaturaAceite, "°C", esAlerta = form.isTemperaturaAlerta) {
                viewModel.onTemperaturaChange(it)
            }


            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Observaciones:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("${form.observaciones.length}/200", fontSize = 12.sp, color = Color.Gray)
            }
            OutlinedTextField(
                value = form.observaciones,
                onValueChange = { viewModel.onObservacionesChange(it) },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(8.dp)
            )

            // 5. Botón Guardar
            BotonPrincipalInduztek(
                texto = "GUARDAR MEDICIÓN EN TERRENO",
                onClick = { viewModel.guardarMedicion() }
            )
        }
    }
}

@Composable
private fun CampoMedicion(
    label: String,
    valor: String,
    unidad: String,
    esAlerta: Boolean = false,
    onCambio: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(label) },
        suffix = { Text(unidad, fontWeight = FontWeight.Bold) },
        trailingIcon = {
            Icon(
                imageVector = if (esAlerta) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (esAlerta) Color(0xFFC62828) else Color(0xFF2E7D32)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    )
}
