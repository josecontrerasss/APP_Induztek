package com.example.app_induztek.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Pin
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_induztek.ui.components.BotonPrincipalInduztek
import com.example.app_induztek.ui.components.CampoContrasenaInduztek
import com.example.app_induztek.ui.components.RequisitoContrasena
import com.example.app_induztek.ui.components.CampoTextoInduztek
import com.example.app_induztek.ui.viewmodel.LoginViewModel

private val ColorExito = Color(0xFF2E7D32)
private val ColorAmbar = Color(0xFFB26A00)

@Composable
fun RecuperarClaveScreen(
    viewModel: LoginViewModel,
    onVolver: () -> Unit
) {
    val uiState by viewModel.recuperarState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.restablecido) {
        if (uiState.restablecido) {
            viewModel.reiniciarRecuperacion()
            onVolver()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Text("IN", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Recuperar contraseña",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Ingresa el correo con el que te registraste y te enviaremos un código de 6 dígitos para restablecer tu contraseña.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            CampoTextoInduztek(
                valor = uiState.correo,
                onValorCambia = { viewModel.onRecuperarCorreoChange(it) },
                etiqueta = "Correo electrónico",
                icono = Icons.Default.Email,
                tipoTeclado = KeyboardType.Email,
                error = uiState.correoError
            )

            AnimatedVisibility(
                visible = uiState.mensaje != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val mensaje = uiState.mensaje.orEmpty()
                val esServicioNoDisponible = !uiState.exito &&
                    mensaje == LoginViewModel.MENSAJE_SERVICIO_NO_DISPONIBLE
                val colorFondo = when {
                    uiState.exito -> ColorExito.copy(alpha = 0.12f)
                    esServicioNoDisponible -> ColorAmbar.copy(alpha = 0.14f)
                    else -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                }
                val colorBorde = when {
                    uiState.exito -> ColorExito
                    esServicioNoDisponible -> ColorAmbar
                    else -> MaterialTheme.colorScheme.error
                }
                val colorTexto = when {
                    uiState.exito -> ColorExito
                    esServicioNoDisponible -> ColorAmbar
                    else -> MaterialTheme.colorScheme.error
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = colorFondo,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colorBorde)
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.MarkEmailRead,
                            contentDescription = null,
                            tint = colorTexto,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = mensaje,
                            color = colorTexto,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = uiState.exito, enter = fadeIn(), exit = fadeOut()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoTextoInduztek(
                        valor = uiState.codigo,
                        onValorCambia = { viewModel.onRecuperarCodigoChange(it) },
                        etiqueta = "Código de 6 dígitos",
                        icono = Icons.Default.Pin,
                        tipoTeclado = KeyboardType.NumberPassword,
                        error = uiState.codigoError
                    )
                    CampoContrasenaInduztek(
                        valor = uiState.claveNueva,
                        onValorCambia = { viewModel.onRecuperarClaveNuevaChange(it) },
                        etiqueta = "Nueva contraseña",
                        error = uiState.claveNuevaError
                    )
                    RequisitoContrasena(cumple = uiState.requisitos.longitudOk, texto = "Mínimo 8 caracteres")
                    RequisitoContrasena(cumple = uiState.requisitos.tieneMayuscula, texto = "Al menos 1 mayúscula")
                    RequisitoContrasena(cumple = uiState.requisitos.tieneNumero, texto = "Al menos 1 número")
                    RequisitoContrasena(cumple = uiState.requisitos.tieneEspecial, texto = "Al menos 1 carácter especial")
                    CampoContrasenaInduztek(
                        valor = uiState.confirmarClave,
                        onValorCambia = { viewModel.onRecuperarConfirmarChange(it) },
                        etiqueta = "Repetir nueva contraseña",
                        error = uiState.confirmarError
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.exito) {
                BotonPrincipalInduztek(
                    texto = "Cambiar contraseña",
                    cargando = uiState.enviando,
                    onClick = { viewModel.restablecerClave() }
                )
                TextButton(onClick = { viewModel.enviarRecuperacion() }, enabled = !uiState.enviando) {
                    Text("Reenviar código", color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                BotonPrincipalInduztek(
                    texto = "Enviar código",
                    cargando = uiState.enviando,
                    onClick = { viewModel.enviarRecuperacion() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    viewModel.reiniciarRecuperacion()
                    onVolver()
                }
            ) {
                Text(
                    text = "Volver al inicio de sesión",
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
