package com.example.app_induztek.ui.screens.supervisor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.app_induztek.data.remote.dto.UsuarioDto
import com.example.app_induztek.ui.components.BotonPrincipalInduztek
import com.example.app_induztek.ui.components.CampoTextoInduztek
import com.example.app_induztek.ui.components.MensajeError
import com.example.app_induztek.ui.viewmodel.CrearTecnicoUiState

@Composable
fun CrearTecnicoFormulario(
    estado: CrearTecnicoUiState,
    tecnicos: List<UsuarioDto>,
    onRutChange: (String) -> Unit,
    onNombreChange: (String) -> Unit,
    onApellidoChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onCrear: () -> Unit,
    onCerrarClave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Crear técnico", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Se genera una contraseña temporal que el técnico cambia en su perfil.",
                    style = MaterialTheme.typography.bodySmall
                )
                CampoTextoInduztek(
                    valor = estado.rut,
                    onValorCambia = onRutChange,
                    etiqueta = "RUT",
                    icono = Icons.Default.Badge,
                    error = estado.rutError,
                    ayuda = "Ejemplo: 15.234.567-4",
                    imeAction = ImeAction.Next
                )
                CampoTextoInduztek(
                    valor = estado.nombre,
                    onValorCambia = onNombreChange,
                    etiqueta = "Primer nombre",
                    icono = Icons.Default.Person,
                    error = estado.nombreError,
                    imeAction = ImeAction.Next
                )
                CampoTextoInduztek(
                    valor = estado.apellido,
                    onValorCambia = onApellidoChange,
                    etiqueta = "Primer apellido",
                    icono = Icons.Default.Person,
                    error = estado.apellidoError,
                    imeAction = ImeAction.Next
                )
                CampoTextoInduztek(
                    valor = estado.telefono,
                    onValorCambia = onTelefonoChange,
                    etiqueta = "Teléfono",
                    icono = Icons.Default.Phone,
                    tipoTeclado = KeyboardType.Phone,
                    error = estado.telefonoError,
                    ayuda = "Ejemplo: +56 9 1234 5678",
                    imeAction = ImeAction.Next
                )
                CampoTextoInduztek(
                    valor = estado.correo,
                    onValorCambia = onCorreoChange,
                    etiqueta = "Correo electrónico",
                    icono = Icons.Default.Email,
                    tipoTeclado = KeyboardType.Email,
                    error = estado.correoError,
                    imeAction = ImeAction.Done
                )
                estado.error?.let { MensajeError(it) }
                BotonPrincipalInduztek(texto = "Crear técnico", onClick = onCrear, cargando = estado.guardando)
            }
        }

        Text("Técnicos registrados (${tecnicos.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        tecnicos.forEach { tecnico ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(tecnico.nombre, fontWeight = FontWeight.SemiBold)
                    Text("${tecnico.rut} · ${tecnico.correo}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    if (estado.claveTemporal != null) {
        AlertDialog(
            onDismissRequest = onCerrarClave,
            confirmButton = { TextButton(onClick = onCerrarClave) { Text("Entendido") } },
            title = { Text("Técnico creado") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${estado.tecnicoCreado} ya puede iniciar sesión con su correo y esta contraseña temporal:")
                    Text(estado.claveTemporal, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Anótala y entrégasela. No se vuelve a mostrar. Podrá cambiarla en su perfil.", style = MaterialTheme.typography.bodySmall)
                }
            }
        )
    }
}
