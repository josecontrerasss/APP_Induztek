package com.example.app_induztek.ui.screens.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_induztek.ui.viewmodel.InduztekViewModel

private val VerdeInduztek = Color(0xFF1B5E20)

@Composable
fun ClienteProtocolosScreen(
    viewModel: InduztekViewModel,
    modifier: Modifier = Modifier
) {
    val protocolos by viewModel.protocolosCliente.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("TODOS") }

    val filteredList = when (selectedFilter) {
        "APROBADO" -> protocolos.filter { it.estado == "FORMALIZADO" }
        "EN_REVISION" -> protocolos.filter { it.estado != "FORMALIZADO" }
        else -> protocolos
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Historial Oficial de Protocolos",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Certificados formalizados y registros en revisión técnica (RN-18 y RN-19).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )


        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = selectedFilter == "TODOS",
                onClick = { selectedFilter = "TODOS" },
                label = { Text("Todos (${protocolos.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = VerdeInduztek,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedFilter == "APROBADO",
                onClick = { selectedFilter = "APROBADO" },
                label = { Text("Aprobados") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = VerdeInduztek,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedFilter == "EN_REVISION",
                onClick = { selectedFilter = "EN_REVISION" },
                label = { Text("En revisión") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = VerdeInduztek,
                    selectedLabelColor = Color.White
                )
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredList) { proto ->
                ProtocoloClienteCard(
                    protocolo = proto,
                    onVerDetalle = { viewModel.verDetalleProtocolo(proto) },
                    onDescargar = { viewModel.simularDescargaCertificado(proto) }
                )
            }
        }
    }
}

@Composable
fun ClientePerfilScreen(
    viewModel: InduztekViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Cuenta Empresa (Cliente)",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        ElevatedCard(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = VerdeInduztek.copy(alpha = 0.12f),
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Apartment,
                                contentDescription = null,
                                tint = VerdeInduztek,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Minera del Norte SpA",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "RUT: 76.543.210-K",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Giro: Minería y Concentrado de Cobre",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                PerfilItem(icon = Icons.Default.Person, title = "Contacto Técnico Responsable", subtitle = "Carlos Mendoza (Jefe Mantenimiento Eléctrico)")
                PerfilItem(icon = Icons.Default.Email, title = "Correo de Notificaciones", subtitle = "contacto@mineranorte.cl")
                PerfilItem(icon = Icons.Default.Phone, title = "Teléfono de Enlace", subtitle = "+56 2 2345 6789")
                PerfilItem(icon = Icons.Default.LocationOn, title = "Región de Operación", subtitle = "Región de Antofagasta, Chile")
                PerfilItem(icon = Icons.Default.Security, title = "Nivel de Acceso", subtitle = "P-04 Cliente Empresa (Acceso Seguro Read-Only RN-18)")
            }
        }
    }
}

@Composable
private fun PerfilItem(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = VerdeInduztek,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = subtitle, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

