package com.example.app_induztek.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.app_induztek.ui.viewmodel.UserRole


private val VerdeInduztek = Color(0xFF1B5E20)
private val AlertaNaranja = Color(0xFFFB8C00)
private val EstadoVerde = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarHeader(
    currentRole: UserRole,
    userName: String,
    title: String,
    pendientesSyncCount: Int,
    onSyncClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (pendientesSyncCount > 0) AlertaNaranja else EstadoVerde)
                )
            }
        },
        navigationIcon = {
            Box(
                modifier = Modifier
                    .padding(start = 12.dp, end = 6.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (currentRole == UserRole.TECNICO) Icons.Default.Engineering else Icons.Default.Business,
                    contentDescription = "Rol Activo",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        actions = {

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.18f),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(
                    text = userName,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }


            if (currentRole == UserRole.TECNICO) {
                IconButton(onClick = onSyncClick) {
                    if (pendientesSyncCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = AlertaNaranja) {
                                    Text(pendientesSyncCount.toString(), color = Color.White)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = "Sincronizar", tint = Color.White)
                        }
                    } else {
                        Icon(Icons.Default.CloudDone, contentDescription = "Sincronizado", tint = Color.White.copy(alpha = 0.85f))
                    }
                }
            }


            IconButton(onClick = onNotificationsClick) {
                Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Color.White)
            }


            IconButton(onClick = onLogoutClick) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = VerdeInduztek
        )
    )
}

