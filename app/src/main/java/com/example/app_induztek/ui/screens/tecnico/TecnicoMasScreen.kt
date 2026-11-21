package com.example.app_induztek.ui.screens.tecnico

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_induztek.ui.screens.CambiarClaveDialog
import com.example.app_induztek.ui.viewmodel.TecnicoViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_induztek.ui.viewmodel.AppViewModelProvider
import com.example.app_induztek.ui.viewmodel.FiltroHistorial
import com.example.app_induztek.ui.viewmodel.PerfilViewModel

@Composable
fun TecnicoMasScreen(
    viewModel: TecnicoViewModel,
    perfilViewModel: PerfilViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val protocolos by viewModel.historialFiltrado.collectAsStateWithLifecycle()
    val todasLasMediciones by viewModel.medicionesDelTecnico.collectAsStateWithLifecycle()
    val busqueda by viewModel.busquedaHistorial.collectAsStateWithLifecycle()
    val filtro by viewModel.filtroHistorial.collectAsStateWithLifecycle()
    var mostrarCambioClave by remember { mutableStateOf(false) }

    if (mostrarCambioClave) {
        CambiarClaveDialog(onDismiss = { mostrarCambioClave = false })
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            PerfilTecnicoCard(
                viewModel = perfilViewModel,
                onCambiarClave = { mostrarCambioClave = true }
            )
        }

        item {
            Text(
                text = "Historial de Mediciones Locales",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            BarraFiltroHistorial(
                busqueda = busqueda,
                onBusquedaChange = { viewModel.onBusquedaHistorialChange(it) },
                filtro = filtro,
                onFiltroChange = { viewModel.onFiltroHistorialChange(it) }
            )
        }

        if (protocolos.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (busqueda.isBlank() && filtro == FiltroHistorial.TODOS) {
                            "No hay mediciones registradas aún."
                        } else {
                            "Sin resultados para esta búsqueda o filtro."
                        },
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            itemsIndexed(protocolos, key = { _, protocolo -> protocolo.id_protocolo }) { _, protocolo ->
                val medicionesDelProtocolo = todasLasMediciones.filter { it.id_protocolo == protocolo.id_protocolo }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (protocolo.sincronizado)
                            MaterialTheme.colorScheme.surface
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
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
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (protocolo.sincronizado) Color(0xFF4CAF50) else Color(0xFFFB8C00),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (protocolo.sincronizado) "Sincronizado" else "Pendiente",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (protocolo.sincronizado) Color(0xFF4CAF50) else Color(0xFFFB8C00)
                                )
                            }
                        }

                        Text(text = "Fecha: ${protocolo.fecha_hora_prueba}", fontSize = 11.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(2.dp))
                        medicionesDelProtocolo.forEach { med ->
                            Text(
                                text = "• ${med.parametro_nombre}: ${med.valor} ${med.unidad}",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        }

                        if (protocolo.observaciones.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "Obs: ${protocolo.observaciones}", fontSize = 11.sp, color = Color.Gray)
                        }

                        if (!protocolo.fotoPath.isNullOrEmpty()) {
                            val bitmapFoto = remember(protocolo.fotoPath) {
                                BitmapFactory.decodeFile(protocolo.fotoPath)
                            }
                            if (bitmapFoto != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    Image(
                                        bitmap = bitmapFoto.asImageBitmap(),
                                        contentDescription = "Evidencia fotográfica",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
