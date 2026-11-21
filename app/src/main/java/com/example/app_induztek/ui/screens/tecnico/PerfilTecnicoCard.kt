package com.example.app_induztek.ui.screens.tecnico

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.ui.viewmodel.PerfilEditState
import com.example.app_induztek.ui.viewmodel.PerfilViewModel
import com.example.app_induztek.ui.components.CampoTextoInduztek

@Composable
fun PerfilTecnicoCard(
    viewModel: PerfilViewModel,
    onCambiarClave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val usuario by viewModel.perfil.collectAsStateWithLifecycle()
    val perfilEdit by viewModel.perfilEdit.collectAsStateWithLifecycle()

    ElevatedCard(
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        AnimatedContent(
            targetState = perfilEdit.editando,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "perfil_tecnico",
            modifier = Modifier.padding(16.dp)
        ) { editando ->
            if (editando) {
                PerfilTecnicoEdicion(perfilEdit = perfilEdit, viewModel = viewModel)
            } else {
                PerfilTecnicoLectura(
                    usuario = usuario,
                    onEditar = { viewModel.iniciarEdicionPerfil() },
                    onCambiarClave = onCambiarClave
                )
            }
        }
    }
}

@Composable
private fun PerfilTecnicoLectura(
    usuario: UsuarioEntity?,
    onEditar: () -> Unit,
    onCambiarClave: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Engineering,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = usuario?.nombre ?: "No informado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Técnico de terreno",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider()
        DatoPerfil(
            icono = Icons.Default.Badge,
            titulo = "RUT",
            valor = usuario?.rut?.takeIf { it.isNotBlank() } ?: "No informado"
        )
        DatoPerfil(
            icono = Icons.Default.Email,
            titulo = "Correo",
            valor = usuario?.correo?.takeIf { it.isNotBlank() } ?: "No informado"
        )
        DatoPerfil(
            icono = Icons.Default.Phone,
            titulo = "Teléfono",
            valor = usuario?.telefono?.takeIf { it.isNotBlank() } ?: "No informado"
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = onEditar,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Editar perfil")
            }
            OutlinedButton(
                onClick = onCambiarClave,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("Cambiar clave")
            }
        }
    }
}

@Composable
private fun DatoPerfil(
    icono: ImageVector,
    titulo: String,
    valor: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = titulo,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun PerfilTecnicoEdicion(
    perfilEdit: PerfilEditState,
    viewModel: PerfilViewModel
) {
    val habilitado = !perfilEdit.guardando
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CampoTextoInduztek(
            valor = perfilEdit.nombre,
            onValorCambia = { viewModel.onPerfilNombreChange(it) },
            etiqueta = "Nombre",
            icono = Icons.Default.Person,
            tipoTeclado = KeyboardType.Text,
            error = perfilEdit.nombreError,
            habilitado = habilitado
        )
        CampoTextoInduztek(
            valor = perfilEdit.correo,
            onValorCambia = { viewModel.onPerfilCorreoChange(it) },
            etiqueta = "Correo",
            icono = Icons.Default.Email,
            tipoTeclado = KeyboardType.Email,
            error = perfilEdit.correoError,
            habilitado = habilitado
        )
        CampoTextoInduztek(
            valor = perfilEdit.telefono,
            onValorCambia = { viewModel.onPerfilTelefonoChange(it) },
            etiqueta = "Teléfono",
            icono = Icons.Default.Phone,
            tipoTeclado = KeyboardType.Phone,
            error = perfilEdit.telefonoError,
            habilitado = habilitado
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { viewModel.cancelarEdicionPerfil() },
                enabled = habilitado,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = { viewModel.guardarPerfil() },
                enabled = habilitado,
                modifier = Modifier.weight(1f)
            ) {
                if (perfilEdit.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Guardar")
                }
            }
        }
    }
}
