package com.example.app_induztek.ui.screens.cliente

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_induztek.data.local.entities.UsuarioEntity
import com.example.app_induztek.ui.screens.CambiarClaveDialog
import com.example.app_induztek.ui.viewmodel.ClienteViewModel
import com.example.app_induztek.ui.viewmodel.PerfilEditState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_induztek.ui.viewmodel.AppViewModelProvider
import com.example.app_induztek.ui.viewmodel.PerfilViewModel
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.app_induztek.ui.components.CampoTextoInduztek

private val VerdeInduztek = Color(0xFF1B5E20)

@Composable
fun ClienteProtocolosScreen(
    viewModel: ClienteViewModel,
    modifier: Modifier = Modifier
) {
    val protocolos by viewModel.protocolosCliente.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("TODOS") }

    CertificadoEffect(viewModel)

    val filteredList = when (selectedFilter) {
        "APROBADO" -> protocolos.filter { it.estado == "FORMALIZADO" }
        "EN_REVISION" -> protocolos.filter { it.estado != "FORMALIZADO" }
        else -> protocolos
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Historial Oficial de Protocolos",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Certificados formalizados y registros en revisión técnica (RN-18 y RN-19).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = selectedFilter == "TODOS",
                onClick = { selectedFilter = "TODOS" },
                label = { Text("Todos (${protocolos.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = VerdeInduztek,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedFilter == "APROBADO",
                onClick = { selectedFilter = "APROBADO" },
                label = { Text("Aprobados") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = VerdeInduztek,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedFilter == "EN_REVISION",
                onClick = { selectedFilter = "EN_REVISION" },
                label = { Text("En revisión") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = VerdeInduztek,
                    selectedLabelColor = Color.White
                )
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredList.isEmpty()) {
                item {
                    Text(
                        text = "No hay protocolos para este filtro.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(filteredList, key = { it.id_protocolo }) { proto ->
                ProtocoloClienteCard(
                    protocolo = proto,
                    onVerDetalle = { viewModel.verDetalleProtocolo(proto) },
                    onDescargar = { viewModel.descargarCertificado(proto) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
fun ClientePerfilScreen(
    viewModel: ClienteViewModel,
    modifier: Modifier = Modifier,
    perfilViewModel: PerfilViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val perfil by viewModel.perfil.collectAsStateWithLifecycle()
    val tecnicosAsignados by viewModel.tecnicosAsignados.collectAsStateWithLifecycle()
    val perfilEdit by perfilViewModel.perfilEdit.collectAsStateWithLifecycle()
    var mostrarCambioClave by remember { mutableStateOf(false) }

    CertificadoEffect(viewModel)

    if (mostrarCambioClave) {
        CambiarClaveDialog(onDismiss = { mostrarCambioClave = false })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Cuenta Empresa (Cliente)",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        ElevatedCard(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = perfilEdit.editando,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "perfil_modo",
                modifier = Modifier.padding(16.dp)
            ) { editando ->
                if (editando) {
                    PerfilModoEdicion(
                        perfilEdit = perfilEdit,
                        perfil = perfil,
                        viewModel = perfilViewModel
                    )
                } else {
                    PerfilModoLectura(
                        perfil = perfil,
                        tecnicosAsignados = tecnicosAsignados,
                        onEditar = { perfilViewModel.iniciarEdicionPerfil() },
                        onCambiarClave = { mostrarCambioClave = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun PerfilModoLectura(
    perfil: UsuarioEntity?,
    tecnicosAsignados: String?,
    onEditar: () -> Unit,
    onCambiarClave: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = VerdeInduztek.copy(alpha = 0.12f),
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Apartment,
                        contentDescription = null,
                        tint = VerdeInduztek,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = perfil?.nombre?.ifBlank { null } ?: "No informado",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                val rut = perfil?.rut?.ifBlank { null }
                if (rut != null) {
                    Text(
                        text = "RUT: $rut",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                val giro = perfil?.giro?.ifBlank { null }
                if (giro != null) {
                    Text(
                        text = "Giro: $giro",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        HorizontalDivider()

        val contactoNombre = perfil?.contacto_nombre?.ifBlank { null }
        val contactoCargo = perfil?.contacto_cargo?.ifBlank { null }
        val contacto = buildList {
            if (contactoNombre != null) add(contactoNombre)
            if (contactoCargo != null) add("($contactoCargo)")
        }.joinToString(" ")
        PerfilItem(
            icon = Icons.Default.Engineering,
            title = "Técnico Responsable Induztek",
            subtitle = tecnicosAsignados ?: "Cargando..."
        )

        PerfilItem(
            icon = Icons.Default.Person,
            title = "Contacto en la Empresa",
            subtitle = contacto.ifBlank { "No informado" }
        )

        val correo = perfil?.correo?.ifBlank { null }
        if (correo != null) {
            PerfilItem(
                icon = Icons.Default.Email,
                title = "Correo de Notificaciones",
                subtitle = correo
            )
        }

        val telefono = perfil?.telefono?.ifBlank { null }
        if (telefono != null) {
            PerfilItem(
                icon = Icons.Default.Phone,
                title = "Teléfono de Enlace",
                subtitle = telefono
            )
        }

        PerfilItem(
            icon = Icons.Default.Security,
            title = "Nivel de Acceso",
            subtitle = "P-04 Cliente Empresa (Acceso Seguro Read-Only RN-18)"
        )

        OutlinedButton(
            onClick = onEditar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Editar perfil")
        }

        OutlinedButton(
            onClick = onCambiarClave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cambiar contraseña")
        }
    }
}

@Composable
private fun PerfilModoEdicion(
    perfilEdit: PerfilEditState,
    perfil: UsuarioEntity?,
    viewModel: PerfilViewModel
) {
    val habilitado = !perfilEdit.guardando

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CampoTextoInduztek(
            valor = perfilEdit.nombre,
            onValorCambia = { viewModel.onPerfilNombreChange(it) },
            etiqueta = "Razón social",
            icono = Icons.Default.Business,
            modifier = Modifier.fillMaxWidth(),
            error = perfilEdit.nombreError,
            habilitado = habilitado,
            imeAction = ImeAction.Next
        )

        CampoTextoInduztek(
            valor = perfilEdit.correo,
            onValorCambia = { viewModel.onPerfilCorreoChange(it) },
            etiqueta = "Correo",
            icono = Icons.Default.Email,
            modifier = Modifier.fillMaxWidth(),
            tipoTeclado = KeyboardType.Email,
            error = perfilEdit.correoError,
            ayuda = "Este correo también es tu usuario para iniciar sesión.",
            habilitado = habilitado,
            imeAction = ImeAction.Next
        )

        CampoTextoInduztek(
            valor = perfilEdit.telefono,
            onValorCambia = { viewModel.onPerfilTelefonoChange(it) },
            etiqueta = "Teléfono",
            icono = Icons.Default.Phone,
            modifier = Modifier.fillMaxWidth(),
            tipoTeclado = KeyboardType.Phone,
            error = perfilEdit.telefonoError,
            habilitado = habilitado,
            imeAction = ImeAction.Next
        )

        CampoTextoInduztek(
            valor = perfilEdit.giro,
            onValorCambia = { viewModel.onPerfilGiroChange(it) },
            etiqueta = "Giro",
            icono = Icons.Default.Work,
            modifier = Modifier.fillMaxWidth(),
            habilitado = habilitado,
            imeAction = ImeAction.Next
        )

        CampoTextoInduztek(
            valor = perfilEdit.contactoNombre,
            onValorCambia = { viewModel.onPerfilContactoNombreChange(it) },
            etiqueta = "Nombre del contacto",
            icono = Icons.Default.Person,
            modifier = Modifier.fillMaxWidth(),
            habilitado = habilitado,
            imeAction = ImeAction.Next
        )

        CampoTextoInduztek(
            valor = perfilEdit.contactoCargo,
            onValorCambia = { viewModel.onPerfilContactoCargoChange(it) },
            etiqueta = "Cargo del contacto",
            icono = Icons.Default.Badge,
            modifier = Modifier.fillMaxWidth(),
            habilitado = habilitado,
            imeAction = ImeAction.Done
        )

        val rut = perfil?.rut?.ifBlank { null } ?: "No informado"
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = VerdeInduztek,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "RUT (no editable)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = rut,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

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
                    Text("Guardar cambios")
                }
            }
        }
    }
}

@Composable
private fun PerfilItem(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = VerdeInduztek,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = subtitle, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
