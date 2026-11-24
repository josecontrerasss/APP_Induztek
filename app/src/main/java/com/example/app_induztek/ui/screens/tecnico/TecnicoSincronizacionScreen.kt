package com.example.app_induztek.ui.screens.tecnico

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_induztek.ui.components.BotonPrincipalInduztek
import com.example.app_induztek.ui.viewmodel.TecnicoViewModel

@Composable
fun TecnicoSincronizacionScreen(
    viewModel: TecnicoViewModel
) {
    val pendientes by viewModel.pendientesSyncCount.collectAsStateWithLifecycle()
    val protocolos by viewModel.protocolosDelTecnico.collectAsStateWithLifecycle()
    val todasLasMediciones by viewModel.medicionesDelTecnico.collectAsStateWithLifecycle()
    val protocolosPendientes = protocolos.filter { !it.sincronizado }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (pendientes > 0) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Marcar como sincronizado",
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFFB8C00)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tienes $pendientes protocolo(s) pendientes de enviar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Al sincronizar, los protocolos guardados en el teléfono se envían al servidor de Induztek. Si no hay conexión, quedan guardados para enviarlos después.",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    BotonPrincipalInduztek(
                        texto = "SINCRONIZAR CON EL SERVIDOR",
                        onClick = { viewModel.sincronizarProtocolos() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Detalle de protocolos pendientes:",
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(protocolosPendientes, key = { it.id_protocolo }) { protocolo ->
                    val medicionesDelProtocolo = todasLasMediciones.filter { it.id_protocolo == protocolo.id_protocolo }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Protocolo: ${protocolo.numero}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = if (protocolo.resultado == "APROBADO") "Valores conformes" else "Con observaciones",
                                    fontWeight = FontWeight.Bold,
                                    color = if (protocolo.resultado == "APROBADO") Color(0xFF4CAF50) else Color(0xFFFB8C00)
                                )
                            }
                            Text(text = "Fecha: ${protocolo.fecha_hora_prueba}", fontSize = 12.sp, color = Color.Gray)

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Valores registrados:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

                            medicionesDelProtocolo.forEach { med ->
                                Text(
                                    text = "• ${med.parametro_nombre}: ${med.valor} ${med.unidad} (${med.estado_alerta})",
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "Temp. Aceite: ${protocolo.temperatura_c}°C", fontSize = 12.sp)
                            Text(text = "Humedad: ${protocolo.humedad_pct}%", fontSize = 12.sp)
                            Text(text = "Obs: ${protocolo.observaciones}", fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = "Todo al día",
                    modifier = Modifier.size(100.dp),
                    tint = Color(0xFF4CAF50)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Sin protocolos pendientes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Todos tus protocolos ya fueron enviados al servidor.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
                    color = Color.Gray
                )
            }
        }
    }
}
