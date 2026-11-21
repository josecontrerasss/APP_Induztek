package com.example.app_induztek.ui.screens.cliente

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_induztek.ui.util.CertificadoPdf
import com.example.app_induztek.ui.viewmodel.ClienteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CertificadoEffect(viewModel: ClienteViewModel) {
    val context = LocalContext.current
    val data by viewModel.certificadoPendiente.collectAsStateWithLifecycle()

    LaunchedEffect(data) {
        val actual = data ?: return@LaunchedEffect
        try {
            val archivo = withContext(Dispatchers.IO) {
                CertificadoPdf.generar(context, actual)
            }
            CertificadoPdf.compartir(context, archivo)
            viewModel.certificadoEntregado()
        } catch (e: Exception) {
            viewModel.certificadoFallido()
        }
    }
}
