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
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.app_induztek.ui.viewmodel.ClienteTab
import com.example.app_induztek.ui.viewmodel.TecnicoTab


private val VerdeInduztek = Color(0xFF1B5E20)
private val ContenedorVerde = Color(0xFFC8E6C9)
private val AlertaNaranja = Color(0xFFFB8C00)


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
        ItemNav(
            titulo = "Inicio",
            icono = Icons.Default.Home,
            seleccionado = selectedTab == TecnicoTab.INICIO,
            onClick = { onTabSelected(TecnicoTab.INICIO) }
        )
        ItemNav(
            titulo = "Mediciones",
            icono = Icons.Default.Speed,
            seleccionado = selectedTab == TecnicoTab.MEDICIONES,
            onClick = { onTabSelected(TecnicoTab.MEDICIONES) }
        )
        ItemNav(
            titulo = "Equipos",
            icono = Icons.Default.ElectricalServices,
            seleccionado = selectedTab == TecnicoTab.EQUIPOS,
            onClick = { onTabSelected(TecnicoTab.EQUIPOS) }
        )
        // Pestaña con badge si hay mediciones pendientes offline
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
                indicatorColor = ContenedorVerde,
                selectedIconColor = VerdeInduztek
            )
        )
        ItemNav(
            titulo = "Más",
            icono = Icons.Default.MoreHoriz,
            seleccionado = selectedTab == TecnicoTab.MAS,
            onClick = { onTabSelected(TecnicoTab.MAS) }
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
        ItemNav(
            titulo = "Inicio",
            icono = Icons.Default.Home,
            seleccionado = selectedTab == ClienteTab.INICIO,
            onClick = { onTabSelected(ClienteTab.INICIO) }
        )
        ItemNav(
            titulo = "Protocolos",
            icono = Icons.Default.Description,
            seleccionado = selectedTab == ClienteTab.PROTOCOLOS,
            onClick = { onTabSelected(ClienteTab.PROTOCOLOS) }
        )
        ItemNav(
            titulo = "Descargas",
            icono = Icons.Default.Download,
            seleccionado = selectedTab == ClienteTab.DESCARGAS,
            onClick = { onTabSelected(ClienteTab.DESCARGAS) }
        )
        ItemNav(
            titulo = "Perfil",
            icono = Icons.Default.Person,
            seleccionado = selectedTab == ClienteTab.PERFIL,
            onClick = { onTabSelected(ClienteTab.PERFIL) }
        )
    }
}


@Composable
private fun androidx.compose.foundation.layout.RowScope.ItemNav(
    titulo: String,
    icono: ImageVector,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = seleccionado,
        onClick = onClick,
        icon = { Icon(icono, contentDescription = titulo) },
        label = { Text(titulo) },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = ContenedorVerde,
            selectedIconColor = VerdeInduztek
        )
    )
}

