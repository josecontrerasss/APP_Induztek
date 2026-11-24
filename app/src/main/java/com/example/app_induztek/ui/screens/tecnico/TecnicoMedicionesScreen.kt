package com.example.app_induztek.ui.screens.tecnico

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_induztek.R
import com.example.app_induztek.ui.theme.AlertaNaranja
import com.example.app_induztek.ui.theme.Blanco
import com.example.app_induztek.ui.theme.ErrorRojo
import com.example.app_induztek.ui.theme.ExitoVerde
import com.example.app_induztek.ui.theme.Sincronizacion
import com.example.app_induztek.ui.theme.TextoOscuro
import com.example.app_induztek.ui.theme.TextoSecundario
import com.example.app_induztek.ui.theme.VerdeFondo
import com.example.app_induztek.ui.theme.VerdeTecnico
import com.example.app_induztek.ui.util.FotoStorage
import com.example.app_induztek.ui.viewmodel.MedicionViewModel
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TecnicoMedicionScreen(
    viewModel: MedicionViewModel,
    pendientes: Int,
    onSincronizar: () -> Unit,
    onLogout: () -> Unit
) {
    val contexto = LocalContext.current
    val form by viewModel.formState.collectAsStateWithLifecycle()
    val instalaciones by viewModel.instalacionesAsignadas.collectAsStateWithLifecycle()
    val equipos by viewModel.equiposAsignados.collectAsStateWithLifecycle()
    val selInstalacion by viewModel.selectedInstalacionId.collectAsStateWithLifecycle()
    val selEquipo by viewModel.selectedEquipoId.collectAsStateWithLifecycle()

    val mensajePermisoDenegado = stringResource(R.string.permiso_camara_denegado)

    val camaraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val ruta = FotoStorage.guardarJpeg(contexto, bitmap)
            if (ruta != null) {
                viewModel.onFotoGuardada(ruta)
            } else {
                viewModel.onFotoError()
            }
        }
    }

    val permisoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            camaraLauncher.launch(null)
        } else {
            Toast.makeText(contexto, mensajePermisoDenegado, Toast.LENGTH_SHORT).show()
        }
    }

    val lanzarCamara: () -> Unit = {
        val estadoPermiso = ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA)
        if (estadoPermiso == PackageManager.PERMISSION_GRANTED) {
            camaraLauncher.launch(null)
        } else {
            permisoLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val equiposDeInstalacion = equipos.filter { it.id_instalacion == selInstalacion }
    val sinInstalaciones = instalaciones.isEmpty()
    val sinEquipos = !sinInstalaciones && selInstalacion != null && equiposDeInstalacion.isEmpty()
    val puedeGuardar = !form.guardando && !sinInstalaciones && !sinEquipos && selEquipo != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (pendientes > 0) Sincronizacion.copy(alpha = 0.15f) else VerdeFondo
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (pendientes > 0) "$pendientes mediciones por sincronizar" else "SQLite local sincronizado ✔",
                    color = if (pendientes > 0) AlertaNaranja else ExitoVerde,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                if (pendientes > 0) {
                    TextButton(onClick = onSincronizar) {
                        Text("Marcar como sincronizado", color = TextoOscuro)
                    }
                }
            }
        }

        if (sinInstalaciones) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = TextoSecundario
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "No hay instalaciones registradas para este usuario.",
                        textAlign = TextAlign.Center,
                        color = TextoOscuro
                    )
                }
            }
        } else {
            Text("Subestación y Equipo:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextoOscuro)
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                instalaciones.forEach { inst ->
                    FilterChip(
                        selected = selInstalacion == inst.id_instalacion,
                        onClick = { viewModel.selectInstalacion(inst.id_instalacion) },
                        label = { Text(inst.codigo) }
                    )
                }
            }

            if (sinEquipos) {
                Text(
                    text = "Esta instalación no tiene equipos registrados",
                    color = ErrorRojo,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    equiposDeInstalacion.forEach { eq ->
                        FilterChip(
                            selected = selEquipo == eq.id_equipo,
                            onClick = { viewModel.selectEquipo(eq.id_equipo) },
                            label = { Text(eq.tag) }
                        )
                    }
                }
            }

            Text("Mediciones:", fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = TextoOscuro)

            CampoMedicion(
                label = "Tensión Primario",
                valor = form.tensionPrimario,
                unidad = "V",
                mostrarError = !form.isTensionPrimarioOk,
                onCambio = { viewModel.onTensionPrimarioChange(it) }
            )

            CampoMedicion(
                label = "Tensión Secundario",
                valor = form.tensionSecundario,
                unidad = "V",
                mostrarError = !form.isTensionSecundarioOk,
                onCambio = { viewModel.onTensionSecundarioChange(it) }
            )

            CampoMedicion(
                label = "Corriente Medida",
                valor = form.corriente,
                unidad = "A",
                mostrarError = !form.isCorrienteOk,
                onCambio = { viewModel.onCorrienteChange(it) }
            )

            CampoMedicion(
                label = "Resistencia Aislamiento",
                valor = form.resistenciaAislamiento,
                unidad = "MΩ",
                aceptaPrefijo = true,
                mostrarError = !form.isResistenciaOk,
                onCambio = { viewModel.onResistenciaChange(it) }
            )

            CampoMedicion(
                label = "Temperatura Aceite",
                valor = form.temperaturaAceite,
                unidad = "°C",
                esAlerta = form.isTemperaturaAlerta,
                mostrarError = !form.isTemperaturaOk,
                onCambio = { viewModel.onTemperaturaChange(it) }
            )

            CampoMedicion(
                label = "Humedad relativa (%)",
                valor = form.humedad,
                unidad = "%",
                mostrarError = !form.isHumedadOk,
                mensajeError = if (form.humedad.isNotBlank() && form.humedad.replace(',', '.').toDoubleOrNull() == null) {
                    "Ingrese un número válido"
                } else {
                    "Debe estar entre 0 y 100"
                },
                onCambio = { viewModel.onHumedadChange(it) }
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Observaciones:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextoOscuro)
                Text("${form.observaciones.length}/200", fontSize = 12.sp, color = TextoSecundario)
            }
            OutlinedTextField(
                value = form.observaciones,
                onValueChange = { viewModel.onObservacionesChange(it) },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(8.dp)
            )

            BotonUbicacionMedicion(
                onUbicacionCapturada = { lat, lon -> viewModel.onUbicacionCapturada(lat, lon) }
            )

            Card(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = VerdeTecnico
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Evidencia fotográfica",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextoOscuro
                        )
                    }

                    Text(
                        text = "La foto se guarda en el almacenamiento interno del dispositivo y queda asociada al protocolo.",
                        fontSize = 12.sp,
                        color = TextoSecundario
                    )

                    AnimatedVisibility(visible = form.fotoPath != null) {
                        val fotoPath = form.fotoPath
                        if (fotoPath != null) {
                            val bitmap = remember(fotoPath) { BitmapFactory.decodeFile(fotoPath) }
                            if (bitmap != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Evidencia fotográfica",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }

                    if (form.fotoPath == null) {
                        Button(
                            onClick = { lanzarCamara() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VerdeTecnico,
                                contentColor = Blanco
                            )
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Tomar foto", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { lanzarCamara() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Volver a tomar")
                            }
                            OutlinedButton(
                                onClick = { viewModel.onFotoEliminada() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Quitar foto", color = ErrorRojo)
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { viewModel.guardarMedicion() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = puedeGuardar,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdeTecnico,
                    contentColor = Blanco
                )
            ) {
                if (form.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Blanco,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Guardando...", fontWeight = FontWeight.Bold)
                } else {
                    Text("GUARDAR MEDICIÓN EN TERRENO", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CampoMedicion(
    label: String,
    valor: String,
    unidad: String,
    esAlerta: Boolean = false,
    aceptaPrefijo: Boolean = false,
    mostrarError: Boolean = false,
    mensajeError: String = "Ingrese un número válido",
    onCambio: (String) -> Unit
) {
    val icono = when {
        mostrarError -> Icons.Default.Error
        esAlerta -> Icons.Default.Warning
        else -> Icons.Default.CheckCircle
    }
    val tintIcono = when {
        mostrarError -> ErrorRojo
        esAlerta -> ErrorRojo
        else -> ExitoVerde
    }

    OutlinedTextField(
        value = valor,
        onValueChange = { nuevoValor ->
            val filtrado = nuevoValor.filter { c ->
                c.isDigit() || c == ',' || c == '.' ||
                    (aceptaPrefijo && (c == '>' || c == '<'))
            }
            onCambio(filtrado)
        },
        label = { Text(label, color = TextoSecundario) },
        suffix = { Text(unidad, fontWeight = FontWeight.Bold, color = TextoOscuro) },
        trailingIcon = if (valor.any { it.isDigit() } || mostrarError) {
            { Icon(imageVector = icono, contentDescription = null, tint = tintIcono) }
        } else null,
        supportingText = if (mostrarError) {
            { Text(mensajeError, color = ErrorRojo) }
        } else null,
        isError = mostrarError,
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}

@Composable
fun BotonUbicacionMedicion(onUbicacionCapturada: (String, String) -> Unit) {
    val contexto = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(contexto) }
    var latitud by remember { mutableStateOf("") }
    var longitud by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        if (permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            if (ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation.addOnSuccessListener { ubicacion ->
                    ubicacion?.let {
                        latitud = it.latitude.toString()
                        longitud = it.longitude.toString()
                        onUbicacionCapturada(latitud, longitud)
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Button(
            onClick = {
                if (ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { ubicacion ->
                        ubicacion?.let {
                            latitud = it.latitude.toString()
                            longitud = it.longitude.toString()
                            onUbicacionCapturada(latitud, longitud)
                        }
                    }
                } else {
                    permissionLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdeTecnico,
                contentColor = Blanco
            )
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = "GPS")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Validar ubicación en terreno")
        }

        if (latitud.isNotEmpty() && longitud.isNotEmpty()) {
            Text(
                text = "📍 Coordenadas: $latitud, $longitud",
                color = TextoOscuro,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally)
            )

            val latLng = com.google.android.gms.maps.model.LatLng(latitud.toDouble(), longitud.toDouble())
            val cameraPositionState = com.google.maps.android.compose.rememberCameraPositionState {
                position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(latLng, 15f)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 8.dp)
            ) {
                com.google.maps.android.compose.GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    com.google.maps.android.compose.Marker(
                        state = com.google.maps.android.compose.MarkerState(position = latLng),
                        title = "Ubicación del Técnico"
                    )
                }
            }
        }
    }
}
