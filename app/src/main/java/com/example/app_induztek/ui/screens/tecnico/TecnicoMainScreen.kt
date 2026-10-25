package com.example.app_induztek.ui.screens.tecnico

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.app_induztek.ui.viewmodel.InduztekViewModel

@Composable
fun TecnicoMainScreen(
    viewModel: InduztekViewModel,
    onLogout: () -> Unit
) {
    var pestañaSeleccionada by remember { mutableIntStateOf(1) }

    val items = listOf(
        Pair("Inicio", Icons.Default.Home),
        Pair("Mediciones", Icons.Default.Analytics),
        Pair("Equipos", Icons.Default.Dns),
        Pair("Sincronizar", Icons.Default.Sync),
        Pair("Más", Icons.Default.MoreVert)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.second, contentDescription = item.first) },
                        label = { Text(item.first) },
                        selected = pestañaSeleccionada == index,
                        onClick = { pestañaSeleccionada = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (pestañaSeleccionada) {
                0 -> TecnicoInicioScreen(
                    viewModel = viewModel,
                    onNavigateToMediciones = { pestañaSeleccionada = 1 }
                )
                1 -> TecnicoMedicionScreen(viewModel = viewModel, onLogout = onLogout)
                2 -> TecnicoEquiposScreen(viewModel = viewModel)
                3 -> TecnicoSincronizacionScreen(viewModel = viewModel)
                4 -> TecnicoMasScreen()
            }
        }
    }
}