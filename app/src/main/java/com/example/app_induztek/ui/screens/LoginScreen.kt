package com.example.app_induztek.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.example.app_induztek.ui.components.CampoTextoInduztek
import com.example.app_induztek.ui.viewmodel.InduztekViewModel

@Composable
fun LoginScreen(
    viewModel: InduztekViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegistro: () -> Unit
) {
    val email by viewModel.loginEmail.collectAsStateWithLifecycle()
    val password by viewModel.loginPassword.collectAsStateWithLifecycle()
    val error by viewModel.loginError.collectAsStateWithLifecycle()
    val isLoggingIn by viewModel.isLoggingIn.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onLoginSuccess()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
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
                text = "Portal Induztek",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Ingresa tus credenciales para continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            CampoTextoInduztek(
                valor = email,
                onValorCambia = { viewModel.onLoginEmailChange(it) },
                etiqueta = "Correo Electrónico",
                icono = Icons.Default.Email,
                tipoTeclado = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            CampoContrasenaInduztek(
                valor = password,
                onValorCambia = { viewModel.onLoginPasswordChange(it) },
                etiqueta = "Contraseña",
                icono = Icons.Default.Lock
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            BotonPrincipalInduztek(
                texto = "INICIAR SESIÓN",
                cargando = isLoggingIn,
                onClick = { viewModel.iniciarSesion() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToRegistro) {
                Text(
                    text = "¿No tienes cuenta empresa? Regístrate aquí",
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}