package com.example.app_induztek.ui.screens.tecnico

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_induztek.R
import com.example.app_induztek.ui.components.BotonPrincipalInduztek
import com.example.app_induztek.ui.viewmodel.InduztekViewModel

@Composable
fun TecnicoMasScreen(viewModel: InduztekViewModel) {
    val context = LocalContext.current
    var seccionSeleccionada by remember { mutableStateOf(0) }
    val mensajeDenegado = stringResource(R.string.permiso_camara_denegado)

    val protocolos by viewModel.todosLosProtocolos.collectAsStateWithLifecycle()
    val todasLasMediciones by viewModel.todasLasMediciones.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            viewModel.onFotoCapturadaBitmap(it)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, mensajeDenegado, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TabRow(
            selectedTabIndex = seccionSeleccionada,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = seccionSeleccionada == 0,
                onClick = { seccionSeleccionada = 0 },
                text = { Text("Cámara y Evidencia") },
                icon = { Icon(Icons.Default.CameraAlt, contentDescription = null) }
            )
            Tab(
                selected = seccionSeleccionada == 1,
                onClick = { seccionSeleccionada = 1 },
                text = { Text("Historial (${protocolos.size})") },
                icon = { Icon(Icons.Default.History, contentDescription = null) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (seccionSeleccionada) {
            0 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Evidencia Fotográfica",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Captura la placa del equipo o el estado de la instalación.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.LightGray.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (formState.fotoBitmap != null) {
                            Image(
                                bitmap = formState.fotoBitmap!!.asImageBitmap(),
                                contentDescription = "Foto capturada",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Sin foto",
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    BotonPrincipalInduztek(
                        texto = stringResource(R.string.btn_camara),
                        onClick = {
                            val permissionCheckResult = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            )
                            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                cameraLauncher.launch(null)
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    )
                }
            }
            1 -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Historial de Mediciones Locales",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (protocolos.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay mediciones registradas aún.",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(protocolos) { _, protocolo ->
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

                                        // Renderizamos la foto directamente desde el archivo guardado en el teléfono
                                        if (!protocolo.fotoPath.isNullOrEmpty()) {
                                            val bitmapFoto = remember(protocolo.fotoPath) {
                                                BitmapFactory.decodeFile(protocolo.fotoPath)
                                            }
                                            if (bitmapFoto != null) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(120.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                ) {
                                                    Image(
                                                        bitmap = bitmapFoto.asImageBitmap(),
                                                        contentDescription = "Evidencia persistente",
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
            }
        }
    }
}
