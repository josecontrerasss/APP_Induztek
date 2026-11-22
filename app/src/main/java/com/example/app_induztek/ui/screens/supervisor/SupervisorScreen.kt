package com.example.app_induztek.ui.screens.supervisor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app_induztek.ui.components.BotonPrincipalInduztek
import com.example.app_induztek.ui.components.MensajeError
import com.example.app_induztek.ui.viewmodel.SupervisorUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SupervisorScreen(
    nombre: String,
    estado: SupervisorUiState,
    onSeleccionarCliente: (Int) -> Unit,
    onSeleccionarTecnico: (Int) -> Unit,
    onAsignar: () -> Unit,
    onEliminar: (Int) -> Unit,
    onRecargar: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = {
                    Column {
                        Text("Supervisor", fontWeight = FontWeight.Bold)
                        Text(nombre, style = MaterialTheme.typography.bodySmall)
                    }
                },
                actions = {
                    IconButton(onClick = onRecargar) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar Sesión")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (estado.cargando) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Asignar técnico a empresa",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Empresa", style = MaterialTheme.typography.labelLarge)
                        if (estado.clientes.isEmpty()) {
                            Text(
                                "No hay empresas registradas",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                estado.clientes.forEach { cliente ->
                                    FilterChip(
                                        selected = cliente.idUsuario == estado.idClienteSeleccionado,
                                        onClick = { onSeleccionarCliente(cliente.idUsuario) },
                                        label = { Text(cliente.nombre) }
                                    )
                                }
                            }
                        }
                        Text("Técnico", style = MaterialTheme.typography.labelLarge)
                        if (estado.tecnicos.isEmpty()) {
                            Text(
                                "No hay técnicos registrados",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                estado.tecnicos.forEach { tecnico ->
                                    FilterChip(
                                        selected = tecnico.idUsuario == estado.idTecnicoSeleccionado,
                                        onClick = { onSeleccionarTecnico(tecnico.idUsuario) },
                                        label = { Text(tecnico.nombre) }
                                    )
                                }
                            }
                        }
                        estado.error?.let { MensajeError(it) }
                        BotonPrincipalInduztek(
                            texto = "Asignar",
                            onClick = onAsignar,
                            cargando = estado.guardando
                        )
                    }
                }
            }
            item {
                Text(
                    "Asignaciones actuales (${estado.asignaciones.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (estado.asignaciones.isEmpty()) {
                item {
                    Text("Aún no hay asignaciones", style = MaterialTheme.typography.bodyMedium)
                }
            }
            items(estado.asignaciones, key = { it.idAsignacion }) { a ->
                Card(modifier = Modifier.fillMaxWidth().animateItem()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Engineering,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(a.nombreTecnico, fontWeight = FontWeight.Bold)
                            Text("→ " + a.nombreCliente, style = MaterialTheme.typography.bodyMedium)
                            a.fechaAsignacion?.let {
                                Text(
                                    "Asignado el $it",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { onEliminar(a.idAsignacion) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar asignación",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
