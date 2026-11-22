package com.example.app_induztek.ui.screens.tecnico

import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import com.example.app_induztek.ui.viewmodel.RegistroTerrenoViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_induztek.data.local.entities.EquipoEntity
import com.example.app_induztek.data.local.entities.InstalacionEntity
import com.example.app_induztek.ui.viewmodel.TecnicoViewModel

@Composable
fun TecnicoEquiposScreen(
    viewModel: TecnicoViewModel,
    registroViewModel: RegistroTerrenoViewModel
) {
    val formEquipo by registroViewModel.equipoForm.collectAsStateWithLifecycle()
    val instalacionesRegistro by registroViewModel.instalaciones.collectAsStateWithLifecycle()
    val tipos by registroViewModel.tiposEquipo.collectAsStateWithLifecycle()

    RegistrarEquipoDialog(
        estado = formEquipo,
        instalaciones = instalacionesRegistro,
        tipos = tipos,
        onSeleccionarInstalacion = registroViewModel::seleccionarInstalacion,
        onSeleccionarTipo = registroViewModel::seleccionarTipoEquipo,
        onCambio = registroViewModel::onEquipoChange,
        onGuardar = registroViewModel::guardarEquipo,
        onCerrar = registroViewModel::cerrarRegistroEquipo
    )

    val equipos by viewModel.equiposAsignados.collectAsStateWithLifecycle()
    val instalaciones by viewModel.instalacionesAsignadas.collectAsStateWithLifecycle()
    var busqueda by remember { mutableStateOf("") }

    val filtrados = remember(equipos, busqueda) {
        val q = busqueda.trim().lowercase()
        if (q.isEmpty()) equipos else equipos.filter { eq ->
            eq.tag.lowercase().contains(q) ||
                eq.marca.lowercase().contains(q) ||
                eq.modelo.lowercase().contains(q) ||
                eq.numero_serie.lowercase().contains(q)
        }
    }

    val agrupados = remember(filtrados) { filtrados.groupBy { it.id_instalacion } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Directorio de Equipos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Equipos registrados en tus instalaciones",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedButton(
            onClick = registroViewModel::abrirRegistroEquipo,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Registrar Equipo", fontWeight = FontWeight.Bold)
        }

        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            label = { Text("Buscar por tag, marca, modelo o serie") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        )

        if (filtrados.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Dns,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (equipos.isEmpty()) "Aún no hay equipos registrados en tus instalaciones"
                    else "Sin resultados para tu búsqueda",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                val idsInstalaciones = instalaciones.map { it.id_instalacion }.toSet()

                instalaciones.forEach { inst ->
                    val equiposInst = agrupados[inst.id_instalacion].orEmpty()
                    if (equiposInst.isNotEmpty()) {
                        item(key = "enc-${inst.id_instalacion}") {
                            EncabezadoInstalacion(inst)
                        }
                        items(equiposInst, key = { it.id_equipo }) { equipo ->
                            EquipoCard(equipo)
                        }
                    }
                }

                val huerfanos = filtrados.filter { it.id_instalacion !in idsInstalaciones }
                if (huerfanos.isNotEmpty()) {
                    item(key = "enc-sin-inst") {
                        EncabezadoInstalacion(null)
                    }
                    items(huerfanos, key = { it.id_equipo }) { equipo ->
                        EquipoCard(equipo)
                    }
                }
            }
        }
    }
}

@Composable
private fun EncabezadoInstalacion(instalacion: InstalacionEntity?) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp)) {
        Text(
            text = instalacion?.let { "${it.codigo} · ${it.nombre}" } ?: "Sin instalación",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider()
    }
}

@Composable
private fun EquipoCard(equipo: EquipoEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.Dns,
                contentDescription = "Equipo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(equipo.tag, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Marca: ${equipo.marca}", style = MaterialTheme.typography.bodySmall)
                Text("Modelo: ${equipo.modelo}", style = MaterialTheme.typography.bodySmall)
                Text("N° Serie: ${equipo.numero_serie}", style = MaterialTheme.typography.bodySmall)
                Text("Potencia: ${equipo.potencia_kva} kVA", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "Tensiones: ${equipo.tension_primaria_kv} kV / ${equipo.tension_secundaria_kv} kV",
                    style = MaterialTheme.typography.bodySmall
                )
                Text("Corriente nominal: ${equipo.corriente_nominal_a} A", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
