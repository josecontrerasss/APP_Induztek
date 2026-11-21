package com.example.app_induztek.ui.screens.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_induztek.data.local.entities.InstalacionEntity
import com.example.app_induztek.data.local.entities.ProtocoloEntity
import com.example.app_induztek.ui.viewmodel.ClienteViewModel

private val VerdeInduztek = Color(0xFF1B5E20)
private val VerdeMedio = Color(0xFF2E7D32)
private val ContenedorAmbar = Color(0xFFFFE0B2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteInstalacionesScreen(
    viewModel: ClienteViewModel,
    modifier: Modifier = Modifier
) {
    val instalaciones by viewModel.instalacionesCliente.collectAsStateWithLifecycle()
    val totalInstalaciones by viewModel.totalInstalacionesCliente.collectAsStateWithLifecycle()
    val protocolos by viewModel.protocolosCliente.collectAsStateWithLifecycle()
    val searchQuery by viewModel.clienteSearchQuery.collectAsStateWithLifecycle()
    val perfil by viewModel.perfil.collectAsStateWithLifecycle()
    val instalacionSeleccionada by viewModel.instalacionSeleccionada.collectAsStateWithLifecycle()

    CertificadoEffect(viewModel)

    if (instalacionSeleccionada != null) {
        InstalacionDetalleDialog(
            instalacion = instalacionSeleccionada!!,
            viewModel = viewModel,
            onDismiss = { viewModel.cerrarInstalacion() }
        )
    }

    val busquedaActiva = searchQuery.trim().isNotEmpty()
    val searchTrim = searchQuery.trim()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = VerdeInduztek.copy(alpha = 0.1f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Apartment,
                                contentDescription = null,
                                tint = VerdeInduztek,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Empresa",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = perfil?.nombre?.ifBlank { null } ?: "No informado",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val rut = perfil?.rut?.ifBlank { null }
                        val giro = perfil?.giro?.ifBlank { null }
                        val detalle = buildList {
                            if (rut != null) add("RUT: $rut")
                            if (giro != null) add("Giro: $giro")
                        }.joinToString(" · ")
                        Text(
                            text = if (detalle.isNotBlank()) detalle else "No informado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setClienteSearchQuery(it) },
                placeholder = { Text("Buscar instalación por código o nombre...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setClienteSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpiar búsqueda",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdeInduztek,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        item {
            Text(
                text = "INSTALACIONES",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (instalaciones.isEmpty()) {
            item {
                Text(
                    text = if (busquedaActiva) {
                        "No se encontraron instalaciones con \"$searchTrim\""
                    } else {
                        "No hay instalaciones registradas."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(instalaciones, key = { "inst-${it.id_instalacion}" }) { inst ->
            InstalacionClienteCard(
                instalacion = inst,
                onClick = { viewModel.seleccionarInstalacion(inst) },
                modifier = Modifier.animateItem()
            )
        }

        if (!busquedaActiva && totalInstalaciones > 3) {
            item {
                Text(
                    text = "Mostrando 3 de $totalInstalaciones instalaciones. Usa el buscador para encontrar las demás.",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ÚLTIMOS PROTOCOLOS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (protocolos.isEmpty()) {
            item {
                Text(
                    text = "No hay protocolos registrados.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(protocolos, key = { "prot-${it.id_protocolo}" }) { protocolo ->
            ProtocoloClienteCard(
                protocolo = protocolo,
                onVerDetalle = { viewModel.verDetalleProtocolo(protocolo) },
                onDescargar = { viewModel.descargarCertificado(protocolo) },
                modifier = Modifier.animateItem()
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun InstalacionClienteCard(
    instalacion: InstalacionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${instalacion.codigo} · ${instalacion.nombre}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Última actividad: ${instalacion.ultima_actividad}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = instalacion.ubicacion,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = "Ver detalles",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ProtocoloClienteCard(
    protocolo: ProtocoloEntity,
    onVerDetalle: () -> Unit,
    onDescargar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFormalizado = protocolo.estado == "FORMALIZADO"
    val estadoLabel = if (isFormalizado) "Aprobado" else "En revisión"
    val chipBgColor = if (isFormalizado) Color(0xFFE8F5E9) else ContenedorAmbar
    val chipTextColor = if (isFormalizado) VerdeMedio else Color(0xFF8A6508)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onVerDetalle)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = protocolo.numero,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = protocolo.fecha_hora_prueba,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = chipBgColor,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = estadoLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = chipTextColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(
                    onClick = onDescargar,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFormalizado) Icons.Default.Download else Icons.Default.Visibility,
                        contentDescription = if (isFormalizado) "Descargar PDF" else "Ver protocolo",
                        tint = VerdeInduztek,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
