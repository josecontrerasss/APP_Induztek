package com.example.app_induztek.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.app_induztek.R

@Composable
fun MensajeError(
    texto: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = texto,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun CampoTextoInduztek(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    error: String? = null,
    ayuda: String? = null,
    habilitado: Boolean = true,
    imeAction: ImeAction = ImeAction.Default
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(imageVector = icono, contentDescription = null) },
        isError = error != null,
        supportingText = when {
            error != null -> {
                { MensajeError(error) }
            }
            ayuda != null -> {
                { Text(ayuda) }
            }
            else -> null
        },
        enabled = habilitado,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado, imeAction = imeAction),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun CampoContrasenaInduztek(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector = Icons.Filled.Lock,
    modifier: Modifier = Modifier,
    error: String? = null,
    habilitado: Boolean = true,
    imeAction: ImeAction = ImeAction.Default
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(imageVector = icono, contentDescription = null) },
        trailingIcon = {
            val iconImage = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
            val description = if (passwordVisible) stringResource(R.string.cd_ocultar_contrasena) else stringResource(R.string.cd_mostrar_contrasena)
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = iconImage, contentDescription = description)
            }
        },
        isError = error != null,
        supportingText = error?.let { { MensajeError(it) } },
        enabled = habilitado,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun RequisitoContrasena(
    cumple: Boolean,
    texto: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (cumple) Icons.Filled.CheckCircle else Icons.Filled.Circle,
            contentDescription = null,
            tint = if (cumple) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodySmall,
            color = if (cumple) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun BotonPrincipalInduztek(
    texto: String,
    onClick: () -> Unit,
    cargando: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !cargando,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(texto)
        }
    }
}
