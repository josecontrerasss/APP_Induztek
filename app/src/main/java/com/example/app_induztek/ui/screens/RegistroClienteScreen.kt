package com.example.app_induztek.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_induztek.R
import com.example.app_induztek.ui.components.BotonPrincipalInduztek
import com.example.app_induztek.ui.viewmodel.RegistroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroClienteScreen(
    viewModel: RegistroViewModel,
    onVolver: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val mostrarErrorRut = state.rut.isNotEmpty() || state.intentoEnviar
    val mostrarErrorCorreo = state.correo.isNotEmpty() || state.intentoEnviar
    val mostrarErrorTelefono = state.telefono.isNotEmpty() || state.intentoEnviar
    val mostrarErrorConfirmar = state.confirmarContrasena.isNotEmpty() || state.intentoEnviar
    val coincidenContrasenas = state.confirmarContrasena.isNotEmpty() && state.confirmarContrasena == state.contrasena

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_registro)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_volver))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.subtitulo_registro),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.desc_registro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedVisibility(visible = !state.registroExitoso) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CampoRegistroInduztek(
                        valor = state.rut,
                        onValorCambia = { viewModel.onRutChange(it) },
                        etiqueta = stringResource(R.string.campo_rut),
                        icono = Icons.Default.Badge,
                        tipoTeclado = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        error = if (mostrarErrorRut) state.rutError else null,
                        habilitado = !state.enviando
                    )

                    CampoRegistroInduztek(
                        valor = state.nombre,
                        onValorCambia = { viewModel.onNombreChange(it) },
                        etiqueta = stringResource(R.string.campo_razon_social),
                        icono = Icons.Default.Business,
                        tipoTeclado = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                        error = null,
                        habilitado = !state.enviando
                    )

                    CampoRegistroInduztek(
                        valor = state.correo,
                        onValorCambia = { viewModel.onCorreoChange(it) },
                        etiqueta = stringResource(R.string.campo_correo),
                        icono = Icons.Default.Email,
                        tipoTeclado = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                        error = if (mostrarErrorCorreo) state.correoError else null,
                        habilitado = !state.enviando
                    )

                    CampoRegistroInduztek(
                        valor = state.telefono,
                        onValorCambia = { viewModel.onTelefonoChange(it) },
                        etiqueta = stringResource(R.string.campo_telefono),
                        icono = Icons.Default.Phone,
                        tipoTeclado = KeyboardType.Phone,
                        imeAction = ImeAction.Next,
                        error = if (mostrarErrorTelefono) state.telefonoError else null,
                        habilitado = !state.enviando
                    )

                    CampoContrasenaRegistro(
                        valor = state.contrasena,
                        onValorCambia = { viewModel.onContrasenaChange(it) },
                        etiqueta = stringResource(R.string.campo_contrasena),
                        error = null,
                        imeAction = ImeAction.Next,
                        habilitado = !state.enviando
                    )

                    CampoContrasenaRegistro(
                        valor = state.confirmarContrasena,
                        onValorCambia = { viewModel.onConfirmarContrasenaChange(it) },
                        etiqueta = "Repetir contraseña",
                        error = if (mostrarErrorConfirmar) state.confirmarError else null,
                        imeAction = ImeAction.Done,
                        habilitado = !state.enviando
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RequisitoContrasena(cumple = state.requisitos.longitudOk, texto = "Mínimo 8 caracteres")
                        RequisitoContrasena(cumple = state.requisitos.tieneNumero, texto = "Al menos 1 número")
                        RequisitoContrasena(cumple = state.requisitos.tieneEspecial, texto = "Al menos 1 carácter especial")
                        RequisitoContrasena(cumple = coincidenContrasenas, texto = "Las contraseñas coinciden")
                    }

                    AnimatedVisibility(visible = state.mensajeError != null) {
                        Text(
                            text = state.mensajeError.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    BotonPrincipalInduztek(
                        texto = "Crear cuenta",
                        cargando = state.enviando,
                        onClick = { viewModel.registrarCliente() }
                    )
                }
            }

            AnimatedVisibility(visible = state.registroExitoso) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )

                    Text(
                        text = "¡Cuenta creada correctamente!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Ya puedes iniciar sesión con tu correo y contraseña.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    BotonPrincipalInduztek(
                        texto = "Ir a iniciar sesión",
                        cargando = false,
                        onClick = {
                            viewModel.limpiarRegistro()
                            onVolver()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CampoRegistroInduztek(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    tipoTeclado: KeyboardType,
    imeAction: ImeAction,
    error: String?,
    habilitado: Boolean
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        modifier = Modifier.fillMaxWidth(),
        enabled = habilitado,
        singleLine = true,
        label = { Text(text = etiqueta) },
        leadingIcon = { Icon(imageVector = icono, contentDescription = null) },
        isError = error != null,
        supportingText = if (error != null) {
            { Text(text = error) }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado, imeAction = imeAction)
    )
}

@Composable
private fun CampoContrasenaRegistro(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    error: String?,
    imeAction: ImeAction,
    habilitado: Boolean
) {
    var visible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        modifier = Modifier.fillMaxWidth(),
        enabled = habilitado,
        singleLine = true,
        label = { Text(text = etiqueta) },
        leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña"
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        isError = error != null,
        supportingText = if (error != null) {
            { Text(text = error) }
        } else {
            null
        }
    )
}

@Composable
private fun RequisitoContrasena(
    cumple: Boolean,
    texto: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (cumple) Icons.Default.CheckCircle else Icons.Default.Circle,
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
