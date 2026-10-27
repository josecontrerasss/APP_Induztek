package com.example.app_induztek.ui.components

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.app_induztek.ui.theme.AlertaNaranja
import com.example.app_induztek.ui.theme.VerdeFondo
import com.example.app_induztek.ui.theme.VerdeTecnico
import com.example.app_induztek.ui.viewmodel.ClienteTab
import com.example.app_induztek.ui.viewmodel.TecnicoTab

@Composable
fun TecnicoBottomNav(
    selectedTab: TecnicoTab,
    pendientesSyncCount: Int,
    onTabSelected: (TecnicoTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.navigationBarsPadding(),
        containerColor = Color.White
    ) {
        NavigationBarItem(
            selected = selectedTab == TecnicoTab.INICIO,
            onClick = { onTabSelected(TecnicoTab.INICIO) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = VerdeFondo,
                selectedIconColor = VerdeTecnico
            )
        )
        NavigationBarItem(
            selected = selectedTab == TecnicoTab.MEDICIONES,
            onClick = { onTabSelected(TecnicoTab.MEDICIONES) },
            icon = { Icon(Icons.Default.Speed, contentDescription = "Mediciones") },
            label = { Text("Mediciones") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = VerdeFondo,
                selectedIconColor = VerdeTecnico
            )
        )
        NavigationBarItem(
            selected = selectedTab == TecnicoTab.EQUIPOS,
            onClick = { onTabSelected(TecnicoTab.EQUIPOS) },
            icon = { Icon(Icons.Default.ElectricalServices, contentDescription = "Equipos") },
            label = { Text("Equipos") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = VerdeFondo,
                selectedIconColor = VerdeTecnico
            )
        )
        NavigationBarItem(
            selected = selectedTab == TecnicoTab.SINCRONIZAR,
            onClick = { onTabSelected(TecnicoTab.SINCRONIZAR) },
            icon = {
                if (pendientesSyncCount > 0) {
                    BadgedBox(
                        badge = {
                            Badge(containerColor = AlertaNaranja) {
                                Text(pendientesSyncCount.toString(), color = Color.White)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = "Sincronizar")
                    }
                } else {
                    Icon(Icons.Default.Sync, contentDescription = "Sincronizar")
                }
            },
            label = { Text("Sincronizar") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = VerdeFondo,
                selectedIconColor = VerdeTecnico
            )
        )
        NavigationBarItem(
            selected = selectedTab == TecnicoTab.MAS,
            onClick = { onTabSelected(TecnicoTab.MAS) },
            icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "Más") },
            label = { Text("Más") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = VerdeFondo,
                selectedIconColor = VerdeTecnico
            )
        )
    }
}

@Composable
fun ClienteBottomNav(
    selectedTab: ClienteTab,
    onTabSelected: (ClienteTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.navigationBarsPadding(),
        containerColor = Color.White
    ) {
        NavigationBarItem(
            selected = selectedTab == ClienteTab.INICIO,
            onClick = { onTabSelected(ClienteTab.INICIO) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = VerdeFondo,
                selectedIconColor = VerdeTecnico
            )
        )
        NavigationBarItem(
            selected = selectedTab == ClienteTab.PROTOCOLOS,
            onClick = { onTabSelected(ClienteTab.PROTOCOLOS) },
            icon = { Icon(Icons.Default.Description, contentDescription = "Protocolos") },
            label = { Text("Protocolos") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = VerdeFondo,
                selectedIconColor = VerdeTecnico
            )
        )
        NavigationBarItem(
            selected = selectedTab == ClienteTab.DESCARGAS,
            onClick = { onTabSelected(ClienteTab.DESCARGAS) },
            icon = { Icon(Icons.Default.Download, contentDescription = "Descargas") },
            label = { Text("Descargas") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = VerdeFondo,
                selectedIconColor = VerdeTecnico
            )
        )
        NavigationBarItem(
            selected = selectedTab == ClienteTab.PERFIL,
            onClick = { onTabSelected(ClienteTab.PERFIL) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = VerdeFondo,
                selectedIconColor = VerdeTecnico
            )
        )
    }
}

