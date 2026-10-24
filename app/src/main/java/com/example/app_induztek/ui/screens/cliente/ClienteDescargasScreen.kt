package com.example.app_induztek.ui.screens.cliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_induztek.ui.theme.*
import com.example.app_induztek.ui.viewmodel.InduztekViewModel

@Composable
fun ClienteDescargasScreen(
    viewModel: InduztekViewModel,
    modifier: Modifier = Modifier
) {
    val protocolos by viewModel.protocolosCliente.collectAsStateWithLifecycle()
    val formalizados = protocolos.filter { it.estado == "FORMALIZADO" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Certificados y Protocolos Oficiales (PDF)",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Documentos formalizados por el Ingeniero de Pruebas listos para auditorías y fiscalizaciones SEC.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(formalizados) { proto ->
                ElevatedCard(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFEBEE),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = ErrorRojo,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Certificado_${proto.numero}.pdf",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Generado: ${proto.fecha_hora_prueba}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = ExitoVerde,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Firma Digital Válida",
                                        fontSize = 11.sp,
                                        color = ExitoVerde,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Row {
                            IconButton(onClick = { viewModel.simularDescargaCertificado(proto) }) {
                                Icon(Icons.Default.FileDownload, contentDescription = "Descargar", tint = VerdeTecnico)
                            }
                            IconButton(onClick = { viewModel.simularDescargaCertificado(proto) }) {
                                Icon(Icons.Default.Share, contentDescription = "Compartir", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
