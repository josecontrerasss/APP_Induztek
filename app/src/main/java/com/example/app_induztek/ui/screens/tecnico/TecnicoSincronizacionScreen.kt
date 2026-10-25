package com.example.app_induztek.ui.screens.tecnico

import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_induztek.ui.components.BotonPrincipalInduztek
import com.example.app_induztek.ui.viewmodel.InduztekViewModel

@Composable
fun TecnicoSincronizacionScreen(
    viewModel: InduztekViewModel
) {
    val pendientes by viewModel.pendientesSyncCount.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (pendientes > 0) {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = "Subir datos",
                modifier = Modifier.size(100.dp),
                tint = Color(0xFFFB8C00) // Alerta Naranja
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Sincronización Pendiente",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Tienes $pendientes mediciones guardadas localmente esperando ser enviadas al servidor central.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
                color = Color.Gray
            )

            BotonPrincipalInduztek(
                texto = "SINCRONIZAR AHORA",
                onClick = { viewModel.sincronizarProtocolos() }
            )
        } else {
            Icon(
                imageVector = Icons.Default.CloudDone,
                contentDescription = "Todo al día",
                modifier = Modifier.size(100.dp),
                tint = Color(0xFF4CAF50) // Estado Verde
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Base de Datos Actualizada",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Todas tus mediciones en terreno han sido respaldadas correctamente.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
                color = Color.Gray
            )
        }
    }
}

