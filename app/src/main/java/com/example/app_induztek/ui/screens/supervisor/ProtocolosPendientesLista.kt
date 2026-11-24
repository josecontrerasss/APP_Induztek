package com.example.app_induztek.ui.screens.supervisor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.app_induztek.data.remote.dto.ProtocoloSyncDto

@Composable
fun ProtocolosPendientesLista(
    pendientes: List<ProtocoloSyncDto>,
    nombreEmpresa: (Int?) -> String,
    nombreTecnico: (Int) -> String,
    onAprobar: (Int) -> Unit,
    onRechazar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (pendientes.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.TaskAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "No hay protocolos por revisar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Cuando un técnico sincronice, aparecerán aquí.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pendientes, key = { it.protocolo.idProtocolo }) { item ->
                val p = item.protocolo
                Card(
                    modifier = Modifier.fillMaxWidth().animateItem()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = p.numero,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (p.resultado == "APROBADO") "Valores conformes" else "Con observaciones",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (p.resultado == "APROBADO") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = nombreEmpresa(p.idCliente),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Técnico: " + nombreTecnico(p.idUsuarioRegistra) + " · " + p.fechaHoraPrueba,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        item.mediciones.orEmpty().forEach { m ->
                            Text(
                                text = "• ${m.parametroNombre}: ${m.valor} ${m.unidad}" + if (m.estadoAlerta == "ALERTA") " ⚠" else "",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        p.observaciones?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = "Obs: $it",
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onRechazar(p.idProtocolo) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Rechazar")
                            }
                            Button(
                                onClick = { onAprobar(p.idProtocolo) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Aprobar")
                            }
                        }
                    }
                }
            }
        }
    }
}
